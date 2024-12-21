package org.example.server;

import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Connection connect;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // Устанавливаем соединение с базой данных
            connect = database.connectBd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Получен запрос: " + request);
                String[] parts = request.split("\\|");
                String command = parts[0];

                switch (command) {
                    case "GET_USERS":
                        sendUsers();
                        break;
                    case "ADD_USER":
                        if (parts.length == 3) {
                            addUser(parts[1], parts[2]);
                        }
                        break;
                    case "UPDATE_USER":
                        if (parts.length == 4) {
                            updateUser(Integer.parseInt(parts[1]), parts[2], parts[3]);
                        }
                        break;
                    default:
                        out.println("UNKNOWN_COMMAND");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connect.close();
                socket.close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendUsers() {
        try {
            Statement stmt = connect.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT student_id, date, discipline, group_id, first_name, last_name, status, note FROM student");
            List<String> students = new ArrayList<>();
            while (rs.next()) {
                int studentID = rs.getInt("student_id");
                Date date = rs.getDate("date");
                String discipline = rs.getString("discipline");
                String groupID = rs.getString("group_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String status = rs.getString("status");
                String note = rs.getString("note");
                students.add(studentID + "," +
                        date + "," +
                        discipline + "," +
                        groupID + "," +
                        firstName + "," +
                        lastName + "," +
                        status + "," +
                        note);
            }
            System.out.println("Server response: " + students);
            // Отправляем пользователей, разделенных ";"
            out.println(String.join(";", students));
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }

    private void addUser(String name, String email) {
        try {
            PreparedStatement pstmt = connect.prepareStatement("INSERT INTO student (student_id, date, discipline, group_id, first_name, last_name, status, note) VALUES (?, ?)");
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
            out.println("SUCCESS");
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }

    private void updateUser(int id, String name, String email) {
        try {
            PreparedStatement pstmt = connect.prepareStatement(
                    "UPDATE student SET " +
                            "student_id = ?, " +
                            "date = ?, " +
                            "discipline = ?, " +
                            "group_id = ?, " +
                            "first_name = ?, " +
                            "last_name = ?, " +
                            "status = ?, " +
                            "note = ?," +
                            "WHERE id = ?");
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
            out.println("SUCCESS");
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }
}

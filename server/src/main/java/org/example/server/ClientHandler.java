package org.example.server;

import org.example.shared.StudentData;

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
                    case "GET":
                        handleGet();
                        break;
                    case "ADD":
                        handleAdd(parts);
                        break;
                    case "UPDATE":
                        if (parts.length == 9) {
                            updateUser(Integer.parseInt(parts[1]), parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
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

    private void handleGet() {
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

    private void handleAdd(String[] parts) {
        try {
            // Ожидается формат: ADD|Date|Discipline|GroupID|FirstName|LastName|Status|Note
            String query = "INSERT INTO student (date, discipline, group_id, first_name, last_name, status, note) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connect.prepareStatement(query);
            pstmt.setString(1, parts[1]);
            pstmt.setString(2, parts[2]);
            pstmt.setString(3, parts[3]);
            pstmt.setString(4, parts[4]);
            pstmt.setString(5, parts[5]);
            pstmt.setString(6, parts[6]);
            pstmt.setString(7, parts[7]);
            pstmt.executeUpdate();
            out.println("SUCCESS|Запись добавлена");
        } catch (SQLException e) {
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void updateUser(int studentID, String date, String discilpine, String groupID, String firstName, String lastName, String status, String note) {
        try {
            PreparedStatement pstmt = connect.prepareStatement(
                    "UPDATE student SET " +
                            "date = ?, " +
                            "discipline = ?, " +
                            "group_id = ?, " +
                            "first_name = ?, " +
                            "last_name = ?, " +
                            "status = ?, " +
                            "note = ?," +
                            "WHERE student_id = ?");
            pstmt.setString(1, date);
            pstmt.setString(2, discilpine);
            pstmt.setString(3, groupID);
            pstmt.setString(4, firstName);
            pstmt.setString(5, lastName);
            pstmt.setString(6, status);
            pstmt.setString(7, note);
            pstmt.setInt(8, studentID);
            pstmt.executeUpdate();
            out.println("SUCCESS");
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }
}

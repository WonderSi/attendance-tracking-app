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
                        handleUpdate(parts);
                        break;
                    case "DELETE":
                        handleDelete(parts);
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
            String query = "INSERT INTO student (" +
                    "date, " +
                    "discipline, " +
                    "group_id, " +
                    "first_name, " +
                    "last_name, " +
                    "status, " +
                    "note" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?)";
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

    private void handleUpdate(String[] parts) {
        try {
            // Ожидается формат: UPDATE|StudentID|Date|Discipline|GroupID|FirstName|LastName|Status|Note
            String query = "UPDATE student SET " +
                    "date = ?, " +
                    "discipline = ?, " +
                    "group_id = ?, " +
                    "first_name = ?, " +
                    "last_name = ?, " +
                    "status = ?, " +
                    "note = ?" +
                    "WHERE student_id = ?";
            PreparedStatement pstmt = connect.prepareStatement(query);
            pstmt.setString(1, parts[2]);
            pstmt.setString(2, parts[3]);
            pstmt.setString(3, parts[4]);
            pstmt.setString(4, parts[5]);
            pstmt.setString(5, parts[6]);
            pstmt.setString(6, parts[7]);
            pstmt.setString(7, parts[8]);
            pstmt.setString(8, parts[1]);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("SUCCESS|Запись обновлена");
            } else {
                out.println("ERROR|Запись не найдена");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleDelete(String[] parts) {
        try {
            // Ожидается формат: DELETE|StudentID
            String query = "DELETE FROM student WHERE student_id=?";
            PreparedStatement pstmt = connect.prepareStatement(query);
            pstmt.setInt(1, Integer.parseInt(parts[1]));
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("SUCCESS|Запись удалена");
            } else {
                out.println("ERROR|Запись не найдена");
            }
        } catch (SQLException e) {
            out.println("ERROR|" + e.getMessage());
        }
    }
}

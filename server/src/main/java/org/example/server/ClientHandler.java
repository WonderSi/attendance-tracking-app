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
            //устанавливаем соединение с базой данных
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
                        break;
                    case "REPORT_GENERAL":
                        handleReportGeneral(parts);
                        break;
                    case "REPORT_STUDENT":
                        handleReportStudent(parts);
                        break;
                    case "REPORT_GLOBAL":
                        handleReportGlobal();
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
            ResultSet rs = stmt.executeQuery("SELECT student_id, date, discipline, group_id, first_name, last_name, status, note, point FROM student");
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
                String point = rs.getString("point");
                students.add(studentID + "," +
                        date + "," +
                        discipline + "," +
                        groupID + "," +
                        firstName + "," +
                        lastName + "," +
                        status + "," +
                        note + "," +
                        point
                        );
            }
            System.out.println("Server response: " + students);
            //отправляем пользователей, разделенных ";"
            out.println(String.join(";", students));
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }

    private void handleAdd(String[] parts) {
        try {
            String query = "INSERT INTO student (date, discipline, group_id, first_name, last_name, status, note, point) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connect.prepareStatement(query);
            pstmt.setString(1, parts[1]);
            pstmt.setString(2, parts[2]);
            pstmt.setString(3, parts[3]);
            pstmt.setString(4, parts[4]);
            pstmt.setString(5, parts[5]);
            pstmt.setString(6, parts[6]);
            pstmt.setString(7, parts[7]);
            pstmt.setString(8, parts[8]);
            pstmt.executeUpdate();
            out.println("SUCCESS|Запись добавлена");
        } catch (SQLException e) {
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleUpdate(String[] parts) {
        try {
            String query = "UPDATE student SET " +
                    "date = ?, " +
                    "discipline = ?, " +
                    "group_id = ?, " +
                    "first_name = ?, " +
                    "last_name = ?, " +
                    "status = ?, " +
                    "note = ?," +
                    "point = ?" +
                    "WHERE student_id = ?";
            PreparedStatement pstmt = connect.prepareStatement(query);
            pstmt.setString(1, parts[2]);
            pstmt.setString(2, parts[3]);
            pstmt.setString(3, parts[4]);
            pstmt.setString(4, parts[5]);
            pstmt.setString(5, parts[6]);
            pstmt.setString(6, parts[7]);
            pstmt.setString(7, parts[8]);
            pstmt.setString(8, parts[9]);
            pstmt.setString(9, parts[1]);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("SUCCESS|Запись обновлена");
            } else {
                out.println("ERROR|Запись не найдена");
            }
        } catch (SQLException e) {
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

    private void handleReportGeneral(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR|Неверный формат команды REPORT_GENERAL");
            return;
        }
        String groupId = parts[1];
        try {
            // Количество студентов в группе
            PreparedStatement pstmtCount = connect.prepareStatement(
                    "SELECT COUNT(*) AS student_count FROM student WHERE group_id = ?");
            pstmtCount.setString(1, groupId);
            ResultSet rsCount = pstmtCount.executeQuery();
            int studentCount = 0;
            if (rsCount.next()) {
                studentCount = rsCount.getInt("student_count");
            }

            // Средний балл группы
            // Предполагается, что есть поле average_grade в таблице student
            PreparedStatement pstmtAvg = connect.prepareStatement(
                    "SELECT AVG(average_grade) AS avg_grade FROM student WHERE group_id = ?");
            pstmtAvg.setString(1, groupId);
            ResultSet rsAvg = pstmtAvg.executeQuery();
            double avgGrade = 0.0;
            if (rsAvg.next()) {
                avgGrade = rsAvg.getDouble("avg_grade");
            }

            // Список студентов с деталями
            PreparedStatement pstmtList = connect.prepareStatement(
                    "SELECT student_id, first_name, last_name, average_grade, status, note FROM student WHERE group_id = ?");
            pstmtList.setString(1, groupId);
            ResultSet rsList = pstmtList.executeQuery();
            List<String> students = new ArrayList<>();
            while (rsList.next()) {
                int id = rsList.getInt("student_id");
                String firstName = rsList.getString("first_name");
                String lastName = rsList.getString("last_name");
                double grade = rsList.getDouble("average_grade");
                String status = rsList.getString("status");
                String note = rsList.getString("note");
                students.add(String.format("%d,%s,%s,%.2f,%s,%s", id, firstName, lastName, grade, status, note));
            }

            // Формирование ответа
            String response = String.format("REPORT_GENERAL|%d|%.2f|%s", studentCount, avgGrade, String.join(";", students));
            out.println(response);
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleReportStudent(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR|Неверный формат команды REPORT_STUDENT");
            return;
        }
        int studentId;
        try {
            studentId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            out.println("ERROR|Неверный ID студента");
            return;
        }
        try {
            // Информация о студенте
            PreparedStatement pstmtInfo = connect.prepareStatement(
                    "SELECT group_id, first_name, last_name, average_grade FROM student WHERE student_id = ?");
            pstmtInfo.setInt(1, studentId);
            ResultSet rsInfo = pstmtInfo.executeQuery();
            if (!rsInfo.next()) {
                out.println("ERROR|Студент не найден");
                return;
            }
            String groupId = rsInfo.getString("group_id");
            String firstName = rsInfo.getString("first_name");
            String lastName = rsInfo.getString("last_name");
            double avgGrade = rsInfo.getDouble("average_grade");

            // Процент посещаемости
            // Предполагается, что есть таблица attendance с полями student_id и status
            PreparedStatement pstmtAttendance = connect.prepareStatement(
                    "SELECT COUNT(*) AS total, SUM(CASE WHEN status = 'Присутствовал' THEN 1 ELSE 0 END) AS present FROM attendance WHERE student_id = ?");
            pstmtAttendance.setInt(1, studentId);
            ResultSet rsAttendance = pstmtAttendance.executeQuery();
            double attendancePercentage = 0.0;
            if (rsAttendance.next()) {
                int total = rsAttendance.getInt("total");
                int present = rsAttendance.getInt("present");
                if (total > 0) {
                    attendancePercentage = ((double) present / total) * 100.0;
                }
            }

            // Всего предметов
            PreparedStatement pstmtSubjects = connect.prepareStatement(
                    "SELECT COUNT(DISTINCT discipline) AS total_subjects FROM student WHERE student_id = ?");
            pstmtSubjects.setInt(1, studentId);
            ResultSet rsSubjects = pstmtSubjects.executeQuery();
            int totalSubjects = 0;
            if (rsSubjects.next()) {
                totalSubjects = rsSubjects.getInt("total_subjects");
            }

            // Успеваемость по предметам
            // Предполагается, что есть таблица grades с полями student_id, discipline, grade
            PreparedStatement pstmtGrades = connect.prepareStatement(
                    "SELECT discipline, AVG(grade) AS avg_grade FROM grades WHERE student_id = ? GROUP BY discipline");
            pstmtGrades.setInt(1, studentId);
            ResultSet rsGrades = pstmtGrades.executeQuery();
            List<String> grades = new ArrayList<>();
            while (rsGrades.next()) {
                String discipline = rsGrades.getString("discipline");
                double avgDisciplineGrade = rsGrades.getDouble("avg_grade");
                grades.add(String.format("%s,%.2f", discipline, avgDisciplineGrade));
            }

            // Формирование ответа
            String response = String.format("REPORT_STUDENT|%s|%s|%.2f|%d|%s", firstName + " " + lastName, groupId, avgGrade, totalSubjects, String.join(";", grades));
            out.println(response);
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleReportGlobal() {
        try {
            // Общее количество студентов
            Statement stmt = connect.createStatement();
            ResultSet rsTotalStudents = stmt.executeQuery("SELECT COUNT(*) AS total_students FROM student");
            int totalStudents = 0;
            if (rsTotalStudents.next()) {
                totalStudents = rsTotalStudents.getInt("total_students");
            }

            // Количество групп
            ResultSet rsTotalGroups = stmt.executeQuery("SELECT COUNT(DISTINCT group_id) AS total_groups FROM student");
            int totalGroups = 0;
            if (rsTotalGroups.next()) {
                totalGroups = rsTotalGroups.getInt("total_groups");
            }

            // Средний балл по факультету
            ResultSet rsAvgFaculty = stmt.executeQuery("SELECT AVG(average_grade) AS avg_faculty_grade FROM student");
            double avgFacultyGrade = 0.0;
            if (rsAvgFaculty.next()) {
                avgFacultyGrade = rsAvgFaculty.getDouble("avg_faculty_grade");
            }

            // Посещаемость (общая)
            ResultSet rsAttendance = stmt.executeQuery("SELECT COUNT(*) AS total, SUM(CASE WHEN status = 'Присутствовал' THEN 1 ELSE 0 END) AS present FROM attendance");
            double overallAttendance = 0.0;
            if (rsAttendance.next()) {
                int total = rsAttendance.getInt("total");
                int present = rsAttendance.getInt("present");
                if (total > 0) {
                    overallAttendance = ((double) present / total) * 100.0;
                }
            }

            // Таблица по группам
            PreparedStatement pstmtGroup = connect.prepareStatement(
                    "SELECT group_id, COUNT(*) AS student_count, AVG(average_grade) AS avg_grade, " +
                            "SUM(CASE WHEN status = 'Присутствовал' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS attendance_percentage, " +
                            "MAX(average_grade) AS best_grade " +
                            "FROM student GROUP BY group_id");
            ResultSet rsGroup = pstmtGroup.executeQuery();
            List<String> groups = new ArrayList<>();
            while (rsGroup.next()) {
                String groupId = rsGroup.getString("group_id");
                int studentCount = rsGroup.getInt("student_count");
                double avgGrade = rsGroup.getDouble("avg_grade");
                double attendance = rsGroup.getDouble("attendance_percentage");
                double bestGrade = rsGroup.getDouble("best_grade");
                groups.add(String.format("%s,%d,%.2f,%.2f,%.2f", groupId, studentCount, avgGrade, attendance, bestGrade));
            }

            // Формирование ответа
            String response = String.format("REPORT_GLOBAL|%d|%d|%.2f|%.2f|%s", totalStudents, totalGroups, avgFacultyGrade, overallAttendance, String.join(";", groups));
            out.println(response);
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR|" + e.getMessage());
        }
    }

}

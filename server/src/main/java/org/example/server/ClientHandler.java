package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

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
            logger.info("Подключен клиент: " + socket.getInetAddress());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при подключении клиента", e);
        }
    }

    @Override
    public void run() {
        try {
            String request;
            while ((request = in.readLine()) != null) {
                logger.info("Получен запрос: " + request);
                String[] parts = request.split("\\|");
                String command = parts[0];

                switch (command) {
                    case "GET":
                        handleGet();
                        break;
                    case "GET_DISCIPLINES":
                        handleGetDisciplines();
                        break;
                    case "GET_GROUPS":
                        handleGetGroups();
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
                        logger.warning("Неизвестная команда: " + command);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при обработке запроса клиента", e);
        } finally {
            try {
                connect.close();
                socket.close();
                logger.info("Клиент отключился: " + socket.getInetAddress());
            } catch (IOException | SQLException e) {
                logger.log(Level.SEVERE, "Ошибка при закрытии соединения", e);
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
            logger.info("Отправка данных студентов клиенту");
            out.println(String.join(";", students));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении handleGet", e);
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleGetDisciplines() {
        try {
            Statement stmt = connect.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT discipline FROM student");
            List<String> disciplines = new ArrayList<>();

            while (rs.next()) {
                disciplines.add(rs.getString("discipline"));
            }

            out.println(String.join(";", disciplines));
            logger.info("Отправлены дисциплины клиенту");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении handleGetDisciplines", e);
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleGetGroups() {
        try {
            Statement stmt = connect.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT group_id FROM student");
            List<String> groups = new ArrayList<>();

            while (rs.next()) {
                groups.add(rs.getString("group_id"));
            }

            out.println(String.join(";", groups));
            logger.info("Отправлены группы клиенты");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении handleGetGroups", e);
            out.println("ERROR|" + e.getMessage());
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
            logger.info("Добавлена новая запись: " + parts[4] + " " + parts[5]);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении записи", e);
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
                logger.info("Обновлена запись");
            } else {
                out.println("ERROR|Запись не найдена");
                logger.info("Запись не найдена");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении записи", e);
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleDelete(String[] parts) {
        try {
            String query = "DELETE FROM student WHERE student_id=?";
            PreparedStatement pstmt = connect.prepareStatement(query);
            pstmt.setInt(1, Integer.parseInt(parts[1]));
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("SUCCESS|Запись удалена");
                logger.info("Запись удалена");
            } else {
                out.println("ERROR|Запись не найдена");
                logger.info("Запись не найдена");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении записи", e);
            out.println("ERROR|" + e.getMessage());
        }
    }

    private void handleReportGeneral(String[] parts) {
    }

    private void handleReportStudent(String[] parts) {
    }

    private void handleReportGlobal() {
    }

    private void setupLogger() {
        try {
            //удаление стандартных обработчиков
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            //создание обработчика для консоли
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());

            //создание обработчика для файла
            FileHandler fileHandler = new FileHandler("server.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            //добавление обработчиков к логгеру
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
            // Если логирование не удалось настроить, продолжаем без него
        }
    }

}

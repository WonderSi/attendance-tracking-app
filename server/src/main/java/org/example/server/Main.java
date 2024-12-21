package org.example.server;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(2048)) {

            System.out.println("Сервер запущен и ожидает подключения");
            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        Connection dbConnection = database.connectBd()
                ) {
                    ObservableList<StudentData> listData = FXCollections.observableArrayList();

                    String command = in.readLine();
                    if ("SET_STUDENTS".equals(command)) {
                        // Выполнение SQL-запроса
                        Statement stmt = dbConnection.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * FROM table");
                        while (rs.next()) {
                            StudentData studentID = new StudentData(rs.getInt("student_ID")
                                    , rs.getDate("date")
                                    , rs.getString("discipline")
                                    , rs.getString("grouping")
                                    , rs.getString("first_name")
                                    , rs.getString("last_name")
                                    , rs.getString("status")
                                    , rs.getString("note"));
                            listData.add(studentID);
                        }
                        // Преобразуем данные в JSON
                        Gson gson = new Gson();
                        String jsonData = gson.toJson(listData);

                        // Отправляем данные клиенту
                        out.println(jsonData);
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

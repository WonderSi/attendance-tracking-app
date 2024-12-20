package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {

            String dbUrl = "jdbc:mysql://localhost:3306/test";
            String dbUser = "root";
            String dbPassword = "root";

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     Connection dbConnection = database.connectBd()) {

                    String command = in.readLine();
                    if ("GET_DATA".equals(command)) {
                        // Выполнение SQL-запроса
                        Statement stmt = dbConnection.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT data FROM table");
                        if (rs.next()) {
                            String data = rs.getString("data");
                            // Отправка данных клиенту
                            out.println(data);
                        }
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

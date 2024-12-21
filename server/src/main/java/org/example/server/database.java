package org.example.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {
    public static Connection connectBd() {
        try{
            String DB_URL = "jdbc:mysql://localhost:3306/test";
            String DB_USER = "root";
            String DB_PASSWORD = "root";

            Connection connect = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            return connect;

        }catch (SQLException e) {e.printStackTrace();}
        return null;
    }
}

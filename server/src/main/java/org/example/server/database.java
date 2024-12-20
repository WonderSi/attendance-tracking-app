package org.example.server;

import java.sql.Connection;
import java.sql.DriverManager;

public class database {
    public static Connection connectBd() {
        try{

            Class.forName("com.mysql.jdbc.Driver");

            String dbUrl = "jdbc:mysql://localhost:3306/test";
            String dbUser = "root";
            String dbPassword = "root";

            Connection connect = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            return connect;

        }catch (Exception e) {e.printStackTrace();}
        return null;
    }
}

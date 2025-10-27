package com.example.final_project.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

   private static final String URL = "jdbc:mysql://10.0.2.2:3306/FridgeManager"; // 10.0.2.2 = localhost của máy thật
    //private static final String URL = "jdbc:mysql://192.168.1.2:3306/FridgeManager"; // 10.0.2.2 = localhost của máy thật
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to MySQL successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Database connection failed!");
        }
        return connection;
    }
}

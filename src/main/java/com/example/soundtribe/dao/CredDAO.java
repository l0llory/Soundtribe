package com.example.soundtribe.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CredDAO {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/soundtribe";
    private static final String USER = "postgres";
    private static final String PASSWORD = "SALAmandra22";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}

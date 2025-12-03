package org.example.todo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    // --- Параметры подключения к PostgreSQL ---
    // Убедитесь, что ваш PostgreSQL-сервер запущен, а БД 'todo_db' существует
    private static final String URL = "jdbc:postgresql://localhost:5432/duwniy";
    private static final String USER = "duwniy"; // Ваше имя пользователя
    private static final String PASSWORD = "duwniy00"; // Ваш пароль
    // ------------------------------------------

    public static Connection getConnection() throws SQLException {
        // Устанавливаем соединение с БД
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Создание таблицы пользователей (только для аутентификации)
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY," + // SERIAL для автоинкремента в Postgres
                    "username VARCHAR(100) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL" +
                    ");";
            stmt.execute(sqlUsers);

            System.out.println("PostgreSQL database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization error (Check server connection and credentials!): " + e.getMessage());
            // В реальном приложении здесь лучше бросить RuntimeException, чтобы приложение не продолжало работу
        }
    }
}
package org.example.todo.service;

import org.example.todo.db.DatabaseConfig;
import org.example.todo.model.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public UserService() {
        // Инициализация БД при создании сервиса
        DatabaseConfig.initializeDatabase();
    }

    public User authenticate(String username, String password) {
        User user = getUserByUsername(username);
        // Сравниваем пароль в Java-коде
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User getUserByUsername(String username) {
        // ФИКС: Явно указываем схему 'public' для поиска таблицы
        String sql = "SELECT id, password, created_date FROM public.users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String passwordDb = rs.getString("password");
                LocalDateTime createdDate = rs.getTimestamp("created_date").toLocalDateTime();

                // ВНИМАНИЕ: Если вы используете хэширование, здесь нужно загружать хэш, а не чистый пароль.
                return new User(id, username, passwordDb, createdDate);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    public void saveUser(User user) {
        if (getUserByUsername(user.getUsername()) != null) {
            System.err.println("User already exists, cannot save.");
            return;
        }

        // ФИКС: Явно указываем схему 'public' для вставки
        String sql = "INSERT INTO public.users (username, password, created_date) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setTimestamp(3, Timestamp.valueOf(user.getCreatedDate()));

            ResultSet generatedKeys = pstmt.executeQuery();

            if (generatedKeys.next()) {
                user.setId(generatedKeys.getInt(1)); // Устанавливаем полученный ID
            }

        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }
}
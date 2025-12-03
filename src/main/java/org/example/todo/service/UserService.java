package org.example.todo.service;

import org.example.todo.model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final String USERS_FILE = "users.dat";
    private List<User> users;

    public UserService() {
        loadUsers();
    }

    public User authenticate(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public void saveUser(User user) {
        if (getUserByUsername(user.getUsername()) == null) {
            users.add(user);
            saveAllUsers();
        }
    }

    public void updateUser(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(user.getUsername())) {
                // Заменяем существующего пользователя на обновленный объект
                users.set(i, user);
                saveAllUsers();
                break;
            }
        }
    }

    public void deleteUser(String username) {
        users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        saveAllUsers();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    private void saveAllUsers() {
        // Используем try-with-resources для автоматического закрытия потоков
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            users = (List<User>) ois.readObject();
        } catch (FileNotFoundException e) {
            // Файл не найден, это нормально при первом запуске
            users = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading users: " + e.getMessage());
            users = new ArrayList<>();
        }
    }
}
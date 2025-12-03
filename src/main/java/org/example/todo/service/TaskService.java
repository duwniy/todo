package org.example.todo.service;

import org.example.todo.model.User;
import org.example.todo.model.Task;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TaskService {

    // Генерируем уникальное имя файла для каждого пользователя
    private static String getTaskFileName(String username) {
        // Например: tasks_ivan.dat
        return "tasks_" + username.toLowerCase() + ".dat";
    }

    // Сохранение списка задач конкретного пользователя
    public void saveUserTasks(User user) {
        String fileName = getTaskFileName(user.getUsername());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            // Сохраняем ТОЛЬКО список задач (List<Task>)
            oos.writeObject(user.getTasks());
        } catch (IOException e) {
            System.err.println("Error saving tasks for user " + user.getUsername() + ": " + e.getMessage());
        }
    }

    // Загрузка списка задач конкретного пользователя
    @SuppressWarnings("unchecked")
    public List<Task> loadUserTasks(String username) {
        String fileName = getTaskFileName(username);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            // Читаем и возвращаем список
            return (List<Task>) ois.readObject();
        } catch (FileNotFoundException e) {
            // Файл не найден (новый пользователь), возвращаем пустой список
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading tasks for user " + username + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
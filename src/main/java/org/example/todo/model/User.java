package org.example.todo.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id; // <-- НОВОЕ: ID, сгенерированный PostgreSQL
    private String username;
    private String password;
    private LocalDateTime createdDate;
    private List<Task> tasks;

    // Конструктор для регистрации (ID еще не известен)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdDate = LocalDateTime.now();
        this.tasks = new ArrayList<>();
    }

    // Конструктор для загрузки из БД (ID и все поля известны)
    public User(int id, String username, String password, LocalDateTime createdDate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createdDate = createdDate;
        this.tasks = new ArrayList<>(); // Задачи будут загружены TaskService позже
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public void removeTask(Task task) {
        this.tasks.remove(task);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", createdDate=" + createdDate +
                ", tasksCount=" + tasks.size() +
                '}';
    }
}
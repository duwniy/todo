package org.example.todo.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private LocalDateTime createdDate;
    private List<Task> tasks;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdDate = LocalDateTime.now();
        this.tasks = new ArrayList<>();
    }

    // Getters and Setters
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
                "username='" + username + '\'' +
                ", createdDate=" + createdDate +
                ", tasksCount=" + tasks.size() +
                '}';
    }
}
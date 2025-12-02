package org.example.todo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String description;
    private String category;
    private Priority priority;
    private boolean completed;
    private LocalDateTime createdDate;
    private LocalDateTime dueDate;

    public enum Priority {
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Task(String title, String description, String category, Priority priority, LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.completed = false;
        this.createdDate = LocalDateTime.now();
    }

    public Task(String title, String category, Priority priority) {
        this(title, "", category, priority, null);
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getCreatedDate() { return createdDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public String getFormattedCreatedDate() {
        return createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getFormattedDueDate() {
        if (dueDate == null) return "No due date";
        return dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        String status = completed ? "[✓] " : "[ ] ";
        return status + title + " - " + category + " (" + priority.getDisplayName() + ")";
    }
}
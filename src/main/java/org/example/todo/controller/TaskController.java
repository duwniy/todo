package org.example.todo.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.todo.model.Task;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskController {
    @FXML
    private ListView<Task> taskListView;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private ComboBox<Task.Priority> priorityCombo;
    @FXML
    private CheckBox completedCheckBox;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private Label taskCountLabel;

    private List<Task> allTasks = new ArrayList<>();
    private ObservableList<Task> displayedTasks = FXCollections.observableArrayList();
    private Task selectedTask = null;
    private final String SAVE_FILE = "tasks.dat";

    @FXML
    public void initialize() {
        setupComboBoxes();
        loadTasks();
        updateTaskList();
        taskListView.setItems(displayedTasks);

        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedTask = newVal;
            populateTaskDetails();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTasks());
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterTasks());
    }

    private void setupComboBoxes() {
        // Categories
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Work", "Personal", "Shopping", "Health", "Education", "Other"
        );
        categoryCombo.setItems(categories);
        categoryCombo.setValue("Work");

        // Priorities
        ObservableList<Task.Priority> priorities = FXCollections.observableArrayList(
                Task.Priority.HIGH, Task.Priority.MEDIUM, Task.Priority.LOW
        );
        priorityCombo.setItems(priorities);
        priorityCombo.setValue(Task.Priority.MEDIUM);

        // Filters
        ObservableList<String> filters = FXCollections.observableArrayList(
                "All", "Completed", "Incomplete", "High Priority", "Today"
        );
        filterCombo.setItems(filters);
        filterCombo.setValue("All");
    }

    @FXML
    protected void onAddTaskClick() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Title", "Please enter a task title");
            return;
        }

        Task newTask = new Task(
                title,
                descriptionArea.getText(),
                categoryCombo.getValue(),
                priorityCombo.getValue(),
                null
        );

        allTasks.add(newTask);
        clearFields();
        updateTaskList();
        saveTasks();
    }

    @FXML
    protected void onUpdateTaskClick() {
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to update");
            return;
        }

        selectedTask.setTitle(titleField.getText().trim());
        selectedTask.setDescription(descriptionArea.getText());
        selectedTask.setCategory(categoryCombo.getValue());
        selectedTask.setPriority(priorityCombo.getValue());
        selectedTask.setCompleted(completedCheckBox.isSelected());

        taskListView.refresh();
        updateTaskList();
        saveTasks();
    }

    @FXML
    protected void onDeleteTaskClick() {
        if (selectedTask == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a task to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete task?");
        confirmation.setContentText("Are you sure you want to delete this task?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            allTasks.remove(selectedTask);
            clearFields();
            updateTaskList();
            saveTasks();
        }
    }

    @FXML
    protected void onClearAllClick() {
        if (allTasks.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Empty", "No tasks to clear");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Clear All");
        confirmation.setHeaderText("Delete all tasks?");
        confirmation.setContentText("This action cannot be undone!");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            allTasks.clear();
            clearFields();
            updateTaskList();
            saveTasks();
        }
    }

    private void filterTasks() {
        String searchText = searchField.getText().toLowerCase();
        String filterType = filterCombo.getValue();

        List<Task> filtered = allTasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(searchText) ||
                        task.getDescription().toLowerCase().contains(searchText))
                .filter(task -> {
                    switch (filterType) {
                        case "Completed":
                            return task.isCompleted();
                        case "Incomplete":
                            return !task.isCompleted();
                        case "High Priority":
                            return task.getPriority() == Task.Priority.HIGH;
                        case "Today":
                            return task.getCreatedDate().toLocalDate().equals(LocalDateTime.now().toLocalDate());
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());

        displayedTasks.setAll(filtered);
        updateTaskCount();
    }

    private void updateTaskList() {
        filterTasks();
    }

    private void populateTaskDetails() {
        if (selectedTask != null) {
            titleField.setText(selectedTask.getTitle());
            descriptionArea.setText(selectedTask.getDescription());
            categoryCombo.setValue(selectedTask.getCategory());
            priorityCombo.setValue(selectedTask.getPriority());
            completedCheckBox.setSelected(selectedTask.isCompleted());
        }
    }

    private void clearFields() {
        titleField.clear();
        descriptionArea.clear();
        categoryCombo.setValue("Work");
        priorityCombo.setValue(Task.Priority.MEDIUM);
        completedCheckBox.setSelected(false);
        selectedTask = null;
        taskListView.getSelectionModel().clearSelection();
    }

    private void updateTaskCount() {
        int total = allTasks.size();
        int completed = (int) allTasks.stream().filter(Task::isCompleted).count();
        taskCountLabel.setText("Tasks: " + total + " | Completed: " + completed);
    }

    @FXML
    protected void onSaveClick() {
        saveTasks();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Tasks saved successfully");
    }

    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(new ArrayList<>(allTasks));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save tasks: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            allTasks = (List<Task>) ois.readObject();
        } catch (FileNotFoundException e) {
            allTasks = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load tasks: " + e.getMessage());
            allTasks = new ArrayList<>();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
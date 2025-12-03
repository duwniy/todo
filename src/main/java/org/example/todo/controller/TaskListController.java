package org.example.todo.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.todo.model.Task;
import org.example.todo.model.User;
import org.example.todo.service.UserService;
import org.example.todo.service.TaskService; // <-- НОВЫЙ ИМПОРТ

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TaskListController {
    @FXML
    private ListView<Task> taskListView;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<Task.Priority> priorityCombo;
    @FXML private CheckBox completedCheckBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label taskCountLabel;
    @FXML private Label userNameLabel;
    @FXML private DatePicker dueDatePicker;
    @FXML private Button logoutButton;

    private User currentUser;
    private Task selectedTask = null;
    private UserService userService = new UserService();
    private TaskService taskService = new TaskService(); // <-- ИНИЦИАЛИЗАЦИЯ НОВОГО СЕРВИСА
    private ObservableList<Task> displayedTasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBoxes();
        taskListView.setItems(displayedTasks);
        setupTaskListCellFactory();

        taskListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedTask = newVal;
            populateTaskDetails();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTasks());
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterTasks());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userNameLabel.setText("User: " + user.getUsername());
        // Вызываем загрузку задач через TaskService
        loadUserTasks();
        updateTaskList();
    }

    // ... (Методы setupComboBoxes, setupTaskListCellFactory, TaskListCell остаются без изменений) ...

    private void setupComboBoxes() {
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Work", "Personal", "Shopping", "Health", "Education", "Other"
        );
        categoryCombo.setItems(categories);
        categoryCombo.setValue("Work");

        ObservableList<Task.Priority> priorities = FXCollections.observableArrayList(
                Task.Priority.HIGH, Task.Priority.MEDIUM, Task.Priority.LOW
        );
        priorityCombo.setItems(priorities);
        priorityCombo.setValue(Task.Priority.MEDIUM);

        ObservableList<String> filters = FXCollections.observableArrayList(
                "All", "Completed", "Incomplete", "High Priority", "Overdue", "Today"
        );
        filterCombo.setItems(filters);
        filterCombo.setValue("All");
    }

    private void setupTaskListCellFactory() {
        taskListView.setCellFactory(lv -> new TaskListCell());
    }

    @FXML
    protected void onAddTaskClick() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Title", "Please enter a task title");
            return;
        }

        LocalDateTime dueDate = null;
        if (dueDatePicker.getValue() != null) {
            dueDate = dueDatePicker.getValue().atStartOfDay();
        }

        Task newTask = new Task(
                title,
                descriptionArea.getText(),
                categoryCombo.getValue(),
                priorityCombo.getValue(),
                dueDate
        );

        currentUser.addTask(newTask);
        clearFields();
        updateTaskList();
        saveUserTasks();
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

        if (dueDatePicker.getValue() != null) {
            selectedTask.setDueDate(dueDatePicker.getValue().atStartOfDay());
        }

        taskListView.refresh();
        updateTaskList();
        saveUserTasks();
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
            currentUser.removeTask(selectedTask);
            clearFields();
            updateTaskList();
            saveUserTasks();
        }
    }

    @FXML
    protected void onClearAllClick() {
        if (currentUser.getTasks().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Empty", "No tasks to clear");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm");
        confirmation.setHeaderText("Delete all tasks?");
        confirmation.setContentText("This action cannot be undone!");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            currentUser.getTasks().clear();
            clearFields();
            updateTaskList();
            saveUserTasks();
        }
    }

    @FXML
    protected void onLogoutClick() {
        saveUserTasks(); // <-- Сохраняем задачи перед выходом
        try {
            URL resource = getClass().getResource("/org/example/todo/login.fxml");
            if (resource == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "FXML file not found");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Scene scene = new Scene(fxmlLoader.load(), 500, 600);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setTitle("Todo List - Sign In");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error signing out: " + e.getMessage());
        }
    }

    private void filterTasks() {
        if (currentUser == null) return;

        String searchText = searchField.getText().toLowerCase();
        String filterType = filterCombo.getValue();
        LocalDate today = LocalDate.now();

        List<Task> filtered = currentUser.getTasks().stream()
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
                        case "Overdue":
                            return task.getDueDate() != null &&
                                    task.getDueDate().toLocalDate().isBefore(today) &&
                                    !task.isCompleted();
                        case "Today":
                            return task.getDueDate() != null &&
                                    task.getDueDate().toLocalDate().equals(today);
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
            if (selectedTask.getDueDate() != null) {
                dueDatePicker.setValue(selectedTask.getDueDate().toLocalDate());
            } else {
                dueDatePicker.setValue(null);
            }
        }
    }

    private void clearFields() {
        titleField.clear();
        descriptionArea.clear();
        categoryCombo.setValue("Work");
        priorityCombo.setValue(Task.Priority.MEDIUM);
        completedCheckBox.setSelected(false);
        dueDatePicker.setValue(null);
        selectedTask = null;
        taskListView.getSelectionModel().clearSelection();
    }

    private void updateTaskCount() {
        int total = currentUser.getTasks().size();
        int completed = (int) currentUser.getTasks().stream().filter(Task::isCompleted).count();
        taskCountLabel.setText("Tasks: " + total + " | Completed: " + completed);
    }

    // --- ЛОГИКА СОХРАНЕНИЯ/ЗАГРУЗКИ (TaskService) ---

    private void saveUserTasks() {
        // Вызывает TaskService для сохранения задач в файл (tasks_username.dat)
        taskService.saveUserTasks(currentUser);
    }

    private void loadUserTasks() {
        // Вызывает TaskService для загрузки задач из файла
        List<Task> loadedTasks = taskService.loadUserTasks(currentUser.getUsername());
        currentUser.setTasks(loadedTasks);
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class for custom task cell rendering with colors
    private class TaskListCell extends ListCell<Task> {
        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            if (empty || task == null) {
                setText(null);
                setStyle("");
            } else {
                setText(task.toString());
                LocalDate today = LocalDate.now();
                String baseStyle = "-fx-font-weight: normal;";

                if (task.isCompleted()) {
                    setStyle(baseStyle + "-fx-text-fill: #008800; -fx-font-style: italic;");
                } else if (task.getDueDate() != null &&
                        task.getDueDate().toLocalDate().isBefore(today)) {
                    // Просрочено и не выполнено
                    setStyle(baseStyle + "-fx-text-fill: #ff0000; -fx-font-weight: bold;");
                } else if (task.getPriority() == Task.Priority.HIGH) {
                    setStyle(baseStyle + "-fx-text-fill: #cc0000; -fx-font-weight: bold;");
                } else if (task.getPriority() == Task.Priority.MEDIUM) {
                    setStyle(baseStyle + "-fx-text-fill: #ff8800;");
                } else {
                    setStyle(baseStyle + "-fx-text-fill: #000000;"); // Default black/dark
                }
            }
        }
    }
}
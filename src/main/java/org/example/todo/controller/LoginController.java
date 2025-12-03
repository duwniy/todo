package org.example.todo.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.todo.model.User;
import org.example.todo.service.UserService;

import java.io.IOException;
import java.net.URL;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Hyperlink switchToRegisterLink;

    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        errorLabel.setText("");
        loginButton.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        registerButton.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        switchToRegisterLink.setOnAction(event -> onSwitchToRegister());
    }

    @FXML
    protected void onLoginClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields!");
            return;
        }

        User user = userService.authenticate(username, password);
        if (user != null) {
            openTaskList(user);
        } else {
            showError("Invalid username or password");
        }
    }

    @FXML
    protected void onRegisterClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields!");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters long");
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters long");
            return;
        }

        if (userService.getUserByUsername(username) != null) {
            showError("User with this username already exists");
            return;
        }

        User newUser = new User(username, password);
        userService.saveUser(newUser);
        showSuccess("User successfully registered! Now sign in.");
        clearFields();
        onSwitchToLogin();
    }

    @FXML
    protected void onSwitchToRegister() {
        clearFields();
        errorLabel.setText("");
        loginButton.setVisible(false);
        registerButton.setVisible(true);
        switchToRegisterLink.setText("Already have an account? Sign in");
        switchToRegisterLink.setOnAction(event -> onSwitchToLogin());
    }

    @FXML
    protected void onSwitchToLogin() {
        clearFields();
        errorLabel.setText("");
        loginButton.setVisible(true);
        registerButton.setVisible(false);
        switchToRegisterLink.setText("No account? Register here");
        switchToRegisterLink.setOnAction(event -> onSwitchToRegister());
    }

    private void openTaskList(User user) {
        try {
            URL resource = getClass().getResource("/org/example/todo/task-list.fxml");
            if (resource == null) {
                showError("task-list.fxml not found at /org/example/todo/task-list.fxml");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Scene scene = new Scene(fxmlLoader.load(), 900, 700);

            TaskListController taskListController = fxmlLoader.getController();
            // ПЕРЕДАЕМ ПОЛЬЗОВАТЕЛЯ КОНТРОЛЛЕРУ СПИСКА ЗАДАЧ
            taskListController.setCurrentUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Todo List - " + user.getUsername());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error opening application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 12;");
        errorLabel.setText(message);
    }

    private void showSuccess(String message) {
        errorLabel.setStyle("-fx-text-fill: #00cc00; -fx-font-size: 12;");
        errorLabel.setText(message);
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }
}
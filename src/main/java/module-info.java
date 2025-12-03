module org.example.todo {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.todo to javafx.fxml;
    exports org.example.todo;

    opens org.example.todo.model to javafx.fxml;
    exports org.example.todo.model;

    opens org.example.todo.controller to javafx.fxml;
    exports org.example.todo.controller;

    opens org.example.todo.service to javafx.fxml;
    exports org.example.todo.service;
}
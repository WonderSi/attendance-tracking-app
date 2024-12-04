package org.example.attendancetrackingapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle("Attendance Tracking App");

        Button createTableButton = new Button("Create Table");
        Button addButton = new Button("Add date in BD");
        Button viewButton = new Button("View date in BD");

        createTableButton.setOnAction(e -> createTable());

        VBox layout = new VBox(10, createTableButton, addButton, viewButton);
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void createTable() {
        String url = "jdbc:mysql://localhost:3306/test";

    }

    public static void main(String[] args) {
        launch(args);
    }
}

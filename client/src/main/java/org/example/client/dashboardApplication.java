package org.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class dashboardApplication extends Application {
    private dashboardController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/fxml/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Система учета посещаемости");
        primaryStage.setScene(scene);
//        primaryStage.setOnCloseRequest(event -> controller.shutdown()); // Обработчик закрытия
        primaryStage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
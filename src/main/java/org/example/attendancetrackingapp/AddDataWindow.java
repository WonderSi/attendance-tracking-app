package org.example.attendancetrackingapp;

import com.sun.source.tree.TryTree;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class AddDataWindow {
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Attendance Tracker");
        TextField idField = new TextField();
        idField.setPromptText("Введите ID (или оставьте поле пустым для автоинкремента)");

        TextField nameField = new TextField();
        nameField.setPromptText("Введите имя");

        TextField valueField = new TextField();
        valueField.setPromptText("Введите значение");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String idText = idField.getText();
            int id = idText.isEmpty() ? 0 : Integer.parseInt(idText);
            String name = nameField.getText();
            String value = valueField.getText();

            Data data = new Data(id, name, value);
            saveToDataBase(data);
            window.close();
        });

        VBox laout = new VBox(10,idField,nameField,valueField, saveButton);
        Scene scene = new Scene(laout,300,250);
        window.setScene(scene);
        window.showAndWait();
    }

    public static void saveToDataBase(Data data) {
        String url = "jdbc:mysql://localhost:3306/test";
        String user = "root";
        String password = "root";

        try(Connection conn = DriverManager.getConnection(url,user,password);){
            String query;
            if (data.id() == 0) {
                query = "INSERT INTO data(name,value) VALUES(?,?)";
            } else {
                query = "INSERT INTO data(id, name, value) VALUES(?,?,?)";
            }
            PreparedStatement stmt = conn.prepareStatement(query);
            if (data.id() == 0) {
                stmt.setString(1,data.name());
                stmt.setString(2,data.value());
            } else {
                stmt.setInt(1, data.id());
                stmt.setString(2,data.name());
                stmt.setString(3,data.value());
            }
            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Успех!", "Данные успешно добавлены!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка!", "Не удалось добавить данные в СУБД MySQL!");
        }
    }
}

package org.example.attendancetrackingapp;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class AddViewWindow {
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void display() {
        Stage window = new Stage();
        window.setTitle("Add View");

        TableView<Data> tableView = new TableView<>();

        TableColumn<Data, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().id()));

        TableColumn<Data, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().name()));

        TableColumn<Data, String> valueColumn = new TableColumn<>("Name");
        valueColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().value()));

        tableView.getColumns().addAll(idColumn, nameColumn, valueColumn);
        tableView.setItems(fetchDataFromDatabase());

        Scene scene = new Scene(tableView, 400, 300);
        window.setScene(scene);
        window.showAndWait();

    }

    private static ObservableList<Data> fetchDataFromDatabase() {
        ObservableList<Data> dataList = FXCollections.observableArrayList();
        String url = "jdbc:mysql://localhost:3306/test";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password);) {
            String query = "SELECT * FROM data";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while(rs.next()) {
                dataList.add(new Data(rs.getInt("id"), rs.getString("name"), rs.getString("value")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка!", "Не удалось вывести данные из СУБД MySQL!");
        }
        return dataList;
    }
}

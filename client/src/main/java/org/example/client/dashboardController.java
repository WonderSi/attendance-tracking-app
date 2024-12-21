package org.example.client;

import com.google.gson.Gson;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class dashboardController implements Initializable {

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button home_btn;

    @FXML
    private BarChart<?, ?> home_chart;

    @FXML
    private AnchorPane home_form;

    @FXML
    private Button students_addBtn;

    @FXML
    private Button students_btn;

    @FXML
    private Button students_clearBtn;

    @FXML
    private TableColumn<?, ?> students_col_ID;

    @FXML
    private TableColumn<?, ?> students_col_date;

    @FXML
    private TableColumn<?, ?> students_col_descipline;

    @FXML
    private TableColumn<?, ?> students_col_firstName;

    @FXML
    private TableColumn<?, ?> students_col_group;

    @FXML
    private TableColumn<?, ?> students_col_lastName;

    @FXML
    private TableColumn<?, ?> students_col_note;

    @FXML
    private TableColumn<?, ?> students_col_status;

    @FXML
    private TextField students_date;

    @FXML
    private Button students_deleteBtn;

    @FXML
    private TextField students_discipline;

    @FXML
    private TextField students_firstName;

    @FXML
    private AnchorPane students_form;

    @FXML
    private TextField students_group;

    @FXML
    private TextField students_lastName;

    @FXML
    private TextField students_note;

    @FXML
    private TextField students_search;

    @FXML
    private ChoiceBox<?> students_status;

    @FXML
    private TextField students_studentsID;

    @FXML
    private AnchorPane students_tableView;

    @FXML
    private Button students_updateBtn;

    @FXML
    private Label username;

    @FXML
    private TextArea serverMessages;

    @FXML
    private TextField commandInput;

    @FXML
    private Button sendCommandBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        students_addBtn.setOnAction(event -> {
            try (Socket socket = new Socket("127.0.0.1", 2048);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Отправка команды на сервер
                out.println("GET_DATA");

                // Получение ответа от сервера
                String response = in.readLine();
                // Обработка ответа и обновление интерфейса
                textArea.setText(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadDataFromServer() {
        try (Socket socket = new Socket("localhost", 2048);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Отправляем команду серверу
            out.println("SET_STUDENTS");

            // Получаем JSON с сервера
            String jsonData = in.readLine();

            // Преобразуем JSON в список объектов
            Gson gson = new Gson();
            Type listType = new TypeToken<List<StudentData>>() {}.getType();
            List<StudentData> students = gson.fromJson(jsonData, listType);

            // Добавляем данные в таблицу
            ObservableList<StudentData> data = FXCollections.observableArrayList(students);
            tableView.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void addStudentAdd{
        Date student = new Date();
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    }

    public void switchForm(ActionEvent event) {
        if (event.getSource() == home_btn) {
            home_form.setVisible(true);
            students_form.setVisible(false);
        } else if (event.getSource() == students_btn) {
            home_form.setVisible(false);
            students_form.setVisible(true);
        }

    }
}

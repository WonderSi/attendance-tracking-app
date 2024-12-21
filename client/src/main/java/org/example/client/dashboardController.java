package org.example.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.example.shared.StudentData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.util.List;
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
    private TableView<StudentData> students_tableView;

    @FXML
    private TableColumn<StudentData, Integer> students_col_ID;

    @FXML
    private TableColumn<StudentData, Date> students_col_date;

    @FXML
    private TableColumn<StudentData, String> students_col_discipline;

    @FXML
    private TableColumn<StudentData, String> students_col_firstName;

    @FXML
    private TableColumn<StudentData, String> students_col_group;

    @FXML
    private TableColumn<StudentData, String> students_col_lastName;

    @FXML
    private TableColumn<StudentData, String> students_col_note;

    @FXML
    private TableColumn<StudentData, String> students_col_status;

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
    private Button students_updateBtn;

    @FXML
    private Label username;

    @FXML
    private TextArea serverMessages;

    @FXML
    private TextField commandInput;

    @FXML
    private Button sendCommandBtn;

    private final ObservableList<StudentData> student = FXCollections.observableArrayList();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //инициализация
        students_col_ID.setCellValueFactory(new PropertyValueFactory<>("studentID"));
        students_col_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        students_col_discipline.setCellValueFactory(new PropertyValueFactory<>("discipline"));
        students_col_group.setCellValueFactory(new PropertyValueFactory<>("groupID"));
        students_col_firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        students_col_lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        students_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        students_col_note.setCellValueFactory(new PropertyValueFactory<>("note"));

        students_tableView.setItems(student);

        //подключение
        try {
            socket = new Socket("127.0.0.1", 2048);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Connection Error", "Не удалось подключиться к серверу.");
        }

    }

    private void refreshTable() {
        out.println("GET_USERS");
        try {
            String response = in.readLine();
            if ("ERROR".equals(response)) {
                showAlert("Error", "Failed to retrieve users.");
                return;
            }

            student.clear();
            if (!response.isEmpty()) {
                String[] userEntries = response.split(";");
                for (String entry : userEntries) {
                    String[] fields = entry.split(",");
                    if (fields.length == 8) {
                        int studentID = Integer.parseInt(fields[0]);
                        Date date = Date.valueOf(fields[1]);
                        String discipline = fields[2];
                        String groupID = fields[3];
                        String firstName = fields[4];
                        String lastName = fields[5];
                        String status = fields[6];
                        String note = fields[7];
                        student.add(new StudentData(studentID, date, discipline, groupID, firstName, lastName, status, note));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Communication Error", "Ошибка связи с сервером.");
        }
    }

    private void loadDataFromServer() {
        try (Socket socket = new Socket("127.0.0.1", 2048);
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
            students_tableView.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

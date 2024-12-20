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

import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
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

    @FXML private TableView<StudentData> studentsTableView;
    @FXML private TableColumn<StudentData, Integer> studentsColStudentID;
    @FXML private TableColumn<StudentData, String> studentsColDate;
    @FXML private TableColumn<StudentData, String> studentsColDiscipline;
    @FXML private TableColumn<StudentData, String> studentsColGroupID;
    @FXML private TableColumn<StudentData, String> studentsColFirstName;
    @FXML private TableColumn<StudentData, String> studentsColLastName;
    @FXML private TableColumn<StudentData, String> studentsColStatus;
    @FXML private TableColumn<StudentData, String> studentsColNote;

    @FXML private TextField studentsFieldStudentID;
    @FXML private TextField studentsFieldDate;
    @FXML private TextField studentsFieldDiscipline;
    @FXML private TextField studentsFieldGroupID;
    @FXML private TextField studentsFieldFirstName;
    @FXML private TextField studentsFieldLastName;
    @FXML private ChoiceBox studentsChoiceBoxStatus;
    @FXML private TextField studentsFieldNote;

    @FXML
    private Button students_deleteBtn;
    @FXML
    private AnchorPane students_form;
    @FXML
    private TextField students_search;
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


    @FXML
    private void studentsBtnClear() {
        studentsFieldStudentID.clear();
        studentsFieldDate.clear();
        studentsFieldDiscipline.clear();
        studentsFieldGroupID.clear();
        studentsFieldFirstName.clear();
        studentsFieldLastName.clear();
        studentsChoiceBoxStatus.getSelectionModel().clearSelection();
        studentsFieldNote.clear();
    }
    @FXML
    private void studentsBtnAdd() {
        String date = studentsFieldDate.getText();
        String discipline = studentsFieldDiscipline.getText();
        String groupID = studentsFieldGroupID.getText();
        String firstName = studentsFieldFirstName.getText();
        String lastName = studentsFieldLastName.getText();
        String status = (String) studentsChoiceBoxStatus.getValue();
        String note = studentsFieldNote.getText();

        if (date.isEmpty() || discipline.isEmpty() || groupID.isEmpty() ||
                firstName.isEmpty() || lastName.isEmpty() || status.isEmpty()) {
            showAlert("Внимание", "Пожалуйста, заполните все поля.");
            return;
        }

        String command = String.join("|", "ADD", date, discipline, groupID, firstName, lastName, status, note);
        out.println(command);

        try {
            String response = in.readLine();
            if (response.startsWith("SUCCESS|")) {
                showAlert("Успех", response.substring(8));
                refreshTable();
                studentsBtnClear();
            } else if (response.startsWith("ERROR|")) {
                showAlert("Ошибка", response.substring(6));
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка при добавлении записи.");
        }
    }

//    @FXML
//    private void studentsUpdate() {
//        StudentData selected = studentsTableView.getSelectionModel().getSelectedItem();
//        if (selected == null) {
//            showAlert("Внимание", "Выберите запись для обновления.");
//            return;
//        }
//
//        String studentID = String.valueOf(selected.getStudentID());
//        String date = fieldDate.getText();
//        String discipline = fieldDiscipline.getText();
//        String groupID = fieldGroupID.getText();
//        String firstName = fieldFirstName.getText();
//        String lastName = fieldLastName.getText();
//        String status = fieldStatus.getText();
//        String note = fieldNote.getText();
//
//        String command = String.join("|", "UPDATE", studentID, date, discipline, groupID, firstName, lastName, status, note);
//        out.println(command);
//
//        try {
//            String response = in.readLine();
//            if (response.startsWith("SUCCESS|")) {
//                showAlert("Успех", response.substring(8));
//                loadData();
//                onClear();
//            } else if (response.startsWith("ERROR|")) {
//                showAlert("Ошибка", response.substring(6));
//            }
//        } catch (IOException e) {
//            showAlert("Ошибка", "Ошибка при обновлении записи.");
//        }
//    }

    private final ObservableList<StudentData> student = FXCollections.observableArrayList();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        studentShowListData();
        studentStatusList();

    }

    public void studentShowListData() {
        //инициализация
        studentsColStudentID.setCellValueFactory(new PropertyValueFactory<>("studentID"));
        studentsColDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        studentsColDiscipline.setCellValueFactory(new PropertyValueFactory<>("discipline"));
        studentsColGroupID.setCellValueFactory(new PropertyValueFactory<>("groupID"));
        studentsColFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        studentsColLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        studentsColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        studentsColNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        studentsTableView.setItems(student);

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
        out.println("GET");
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

    private String[] listStatus = {"Присутствовал", "Отсутствовал"};
    public void studentStatusList() {
        List<String> listS = new ArrayList<> ();

        for (String data : listStatus) {
            listS.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(listS);
        studentsChoiceBoxStatus.setItems(listData);
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

    public void onTableClick(javafx.scene.input.MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) { // Для двойного клика
            StudentData selected = studentsTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                System.out.println("StudentID: " + selected.getStudentID());
                System.out.println("Date: " + selected.getDate());
                System.out.println("Discipline: " + selected.getDiscipline());
                System.out.println("GroupID: " + selected.getGroupID());
                System.out.println("FirstName: " + selected.getFirstName());
                System.out.println("LastName: " + selected.getLastName());
                System.out.println("Status: " + selected.getStatus());
                System.out.println("Note: " + selected.getNote());

                studentsFieldStudentID.setText(String.valueOf(selected.getStudentID()));
                studentsFieldDate.setText(selected.getDate());
                studentsFieldDiscipline.setText(selected.getDiscipline());
                studentsFieldGroupID.setText(selected.getGroupID());
                studentsFieldFirstName.setText(selected.getFirstName());
                studentsFieldLastName.setText(selected.getLastName());
                studentsChoiceBoxStatus.setValue(selected.getStatus());
                studentsFieldNote.setText(selected.getNote());
            }
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

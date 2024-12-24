package org.example.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import org.example.shared.StudentData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.util.*;

public class dashboardController implements Initializable {

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button home_btn;

    @FXML private BarChart<String, Number> home_chart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

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
    @FXML private ComboBox studentsComboBoxStatus;
    @FXML private TextField studentsFieldNote;

    @FXML
    private TextField studentsFieldSearch;
    @FXML
    private ComboBox studentsComboBoxFilterOne;
    @FXML
    private ComboBox studentsComboBoxFilterTwo;
    @FXML
    private ComboBox studentsComboBoxFilterThree;

    @FXML
    private Button students_deleteBtn;
    @FXML
    private AnchorPane students_form;

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

        studentShowListData();
        studentStatusList();
        studentFilterListOne();
        studentFilterListTwo();
        studentFilterListThree();
        updateBarChart();


        studentsTableView.addEventFilter(ScrollEvent.ANY, event -> { if (event.getDeltaX() != 0) { event.consume(); } });
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
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Connection Error", "Не удалось подключиться к серверу.");
        }
    }

    private void loadData() {
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

    private void updateBarChart() {
        //создание серии данных, для столбчатой диаграммы
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Количество посещений по датам");

        //группируем данные по дате и считаем количество посещений,
        Map<String, Long> attendancePerDate = new TreeMap<>();
        for (StudentData sd : student) {
            String date = sd.getDate();
            if ("Присутствовал".equals(sd.getStatus())) {
                //проходимся по студентам и добавляем к счетчику общую посещаемость
                attendancePerDate.put(date, attendancePerDate.getOrDefault(date, 0L) + 1);
            }
        }

        //перебираем все пары и добавляем данные в серию. для каждойпары создается объект XYChart.Data
        for (Map.Entry<String, Long> entry : attendancePerDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Очищаем предыдущие данные и добавляем новую серию
        home_chart.getData().clear();
        home_chart.getData().add(series);
    }



    private String[] listStatus = {"Присутствовал", "Отсутствовал"};
    public void studentStatusList() {
        List<String> listS = new ArrayList<> ();

        for (String data : listStatus) {
            listS.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(listS);
        studentsComboBoxStatus.setItems(listData);
    }

    private String[] listFilterOne = {"Все", "Математика", "Информатика", "Русский"};
    public void studentFilterListOne() {
        List<String> listS = new ArrayList<> ();

        for (String data : listFilterOne) {
            listS.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(listS);
        studentsComboBoxFilterOne.setItems(listData);
        studentsComboBoxFilterOne.getSelectionModel().select("Все");
    }
    private String[] listFilterTwo = {"Все", "ФИТ-231", "МОА-231", "ПМИ-231", "КБ-231", "ПИ-231"};
    public void studentFilterListTwo() {
        List<String> listS = new ArrayList<> ();

        for (String data : listFilterTwo) {
            listS.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(listS);
        studentsComboBoxFilterTwo.setItems(listData);
        studentsComboBoxFilterTwo.getSelectionModel().select("Все");
    }
    private String[] listFilterThree = {"Все", "Присутствовал", "Отсутствовал"};
    public void studentFilterListThree() {
        List<String> listS = new ArrayList<> ();

        for (String data : listFilterThree) {
            listS.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(listS);
        studentsComboBoxFilterThree.setItems(listData);
        studentsComboBoxFilterThree.getSelectionModel().select("Все");
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
                studentsComboBoxStatus.setValue(selected.getStatus());
                studentsFieldNote.setText(selected.getNote());
            }
        }
    }



    private void applyFilterAndSearch() {
        String keyword = studentsFieldSearch.getText().toLowerCase();
        String filter1 = (String) studentsComboBoxFilterOne.getSelectionModel().getSelectedItem();
        String filter2 = (String) studentsComboBoxFilterTwo.getSelectionModel().getSelectedItem();
        String filter3 = (String) studentsComboBoxFilterThree.getSelectionModel().getSelectedItem();

        studentsTableView.setItems(student.filtered(student -> {
            // Проверка поиска
            boolean matchesKeyword = student.getFirstName().toLowerCase().contains(keyword) ||
                    student.getLastName().toLowerCase().contains(keyword) ||
                    student.getDiscipline().toLowerCase().contains(keyword);

            // Проверка первого фильтра
            boolean matchesFilterOne = filter1 == null || filter1.equals("Все") || student.getDiscipline().equals(filter1);

            // Проверка второго фильтра
            boolean matchesFilterTwo = filter2 == null || filter2.equals("Все") || student.getGroupID().equals(filter2);

            // Проверка третьего фильтра
            boolean matchesFilterThree = filter3 == null || filter3.equals("Все") || student.getStatus().equals(filter3);

            // Возвращаем результат: поиск + все активные фильтры
            return matchesKeyword && matchesFilterOne && matchesFilterTwo && matchesFilterThree;
        }));
    }
    @FXML
    private void onSearch() {
        applyFilterAndSearch();
    }
    @FXML
    private void onFilterOne() {
        applyFilterAndSearch();
    }
    @FXML
    private void onFilterTwo() {
        applyFilterAndSearch();
    }
    @FXML
    private void onFilterThree() {
        applyFilterAndSearch();
    }



    @FXML
    private void onClear() {
        studentsFieldStudentID.clear();
        studentsFieldDate.clear();
        studentsFieldDiscipline.clear();
        studentsFieldGroupID.clear();
        studentsFieldFirstName.clear();
        studentsFieldLastName.clear();
        studentsComboBoxStatus.getSelectionModel().clearSelection();
        studentsFieldNote.clear();
    };
    @FXML
    private void onDelete() {
        StudentData selected = studentsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите запись для удаления.");
            return;
        }

        String studentID = studentsFieldStudentID.getText();

        String command = String.join("|", "DELETE", studentID);
        out.println(command);

        try {
            String response = in.readLine();
            if (response.startsWith("SUCCESS|")) {
                showAlert("Успех", response.substring(8));
                loadData();
                onClear();
            } else if (response.startsWith("ERROR|")) {
                showAlert("Ошибка", response.substring(6));
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка при удалении записи.");
        }
    };
    @FXML
    private void onUpdate() {
        StudentData selected = studentsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите запись для обновления.");
            return;
        }

        String studentID = studentsFieldStudentID.getText();
        String date = studentsFieldDate.getText();
        String discipline = studentsFieldDiscipline.getText();
        String groupID = studentsFieldGroupID.getText();
        String firstName = studentsFieldFirstName.getText();
        String lastName = studentsFieldLastName.getText();
        String status = (String) studentsComboBoxStatus.getValue();
        String note = studentsFieldNote.getText();

        String command = String.join("|", "UPDATE", studentID, date, discipline, groupID, firstName, lastName, status, note);
        out.println(command);

        try {
            String response = in.readLine();
            if (response.startsWith("SUCCESS|")) {
                showAlert("Успех", response.substring(8));
                loadData();
                updateBarChart();
                onClear();
            } else if (response.startsWith("ERROR|")) {
                showAlert("Ошибка", response.substring(6));
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка при обновлении записи.");
        }
    };
    @FXML
    private void onAdd() {
        String date = studentsFieldDate.getText();
        String discipline = studentsFieldDiscipline.getText();
        String groupID = studentsFieldGroupID.getText();
        String firstName = studentsFieldFirstName.getText();
        String lastName = studentsFieldLastName.getText();
        String status = (String) studentsComboBoxStatus.getValue();
        String note = studentsFieldNote.getText();
        note = (note.isEmpty()) ? " " : note;

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
                loadData();
                onClear();
            } else if (response.startsWith("ERROR|")) {
                showAlert("Ошибка", response.substring(6));
            }
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка при добавлении записи.");
        }
    };


    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

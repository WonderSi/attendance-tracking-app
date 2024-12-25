package org.example.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import java.util.logging.*;
import java.nio.file.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.shared.StudentData;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.util.*;

public class dashboardController implements Initializable {

    private static final Logger logger = Logger.getLogger(dashboardController.class.getName());

    @FXML
    private AnchorPane main_form;

    @FXML private Label homeLabelUniqueStudents;
    @FXML private Label homeLabelGroupCount;
    @FXML private Label homeLabelDisciplineCount;

    @FXML
    private Button home_btn;

    @FXML private BarChart<String, Number> home_chart;

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
    @FXML private TableColumn<StudentData, Integer> studentsColPoint;

    @FXML private TextField studentsFieldStudentID;
    @FXML private TextField studentsFieldDate;
    @FXML private TextField studentsFieldDiscipline;
    @FXML private TextField studentsFieldGroupID;
    @FXML private TextField studentsFieldFirstName;
    @FXML private TextField studentsFieldLastName;
    @FXML private ComboBox studentsComboBoxStatus;
    @FXML private TextField studentsFieldNote;
    @FXML private TextField studentsFieldPoint;

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
    private AnchorPane reports_form;
    @FXML
    private Button reports_btn;

    private final ObservableList<StudentData> student = FXCollections.observableArrayList();
    private final ObservableList<StudentData> dataForReport = FXCollections.observableArrayList(student);

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupLogger();
        logger.info("Инициализация контроллера");
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

        studentShowListData();
        studentStatusList();
        studentFilterListOne();
        studentFilterListTwo();
        studentFilterListThree();
        updateBarChart();

        updateCounters();


        studentsTableView.addEventFilter(ScrollEvent.ANY, event -> { if (event.getDeltaX() != 0) { event.consume(); } });
        home_btn.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.01), rgba(255,255,255,0.17)); ");
    }

    private void setupLogger() {
        try {
            //удаление стандартных обработчиков
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            //создание обработчика для консоли
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(new SimpleFormatter());

            //создание обработчика для файла
            FileHandler fileHandler = new FileHandler("application.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());

            //добавление обработчиков к логгеру
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось настроить логирование.");
        }
    }

    private void updateCounters() {
        logger.info("Обновление чисел для карточек с информацией");
        long uniqueStudents = student.stream()
                .map(s -> s.getFirstName() + " " + s.getLastName() + " " + s.getGroupID()) // Учитываем имя, фамилию и группу
                .distinct()
                .count();

        System.out.println();

        long groupCount = student.stream()
                .map(StudentData::getGroupID)
                .distinct()
                .count();

        long disciplineCount = student.stream()
                .map(StudentData::getDiscipline)
                .distinct()
                .count();

        homeLabelUniqueStudents.setText(String.valueOf(uniqueStudents));
        homeLabelGroupCount.setText(String.valueOf(groupCount));
        homeLabelDisciplineCount.setText(String.valueOf(disciplineCount));
    }


    public void studentShowListData() {
        logger.info("Инициализация таблицы");
        //инициализация
        studentsColStudentID.setCellValueFactory(new PropertyValueFactory<>("studentID"));
        studentsColDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        studentsColDiscipline.setCellValueFactory(new PropertyValueFactory<>("discipline"));
        studentsColGroupID.setCellValueFactory(new PropertyValueFactory<>("groupID"));
        studentsColFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        studentsColLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        studentsColStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        studentsColNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        studentsColPoint.setCellValueFactory(new PropertyValueFactory<>("point"));

        studentsTableView.setItems(student);
    }
    public void reportShowListData() {
        //инициализация

    }

    private void loadData() {
        logger.info("Запрос данных студентов");
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
                    if (fields.length == 9) {
                        int studentID = Integer.parseInt(fields[0]);
                        Date date = Date.valueOf(fields[1]);
                        String discipline = fields[2];
                        String groupID = fields[3];
                        String firstName = fields[4];
                        String lastName = fields[5];
                        String status = fields[6];
                        String note = fields[7];
                        int point = Integer.parseInt(fields[8]);
                        student.add(new StudentData(studentID, date, discipline, groupID, firstName, lastName, status, note, point));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Communication Error", "Ошибка связи с сервером.");
        }
    }

    private void updateBarChart() {
        logger.info("Обновление диаграммы посещаемости");
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
        logger.info("Переключение между окнами");
        if (event.getSource() == home_btn) {
            home_form.setVisible(true);
            students_form.setVisible(false);
            reports_form.setVisible(false);
            home_btn.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.01), rgba(255,255,255,0.17)); ");
            students_btn.setStyle("-fx-background-color: transparent");
            reports_btn.setStyle("-fx-background-color: transparent");
        } else if (event.getSource() == students_btn) {
            home_form.setVisible(false);
            students_form.setVisible(true);
            reports_form.setVisible(false);
            home_btn.setStyle("-fx-background-color: transparent");
            students_btn.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.01), rgba(255,255,255,0.17)); ");
            reports_btn.setStyle("-fx-background-color: transparent");
        } else if (event.getSource() == reports_btn) {
            home_form.setVisible(false);
            students_form.setVisible(false);
            reports_form.setVisible(true);
            home_btn.setStyle("-fx-background-color: transparent");
            students_btn.setStyle("-fx-background-color: transparent");
            reports_btn.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.01), rgba(255,255,255,0.17)); ");
        }

    }


    public void onTableClick(javafx.scene.input.MouseEvent mouseEvent) {
        logger.info("Выделение данных из таблицы");
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
                System.out.println("Point: " + selected.getPoint());

                studentsFieldStudentID.setText(String.valueOf(selected.getStudentID()));
                studentsFieldDate.setText(selected.getDate());
                studentsFieldDiscipline.setText(selected.getDiscipline());
                studentsFieldGroupID.setText(selected.getGroupID());
                studentsFieldFirstName.setText(selected.getFirstName());
                studentsFieldLastName.setText(selected.getLastName());
                studentsComboBoxStatus.setValue(selected.getStatus());
                studentsFieldNote.setText(selected.getNote());
                studentsFieldPoint.setText(String.valueOf(selected.getPoint()));
            }
        }
    }



    private void applyFilterAndSearch() {
        logger.info("Использование ф-ций фильтра и поиска");
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
        logger.info("Использование строки поиска");
        applyFilterAndSearch();
    }
    @FXML
    private void onFilterOne() {
        logger.info("Использование первого фильтра");
        applyFilterAndSearch();
    }
    @FXML
    private void onFilterTwo() {
        logger.info("Использование второго фильтра");
        applyFilterAndSearch();
    }
    @FXML
    private void onFilterThree() {
        logger.info("Использование третьего фильтра");
        applyFilterAndSearch();
    }



    @FXML
    private void onClear() {
        logger.info("Сброс значений в полях ввода");
        studentsFieldStudentID.clear();
        studentsFieldDate.clear();
        studentsFieldDiscipline.clear();
        studentsFieldGroupID.clear();
        studentsFieldFirstName.clear();
        studentsFieldLastName.clear();
        studentsComboBoxStatus.getSelectionModel().clearSelection();
        studentsFieldNote.clear();
        studentsFieldPoint.clear();
    };
    @FXML
    private void onDelete() {
        logger.info("Попытка удаления записи с ID: " + studentsFieldStudentID.getText());
        StudentData selected = studentsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите запись для удаления.");
            return;
        }

        String studentID = studentsFieldStudentID.getText();

        if (studentID.isEmpty()) {
            showAlert("Внимание", "Пожалуйста, заполните поле ID.");
        }

        String command = String.join("|", "DELETE", studentID);
        out.println(command);

        try {
            String response = in.readLine();
            if (response.startsWith("SUCCESS|")) {
                showAlert("Успех", response.substring(8));
                loadData();
                updateCounters();
                onClear();
                logger.info("Запись с ID " + studentsFieldStudentID.getText() + " успешно удалена");
            } else if (response.startsWith("ERROR|")) {
                showAlert("Ошибка", response.substring(6));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при удалении записи.", e);
            showAlert("Ошибка", "Ошибка при удалении записи: " + e.getMessage());
        }
    };
    @FXML
    private void onUpdate() {
        logger.info("Попытка обновлении записи в таблице");
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
        String point = studentsFieldPoint.getText();

        if (date.isEmpty() || discipline.isEmpty() || groupID.isEmpty() ||
                firstName.isEmpty() || lastName.isEmpty() || status.isEmpty() || point.isEmpty()) {
            showAlert("Внимание", "Пожалуйста, заполните все поля.");
            return;
        }

        String command = String.join("|", "UPDATE", studentID, date, discipline, groupID, firstName, lastName, status, note, point);
        out.println(command);

        try {
            String response = in.readLine();
            if (response.startsWith("SUCCESS|")) {
                showAlert("Успех", response.substring(8));
                loadData();
                updateCounters();
                updateBarChart();
                onClear();
            } else if (response.startsWith("ERROR|")) {
                showAlert("Ошибка", response.substring(6));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при обновлении записи.", e);
            showAlert("Ошибка", "Ошибка при обновлении записи: " + e.getMessage());
        }
    };
    @FXML
    private void onAdd() {
        logger.info("Попытка добавления новой записи");
        String date = studentsFieldDate.getText();
        String discipline = studentsFieldDiscipline.getText();
        String groupID = studentsFieldGroupID.getText();
        String firstName = studentsFieldFirstName.getText();
        String lastName = studentsFieldLastName.getText();
        String status = (String) studentsComboBoxStatus.getValue();
        String note = studentsFieldNote.getText();
        String point = studentsFieldPoint.getText();
        note = (note.isEmpty()) ? " " : note;

        if (date.isEmpty() || discipline.isEmpty() || groupID.isEmpty() ||
                firstName.isEmpty() || lastName.isEmpty() || status.isEmpty() || point.isEmpty()) {
            showAlert("Внимание", "Пожалуйста, заполните все поля.");
            return;
        }

        String command = String.join("|", "ADD", date, discipline, groupID, firstName, lastName, status, note, point);
        out.println(command);

        try {
            String response = in.readLine();
            if (response.startsWith("SUCCESS|")) {
                showAlert("Успех", response.substring(8));
                loadData();
                updateCounters();
                updateBarChart();
                onClear();
                logger.info("Новая запись успешно добавлена: " + firstName + " " + lastName);
            } else if (response.startsWith("ERROR|")) {
                showAlert("Ошибка", response.substring(6));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при добавлении записи", e);
            showAlert("Ошибка", "Ошибка при добавлении записи: " + e.getMessage());
        }
    };






    @FXML
    private void onExportExcel() {
        logger.info("Попытка экспорт данных в формате Excel");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить как Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(main_form.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Students");
                Row headerRow = sheet.createRow(0);

                // Creating headers
                for (int i = 0; i < studentsTableView.getColumns().size(); i++) {
                    // Using the correct org.apache.poi.ss.usermodel.Cell class
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(studentsTableView.getColumns().get(i).getText());
                }

                // Заполнение данными
                for (int i = 0; i < student.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    StudentData sd = student.get(i);

                    row.createCell(0).setCellValue(sd.getStudentID());
                    row.createCell(1).setCellValue(sd.getDate());
                    row.createCell(2).setCellValue(sd.getDiscipline());
                    row.createCell(3).setCellValue(sd.getGroupID());
                    row.createCell(4).setCellValue(sd.getFirstName());
                    row.createCell(5).setCellValue(sd.getLastName());
                    row.createCell(6).setCellValue(sd.getStatus());
                    row.createCell(7).setCellValue(sd.getNote());
                }

                // Авторазмер колонок
                for (int i = 0; i < studentsTableView.getColumns().size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // Запись в файл
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                showAlert("Успех", "Экспорт в Excel выполнен успешно.");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Не удалось экспортировать в Excel.", e);
                showAlert("Ошибка", "Не удалось экспортировать в Excel: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        logger.info("Вызов Alert");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

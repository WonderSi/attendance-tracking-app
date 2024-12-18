package org.example.client;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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


    public void switchForm(ActionEvent event) {
        if (event.getSource() == home_btn) {
            home_form.setVisible(true);
            students_form.setVisible(false);
        } else if (event.getSource() == students_btn) {
            home_form.setVisible(false);
            students_form.setVisible(true);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}

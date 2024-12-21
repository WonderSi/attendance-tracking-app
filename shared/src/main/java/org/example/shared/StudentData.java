package org.example.shared;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;

public class StudentData {
    private final IntegerProperty studentID;
    private final StringProperty date;
    private final StringProperty discipline;
    private final StringProperty groupID;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty status;
    private final StringProperty note;

    public StudentData(Integer studentID, Date date, String discipline, String groupID, String firstName, String lastName, String status, String note) {
        this.studentID = new SimpleIntegerProperty(studentID);
        this.date = new SimpleStringProperty(date.toString());
        this.discipline = new SimpleStringProperty(discipline);
        this.groupID = new SimpleStringProperty(groupID);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.status = new SimpleStringProperty(status);
        this.note = new SimpleStringProperty(note);
    }

    public Integer getStudentID() {
        return studentID.get();
    }
    public IntegerProperty studentIDProperty() {
        return studentID;
    }
    public void setStudentID(Integer studentID) {
        this.studentID.set(studentID);
    }


    public String getDate() {
        return date.get();
    }
    public StringProperty dateProperty() {
        return date;
    }
    public void setDate(String date) {
        this.date.set(date);
    }


    public String getDiscipline() {
        return discipline.get();
    }
    public StringProperty disciplineProperty() {
        return discipline;
    }
    public void setDiscipline(String discipline) {
        this.discipline.set(discipline);
    }


    public String getGroupID() {
        return groupID.get();
    }
    public StringProperty groupIDProperty() {
        return groupID;
    }
    public void setGroupID(String groupID) {
        this.groupID.set(groupID);
    }


    public String getFirstName() {
        return firstName.get();
    }
    public StringProperty firstNameProperty() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }


    public String getLastName() {
        return lastName.get();
    }
    public StringProperty lastNameProperty() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }


    public String getStatus() {
        return status.get();
    }
    public StringProperty statusProperty() {
        return status;
    }
    public void setStatus(String status) {
        this.status.set(status);
    }


    public String getNote() {
        return note.get();
    }
    public StringProperty noteProperty() {
        return note;
    }
    public void setNote(String note) {
        this.note.set(note);
    }
}
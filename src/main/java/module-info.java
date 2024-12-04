module org.example.attendancetrackingapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens org.example.attendancetrackingapp to javafx.fxml;
    exports org.example.attendancetrackingapp;
}
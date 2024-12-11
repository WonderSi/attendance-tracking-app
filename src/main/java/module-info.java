module org.example.attendance.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jdk.compiler;
    requires de.jensd.fx.glyphs.fontawesome;

    opens org.example.attendance.client to javafx.fxml;
    exports org.example.attendance.client;
}
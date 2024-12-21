module org.example.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.example.shared; // Указываем зависимость на shared

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires jdk.compiler;
    requires de.jensd.fx.glyphs.fontawesome;
    requires java.sql;
    requires com.google.gson;

    opens org.example.client to javafx.fxml;
    exports org.example.client;
}
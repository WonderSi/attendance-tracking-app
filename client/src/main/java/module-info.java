module org.example.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires jdk.compiler;
    requires de.jensd.fx.glyphs.fontawesome;

    opens org.example.client to javafx.fxml;
    exports org.example.client;
}
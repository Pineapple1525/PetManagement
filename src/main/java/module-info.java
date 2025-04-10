module com.petmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires kernel;
    requires layout;
    requires io;
    requires java.desktop;

    opens com.petmanagement to javafx.fxml;
    exports com.petmanagement;
    exports com.petmanagement.controller;
    opens com.petmanagement.controller to javafx.fxml;
}
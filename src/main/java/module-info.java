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
    requires kotlin.stdlib;

    opens com.petmanagement to javafx.fxml;
    opens com.petmanagement.controller to javafx.fxml;
    opens com.petmanagement.model to javafx.base;

    exports com.petmanagement;
    exports com.petmanagement.controller;
    exports com.petmanagement.model;
}

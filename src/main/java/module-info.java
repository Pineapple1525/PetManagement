module com.petmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.petmanagement to javafx.fxml;
    exports com.petmanagement;
}
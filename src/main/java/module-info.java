module com.petmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    opens com.petmanagement to javafx.graphics, javafx.fxml;
    opens com.petmanagement.Controller to javafx.fxml;

    exports com.petmanagement;
    exports com.petmanagement.Controller;
}

module com.petmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires kernel;
    requires layout;
    requires io;
    requires java.desktop;
    requires mysql.connector.j;
    requires com.google.protobuf;
    requires java.mail; // 👈 cần thêm dòng này
    requires jbcrypt;
    requires itextpdf; // ✅ THÊM DÒNG NÀY ĐỂ FIX LỖI

    opens com.petmanagement to javafx.fxml;
    opens com.petmanagement.model to javafx.base;


    exports com.petmanagement;
    exports com.petmanagement.controller;
    opens com.petmanagement.controller to javafx.fxml;
}
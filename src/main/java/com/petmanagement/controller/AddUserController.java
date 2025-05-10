package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

public class AddUserController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll();
    }

    @FXML
    public void handleSaveUser(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Vui lòng điền đầy đủ thông tin.");
            return;
        }
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, role);

            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Thêm người dùng thành công.");
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi khi thêm người dùng: " + e.getMessage());
        }
    }

    // Phương thức băm mật khẩu bằng SHA-1
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            return no.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }



    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

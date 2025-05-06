package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditUserController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> roleField;
    @FXML
    private TextField passwordField;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        roleField.getItems().setAll("Boss", "Manager", "Employee");
        roleField.setValue(user.getRole());
    }

    @FXML
    private void handleUpdateUser() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleField.getValue().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || role.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Vui lòng điền đầy đủ thông tin.");
            return;
        }

        String sql = "UPDATE users SET username = ?, email = ?, role = ?, password = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, role);
            stmt.setString(4, password);
            stmt.setInt(5, currentUser.getId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Cập nhật người dùng thành công.");
                javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Không tìm thấy người dùng để cập nhật.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

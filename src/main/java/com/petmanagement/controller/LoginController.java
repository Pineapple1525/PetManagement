package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.MessageDigest;
import java.sql.*;

public class LoginController {

    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private TextField usernameField;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label togglePasswordIcon;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        loginButton.setOnAction(this::handleLogin);

        togglePasswordIcon.setOnMouseClicked(event -> togglePasswordVisibility());

        passwordField.textProperty().addListener((obs, oldText, newText) -> visiblePasswordField.setText(newText));
        visiblePasswordField.textProperty().addListener((obs, oldText, newText) -> passwordField.setText(newText));

        forgotPasswordLink.setOnAction(this::handleForgotPassword);
    }

    private void togglePasswordVisibility() {
        boolean showing = passwordField.isVisible();

        if (showing) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordIcon.setText("🙈"); // ẩn
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            togglePasswordIcon.setText("👁"); // hiện
        }
    }
@FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String passwordInput = passwordField.isVisible() ? passwordField.getText().trim() : visiblePasswordField.getText().trim();

        if (username.isEmpty() || passwordInput.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin");
            errorLabel.setVisible(true);
            return;
        }

        String hashedPassword = hashPassword(passwordInput); // SHA-1

        try (Connection conn = DBConnection.getConn()) {
            String sql = "SELECT * FROM users WHERE BINARY username = ? AND BINARY password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                errorLabel.setVisible(false);
                String role = getUserRole(username);
                Session.set(username, role);
                System.out.println("Role người dùng: " + role);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/main.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Trang chính");
                stage.show();
            } else {
                errorLabel.setText("Tên đăng nhập hoặc mật khẩu không đúng");
                errorLabel.setVisible(true);
            }

        } catch (SQLException | IOException e) {
            errorLabel.setText("Lỗi hệ thống: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/forgot_password.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Quên mật khẩu");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở giao diện quên mật khẩu", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1"); // ← Dùng SHA-1 thay vì SHA-256
            byte[] result = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getUserRole(String username) {
        String role = null;
        String sql = "SELECT role FROM users WHERE BINARY username = ?";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                role = rs.getString("role");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }
}

package com.petmanagement.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.MessageDigest;
import java.sql.*;

public class ForgotPasswordController {

    @FXML private VBox emailPane;
    @FXML private VBox passwordPane;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleNewPasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private Label passwordErrorLabel;

    @FXML private Label toggleNewPassIcon;
    @FXML private Label toggleConfirmPassIcon;

    private final String DB_URL = "jdbc:mysql://localhost:3306/petshop";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    private String verifiedEmail = null;

    @FXML
    private void initialize() {
        newPasswordField.textProperty().addListener((obs, oldText, newText) -> visibleNewPasswordField.setText(newText));
        visibleNewPasswordField.textProperty().addListener((obs, oldText, newText) -> newPasswordField.setText(newText));

        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> visibleConfirmPasswordField.setText(newText));
        visibleConfirmPasswordField.textProperty().addListener((obs, oldText, newText) -> confirmPasswordField.setText(newText));

        toggleNewPassIcon.setOnMouseClicked(e -> toggleNewPasswordVisibility());
        toggleConfirmPassIcon.setOnMouseClicked(e -> toggleConfirmPasswordVisibility());
    }

    private void toggleNewPasswordVisibility() {
        boolean isHidden = newPasswordField.isVisible();

        if (isHidden) {
            visibleNewPasswordField.setText(newPasswordField.getText());
            visibleNewPasswordField.setVisible(true);
            visibleNewPasswordField.setManaged(true);
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            toggleNewPassIcon.setText("🙈");
        } else {
            newPasswordField.setText(visibleNewPasswordField.getText());
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            visibleNewPasswordField.setVisible(false);
            visibleNewPasswordField.setManaged(false);
            toggleNewPassIcon.setText("👁");
        }
    }

    private void toggleConfirmPasswordVisibility() {
        boolean isHidden = confirmPasswordField.isVisible();

        if (isHidden) {
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            visibleConfirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            toggleConfirmPassIcon.setText("🙈");
        } else {
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);
            toggleConfirmPassIcon.setText("👁");
        }
    }

    @FXML
    private void handleVerifyEmail() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            emailErrorLabel.setText("Vui lòng nhập email.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                verifiedEmail = email;
                emailPane.setVisible(false);
                passwordPane.setVisible(true);
                emailErrorLabel.setText("");
            } else {
                emailErrorLabel.setText("Email không tồn tại trong hệ thống.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            emailErrorLabel.setText("Lỗi kết nối cơ sở dữ liệu.");
        }
    }

    @FXML
    private void handleResetPassword() {
        String newPass = newPasswordField.isVisible() ? newPasswordField.getText() : visibleNewPasswordField.getText();
        String confirmPass = confirmPasswordField.isVisible() ? confirmPasswordField.getText() : visibleConfirmPasswordField.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            passwordErrorLabel.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            passwordErrorLabel.setText("Mật khẩu không khớp.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String hashedPassword = hashPassword(newPass);

            String updateQuery = "UPDATE users SET password = ? WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, verifiedEmail);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Thành công", "Đặt lại mật khẩu thành công. Đang chuyển về trang đăng nhập...");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) passwordPane.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Đăng nhập");
                stage.show();
            } else {
                passwordErrorLabel.setText("Không thể cập nhật mật khẩu.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            passwordErrorLabel.setText("Lỗi cơ sở dữ liệu.");
        } catch (IOException e) {
            e.printStackTrace();
            passwordErrorLabel.setText("Không thể quay lại trang đăng nhập.");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

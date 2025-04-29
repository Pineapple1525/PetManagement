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

import java.sql.*;
import java.security.*;
import java.nio.charset.StandardCharsets;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink forgotPasswordLink;


    // Xử lý đăng nhập
    // Xử lý đăng nhập
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String hashedPassword = hashSHA1(password);

        try (Connection conn = DBConnection.getConn()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                errorLabel.setText("Login thành công với quyền: " + role);

                // Load giao diện main.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/main.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Trang chính");
                stage.show();
            } else {
                errorLabel.setText("Sai tài khoản hoặc mật khẩu!");
            }

        } catch (Exception e) {
            errorLabel.setText("Lỗi: " + e.getMessage());
        }
    }

    // Mã hóa SHA-1
    public static String hashSHA1(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 không được hỗ trợ");
        }
    }

    @FXML private TextField visiblePasswordField;
    @FXML private Label togglePasswordIcon;

    private boolean passwordVisible = false;

    @FXML
    private void initialize() {
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> visiblePasswordField.setText(newVal));
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> passwordField.setText(newVal));

        togglePasswordIcon.setOnMouseClicked(e -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordIcon.setText("🙈");
        } else {
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordIcon.setText("👁");
        }
    }

    // Xử lý quên mật khẩu
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/forgot_password.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Quên mật khẩu");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            errorLabel.setText("Không thể mở giao diện quên mật khẩu.");
            e.printStackTrace();
        }
    }

}

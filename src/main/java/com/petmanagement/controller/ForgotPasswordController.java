package com.petmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.security.*;
import java.nio.charset.StandardCharsets;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label statusLabel;

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/petshop", "root", "");
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        if (email.isEmpty() || newPassword.isEmpty()) {
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        try (Connection conn = getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Email tồn tại, thực hiện cập nhật
                String hashedPassword = hashSHA1(newPassword);
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET password = ? WHERE email = ?");
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, email);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    statusLabel.setText("Đặt lại mật khẩu thành công!");
                } else {
                    statusLabel.setText("Không thể cập nhật mật khẩu.");
                }

            } else {
                statusLabel.setText("Email không tồn tại trong hệ thống.");
            }
        } catch (SQLException e) {
            statusLabel.setText("Lỗi CSDL: " + e.getMessage());
        }
    }

    private String hashSHA1(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not supported");
        }
    }
}

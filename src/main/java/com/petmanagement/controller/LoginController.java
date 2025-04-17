package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Label togglePasswordIcon;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> visiblePasswordField.setText(newVal));
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> passwordField.setText(newVal));

        togglePasswordIcon.setOnMouseClicked(this::togglePasswordVisibility);
        forgotPasswordLink.setOnAction(e -> handleForgotPassword());
        loginButton.setOnAction(e -> handleLogin());
    }

    private void togglePasswordVisibility(MouseEvent event) {
        boolean isVisible = visiblePasswordField.isVisible();
        visiblePasswordField.setVisible(!isVisible);
        visiblePasswordField.setManaged(!isVisible);
        passwordField.setVisible(isVisible);
        passwordField.setManaged(isVisible);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        try (Connection conn = DBConnection.getConn()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                String enteredHashedPassword = hashPassword(password);

                if (storedHashedPassword.equals(enteredHashedPassword)) {
                    // Login thành công
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/main.fxml"));
                        Parent root = loader.load();
                        Stage stage = new Stage();
                        stage.setTitle("Main Page");
                        stage.setScene(new Scene(root));
                        stage.show();

                        Stage currentStage = (Stage) loginButton.getScene().getWindow();
                        currentStage.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorLabel.setText("Cannot open main page.");
                    }
                } else {
                    errorLabel.setText("Incorrect password.");
                }
            } else {
                errorLabel.setText("Username not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error during login.");
        }
    }

    private void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Reset your password");
        dialog.setContentText("Enter your email:");

        dialog.showAndWait().ifPresent(email -> {
            if (email.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Email cannot be empty.");
            } else {
                if (!emailExists(email)) {
                    showAlert(Alert.AlertType.ERROR, "Email not found in system.");
                    return;
                }

                Dialog<Pair<String, String>> passwordDialog = new Dialog<>();
                passwordDialog.setTitle("Set New Password");
                passwordDialog.setHeaderText("Enter new password");

                ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
                passwordDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

                PasswordField newPassword = new PasswordField();
                newPassword.setPromptText("New Password");
                PasswordField confirmPassword = new PasswordField();
                confirmPassword.setPromptText("Confirm Password");

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.add(new Label("New Password:"), 0, 0);
                grid.add(newPassword, 1, 0);
                grid.add(new Label("Confirm Password:"), 0, 1);
                grid.add(confirmPassword, 1, 1);

                passwordDialog.getDialogPane().setContent(grid);

                passwordDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == confirmButtonType) {
                        return new Pair<>(newPassword.getText(), confirmPassword.getText());
                    }
                    return null;
                });

                passwordDialog.showAndWait().ifPresent(result -> {
                    String pass1 = result.getKey();
                    String pass2 = result.getValue();
                    if (!pass1.equals(pass2)) {
                        showAlert(Alert.AlertType.ERROR, "Passwords do not match.");
                    } else {
                        updatePassword(email, pass1);
                        showAlert(Alert.AlertType.INFORMATION, "Password updated successfully.");
                    }
                });
            }
        });
    }

    private boolean emailExists(String email) {
        try (Connection conn = DBConnection.getConn()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updatePassword(String email, String newPassword) {
        try (Connection conn = DBConnection.getConn()) {
            String hashedPassword = hashPassword(newPassword);
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password = ? WHERE email = ?");
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

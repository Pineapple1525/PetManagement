package com.petmanagement.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ExitController {

    public static void showExitDialog(Stage currentStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Select 'Yes' to return to the login page or 'No' to cancel.");

        ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noBtn = new ButtonType("No", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(yesBtn, noBtn);

        alert.showAndWait().ifPresent(result -> {
            if (result == yesBtn) {
                try {
                    Parent root = FXMLLoader.load(ExitController.class.getResource("/com/petmanagement/pages/login.fxml"));
                    currentStage.setScene(new Scene(root));
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert error = new Alert(Alert.AlertType.ERROR, "Không thể tải trang đăng nhập.");
                    error.showAndWait();
                }
            }
        });
    }
}


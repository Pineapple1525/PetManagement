package com.consenyeunghe.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class LayoutController {

    @FXML
    private VBox sidebar;

    @FXML
    private BorderPane borderPane;
    private Button selectedButton = null;
    @FXML
    private ToggleButton toggleSidebarButton; // Nút ToggleButton để ẩn/hiện sidebar

    @FXML
    private ImageView arrowIcon;
    @FXML
    public void toggleSidebar() {
        if (toggleSidebarButton.isSelected()) {

            sidebar.setVisible(false);

            arrowIcon.setRotate(180);
        } else {
            sidebar.setVisible(true);
            arrowIcon.setRotate(0);
        }
    }
    public void initialize() {
        Font font = Font.loadFont(getClass().getResourceAsStream("/views/styles/font/PatrickHand-Regular.ttf"), 20);
        System.out.println("Loaded font: " + (font != null ? font.getName() : "Font not found"));

        Platform.runLater(() -> {
            try {
                Image normalPaw = new Image(getClass().getResource("/views/images/cat-paw.png").toExternalForm(), 32, 32, true, true);
                Image selectedPaw = new Image(getClass().getResource("/views/images/cat-paw-selected.png").toExternalForm(), 32, 32, true, true);
                Cursor pawCursor = new ImageCursor(normalPaw, 0, 0);

                List<Node> newNodes = new ArrayList<>();
                List<Node> originalChildren = new ArrayList<>(sidebar.getChildren());

                for (Node node : originalChildren) {
                    if (node instanceof Button button) {
                        if (!button.getStyleClass().contains("button")) {
                            button.getStyleClass().add("button");
                        }

                        ImageView paw = new ImageView(normalPaw);
                        paw.setFitWidth(36);
                        paw.setFitHeight(30);
                        paw.setSmooth(true);
                        paw.setVisible(false);

                        StackPane.setAlignment(paw, Pos.TOP_RIGHT);
                        StackPane stack = new StackPane(button, paw);
                        stack.setPadding(new Insets(5));
                        button.setCursor(pawCursor);

                        // Hover shake animation
                        TranslateTransition hoverShake = new TranslateTransition(Duration.millis(100), paw);
                        hoverShake.setFromX(0);
                        hoverShake.setByX(5);
                        hoverShake.setCycleCount(4);
                        hoverShake.setAutoReverse(true);

                        // Click zoom animation
                        ScaleTransition zoomIn = new ScaleTransition(Duration.millis(200), paw);
                        zoomIn.setFromX(1.0);
                        zoomIn.setFromY(1.0);
                        zoomIn.setToX(1.2);
                        zoomIn.setToY(1.2);
                        zoomIn.setCycleCount(1);
                        zoomIn.setAutoReverse(false);

                        // Breathing animation
                        Timeline breathing = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(paw.scaleXProperty(), 1.2), new KeyValue(paw.scaleYProperty(), 1.2)),
                                new KeyFrame(Duration.seconds(1.2), new KeyValue(paw.scaleXProperty(), 1.4), new KeyValue(paw.scaleYProperty(), 1.4)),
                                new KeyFrame(Duration.seconds(2.4), new KeyValue(paw.scaleXProperty(), 1.2), new KeyValue(paw.scaleYProperty(), 1.2))
                        );
                        breathing.setCycleCount(Animation.INDEFINITE);
                        breathing.setAutoReverse(true);

                        button.setOnMouseEntered(e -> {
                            if (button != selectedButton) {
                                paw.setVisible(true);
                                if (!button.getStyleClass().contains("button-hovered")) {
                                    button.getStyleClass().add("button-hovered");
                                }

                                hoverShake.playFromStart();

                                // Slide in
                                TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), stack);
                                slideIn.setFromY(5);
                                slideIn.setToY(0);
                                slideIn.setCycleCount(1);
                                slideIn.play();
                            }
                        });

                        button.setOnMouseExited(e -> {
                            if (button != selectedButton) {
                                paw.setVisible(false);
                                button.getStyleClass().remove("button-hovered");

                                // Slide out (optional)
                                TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), stack);
                                slideOut.setFromY(0);
                                slideOut.setToY(5);
                                slideOut.setCycleCount(1);
                                slideOut.play();
                            }
                        });

                        button.setOnAction(e -> {
                            if (selectedButton != null && selectedButton != button) {
                                selectedButton.getStyleClass().remove("button-selected");
                                selectedButton.getStyleClass().remove("button-hovered");

                                Node oldPawNode = ((StackPane) selectedButton.getParent()).getChildren().get(1);
                                if (oldPawNode instanceof ImageView oldPaw) {
                                    oldPaw.setVisible(false);
                                    oldPaw.setImage(normalPaw);
                                    oldPaw.setScaleX(1.0);
                                    oldPaw.setScaleY(1.0);
                                }
                            }

                            selectedButton = button;
                            button.getStyleClass().remove("button-hovered");

                            if (!button.getStyleClass().contains("button-selected")) {
                                button.getStyleClass().add("button-selected");
                            }

                            paw.setVisible(true);
                            paw.setImage(selectedPaw);
                            zoomIn.playFromStart();
                            breathing.playFromStart();
                        });

                        newNodes.add(stack);
                    } else {
                        newNodes.add(node);
                    }
                }

                sidebar.getChildren().setAll(newNodes);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }


    @FXML
    private void loadView(String file) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(file));
            Parent root = fxmlLoader.load();
            borderPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showUsers() {
        loadView("/views/pages/users.fxml");
    }

    @FXML
    public void showEmployees() {
        loadView("/views/pages/employees.fxml");
    }

    @FXML
    public void showPets() {
        loadView("/views/pages/pets.fxml");
    }

    @FXML
    public void showPetProducts() {
        loadView("/views/pages/pet-products.fxml");
    }

    @FXML
    public void showOrders() {
        loadView("/views/pages/orders.fxml");
    }

    @FXML
    public void showCustomers() {
        loadView("/views/pages/customers.fxml");
    }

    @FXML
    public void showPreviousOwners() {
        loadView("/views/pages/pre-owners.fxml");
    }
}

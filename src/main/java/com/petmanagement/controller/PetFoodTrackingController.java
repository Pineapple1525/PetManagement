package com.petmanagement.controller;



import com.petmanagement.model.PetFoodTracking;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.petmanagement.connection.DBConnection;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class PetFoodTrackingController implements Initializable {

    @FXML private TableView<PetFoodTracking> foodTrackingTable;
    @FXML private TableColumn<PetFoodTracking, String> foodNameColumn;
    @FXML private TableColumn<PetFoodTracking, String> foodTypeColumn;
    @FXML private TableColumn<PetFoodTracking, LocalDateTime> feedingDateColumn;
    @FXML private TableColumn<PetFoodTracking, String> portionSizeColumn;

    private String petCode;

    public void setPetCode(String petCode) {
        this.petCode = petCode;
        loadTrackingData();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        foodNameColumn.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        foodTypeColumn.setCellValueFactory(new PropertyValueFactory<>("foodType"));
        feedingDateColumn.setCellValueFactory(new PropertyValueFactory<>("feedingDate"));
        portionSizeColumn.setCellValueFactory(new PropertyValueFactory<>("portionSize"));

        // Click vào tên thức ăn để hiện dialog chi tiết
        foodNameColumn.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setOnMouseClicked(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: blue; -fx-underline: true;");
                        setOnMouseClicked(event -> {
                            PetFoodTracking tracking = getTableView().getItems().get(getIndex());
                            showFoodDetailDialog(tracking.getFoodName());
                        });
                    }
                }
            };
        });
    }

    private void loadTrackingData() {
        ObservableList<PetFoodTracking> trackingList = FXCollections.observableArrayList();
        String query = "SELECT * FROM pet_food_tracking WHERE pet_code = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, petCode);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PetFoodTracking item = new PetFoodTracking(
                        rs.getInt("food_id"),
                        rs.getString("pet_code"),
                        rs.getString("food_name"),
                        rs.getString("food_type"),
                        rs.getTimestamp("feeding_date").toLocalDateTime(),
                        rs.getString("portion_size")
                );
                trackingList.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        foodTrackingTable.setItems(trackingList);
    }

    private void showFoodDetailDialog(String foodName) {
        String query = "SELECT * FROM pet_food WHERE name = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, foodName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String brand = rs.getString("brand");
                String foodType = rs.getString("food_type");
                String species = rs.getString("species");
                String ingredients = rs.getString("ingredients");
                Date expiryDate = rs.getDate("expiry_date");
                double weight = rs.getDouble("weight");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock_quantity");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Chi tiết Thức ăn");
                alert.setHeaderText("Tên thức ăn: " + foodName);
                alert.setContentText(
                        "Thương hiệu: " + brand + "\n" +
                                "Loại: " + foodType + "\n" +
                                "Dành cho: " + species + "\n" +
                                "Thành phần: " + ingredients + "\n" +
                                "Hạn sử dụng: " + (expiryDate != null ? expiryDate.toString() : "Không rõ") + "\n" +
                                "Khối lượng: " + weight + "kg\n" +
                                "Giá: " + price + " VND\n" +
                                "Số lượng trong kho: " + stock
                );
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Không tìm thấy");
                alert.setHeaderText(null);
                alert.setContentText("Không tìm thấy thông tin chi tiết của thức ăn này.");
                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

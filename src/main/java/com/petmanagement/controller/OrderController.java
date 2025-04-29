package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class OrderController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> deliveryFilter;
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> colCode;
    @FXML private TableColumn<Order, String> colCustomer;
    @FXML private TableColumn<Order, LocalDateTime> colDate;
    @FXML private TableColumn<Order, BigDecimal> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colPayment;
    @FXML private TableColumn<Order, String> colDelivery;
    @FXML private TableColumn<Order, Integer> colID;

    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    public void initialize() {
        // Set up TableView columns
        colID.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getOrderId()).asObject());
        colCode.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCodeOrder()));
        colCustomer.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomerName()));
        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOrderDate()));
        colTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalPrice()));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        colPayment.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentMethod()));
        colDelivery.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDeliveryStatus()));

        // Populate ComboBoxes
        statusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Completed", "Cancelled"));
        deliveryFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Shipped", "Delivered", "Cancelled"));

        // Select "All" as default
        statusFilter.getSelectionModel().select("All");
        deliveryFilter.getSelectionModel().select("All");

        loadOrders();
    }

    private void loadOrders() {
        orderList.clear();
        String searchText = searchField.getText().trim();
        String status = statusFilter.getValue();
        String delivery = deliveryFilter.getValue();

        String query = "SELECT o.order_id, o.code_order, c.name AS customer_name, o.order_date, " +
                "o.total_price, o.status, o.payment_method, o.delivery_status, o.note " +
                "FROM orders o JOIN customers c ON o.code_customer = c.code_customer WHERE 1=1";

        if (!searchText.isEmpty()) {
            query += " AND (o.code_order LIKE ? OR c.name LIKE ?)";
        }
        if (!"All".equals(status)) {
            query += " AND o.status = ?";
        }
        if (!"All".equals(delivery)) {
            query += " AND o.delivery_status = ?";
        }

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (!searchText.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%");
                stmt.setString(paramIndex++, "%" + searchText + "%");
            }
            if (!"All".equals(status)) {
                stmt.setString(paramIndex++, status);
            }
            if (!"All".equals(delivery)) {
                stmt.setString(paramIndex++, delivery);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getString("code_order"),
                        rs.getString("customer_name"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        rs.getBigDecimal("total_price"),
                        rs.getString("status"),
                        rs.getString("payment_method"),
                        rs.getString("delivery_status"),
                        rs.getString("note")
                );
                orderList.add(order);
            }

            orderTable.setItems(orderList);

        } catch (SQLException e) {
            showAlert("Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
        }
    }

    @FXML
    private void onSearch() {
        loadOrders();
    }

    @FXML
    private void onCreateOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/create_order.fxml"));
            Parent root = loader.load();

            CreateOrderController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Tạo đơn hàng mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadOrders();
        } catch (IOException e) {
            showAlert("Lỗi khi mở form tạo đơn hàng: " + e.getMessage());
        }
    }

    @FXML
    private void onViewDetail() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/order_detail.fxml"));
                Parent root = loader.load();

                OrderDetailController controller = loader.getController();
                controller.setOrder(selected);

                Stage stage = new Stage();
                stage.setTitle("Chi tiết đơn hàng");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                showAlert("Lỗi khi mở chi tiết đơn hàng: " + e.getMessage());
            }
        } else {
            showAlert("Vui lòng chọn một đơn hàng để xem chi tiết.");
        }
    }

    @FXML
    private void onModifyOrder() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/edit_order.fxml"));
                Parent root = loader.load();

                EditOrderController controller = loader.getController();
                controller.setOrder(selected);

                Stage stage = new Stage();
                stage.setTitle("Chỉnh sửa đơn hàng");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                loadOrders();
            } catch (IOException e) {
                showAlert("Lỗi khi mở form chỉnh sửa đơn hàng: " + e.getMessage());
            }
        } else {
            showAlert("Vui lòng chọn một đơn hàng để chỉnh sửa.");
        }
    }

    @FXML
    private void onDeleteOrder() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa đơn này?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    deleteOrder(selected.getCodeOrder());
                }
            });
        } else {
            showAlert("Vui lòng chọn một đơn hàng để xóa.");
        }
    }

    private void deleteOrder(String codeOrder) {
        String query = "DELETE FROM orders WHERE code_order = ?";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codeOrder);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Xóa đơn hàng thành công!");
                loadOrders();
            } else {
                showAlert("Không tìm thấy đơn hàng để xóa.");
            }
        } catch (SQLException e) {
            showAlert("Lỗi khi xóa đơn hàng: " + e.getMessage());
        }
    }

    @FXML
    private void onPrintOrder(ActionEvent actionEvent) {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Create a simple print preview window
                Stage printStage = new Stage();
                printStage.setTitle("In đơn hàng - " + selected.getCodeOrder());
                printStage.initModality(Modality.APPLICATION_MODAL);

                TextArea printContent = new TextArea();
                printContent.setEditable(false);
                printContent.setText(generateOrderSummary(selected));

                Button printButton = new Button("In");
                printButton.setOnAction(e -> {
                    // Simulate printing (in a real application, integrate with a printer API)
                    showAlert("Đơn hàng đã được gửi đến máy in!");
                    printStage.close();
                });

                Button closeButton = new Button("Đóng");
                closeButton.setOnAction(e -> printStage.close());

                javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(10, printButton, closeButton);
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
                buttonBox.setPadding(new javafx.geometry.Insets(10));

                javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(10, printContent, buttonBox);
                layout.setPadding(new javafx.geometry.Insets(10));

                Scene scene = new Scene(layout, 400, 300);
                printStage.setScene(scene);
                printStage.showAndWait();
            } catch (Exception e) {
                showAlert("Lỗi khi mở cửa sổ in: " + e.getMessage());
            }
        } else {
            showAlert("Vui lòng chọn một đơn hàng để in.");
        }
    }

    private String generateOrderSummary(Order order) {
        StringBuilder summary = new StringBuilder();
        summary.append("ĐƠN HÀNG: ").append(order.getCodeOrder()).append("\n");
        summary.append("----------------------------------------\n");
        summary.append("Khách hàng: ").append(order.getCustomerName()).append("\n");
        summary.append("Ngày đặt: ").append(order.getOrderDate()).append("\n");
        summary.append("Tổng tiền: ").append(String.format("%,.0f đ", order.getTotalPrice())).append("\n");
        summary.append("Trạng thái: ").append(order.getStatus()).append("\n");
        summary.append("Phương thức thanh toán: ").append(order.getPaymentMethod()).append("\n");
        summary.append("Trạng thái giao hàng: ").append(order.getDeliveryStatus()).append("\n");
        summary.append("Ghi chú: ").append(order.getNote() != null ? order.getNote() : "").append("\n");
        summary.append("----------------------------------------\n");
        return summary.toString();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
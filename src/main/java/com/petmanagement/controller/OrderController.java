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
        colID.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getOrderId()).asObject());
        colCode.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCodeOrder()));
        colCustomer.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomerName()));
        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getOrderDate()));
        colTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalPrice()));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        colPayment.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentMethod()));
        colDelivery.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDeliveryStatus()));

        statusFilter.getSelectionModel().selectFirst();
        deliveryFilter.getSelectionModel().selectFirst();

        loadOrders();
    }

    private void loadOrders() {
        orderList.clear();
        String searchText = searchField.getText().trim();
        String status = statusFilter.getValue();
        String delivery = deliveryFilter.getValue();

        String query = "SELECT o.order_id, o.code_order, c.name AS customer_name, o.order_date, " +
                "o.total_price, o.status, o.payment_method, o.delivery_status " +
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
                        rs.getString("delivery_status")
                );

                orderList.add(order);
            }

            orderTable.setItems(orderList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        loadOrders(); // Gọi hàm loadOrders() để lọc theo điều kiện tìm kiếm (nếu có thêm input tìm kiếm thì đọc dữ liệu ở đây)
    }

    @FXML
    private void onCreateOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/create_order.fxml"));
            Parent root = loader.load();

            // Lấy controller sau khi load
            CreateOrderController controller = loader.getController();

            // (Tùy chọn) Nếu bạn muốn truyền dữ liệu ban đầu:
            // controller.initData(...);

            Stage stage = new Stage();
            stage.setTitle("Tạo đơn hàng mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Sau khi tạo xong đơn hàng, reload lại danh sách
            loadOrders();
        } catch (IOException e) {
            e.printStackTrace();
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
                controller.setOrder(selected); // Gửi đơn hàng được chọn sang controller

                Stage stage = new Stage();
                stage.setTitle("Chi tiết đơn hàng");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
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
                controller.setOrder(selected); // Truyền đơn hàng để chỉnh sửa

                Stage stage = new Stage();
                stage.setTitle("Chỉnh sửa đơn hàng");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                // Sau khi chỉnh sửa thì reload lại bảng
                loadOrders();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Vui lòng chọn một đơn hàng để chỉnh sửa.");
        }
    }

    // Hàm hiển thị cảnh báo đơn giản
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        }
    }

    private void deleteOrder(String codeOrder) {
        String query = "DELETE FROM orders WHERE code_order = ?";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codeOrder);
            stmt.executeUpdate();
            loadOrders();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onPrintOrder(ActionEvent actionEvent) {
    }
}

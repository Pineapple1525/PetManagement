package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    public void initialize() {
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

        String query = "SELECT o.code_order, c.name AS customer_name, o.order_date, o.total_price, o.status, o.payment_method, o.delivery_status " +
                "FROM orders o JOIN customers c ON o.code_customer = c.code_customer WHERE 1=1";

        if (!searchText.isEmpty()) {
            query += " AND (o.code_order LIKE ? OR c.name LIKE ?)";
        }
        if (!"Tất cả".equals(status)) {
            query += " AND o.status = ?";
        }
        if (!"Tất cả".equals(delivery)) {
            query += " AND o.delivery_status = ?";
        }

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (!searchText.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%");
                stmt.setString(paramIndex++, "%" + searchText + "%");
            }
            if (!"Tất cả".equals(status)) {
                stmt.setString(paramIndex++, status);
            }
            if (!"Tất cả".equals(delivery)) {
                stmt.setString(paramIndex++, delivery);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = new Order(
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
        loadOrders();
    }

    @FXML
    private void onCreateOrder() {
        // Mở form tạo đơn hàng mới (có thể dùng Dialog hoặc Scene mới)
    }

    @FXML
    private void onViewDetail() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Mở cửa sổ hiển thị chi tiết đơn hàng
        }
    }

    @FXML
    private void onUpdateStatus() {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Cho phép cập nhật trạng thái đơn hàng
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
}

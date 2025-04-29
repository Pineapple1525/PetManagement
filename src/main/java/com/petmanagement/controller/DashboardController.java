package com.petmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.cell.PropertyValueFactory;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class DashboardController {

    @FXML private Label labelCustomerCount;
    @FXML private Label labelOrderCount;
    @FXML private Label labelRevenue;
    @FXML private Label labelLowStock;
    @FXML private LineChart<String, Number> lineChartRevenue;
    @FXML private TableView<Order> tableRecentOrders;
    @FXML private PieChart pieChartTopProducts;

    @FXML private TableColumn<Order, String> colCodeOrder;
    @FXML private TableColumn<Order, String> colCustomerName;
    @FXML private TableColumn<Order, LocalDateTime> colOrderDate;
    @FXML private TableColumn<Order, BigDecimal> colTotalPrice;

    public void initialize() {
        loadCounts();
        loadRevenueChart();
        loadRecentOrders();
        loadTopSellingProducts();
    }

    private void loadCounts() {
        try (Connection conn = DBConnection.getConn()) {
            // Customer Count
            try (ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM customers")) {
                if (rs1.next()) labelCustomerCount.setText(rs1.getInt(1) + "");
            }

            // Order Count
            try (ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM orders")) {
                if (rs2.next()) labelOrderCount.setText(rs2.getInt(1) + "");
            }

            // Monthly Revenue
            try (ResultSet rs3 = conn.createStatement().executeQuery(
                    "SELECT SUM(total_price - COALESCE(discount, 0)) FROM orders WHERE MONTH(order_date) = MONTH(CURRENT_DATE())"
            )) {
                if (rs3.next()) labelRevenue.setText(String.format("%,.0f đ", rs3.getDouble(1)));
            }

            // Low Stock (include both pet_products and pet_food)
            try (ResultSet rs4 = conn.createStatement().executeQuery(
                    "SELECT (SELECT COUNT(*) FROM pet_products WHERE stock_quantity <= 5) + " +
                            "(SELECT COUNT(*) FROM pet_food WHERE stock_quantity <= 5) AS total_low_stock"
            )) {
                if (rs4.next()) labelLowStock.setText(rs4.getInt("total_low_stock") + "");
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading counts: " + e.getMessage());
        }
    }

    private void loadRevenueChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        try (Connection conn = DBConnection.getConn()) {
            String query = """
                    SELECT DATE_FORMAT(order_date, '%b') AS month, SUM(total_price - COALESCE(discount, 0)) AS revenue
                    FROM orders
                    WHERE order_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 6 MONTH)
                    GROUP BY MONTH(order_date), DATE_FORMAT(order_date, '%b')
                    ORDER BY MIN(order_date)
                    """;
            try (ResultSet rs = conn.createStatement().executeQuery(query)) {
                while (rs.next()) {
                    series.getData().add(new XYChart.Data<>(rs.getString("month"), rs.getDouble("revenue")));
                }
                lineChartRevenue.getData().clear();
                lineChartRevenue.getData().add(series);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading revenue chart: " + e.getMessage());
        }
    }

    private void loadRecentOrders() {
        colCodeOrder.setCellValueFactory(new PropertyValueFactory<>("codeOrder"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        try (Connection conn = DBConnection.getConn()) {
            String query = """
                    SELECT o.code_order, c.name, o.order_date, o.total_price
                    FROM orders o
                    JOIN customers c ON o.code_customer = c.code_customer
                    ORDER BY o.order_date DESC
                    LIMIT 5
                    """;
            try (ResultSet rs = conn.createStatement().executeQuery(query)) {
                tableRecentOrders.getItems().clear();
                while (rs.next()) {
                    Order order = new Order(
                            rs.getString("code_order"),
                            rs.getString("name"),
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            BigDecimal.valueOf(rs.getDouble("total_price")),
                            null, null, null, null  // Include note as null
                    );
                    tableRecentOrders.getItems().add(order);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading recent orders: " + e.getMessage());
        }
    }

    private void loadTopSellingProducts() {
        try (Connection conn = DBConnection.getConn()) {
            String query = """
                    SELECT product_type, product_name, SUM(quantity) AS total_sold
                    FROM order_details
                    WHERE product_type != 'Pet'
                    GROUP BY product_type, product_name
                    ORDER BY total_sold DESC
                    LIMIT 5
                    """;
            try (ResultSet rs = conn.createStatement().executeQuery(query)) {
                pieChartTopProducts.getData().clear();
                while (rs.next()) {
                    String label = rs.getString("product_type") + " - " + rs.getString("product_name");
                    PieChart.Data slice = new PieChart.Data(label, rs.getInt("total_sold"));
                    pieChartTopProducts.getData().add(slice);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading top selling products: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
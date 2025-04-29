package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.Order;
import com.petmanagement.model.OrderDetail;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailController {

    @FXML private Label lblOrderCode;
    @FXML private Label lblCustomer;
    @FXML private Label lblEmployee;
    @FXML private Label lblOrderDate;
    @FXML private Label lblStatus;
    @FXML private Label lblPaymentMethod;
    @FXML private TextArea txtNote;
    @FXML private Label lblTotalPrice;
    @FXML private TableView<OrderDetail> tblOrderDetails;
    @FXML private TableColumn<OrderDetail, String> colProductType;
    @FXML private TableColumn<OrderDetail, String> colProductName;
    @FXML private TableColumn<OrderDetail, BigDecimal> colSalePrice;
    @FXML private TableColumn<OrderDetail, Integer> colQuantity;
    @FXML private TableColumn<OrderDetail, BigDecimal> colWeight;

    private Order order;
    private ObservableList<OrderDetail> orderDetails = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colProductType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductType()));
        colProductName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        colSalePrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSalePrice()));
        colQuantity.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        colWeight.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getWeight()));

        System.out.println("TableView columns initialized: " + tblOrderDetails.getColumns().size());
    }

    public void setOrder(Order selected) {
        this.order = selected;
        populateOrderDetails();
    }

    private void populateOrderDetails() {
        lblOrderCode.setText(order.getCodeOrder());
        lblCustomer.setText(order.getCustomerName());
        lblEmployee.setText(fetchEmployeeName(order.getCodeOrder()));
        lblOrderDate.setText(order.getOrderDate().toString());
        lblStatus.setText(order.getStatus());
        lblPaymentMethod.setText(order.getPaymentMethod());
        lblTotalPrice.setText(order.getTotalPrice().toString());

        txtNote.setText(fetchOrderNote(order.getCodeOrder()));

        List<OrderDetail> details = fetchOrderDetails(order.getCodeOrder());
        System.out.println("Fetched order details: " + details);
        orderDetails.setAll(details);
        tblOrderDetails.setItems(orderDetails);

        tblOrderDetails.refresh();
        System.out.println("TableView items set: " + tblOrderDetails.getItems().size());
    }

    private String fetchEmployeeName(String orderCode) {
        String employeeName = null;
        String sql = "SELECT e.fullname " +
                "FROM orders o " +
                "JOIN employees e ON o.code_employee = e.code_employee " +
                "WHERE o.code_order = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                employeeName = rs.getString("fullname");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employeeName != null ? employeeName : "Unknown";
    }

    private String fetchOrderNote(String orderCode) {
        String note = "";
        String sql = "SELECT note FROM orders WHERE code_order = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                note = rs.getString("note");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return note != null ? note : "";
    }

    private List<OrderDetail> fetchOrderDetails(String orderCode) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT od.order_detail_id, od.order_id, od.product_type, od.code_product, od.code_pet, " +
                "od.sale_price, od.quantity, od.weight, " +
                "CASE " +
                "    WHEN od.product_type = 'Pet' THEN p.name " +
                "    WHEN od.product_type = 'Food' THEN f.name " +
                "    ELSE pp.name " +
                "END AS product_name " +
                "FROM order_details od " +
                "LEFT JOIN pets p ON od.code_pet = p.pet_code " +
                "LEFT JOIN pet_products pp ON od.code_product = pp.code_product " +
                "LEFT JOIN pet_food f ON od.code_product = CAST(f.food_id AS CHAR) " +
                "WHERE od.order_id = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("Fetching order details for orderCode: " + orderCode);
            stmt.setString(1, orderCode);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No order details found for orderCode: " + orderCode);
            }

            while (rs.next()) {
                System.out.println("Fetching order detail for order ID: " + orderCode);
                System.out.println("Product Type: " + rs.getString("product_type"));
                System.out.println("Code Pet: " + rs.getString("code_pet"));
                System.out.println("Code Product: " + rs.getString("code_product"));
                System.out.println("Product Name: " + rs.getString("product_name"));
                System.out.println("Sale Price: " + rs.getBigDecimal("sale_price"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Weight: " + rs.getBigDecimal("weight"));

                OrderDetail detail = new OrderDetail(
                        rs.getInt("order_detail_id"),
                        rs.getString("order_id"),
                        rs.getString("product_type"),
                        rs.getString("code_product"),
                        rs.getString("code_pet"),
                        rs.getString("product_name") != null ? rs.getString("product_name") : "Unknown",
                        rs.getBigDecimal("sale_price"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("weight") != null ? rs.getBigDecimal("weight").doubleValue() : 0.0 // Chuyển đổi BigDecimal sang double
                );
                details.add(detail);
            }
        } catch (Exception e) {
            System.out.println("Error fetching order details: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Number of order details fetched: " + details.size());
        return details;
    }

    @FXML
    public void onClose(ActionEvent actionEvent) {
        tblOrderDetails.getScene().getWindow().hide();
    }
}
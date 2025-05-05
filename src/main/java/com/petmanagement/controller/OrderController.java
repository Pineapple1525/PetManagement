package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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


        statusFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Completed", "Cancelled"));
        deliveryFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Shipped", "Delivered", "Cancelled"));


        statusFilter.getSelectionModel().select("All");
        deliveryFilter.getSelectionModel().select("All");

        loadOrders();

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadOrders());
        deliveryFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadOrders());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadOrders());
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
            query += " AND LOWER(o.status) = ?";
        }
        if (!"All".equals(delivery)) {
            query += " AND LOWER(o.delivery_status) = ?";
        }

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (!searchText.isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%");
                stmt.setString(paramIndex++, "%" + searchText + "%");
            }
            if (!"All".equals(status)) {
                stmt.setString(paramIndex++, status.toLowerCase());
            }
            if (!"All".equals(delivery)) {
                stmt.setString(paramIndex++, delivery.toLowerCase());
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
            showAlert("Error loading order list: " + e.getMessage());
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
            stage.setTitle("Create new order");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadOrders();
        } catch (IOException e) {
            showAlert("Error when opening order creation form: " + e.getMessage());
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
                stage.setTitle("Order details");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                showAlert("Error opening order details: " + e.getMessage());
            }
        } else {
            showAlert("Please select an order to view details.");
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
                stage.setTitle("Edit order");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                loadOrders();
            } catch (IOException e) {
                showAlert("Error when opening order editing form: " + e.getMessage());
            }
        } else {
            showAlert("Please select an order to edit.");
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
            showAlert("Please select an order to delete.");
        }
    }

    private void deleteOrder(String codeOrder) {
        String query = "DELETE FROM orders WHERE code_order = ?";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codeOrder);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Order deleted successfully!");
                loadOrders();
            } else {
                showAlert("No orders found to delete.");
            }
        } catch (SQLException e) {
            showAlert("Error when deleting order: " + e.getMessage());
        }
    }

    @FXML
    private void onPrintOrder(ActionEvent actionEvent) {
        Order selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                Stage printStage = new Stage();
                printStage.setTitle("Print order - " + selected.getCodeOrder());
                printStage.initModality(Modality.APPLICATION_MODAL);

                TextArea printContent = new TextArea();
                printContent.setEditable(false);
                printContent.setText(generateOrderSummary(selected));

                Button printButton = new Button("Print");
                printButton.setOnAction(e -> {
                    exportOrderToPDF(selected);
                    printStage.close();
                });

                Button closeButton = new Button("Close");
                closeButton.setOnAction(e -> printStage.close());

                HBox buttonBox = new HBox(10, printButton, closeButton);
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.setPadding(new Insets(10));

                VBox layout = new VBox(10, printContent, buttonBox);
                layout.setPadding(new Insets(10));

                Scene scene = new Scene(layout, 400, 300);
                printStage.setScene(scene);
                printStage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error opening print window: " + e.getMessage());
            }
        } else {
            showAlert("Please select an order to print.");
        }
    }

    private String generateOrderSummary(Order order) {
        StringBuilder summary = new StringBuilder();
        summary.append("ORDER: ").append(order.getCodeOrder()).append("\n");
        summary.append("----------------------------------------\n");
        summary.append("Customer: ").append(order.getCustomerName()).append("\n");
        summary.append("Order date: ").append(order.getOrderDate()).append("\n");
        summary.append("Total price: ").append(String.format("%,.2f price", order.getTotalPrice().setScale(0, BigDecimal.ROUND_HALF_UP))).append("\n");
        summary.append("Status: ").append(order.getStatus()).append("\n");
        summary.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        summary.append("Delivery Status: ").append(order.getDeliveryStatus()).append("\n");
        summary.append("Note: ").append(order.getNote() != null ? order.getNote() : "").append("\n");
        summary.append("----------------------------------------\n");

        summary.append("Products purchased:\n");
        summary.append(String.format("%-25s %-10s %-15s\n", "Product Name", "Quantity", " Price"));
        try (Connection conn = DBConnection.getConn()) {
            String query = "SELECT od.product_type, od.product_name, od.sale_price, od.quantity, " +
                    "od.code_product, od.code_pet " +
                    "FROM order_details od " +
                    "WHERE od.order_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, order.getCodeOrder());
                ResultSet rs = stmt.executeQuery();
                boolean hasProducts = false;
                while (rs.next()) {
                    hasProducts = true;
                    String productName = rs.getString("product_name");
                    int quantity = rs.getInt("quantity");
                    BigDecimal salePrice = rs.getBigDecimal("sale_price");
                    if (productName == null) {
                        productName = "Unknown Product (Type: " + rs.getString("product_type") + ")";
                    }
                    // Fix sale price formatting to display correctly with thousands separator
                    summary.append(String.format("%-25s %-10d %,15.2f đ\n", productName, quantity, salePrice.setScale(0, BigDecimal.ROUND_HALF_UP)));
                }
                if (!hasProducts) {
                    summary.append("There are no products in this order.\n");
                }
            }
        } catch (SQLException e) {
            summary.append("Error getting product details: ").append(e.getMessage()).append("\n");
        }

        return summary.toString();
    }

    private void exportOrderToPDF(Order order) {
        String outputPath = "Invoice_" + order.getCodeOrder() + ".pdf";

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new java.io.FileOutputStream(outputPath));
            document.open();


            InputStream fontStream = getClass().getResourceAsStream("/com/petmanagement/styles/font/Roboto-VariableFont_wdth,wght.ttf");
            BaseFont baseFont = BaseFont.createFont("DellaRespira-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true,
                    fontStream.readAllBytes(), null);

            Font font = new Font(baseFont, 12);
            Font boldFont = new Font(baseFont, 12, Font.BOLD);

            // Add title
            document.add(new Paragraph("Invoice - " + order.getCodeOrder(), boldFont));
            document.add(new Paragraph(" ", font));


            document.add(new Paragraph("Customer: " + order.getCustomerName(), font));
            document.add(new Paragraph("Order date: " + order.getOrderDate(), font));

            document.add(new Paragraph("Total price: " + String.format("%,.2f cost", order.getTotalPrice().setScale(0, BigDecimal.ROUND_HALF_UP)), font));
            document.add(new Paragraph("Status: " + order.getStatus(), font));
            document.add(new Paragraph("Payment method: " + order.getPaymentMethod(), font));
            document.add(new Paragraph("Delivery status: " + order.getDeliveryStatus(), font));
            document.add(new Paragraph("Note: " + (order.getNote() != null ? order.getNote() : ""), font));
            document.add(new Paragraph("----------------------------------------", font));


            PdfPTable table = new PdfPTable(3);
            table.setWidths(new float[]{4, 1, 2});
            table.addCell(new com.itextpdf.text.Phrase("Product Name", boldFont));
            table.addCell(new com.itextpdf.text.Phrase("Quantity", boldFont));
            table.addCell(new com.itextpdf.text.Phrase("Unit price", boldFont));
            boolean hasProducts = false;
            try (Connection conn = DBConnection.getConn()) {
                String query = "SELECT od.product_type, od.product_name, od.sale_price, od.quantity, " +
                        "od.code_product, od.code_pet " +
                        "FROM order_details od " +
                        "WHERE od.order_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, order.getCodeOrder());
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        hasProducts = true;
                        String productName = rs.getString("product_name");
                        if (productName == null) {
                            productName = "Unknown Product (Type: " + rs.getString("product_type") + ")";
                        }
                        table.addCell(new com.itextpdf.text.Phrase(productName, font));
                        table.addCell(new com.itextpdf.text.Phrase(String.valueOf(rs.getInt("quantity")), font));

                        table.addCell(new com.itextpdf.text.Phrase(String.format("%,.2f đ", rs.getBigDecimal("sale_price").setScale(0, BigDecimal.ROUND_HALF_UP)), font));
                    }
                }
            } catch (SQLException e) {

                document.add(new Paragraph("Error getting product details:" + e.getMessage(), font));
            }

            if (!hasProducts) {
                document.add(new Paragraph("There are no products in this order.", font));
            } else {
                document.add(table);
            }

            document.close();
            showAlert("PDF invoice exported at: " + outputPath);
        } catch (DocumentException | IOException e) {
            showAlert("Error exporting PDF: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.Order;
import com.petmanagement.model.OrderDetail;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditOrderController {

    @FXML private Label lblOrderId;
    @FXML private Label lblCustomer;  // Replaced ComboBox with Label
    @FXML private ComboBox<String> cbEmployee;
    @FXML private DatePicker dpOrderDate;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<String> cbPaymentMethod;
    @FXML private TextArea txtNote;
    @FXML private TextField searchProduct;
    @FXML private TableView<OrderDetail> tblOrderItems;
    @FXML private TableColumn<OrderDetail, String> colProductType;
    @FXML private TableColumn<OrderDetail, String> colProductName;
    @FXML private TableColumn<OrderDetail, BigDecimal> colSalePrice;
    @FXML private TableColumn<OrderDetail, Integer> colQuantity;
    @FXML private TableColumn<OrderDetail, BigDecimal> colWeight;
    @FXML private TableColumn<OrderDetail, String> colAction;
    @FXML private Label lblTotal;

    private Order order;
    private Connection connection;
    private String employeeCode;
    private ObservableList<OrderDetail> orderDetails = FXCollections.observableArrayList();
    private ListView<OrderDetail> suggestionList;
    private VBox searchContainer;

    @FXML
    public void initialize() {
        // Initialize database connection
        try {
            connection = DBConnection.getConn();
            if (connection == null) {
                showAlert("Error", "Failed to connect to the database.");
                return;
            }
        } catch (Exception e) {
            showAlert("Error", "Database connection error: " + e.getMessage());
            return;
        }

        // Populate ComboBoxes (only for employee)
        populateComboBoxes();

        // Initialize status and payment method options
        cbStatus.getItems().addAll("Pending", "Completed", "Cancelled");
        cbPaymentMethod.getItems().addAll("Cash", "Credit Card", "Bank Transfer", "E-Wallet");

        // Initialize table columns
        colProductType.setCellValueFactory(new PropertyValueFactory<>("productType"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colSalePrice.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colAction.setCellValueFactory(param -> new SimpleStringProperty("Actions"));
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            private final Button editBtn = new Button("Edit");
            private final HBox actionBox = new HBox(5, removeBtn, editBtn);

            {
                removeBtn.setOnAction(event -> {
                    OrderDetail item = getTableView().getItems().get(getIndex());
                    orderDetails.remove(item);
                    updateTotal();
                });

                editBtn.setOnAction(event -> {
                    OrderDetail item = getTableView().getItems().get(getIndex());
                    editItemQuantity(item);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        tblOrderItems.setItems(orderDetails);

        // Initialize suggestion ListView
        suggestionList = new ListView<>();
        suggestionList.setPrefHeight(150);
        suggestionList.setVisible(false);
        suggestionList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(OrderDetail item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String code = item.getCodePet() != null ? item.getCodePet() : item.getCodeProduct();
                    String displayText = String.format("%s - %s (Code: %s)", item.getProductType(), item.getProductName(), code);
                    if (item.getProductType().equals("Pet")) {
                        try {
                            String query = "SELECT species FROM pets WHERE pet_code = ?";
                            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                                pstmt.setString(1, item.getCodePet());
                                try (ResultSet rs = pstmt.executeQuery()) {
                                    if (rs.next()) {
                                        String species = rs.getString("species");
                                        if (species != null && !species.isEmpty()) {
                                            displayText = String.format("%s - %s (Code: %s, Species: %s)", item.getProductType(), item.getProductName(), code, species);
                                        }
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            // Ignore errors in fetching species for display
                        }
                    }
                    setText(displayText);
                }
            }
        });

        // Handle selection from suggestion list
        suggestionList.setOnMouseClicked(event -> selectSuggestion());
        suggestionList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                selectSuggestion();
            }
        });

        // Add suggestion list to the layout
        searchContainer = (VBox) searchProduct.getParent().getParent();
        searchContainer.getChildren().add(suggestionList);

        // Add text change listener for search
        searchProduct.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                suggestionList.setVisible(false);
                suggestionList.getItems().clear();
            } else {
                showSuggestions(newValue.trim());
            }
        });

        // Hide suggestions when search field loses focus
        searchProduct.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !suggestionList.isFocused()) {
                suggestionList.setVisible(false);
            }
        });

        // Keep suggestions visible if list is focused
        suggestionList.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                suggestionList.setVisible(true);
            }
        });
    }

    public void setOrder(Order selected) {
        this.order = selected;
        fetchEmployeeCode();
        loadOrderDetails();
        populateOrderDetails();
    }

    private void fetchEmployeeCode() {
        String sql = "SELECT code_employee FROM orders WHERE code_order = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, order.getCodeOrder());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                employeeCode = rs.getString("code_employee");
            } else {
                employeeCode = null;
                showAlert("Error", "Order not found in database: " + order.getCodeOrder());
            }
        } catch (SQLException e) {
            showAlert("Error", "Error fetching employee code: " + e.getMessage());
            employeeCode = null;
        }
    }

    private void loadOrderDetails() {
        orderDetails.clear();
        String sql = "SELECT * FROM order_details WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, order.getCodeOrder());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderDetail detail = new OrderDetail(
                        rs.getInt("order_detail_id"),
                        rs.getString("order_id"),
                        rs.getString("product_type"),
                        rs.getString("code_product"),
                        rs.getString("code_pet"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("sale_price"),
                        rs.getInt("quantity"),
                        rs.getDouble("weight")
                );
                orderDetails.add(detail);
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading order details: " + e.getMessage());
        }
        updateTotal();
    }

    private void populateOrderDetails() {
        lblOrderId.setText(order.getCodeOrder());

        // Set customer (non-editable)
        String customerCode = order.getCustomerName();
        String customerDisplay = fetchCustomerDisplayName(customerCode);
        if (customerDisplay == null) {
            lblCustomer.setText(customerCode + " (Deleted)");
        } else {
            lblCustomer.setText(customerDisplay);
        }

        // Set employee
        if (employeeCode != null) {
            String employeeDisplay = fetchEmployeeDisplayName(employeeCode);
            if (employeeDisplay == null) {
                showAlert("Warning", "Employee with code " + employeeCode + " no longer exists. Please select a new employee.");
                cbEmployee.setValue(null);
            } else {
                cbEmployee.setValue(employeeDisplay);
            }
        } else {
            cbEmployee.setValue(null);
        }

        // Set order date
        LocalDateTime orderDateTime = order.getOrderDate();
        if (orderDateTime != null) {
            dpOrderDate.setValue(orderDateTime.toLocalDate());
        }

        // Set status and payment method
        cbStatus.setValue(order.getStatus());
        cbPaymentMethod.setValue(order.getPaymentMethod());

        // Set note
        txtNote.setText(order.getNote());
    }

    private void populateComboBoxes() {
        // Only populate employees (customer ComboBox removed)
        try (PreparedStatement stmt = connection.prepareStatement("SELECT code_employee, fullname FROM employees")) {
            ResultSet rs = stmt.executeQuery();
            List<String> employees = new ArrayList<>();
            while (rs.next()) {
                employees.add(rs.getString("fullname") + " (" + rs.getString("code_employee") + ")");
            }
            cbEmployee.setItems(FXCollections.observableArrayList(employees));
        } catch (SQLException e) {
            showAlert("Error", "Error loading employees: " + e.getMessage());
        }
    }

    private String fetchCustomerDisplayName(String customerCode) {
        String query = "SELECT name FROM customers WHERE code_customer = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, customerCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name") + " (" + customerCode + ")";
            }
        } catch (SQLException e) {
            showAlert("Error", "Error fetching customer name: " + e.getMessage());
        }
        return null;
    }

    private String fetchEmployeeDisplayName(String employeeCode) {
        String query = "SELECT fullname FROM employees WHERE code_employee = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, employeeCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("fullname") + " (" + employeeCode + ")";
            }
        } catch (SQLException e) {
            showAlert("Error", "Error fetching employee name: " + e.getMessage());
        }
        return null;
    }

    private void showSuggestions(String searchTerm) {
        try {
            String query = "SELECT 'Pet' as product_type, pet_code as code, name, price as sale_price, adoption_status, NULL as stock_quantity, weight, species " +
                    "FROM pets WHERE name LIKE ? OR pet_code LIKE ? OR species LIKE ? OR 'Pet' LIKE ? " +
                    "UNION " +
                    "SELECT 'Accessory' as product_type, code_product as code, name, price as sale_price, NULL as adoption_status, stock_quantity, NULL as weight, NULL as species " +
                    "FROM pet_products WHERE name LIKE ? OR code_product LIKE ? OR 'Accessory' LIKE ? " +
                    "UNION " +
                    "SELECT 'Food' as product_type, CAST(food_id AS CHAR) as code, name, price as sale_price, NULL as adoption_status, stock_quantity, weight, NULL as species " +
                    "FROM pet_food WHERE name LIKE ? OR CAST(food_id AS CHAR) LIKE ? OR 'Food' LIKE ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, "%" + searchTerm + "%");
                pstmt.setString(2, "%" + searchTerm + "%");
                pstmt.setString(3, "%" + searchTerm + "%");
                pstmt.setString(4, "%" + searchTerm + "%");
                pstmt.setString(5, "%" + searchTerm + "%");
                pstmt.setString(6, "%" + searchTerm + "%");
                pstmt.setString(7, "%" + searchTerm + "%");
                pstmt.setString(8, "%" + searchTerm + "%");
                pstmt.setString(9, "%" + searchTerm + "%");
                pstmt.setString(10, "%" + searchTerm + "%");

                try (ResultSet rs = pstmt.executeQuery()) {
                    List<OrderDetail> results = new ArrayList<>();
                    while (rs.next()) {
                        String productType = rs.getString("product_type");
                        String code = rs.getString("code");
                        String adoptionStatus = rs.getString("adoption_status");
                        int stockQuantity = rs.getInt("stock_quantity");
                        double weight = rs.getDouble("weight");

                        int quantity = 1;
                        if (productType.equals("Pet") && (adoptionStatus == null || !adoptionStatus.equals("Available"))) {
                            continue;
                        } else if (!productType.equals("Pet") && stockQuantity < quantity) {
                            continue;
                        }

                        OrderDetail detail = new OrderDetail(
                                0,
                                order.getCodeOrder(),
                                productType,
                                productType.equals("Pet") ? null : code,
                                productType.equals("Pet") ? code : null,
                                rs.getString("name"),
                                new BigDecimal(rs.getDouble("sale_price")),
                                quantity,
                                weight
                        );
                        results.add(detail);
                    }
                    if (results.isEmpty()) {
                        suggestionList.setVisible(false);
                        suggestionList.getItems().clear();
                    } else {
                        suggestionList.setItems(FXCollections.observableArrayList(results));
                        suggestionList.setVisible(true);
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Error searching products: " + e.getMessage());
            suggestionList.setVisible(false);
        }
    }

    private void selectSuggestion() {
        OrderDetail selected = suggestionList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            OrderDetail existingItem = null;
            for (OrderDetail item : orderDetails) {
                if (item.getProductType().equals(selected.getProductType()) &&
                        ((item.getCodePet() != null && item.getCodePet().equals(selected.getCodePet())) ||
                                (item.getCodeProduct() != null && item.getCodeProduct().equals(selected.getCodeProduct())))) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null) {
                int newQuantity = existingItem.getQuantity() + 1;
                if (!existingItem.getProductType().equals("Pet")) {
                    String query = "SELECT stock_quantity FROM " +
                            (existingItem.getProductType().equals("Food") ? "pet_food WHERE food_id = ?" : "pet_products WHERE code_product = ?");
                    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                        pstmt.setString(1, existingItem.getCodeProduct());
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next() && rs.getInt("stock_quantity") < newQuantity) {
                                showAlert("Error", "Insufficient stock for this item.");
                                return;
                            }
                        }
                    } catch (SQLException e) {
                        showAlert("Error", "Error checking stock: " + e.getMessage());
                        return;
                    }
                }
                existingItem.setQuantity(newQuantity);
            } else {
                orderDetails.add(selected);
            }

            updateTotal();
            tblOrderItems.refresh();
            searchProduct.clear();
            suggestionList.setVisible(false);
        }
    }

    private void editItemQuantity(OrderDetail item) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getQuantity()));
        dialog.setTitle("Edit Quantity");
        dialog.setHeaderText("Enter new quantity for " + item.getProductName());
        dialog.setContentText("Quantity:");

        dialog.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                dialog.getEditor().setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newQuantity -> {
            try {
                int quantity = Integer.parseInt(newQuantity);
                if (quantity <= 0) {
                    showAlert("Error", "Quantity must be positive");
                    return;
                }

                if ("Pet".equals(item.getProductType())) {
                    if (quantity != 1) {
                        showAlert("Error", "Only one pet can be adopted at a time.");
                        return;
                    }
                } else {
                    String query = "SELECT stock_quantity FROM " +
                            (item.getProductType().equals("Food") ? "pet_food WHERE food_id = ?" : "pet_products WHERE code_product = ?");
                    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                        pstmt.setString(1, item.getCodeProduct());
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next() && rs.getInt("stock_quantity") < quantity) {
                                showAlert("Error", "Insufficient stock for this item.");
                                return;
                            }
                        }
                    } catch (SQLException e) {
                        showAlert("Error", "Error checking stock: " + e.getMessage());
                        return;
                    }
                }

                item.setQuantity(quantity);
                updateTotal();
                tblOrderItems.refresh();
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid quantity");
            }
        });
    }

    @FXML
    public void onSave(ActionEvent actionEvent) {
        if (!validateInput()) {
            return;
        }

        try {
            connection.setAutoCommit(false);

            // Validate employee code
            String employeeCode = extractCode(cbEmployee.getValue());
            String employeeCheckSql = "SELECT 1 FROM employees WHERE code_employee = ?";
            try (PreparedStatement stmt = connection.prepareStatement(employeeCheckSql)) {
                stmt.setString(1, employeeCode);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    showAlert("Error", "Invalid employee code: " + employeeCode + " does not exist.");
                    connection.rollback();
                    return;
                }
            }

            // Validate order details (ensure referenced pets/products/food exist)
            for (OrderDetail detail : orderDetails) {
                if ("Pet".equals(detail.getProductType())) {
                    String petCheckSql = "SELECT 1 FROM pets WHERE pet_code = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(petCheckSql)) {
                        stmt.setString(1, detail.getCodePet());
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.next()) {
                            showAlert("Error", "Pet with code " + detail.getCodePet() + " does not exist.");
                            connection.rollback();
                            return;
                        }
                    }
                } else if ("Food".equals(detail.getProductType())) {
                    String foodCheckSql = "SELECT 1 FROM pet_food WHERE food_id = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(foodCheckSql)) {
                        stmt.setString(1, detail.getCodeProduct());
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.next()) {
                            showAlert("Error", "Food with ID " + detail.getCodeProduct() + " does not exist.");
                            connection.rollback();
                            return;
                        }
                    }
                } else if ("Accessory".equals(detail.getProductType())) {
                    String accessoryCheckSql = "SELECT 1 FROM pet_products WHERE code_product = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(accessoryCheckSql)) {
                        stmt.setString(1, detail.getCodeProduct());
                        ResultSet rs = stmt.executeQuery();
                        if (!rs.next()) {
                            showAlert("Error", "Accessory with code " + detail.getCodeProduct() + " does not exist.");
                            connection.rollback();
                            return;
                        }
                    }
                }
            }

            // Update order (exclude code_customer)
            String orderSql = "UPDATE orders SET code_employee = ?, order_date = ?, " +
                    "total_price = ?, status = ?, payment_method = ?, note = ? WHERE code_order = ?";
            BigDecimal totalPrice = calculateTotal();
            try (PreparedStatement stmt = connection.prepareStatement(orderSql)) {
                stmt.setString(1, employeeCode);
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(dpOrderDate.getValue().atStartOfDay()));
                stmt.setBigDecimal(3, totalPrice);
                stmt.setString(4, cbStatus.getValue());
                stmt.setString(5, cbPaymentMethod.getValue());
                stmt.setString(6, txtNote.getText());
                stmt.setString(7, order.getCodeOrder());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    showAlert("Error", "No order found with code: " + order.getCodeOrder());
                    connection.rollback();
                    return;
                }
            }

            // Delete existing order details
            String deleteSql = "DELETE FROM order_details WHERE order_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
                stmt.setString(1, order.getCodeOrder());
                stmt.executeUpdate();
            }

            // Insert updated order details
            String detailSql = "INSERT INTO order_details (order_id, product_type, code_product, code_pet, " +
                    "product_name, sale_price, quantity, weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement detailStmt = connection.prepareStatement(detailSql)) {
                for (OrderDetail detail : orderDetails) {
                    detailStmt.setString(1, order.getCodeOrder());
                    detailStmt.setString(2, detail.getProductType());
                    detailStmt.setString(3, detail.getCodeProduct());
                    detailStmt.setString(4, detail.getCodePet());
                    detailStmt.setString(5, detail.getProductName());
                    detailStmt.setBigDecimal(6, detail.getSalePrice());
                    detailStmt.setInt(7, detail.getQuantity());
                    detailStmt.setBigDecimal(8, detail.getWeight());
                    detailStmt.executeUpdate();
                }
            }

            // Update stock and adoption status
            for (OrderDetail detail : orderDetails) {
                if ("Pet".equals(detail.getProductType())) {
                    String updateSql = "UPDATE pets SET adoption_status = 'Adopted' WHERE pet_code = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setString(1, detail.getCodePet());
                        updateStmt.executeUpdate();
                    }
                } else if ("Food".equals(detail.getProductType())) {
                    String updateSql = "UPDATE pet_food SET stock_quantity = stock_quantity - ? WHERE food_id = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, detail.getQuantity());
                        updateStmt.setString(2, detail.getCodeProduct());
                        updateStmt.executeUpdate();
                    }
                } else if ("Accessory".equals(detail.getProductType())) {
                    String updateSql = "UPDATE pet_products SET stock_quantity = stock_quantity - ? WHERE code_product = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, detail.getQuantity());
                        updateStmt.setString(2, detail.getCodeProduct());
                        updateStmt.executeUpdate();
                    }
                }
            }

            connection.commit();
            showAlert("Success", "Order updated successfully");
            closeWindow();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                showAlert("Error", "Rollback error: " + ex.getMessage());
            }
            showAlert("Error", "Error updating order: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                showAlert("Error", "Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onCancel(ActionEvent actionEvent) {
        closeWindow();
    }

    private boolean validateInput() {
        if (cbEmployee.getValue() == null) {
            showAlert("Error", "Employee is required");
            return false;
        }
        if (dpOrderDate.getValue() == null) {
            showAlert("Error", "Order date is required");
            return false;
        }
        if (cbStatus.getValue() == null) {
            showAlert("Error", "Status is required");
            return false;
        }
        if (cbPaymentMethod.getValue() == null) {
            showAlert("Error", "Payment method is required");
            return false;
        }
        if (orderDetails.isEmpty()) {
            showAlert("Error", "At least one item is required");
            return false;
        }
        return true;
    }

    private void updateTotal() {
        BigDecimal total = calculateTotal();
        lblTotal.setText(total.toString());
        order.setTotalPrice(total);
    }

    private BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail detail : orderDetails) {
            total = total.add(detail.calculateTotalPrice());
        }
        return total.max(BigDecimal.ZERO);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String extractCode(String displayText) {
        if (displayText == null || displayText.isEmpty()) {
            return null;
        }
        int startIndex = displayText.lastIndexOf('(');
        int endIndex = displayText.lastIndexOf(')');
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return displayText.substring(startIndex + 1, endIndex);
        }
        return displayText;
    }

    private void closeWindow() {
        lblOrderId.getScene().getWindow().hide();
    }

    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                showAlert("Error", "Error closing database connection: " + e.getMessage());
            }
        }
    }
}
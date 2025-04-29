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
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateOrderController {

    @FXML private TextField txtOrderCode;
    @FXML private ComboBox<String> cbCustomer;
    @FXML private ComboBox<String> cbEmployee;
    @FXML private ComboBox<String> cbPaymentMethod;
    @FXML private TextField txtDiscount;
    @FXML private ComboBox<String> cbOrderStatus;
    @FXML private ComboBox<String> cbDeliveryStatus;
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
    @FXML private Button btnSave;

    private ObservableList<OrderDetail> orderDetails = FXCollections.observableArrayList();
    private Connection connection;
    private ListView<OrderDetail> suggestionList;
    private VBox searchContainer;

    public void initialize() {
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

        txtOrderCode.setText(generateOrderCode());

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

        populateComboBoxes();
        cbPaymentMethod.getItems().addAll("Cash", "Credit Card", "Bank Transfer", "E-Wallet");
        cbOrderStatus.getItems().addAll("Pending", "Completed", "Cancelled");
        cbOrderStatus.setValue("Pending");
        cbDeliveryStatus.getItems().addAll("Pending", "Shipped", "Delivered", "Cancelled");
        cbDeliveryStatus.setValue("Pending");

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
                        String species = (String) item.getAdditionalData("species");
                        String breed = (String) item.getAdditionalData("breed");
                        if (species != null && !species.isEmpty() && breed != null && !breed.isEmpty()) {
                            displayText = String.format("Pet: %s (Species: %s, Breed: %s, Code: %s)",
                                    item.getProductName(), species, breed, code);
                        } else if (species != null && !species.isEmpty()) {
                            displayText = String.format("Pet: %s (Species: %s, Code: %s)",
                                    item.getProductName(), species, code);
                        }
                    }
                    setText(displayText);
                }
            }
        });

        suggestionList.setOnMouseClicked(event -> selectSuggestion());
        suggestionList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                selectSuggestion();
            }
        });

        searchContainer = (VBox) searchProduct.getParent().getParent();
        searchContainer.getChildren().add(suggestionList);

        searchProduct.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                suggestionList.setVisible(false);
                suggestionList.getItems().clear();
            } else {
                showSuggestions(newValue.trim());
            }
        });

        searchProduct.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !suggestionList.isFocused()) {
                suggestionList.setVisible(false);
            }
        });

        suggestionList.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                suggestionList.setVisible(true);
            }
        });

        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtDiscount.setText(oldVal);
            }
        });
    }

    private void populateComboBoxes() {
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT code_customer, name FROM customers")) {
                List<String> customers = new ArrayList<>();
                while (rs.next()) {
                    customers.add(rs.getString("name") + " (" + rs.getString("code_customer") + ")");
                }
                cbCustomer.setItems(FXCollections.observableArrayList(customers));
            }

            try (ResultSet rs = stmt.executeQuery("SELECT code_employee, fullname FROM employees")) {
                List<String> employees = new ArrayList<>();
                while (rs.next()) {
                    employees.add(rs.getString("fullname") + " (" + rs.getString("code_employee") + ")");
                }
                cbEmployee.setItems(FXCollections.observableArrayList(employees));
            }
        } catch (SQLException e) {
            showAlert("Error", "Error loading data: " + e.getMessage());
        }
    }

    private void showSuggestions(String searchTerm) {
        try {
            String query = "SELECT 'Pet' as product_type, pet_code as code, name, price as sale_price, adoption_status, NULL as stock_quantity, weight, species, breed " +
                    "FROM pets WHERE LOWER(name) LIKE LOWER(?) OR LOWER(pet_code) LIKE LOWER(?) OR LOWER(species) LIKE LOWER(?) OR LOWER(breed) LIKE LOWER(?) " +
                    "UNION " +
                    "SELECT 'Accessory' as product_type, code_product as code, name, price as sale_price, NULL as adoption_status, stock_quantity, NULL as weight, NULL as species, NULL as breed " +
                    "FROM pet_products WHERE LOWER(name) LIKE LOWER(?) OR LOWER(code_product) LIKE LOWER(?) " +
                    "UNION " +
                    "SELECT 'Food' as product_type, CAST(food_id AS CHAR) as code, name, price as sale_price, NULL as adoption_status, stock_quantity, weight, NULL as species, NULL as breed " +
                    "FROM pet_food WHERE LOWER(name) LIKE LOWER(?) OR LOWER(CAST(food_id AS CHAR)) LIKE LOWER(?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, "%" + searchTerm.toLowerCase() + "%");
                pstmt.setString(2, "%" + searchTerm.toLowerCase() + "%");
                pstmt.setString(3, "%" + searchTerm.toLowerCase() + "%");
                pstmt.setString(4, "%" + searchTerm.toLowerCase() + "%");
                pstmt.setString(5, "%" + searchTerm.toLowerCase() + "%");
                pstmt.setString(6, "%" + searchTerm.toLowerCase() + "%");
                pstmt.setString(7, "%" + searchTerm.toLowerCase() + "%");
                pstmt.setString(8, "%" + searchTerm.toLowerCase() + "%");

                try (ResultSet rs = pstmt.executeQuery()) {
                    List<OrderDetail> results = new ArrayList<>();
                    while (rs.next()) {
                        String productType = rs.getString("product_type");
                        String code = rs.getString("code");
                        String adoptionStatus = rs.getString("adoption_status");
                        int stockQuantity = rs.getInt("stock_quantity");
                        double weight = rs.getDouble("weight");

                        if (productType.equals("Pet")) {
                            String species = rs.getString("species");
                            String breed = rs.getString("breed");
                            System.out.println("Pet found: " + rs.getString("name") + ", Code: " + code +
                                    ", Adoption Status: " + adoptionStatus + ", Species: " + species + ", Breed: " + breed);
                        }

                        int quantity = 1;
                        if (productType.equals("Pet") && (adoptionStatus == null || !adoptionStatus.equalsIgnoreCase("Available"))) {
                            System.out.println("Skipping pet due to adoption status: " + adoptionStatus);
                            continue;
                        } else if (!productType.equals("Pet") && stockQuantity < quantity) {
                            System.out.println("Skipping " + productType + " due to insufficient stock: " + stockQuantity);
                            continue;
                        }

                        OrderDetail detail = new OrderDetail(
                                0,
                                txtOrderCode.getText(),
                                productType,
                                productType.equals("Pet") ? null : code,
                                productType.equals("Pet") ? code : null,
                                rs.getString("name"),
                                new BigDecimal(rs.getDouble("sale_price")),
                                quantity,
                                weight
                        );

                        if (productType.equals("Pet")) {
                            String species = rs.getString("species");
                            String breed = rs.getString("breed");
                            detail.addAdditionalData("species", species);
                            detail.addAdditionalData("breed", breed);
                        }

                        results.add(detail);
                    }
                    if (results.isEmpty()) {
                        System.out.println("No results found for search term: " + searchTerm);
                        suggestionList.setVisible(false);
                        suggestionList.getItems().clear();
                    } else {
                        System.out.println("Found " + results.size() + " results for search term: " + searchTerm);
                        suggestionList.setItems(FXCollections.observableArrayList(results));
                        suggestionList.setVisible(true);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                } else if (newQuantity > 1) {
                    showAlert("Error", "Only one pet can be adopted at a time.");
                    return;
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
    private void onSearchProduct(ActionEvent event) {
        String searchTerm = searchProduct.getText().trim();
        if (!searchTerm.isEmpty()) {
            showSuggestions(searchTerm);
        } else {
            suggestionList.setVisible(false);
            suggestionList.getItems().clear();
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            connection.setAutoCommit(false);

            BigDecimal discount = txtDiscount.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtDiscount.getText());
            BigDecimal totalPrice = calculateTotal().subtract(discount);
            Order order = new Order(
                    txtOrderCode.getText(),
                    extractCode(cbCustomer.getValue()),
                    LocalDateTime.now(),
                    totalPrice,
                    cbOrderStatus.getValue(),
                    cbPaymentMethod.getValue(),
                    cbDeliveryStatus.getValue(),
                    txtNote.getText()
            );

            try (PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM orders WHERE code_order = ?")) {
                checkStmt.setString(1, order.getCodeOrder());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert("Error", "Order code already exists");
                        connection.rollback();
                        return;
                    }
                }
            }

            String orderSql = "INSERT INTO orders (code_order, code_customer, code_employee, total_price, discount, " +
                    "status, payment_method, delivery_status, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement orderStmt = connection.prepareStatement(orderSql)) {
                orderStmt.setString(1, order.getCodeOrder());
                orderStmt.setString(2, order.getCustomerName());
                orderStmt.setString(3, extractCode(cbEmployee.getValue()));
                orderStmt.setBigDecimal(4, order.getTotalPrice());
                orderStmt.setBigDecimal(5, discount);
                orderStmt.setString(6, order.getStatus());
                orderStmt.setString(7, order.getPaymentMethod());
                orderStmt.setString(8, order.getDeliveryStatus());
                orderStmt.setString(9, order.getNote());
                orderStmt.executeUpdate();
            }

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
            showAlert("Success", "Order created successfully");
            clearForm();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                showAlert("Error", "Rollback error: " + ex.getMessage());
            }
            showAlert("Error", "Error saving order: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                showAlert("Error", "Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onCancel(ActionEvent event) {
        clearForm();
    }

    private boolean validateInput() {
        if (txtOrderCode.getText().isEmpty()) {
            showAlert("Error", "Order code is required");
            return false;
        }
        if (cbCustomer.getValue() == null) {
            showAlert("Error", "Customer is required");
            return false;
        }
        if (cbEmployee.getValue() == null) {
            showAlert("Error", "Employee is required");
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
        if (!txtDiscount.getText().isEmpty()) {
            try {
                BigDecimal discount = new BigDecimal(txtDiscount.getText());
                if (discount.compareTo(calculateTotal()) > 0) {
                    showAlert("Error", "Discount cannot exceed total price");
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid discount value");
                return false;
            }
        }
        return true;
    }

    private void updateTotal() {
        BigDecimal total = calculateTotal();
        lblTotal.setText(total.toString());
    }

    private BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail detail : orderDetails) {
            total = total.add(detail.calculateTotalPrice());
        }
        try {
            BigDecimal discount = txtDiscount.getText().isEmpty() ? BigDecimal.ZERO : new BigDecimal(txtDiscount.getText());
            total = total.subtract(discount);
        } catch (NumberFormatException e) {
            // Ignore invalid discount
        }
        return total.max(BigDecimal.ZERO);
    }

    private void clearForm() {
        txtOrderCode.setText(generateOrderCode());
        cbCustomer.setValue(null);
        cbEmployee.setValue(null);
        cbPaymentMethod.setValue(null);
        txtDiscount.clear();
        cbOrderStatus.setValue("Pending");
        cbDeliveryStatus.setValue("Pending");
        txtNote.clear();
        searchProduct.clear();
        orderDetails.clear();
        suggestionList.setVisible(false);
        updateTotal();
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

    private String generateOrderCode() {
        try {
            String query = "SELECT code_order FROM orders WHERE code_order REGEXP '^O[0-9]{3}$' ORDER BY CAST(SUBSTRING(code_order, 2) AS UNSIGNED) DESC LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    int nextNumber = 1;
                    if (rs.next()) {
                        String lastCode = rs.getString("code_order");
                        int lastNumber = Integer.parseInt(lastCode.substring(1));
                        nextNumber = lastNumber + 1;
                    }
                    return String.format("O%03d", nextNumber);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Error generating order code: " + e.getMessage());
            return "O001";
        }
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
package com.petmanagement.controller;

import com.petmanagement.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class CustomersController {

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> idCol;
    @FXML private TableColumn<Customer, String> codeCol;
    @FXML private TableColumn<Customer, String> nameCol;
    @FXML private TableColumn<Customer, String> phoneCol;
    @FXML private TableColumn<Customer, String> emailCol;
    @FXML private TableColumn<Customer, String> genderCol;
    @FXML private TableColumn<Customer, String> typeCol;
    @FXML private TableColumn<Customer, Double> spentCol;

    @FXML private TextField codeField, nameField, phoneField, emailField, addressField, searchField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> genderBox, typeBox;

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();

    private final String DB_URL = "jdbc:mysql://localhost:3306/petshop";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    @FXML
    private void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        codeCol.setCellValueFactory(new PropertyValueFactory<>("codeCustomer"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("customerType"));
        spentCol.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));

        genderBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        typeBox.setItems(FXCollections.observableArrayList("Regular", "VIP", "Breeder"));

        loadCustomers();

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                showCustomerDetails(newSel);
            }
        });
    }

    private void loadCustomers() {
        customers.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT * FROM customers";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("code_customer"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getDate("birthdate") != null ? rs.getDate("birthdate").toLocalDate() : null,
                        rs.getString("address"),
                        rs.getString("gender"),
                        rs.getString("customer_type"),
                        rs.getDouble("total_spent")
                ));
            }

            customerTable.setItems(customers);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCustomerDetails(Customer c) {
        codeField.setText(c.getCodeCustomer());
        nameField.setText(c.getName());
        phoneField.setText(c.getPhone());
        emailField.setText(c.getEmail());
        birthDatePicker.setValue(c.getBirthdate());
        addressField.setText(c.getAddress());
        genderBox.setValue(c.getGender());
        typeBox.setValue(c.getCustomerType());
    }

    private void clearFields() {
        codeField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        birthDatePicker.setValue(null);
        addressField.clear();
        genderBox.setValue(null);
        typeBox.setValue(null);
        customerTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAdd() {
        showCustomerPopup(null);
    }

    @FXML
    private void handleUpdate() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCustomerPopup(selected);
        }
    }

    @FXML
    private void handleDelete() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "DELETE FROM customers WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            loadCustomers();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        clearFields();
        loadCustomers();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();
        ObservableList<Customer> filtered = FXCollections.observableArrayList();
        for (Customer c : customers) {
            if (c.getName().toLowerCase().contains(keyword)) {
                filtered.add(c);
            }
        }
        customerTable.setItems(filtered);
    }

    @FXML
    private void showDetailPopup() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCustomerPopup(selected);
        }
    }

    private void showCustomerPopup(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/customerDetail.fxml"));
            Parent root = loader.load();

            CustomerDetailController controller = loader.getController();
            controller.setCustomer(customer);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết khách hàng");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                Customer updated = controller.getUpdatedCustomer();
                if (customer == null) {
                    insertCustomer(updated);
                } else {
                    updateCustomer(updated);
                }
                loadCustomers();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertCustomer(Customer c) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "INSERT INTO customers (code_customer, name, phone, email, birthdate, address, gender, customer_type, total_spent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, c.getCodeCustomer());
            stmt.setString(2, c.getName());
            stmt.setString(3, c.getPhone());
            stmt.setString(4, c.getEmail());
            stmt.setDate(5, c.getBirthdate() != null ? Date.valueOf(c.getBirthdate()) : null);
            stmt.setString(6, c.getAddress());
            stmt.setString(7, c.getGender());
            stmt.setString(8, c.getCustomerType());
            stmt.setDouble(9, c.getTotalSpent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateCustomer(Customer c) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "UPDATE customers SET code_customer=?, name=?, phone=?, email=?, birthdate=?, address=?, gender=?, customer_type=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, c.getCodeCustomer());
            stmt.setString(2, c.getName());
            stmt.setString(3, c.getPhone());
            stmt.setString(4, c.getEmail());
            stmt.setDate(5, c.getBirthdate() != null ? Date.valueOf(c.getBirthdate()) : null);
            stmt.setString(6, c.getAddress());
            stmt.setString(7, c.getGender());
            stmt.setString(8, c.getCustomerType());
            stmt.setInt(9, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

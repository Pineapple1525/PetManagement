package com.petmanagement.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.petmanagement.model.Customer;

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

    @FXML
    private void handleAdd() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "INSERT INTO customers (code_customer, name, phone, email, birthdate, address, gender, customer_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, codeField.getText());
            stmt.setString(2, nameField.getText());
            stmt.setString(3, phoneField.getText());
            stmt.setString(4, emailField.getText());
            stmt.setDate(5, birthDatePicker.getValue() != null ? Date.valueOf(birthDatePicker.getValue()) : null);
            stmt.setString(6, addressField.getText());
            stmt.setString(7, genderBox.getValue());
            stmt.setString(8, typeBox.getValue());

            stmt.executeUpdate();
            loadCustomers();
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "UPDATE customers SET code_customer=?, name=?, phone=?, email=?, birthdate=?, address=?, gender=?, customer_type=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, codeField.getText());
            stmt.setString(2, nameField.getText());
            stmt.setString(3, phoneField.getText());
            stmt.setString(4, emailField.getText());
            stmt.setDate(5, birthDatePicker.getValue() != null ? Date.valueOf(birthDatePicker.getValue()) : null);
            stmt.setString(6, addressField.getText());
            stmt.setString(7, genderBox.getValue());
            stmt.setString(8, typeBox.getValue());
            stmt.setInt(9, selected.getId());

            stmt.executeUpdate();
            loadCustomers();
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
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
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            customerTable.setItems(customers);
            return;
        }

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
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/CustomerDetail.fxml"));
            Parent root = loader.load();

            CustomerDetailController controller = loader.getController();
            controller.setCustomer(selected);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết khách hàng");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        searchField.clear();
        customerTable.getSelectionModel().clearSelection();
    }
}

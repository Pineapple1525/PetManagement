package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
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
    @FXML private TableColumn<Customer, String> addressCol;

    @FXML private TextField searchField;

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();

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
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        loadCustomers();

        customerTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openDetailPopup(row.getItem());
                }
            });
            return row;
        });
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchRealTime(newValue);
        });
    }

    private void loadCustomers() {
        customers.clear();
        String sql = "SELECT * FROM customers";
        try (Connection conn = DBConnection.getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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
    private void handleSearchRealTime(String keyword) {
        if (keyword.trim().isEmpty()) {
            customerTable.setItems(customers);
            return;
        }

        String lowerKeyword = keyword.toLowerCase();
        ObservableList<Customer> filtered = FXCollections.observableArrayList();

        for (Customer c : customers) {
            if (
                    (c.getName() != null && c.getName().toLowerCase().contains(lowerKeyword)) ||
                            (c.getPhone() != null && c.getPhone().toLowerCase().contains(lowerKeyword)) ||
                            (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerKeyword)) ||
                            (c.getCodeCustomer() != null && c.getCodeCustomer().toLowerCase().contains(lowerKeyword))
            ) {
                filtered.add(c);
            }
        }

        customerTable.setItems(filtered);
    }


    @FXML
    private void handleAdd() {
        openDetailPopup(null);
    }

    @FXML
    private void handleUpdate() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openDetailPopup(selected);
        } else {
            showAlert("Vui lòng chọn một khách hàng để sửa.");
        }
    }

    @FXML
    private void handleDelete() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một khách hàng để xoá.");
            return;
        }

        String sql = "DELETE FROM customers WHERE id=?";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            loadCustomers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
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
    private void handleViewDetail() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openDetailPopup(selected);
        } else {
            showAlert("Vui lòng chọn một khách hàng để xem chi tiết.");
        }
    }

    private void openDetailPopup(Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/customerDetail.fxml"));
            Parent root = loader.load();

            CustomerDetailController controller = loader.getController();

            // Nếu là thêm mới khách hàng, thì tự sinh mã code_customer
            if (customer == null) {
                Customer newCustomer = new Customer();
                newCustomer.setCodeCustomer(generateCustomerCode()); // Gán mã tự động
                controller.setCustomer(newCustomer);
            } else {
                controller.setCustomer(customer);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết khách hàng");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                Customer updated = controller.getUpdatedCustomer();
                if (updated == null) return;

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
    private String generateCustomerCode() {
        String prefix = "C";
        int maxNumber = 0;

        String sql = "SELECT code_customer FROM customers";
        try (Connection conn = DBConnection.getConn();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String code = rs.getString("code_customer");
                if (code != null && code.matches("C\\d+")) {
                    int num = Integer.parseInt(code.substring(1));
                    if (num > maxNumber) maxNumber = num;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return prefix + String.format("%03d", maxNumber + 1); // ví dụ: C001, C002,...
    }



    private void insertCustomer(Customer c) {
        String sql = "INSERT INTO customers (code_customer, name, phone, email, birthdate, address, gender, customer_type, total_spent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        String sql = "UPDATE customers SET code_customer=?, name=?, phone=?, email=?, birthdate=?, address=?, gender=?, customer_type=?, total_spent=? WHERE id=?";
        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getCodeCustomer());
            stmt.setString(2, c.getName());
            stmt.setString(3, c.getPhone());
            stmt.setString(4, c.getEmail());
            stmt.setDate(5, c.getBirthdate() != null ? Date.valueOf(c.getBirthdate()) : null);
            stmt.setString(6, c.getAddress());
            stmt.setString(7, c.getGender());
            stmt.setString(8, c.getCustomerType());
            stmt.setDouble(9, c.getTotalSpent());
            stmt.setInt(10, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package com.petmanagement.controller;

import com.petmanagement.model.Customer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CustomerDetailController {

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField addressField;
    @FXML private TextField spentField;
    @FXML private ComboBox<String> genderBox;
    @FXML private ComboBox<String> typeBox;
    @FXML private Button saveButton;
    @FXML private Button closeButton;

    private Customer customer;
    private boolean isSaved = false;

    @FXML
    private void initialize() {
        genderBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        typeBox.setItems(FXCollections.observableArrayList("Regular", "VIP", "Breeder"));

        spentField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                spentField.setText(oldVal);
            }
        });
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;

        if (customer != null) {
            codeField.setText(customer.getCodeCustomer());
            codeField.setDisable(true); // Ngăn người dùng sửa mã khách hàng
            nameField.setText(customer.getName());
            phoneField.setText(customer.getPhone());
            emailField.setText(customer.getEmail());
            birthDatePicker.setValue(customer.getBirthdate());
            addressField.setText(customer.getAddress());
            spentField.setText(String.valueOf(customer.getTotalSpent()));
            genderBox.setValue(customer.getGender());
            typeBox.setValue(customer.getCustomerType());
        } else {
            codeField.setDisable(false); // Nếu tạo mới, cho phép nhập mã khách hàng
        }
    }


    public boolean isSaved() {
        return isSaved;
    }

    public Customer getUpdatedCustomer() {
        double spent;

        String spentText = spentField.getText().trim();
        if (spentText.isEmpty()) {
            showAlert("Trường 'Đã chi' không được để trống.");
            return null;
        }

        try {
            spent = Double.parseDouble(spentText);
        } catch (NumberFormatException e) {
            showAlert("Trường 'Đã chi' phải là một số.");
            return null;
        }

        return new Customer(
                customer != null ? customer.getId() : 0,
                codeField.getText().trim(),
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                birthDatePicker.getValue(),
                addressField.getText().trim(),
                genderBox.getValue(),
                typeBox.getValue(),
                spent
        );
    }

    @FXML
    private void handleSave() {
        if (getUpdatedCustomer() == null) return;
        isSaved = true;
        ((Stage) saveButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleClose() {
        isSaved = false;
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Lỗi dữ liệu");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

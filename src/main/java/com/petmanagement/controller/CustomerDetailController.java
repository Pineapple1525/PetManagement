package com.petmanagement.controller;

import com.petmanagement.model.Customer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class CustomerDetailController {

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> genderBox;
    @FXML private ComboBox<String> typeBox;

    private Customer customer;
    private boolean isSaved = false;

    @FXML
    private void initialize() {
        genderBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        typeBox.setItems(FXCollections.observableArrayList("Regular", "VIP", "Breeder"));
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            codeField.setText(customer.getCodeCustomer());
            nameField.setText(customer.getName());
            phoneField.setText(customer.getPhone());
            emailField.setText(customer.getEmail());
            birthDatePicker.setValue(customer.getBirthdate());
            addressField.setText(customer.getAddress());
            genderBox.setValue(customer.getGender());
            typeBox.setValue(customer.getCustomerType());
        }
    }

    public boolean isSaved() {
        return isSaved;
    }

    public Customer getUpdatedCustomer() {
        return new Customer(
                customer != null ? customer.getId() : 0,
                codeField.getText(),
                nameField.getText(),
                phoneField.getText(),
                emailField.getText(),
                birthDatePicker.getValue(),
                addressField.getText(),
                genderBox.getValue(),
                typeBox.getValue(),
                customer != null ? customer.getTotalSpent() : 0
        );
    }

    @FXML
    private void handleSave() {
        isSaved = true;
        ((Stage) codeField.getScene().getWindow()).close();
    }

    @FXML
    private void handleClose() {
        isSaved = false;
        ((Stage) codeField.getScene().getWindow()).close();
    }
}

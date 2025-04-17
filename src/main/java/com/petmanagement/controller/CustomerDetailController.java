package com.petmanagement.controller;

import com.petmanagement.model.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CustomerDetailController {

    @FXML private Label idLabel, codeLabel, nameLabel, phoneLabel, emailLabel,
            birthdateLabel, addressLabel, genderLabel, typeLabel, spentLabel;

    private Customer customer;

    public void setCustomer(Customer customer) {
        this.customer = customer;

        idLabel.setText("ID: " + customer.getId());
        codeLabel.setText("Mã KH: " + customer.getCodeCustomer());
        nameLabel.setText("Tên: " + customer.getName());
        phoneLabel.setText("SĐT: " + customer.getPhone());
        emailLabel.setText("Email: " + customer.getEmail());
        birthdateLabel.setText("Ngày sinh: " + (customer.getBirthdate() != null ? customer.getBirthdate().toString() : ""));
        addressLabel.setText("Địa chỉ: " + customer.getAddress());
        genderLabel.setText("Giới tính: " + customer.getGender());
        typeLabel.setText("Loại KH: " + customer.getCustomerType());
        spentLabel.setText("Tổng chi tiêu: " + customer.getTotalSpent() + " VND");
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }
}

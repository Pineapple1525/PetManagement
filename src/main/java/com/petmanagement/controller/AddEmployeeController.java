package com.petmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.petmanagement.model.Employee;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class AddEmployeeController {

    @FXML private TextField txtCode, txtFullname, txtAddress, txtPhone, txtEmail, txtRole, txtSalary, txtWorkShift, txtStatus;
    @FXML private ComboBox<String> cbGender;
    @FXML private DatePicker dpBirthdate, dpHiringDate;

    @FXML
    public void initialize() {
        cbGender.getItems().addAll("Nam", "Nữ", "Khác");
    }

    private Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/petshop";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void insertEmployee(Employee emp) {
        String sql = "INSERT INTO employees (code_employee, fullname, gender, birthdate, address, phone, email, role, salary, work_shift, status, hiring_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emp.getCodeEmployee());
            stmt.setString(2, emp.getFullname());
            stmt.setString(3, emp.getGender());

            if (emp.getBirthdate() != null) {
                stmt.setDate(4, Date.valueOf(emp.getBirthdate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, emp.getAddress());
            stmt.setString(6, emp.getPhone());
            stmt.setString(7, emp.getEmail());
            stmt.setString(8, emp.getRole());
            stmt.setBigDecimal(9, emp.getSalary());
            stmt.setString(10, emp.getWorkShift());
            stmt.setString(11, emp.getStatus());

            if (emp.getHiringDate() != null) {
                stmt.setTimestamp(12, Timestamp.valueOf(emp.getHiringDate()));
            } else {
                stmt.setNull(12, Types.TIMESTAMP);
            }

            stmt.executeUpdate();
            System.out.println("Thêm nhân viên thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        Employee emp = new Employee();
        emp.setCodeEmployee(txtCode.getText());
        emp.setFullname(txtFullname.getText());

        // Ánh xạ giới tính tiếng Việt sang tiếng Anh phù hợp ENUM trong MySQL
        String genderText = cbGender.getValue();
        switch (genderText) {
            case "Nam": emp.setGender("Male"); break;
            case "Nữ": emp.setGender("Female"); break;
            case "Khác": emp.setGender("Other"); break;
            default: emp.setGender(null); break;
        }

        emp.setBirthdate(dpBirthdate.getValue());
        emp.setAddress(txtAddress.getText());
        emp.setPhone(txtPhone.getText());
        emp.setEmail(txtEmail.getText());
        emp.setRole(txtRole.getText());

        if (!txtSalary.getText().isEmpty()) {
            emp.setSalary(new BigDecimal(txtSalary.getText()));
        }

        emp.setWorkShift(txtWorkShift.getText());
        emp.setStatus(txtStatus.getText());

        if (dpHiringDate.getValue() != null) {
            emp.setHiringDate(dpHiringDate.getValue().atStartOfDay());
        }

        insertEmployee(emp); // Gọi thêm dữ liệu vào MySQL
        ((Stage) txtCode.getScene().getWindow()).close(); // Đóng form sau khi thêm
    }

    @FXML
    private void handleCancel() {
        ((Stage) txtCode.getScene().getWindow()).close();
    }
}

package com.petmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.petmanagement.model.Employee;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class AddEmployeeController {

    @FXML private TextField txtFullname, txtAddress, txtPhone, txtEmail, txtSalary;
    @FXML private ComboBox<String> cbGender, cbRole, cbWorkShift, cbStatus;
    @FXML private DatePicker dpBirthdate, dpHiringDate;

    @FXML
    public void initialize() {
        cbGender.getItems().addAll("Male", "Female", "Other");
        cbRole.getItems().addAll();
        cbWorkShift.getItems().addAll();
        cbStatus.getItems().addAll();
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

            // Hiển thị thông báo thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Thêm nhân viên thành công!");
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            // Hiển thị thông báo lỗi
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi thêm nhân viên: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private String generateEmployeeCode() {
        String sql = "SELECT MAX(CAST(SUBSTRING(code_employee, 4) AS UNSIGNED)) FROM employees";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int lastCode = rs.getInt(1);
                return "EMP" + (lastCode + 1); // Tăng mã nhân viên lên 1
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "EMP1"; // Nếu không tìm thấy mã nào, trả về mã đầu tiên
    }

    @FXML
    private void handleSave() {
        if (txtFullname.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Full name is required!");
            alert.showAndWait();
            return;
        }

        Employee emp = new Employee();

        emp.setCodeEmployee(generateEmployeeCode());
        emp.setFullname(txtFullname.getText());
        emp.setGender(cbGender.getValue());
        emp.setBirthdate(dpBirthdate.getValue());
        emp.setAddress(txtAddress.getText());
        emp.setPhone(txtPhone.getText());
        emp.setEmail(txtEmail.getText());

        emp.setRole(cbRole.getValue());

        try {
            if (!txtSalary.getText().isEmpty()) {
                emp.setSalary(new BigDecimal(txtSalary.getText()));
            } else {
                emp.setSalary(BigDecimal.ZERO);
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid salary: " + txtSalary.getText());
            alert.showAndWait();
            return;
        }

        emp.setWorkShift(cbWorkShift.getValue());
        emp.setStatus(cbStatus.getValue());

        if (dpHiringDate.getValue() != null) {
            emp.setHiringDate(dpHiringDate.getValue().atStartOfDay());
        }

        insertEmployee(emp);

        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }

        ((Stage) txtFullname.getScene().getWindow()).close();
    }



    private Runnable onUpdateCallback;

    public void setOnUpdateCallback(Runnable onUpdateCallback) {
        this.onUpdateCallback = onUpdateCallback;
    }


    @FXML
    private void handleCancel() {
        ((Stage) txtFullname.getScene().getWindow()).close();
    }


}
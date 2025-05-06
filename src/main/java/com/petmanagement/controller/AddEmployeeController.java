package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.petmanagement.model.Employee;

import java.math.BigDecimal;
import java.sql.*;

public class AddEmployeeController {

    @FXML private TextField txtFullname, txtAddress, txtPhone, txtEmail, txtSalary, txtUsername, txtPassword;
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
        return DBConnection.getConn();
    }

    private void insertEmployee(Employee emp) {
        String sql = "INSERT INTO employees (code_employee, fullname, gender, birthdate, address, phone, email, role, salary, work_shift, status, hiring_date, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String userSql = "INSERT INTO users (username,password,email) VALUES (?,?,?)";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, emp.getUsername());
                userStmt.setString(2, emp.getPassword());
                userStmt.setString(3, emp.getEmail());
                userStmt.executeUpdate();

                // Get user_id from users table
                ResultSet rs = userStmt.getGeneratedKeys();
                int userId = -1;
                    userId = rs.getInt(1);
                }


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
                String email = emp.getEmail();
                if (email == null || email.isEmpty()) {
                    email = "default@email.com";
                }
                stmt.setString(7, email);
                stmt.setString(8, emp.getRole());
                stmt.setBigDecimal(9, emp.getSalary());
                stmt.setString(10, emp.getWorkShift());
                stmt.setString(11, emp.getStatus());

                if (emp.getHiringDate() != null) {
                    stmt.setTimestamp(12, Timestamp.valueOf(emp.getHiringDate()));
                } else {
                    stmt.setNull(12, Types.TIMESTAMP);
                }
                stmt.setInt(13, userId);

                stmt.executeUpdate();
                System.out.println("Successfully added employee!");
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Employee added successfully!");
                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error when adding employee: " + e.getMessage());
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
                return "EMP" + (lastCode + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "EMP1";
    }

    @FXML
    private void handleSave() {
        // Get data from UI
        String fullname = txtFullname.getText().trim();
        String gender = cbGender.getValue();
        String address = txtAddress.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String role = cbRole.getValue();
        String salaryStr = txtSalary.getText().trim();
        String workShift = cbWorkShift.getValue();
        String status = cbStatus.getValue();
        String username = txtUsername.getText().trim(); // Get username
        String password = txtPassword.getText().trim(); // Get password

        // Check data validity
        if (fullname.isEmpty()) {
            showAlert("Please enter full name!");
            return;
        }
        if (gender == null) {
            showAlert("Please select gender!");
            return;
        }
        if (dpBirthdate.getValue() == null) {
            showAlert("Please select birthdate!");
            return;
        }
        if (address.isEmpty()) {
            showAlert("Please enter address!");
            return;
        }
        if (!phone.matches("\\d{10,11}")) {
            showAlert("Invalid phone number (must be 10-11 digits)!");
            return;
        }
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert("Invalid email!");
            return;
        }
        if (role == null) {
            showAlert("Please select role!");
            return;
        }
        BigDecimal salary;
        try {
            salary = salaryStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(salaryStr);
            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Salary cannot be negative!");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid salary!");
            return;
        }
        if (workShift == null) {
            showAlert("Please select work shift!");
            return;
        }
        if (status == null) {
            showAlert("Please select status!");
            return;
        }
        if (dpHiringDate.getValue() == null) {
            showAlert("Please select hiring date!");
            return;
        }
        if (username.isEmpty()) {
            showAlert("Please enter username!");
            return;
        }
        if (password.isEmpty()) {
            showAlert("Please enter password!");
            return;
        }

        // Assign data to Employee object
        Employee emp = new Employee();
        emp.setCodeEmployee(generateEmployeeCode());
        emp.setFullname(fullname);
        emp.setGender(gender);
        emp.setBirthdate(dpBirthdate.getValue());
        emp.setAddress(address);
        emp.setPhone(phone);
        emp.setEmail(email);
        emp.setRole(role);
        emp.setSalary(salary);
        emp.setWorkShift(workShift);
        emp.setStatus(status);
        emp.setHiringDate(dpHiringDate.getValue().atStartOfDay());
        emp.setUsername(username);  // Set username
        emp.setPassword(password);  // Set password

        // Add employee to database
        insertEmployee(emp);

        // Trigger callback if exists
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }

        // Close window
        ((Stage) txtFullname.getScene().getWindow()).close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
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

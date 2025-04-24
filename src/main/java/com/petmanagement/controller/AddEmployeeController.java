package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
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
        return DBConnection.getConn();
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
        String fullname = txtFullname.getText().trim();
        String gender = cbGender.getValue();
        String address = txtAddress.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String role = cbRole.getValue();
        String salaryStr = txtSalary.getText().trim();
        String workShift = cbWorkShift.getValue();
        String status = cbStatus.getValue();

        // Validate họ tên
        if (fullname.isEmpty()) {
            showAlert("Vui lòng nhập họ tên!");
            return;
        }

        // Validate giới tính
        if (gender == null) {
            showAlert("Vui lòng chọn giới tính!");
            return;
        }

        // Validate ngày sinh
        if (dpBirthdate.getValue() == null) {
            showAlert("Vui lòng chọn ngày sinh!");
            return;
        }

        // Validate địa chỉ
        if (address.isEmpty()) {
            showAlert("Vui lòng nhập địa chỉ!");
            return;
        }

        // Validate số điện thoại
        if (!phone.matches("\\d{10,11}")) {
            showAlert("Số điện thoại không hợp lệ (chỉ 10-11 chữ số)!");
            return;
        }

        // Validate email
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert("Email không hợp lệ!");
            return;
        }

        // Validate vai trò
        if (role == null) {
            showAlert("Vui lòng chọn vai trò!");
            return;
        }

        // Validate lương
        BigDecimal salary;
        try {
            salary = salaryStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(salaryStr);
            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Lương không được âm!");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Lương không hợp lệ!");
            return;
        }

        // Validate ca làm việc
        if (workShift == null) {
            showAlert("Vui lòng chọn ca làm việc!");
            return;
        }

        // Validate trạng thái
        if (status == null) {
            showAlert("Vui lòng chọn trạng thái!");
            return;
        }

        // Validate ngày vào làm
        if (dpHiringDate.getValue() == null) {
            showAlert("Vui lòng chọn ngày vào làm!");
            return;
        }

        // Gán dữ liệu vào đối tượng Employee
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

        // Thêm nhân viên vào cơ sở dữ liệu
        insertEmployee(emp);

        // Gọi callback nếu có
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }

        // Đóng cửa sổ
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
package com.petmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.petmanagement.model.Employee;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class AddEmployeeController {

    @FXML private TextField txtCode, txtFullname, txtAddress, txtPhone, txtEmail, txtSalary;
    @FXML private ComboBox<String> cbGender, cbRole, cbWorkShift, cbStatus;
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

    @FXML
    private void handleSave() {
        // Kiểm tra các trường bắt buộc
        if (txtCode.getText().isEmpty() || txtFullname.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Mã nhân viên và Họ tên không được để trống!");
            alert.showAndWait();
            return;
        }

        Employee emp = new Employee();

        emp.setCodeEmployee(txtCode.getText() != null ? txtCode.getText() : "");
        emp.setFullname(txtFullname.getText() != null ? txtFullname.getText() : "");

        // Ánh xạ giới tính tiếng Việt sang tiếng Anh
        String genderText = cbGender.getValue();
        if (genderText != null) {
            switch (genderText) {
                case "Nam": emp.setGender("Male"); break;
                case "Nữ": emp.setGender("Female"); break;
                case "Khác": emp.setGender("Other"); break;
                default: emp.setGender(null); break;
            }
        } else {
            emp.setGender(null);
        }

        emp.setBirthdate(dpBirthdate.getValue());
        emp.setAddress(txtAddress.getText() != null ? txtAddress.getText() : "");
        emp.setPhone(txtPhone.getText() != null ? txtPhone.getText() : "");
        emp.setEmail(txtEmail.getText() != null ? txtEmail.getText() : "");

        // Ánh xạ vai trò tiếng Việt sang tiếng Anh
        String roleText = cbRole.getValue();
        if (roleText != null) {
            switch (roleText) {
                case "Quản lý": emp.setRole("Manager"); break;
                case "Nhân viên": emp.setRole("Sales"); break; // Giả sử "Nhân viên" tương ứng với "Sales"
                case "Khách": emp.setRole("Other"); break; // Giả sử "Khách" tương ứng với "Other"
                default: emp.setRole("Other"); break;
            }
        } else {
            emp.setRole("Other"); // Giá trị mặc định nếu không chọn
        }

        // Lấy giá trị lương và kiểm tra rỗng
        if (txtSalary.getText() != null && !txtSalary.getText().isEmpty()) {
            try {
                emp.setSalary(new BigDecimal(txtSalary.getText()));
            } catch (NumberFormatException e) {
                System.out.println("Lương không hợp lệ: " + txtSalary.getText());
                emp.setSalary(BigDecimal.ZERO);
            }
        } else {
            emp.setSalary(BigDecimal.ZERO);
        }

        // Ánh xạ ca làm tiếng Việt sang tiếng Anh
        String workShiftText = cbWorkShift.getValue();
        if (workShiftText != null) {
            switch (workShiftText) {
                case "Sáng": emp.setWorkShift("Morning"); break;
                case "Chiều": emp.setWorkShift("Afternoon"); break;
                case "Tối": emp.setWorkShift("Evening"); break;
                default: emp.setWorkShift("Morning"); break; // Giá trị mặc định
            }
        } else {
            emp.setWorkShift("Morning"); // Giá trị mặc định nếu không chọn
        }

        // Ánh xạ trạng thái tiếng Việt sang tiếng Anh
        String statusText = cbStatus.getValue();
        if (statusText != null) {
            switch (statusText) {
                case "Đang làm": emp.setStatus("Active"); break;
                case "Nghỉ việc": emp.setStatus("Resigned"); break;
                default: emp.setStatus("Active"); break; // Giá trị mặc định
            }
        } else {
            emp.setStatus("Active"); // Giá trị mặc định nếu không chọn
        }

        // Lấy giá trị ngày vào làm
        if (dpHiringDate.getValue() != null) {
            emp.setHiringDate(dpHiringDate.getValue().atStartOfDay());
        }

        // Gọi phương thức để thêm nhân viên vào MySQL
        insertEmployee(emp);

        // Đóng form sau khi thêm
        ((Stage) txtCode.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) txtCode.getScene().getWindow()).close();
    }
}
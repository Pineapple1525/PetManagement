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
        // Kiểm tra các trường bắt buộc
        if (txtFullname.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Họ tên không được để trống!");
            alert.showAndWait();
            return;
        }

        Employee emp = new Employee();

        // Tự động tạo mã nhân viên
        String employeeCode = generateEmployeeCode();
        emp.setCodeEmployee(employeeCode); // Gán mã nhân viên tự động
        emp.setFullname(txtFullname.getText());

        // Ánh xạ giới tính tiếng Việt sang tiếng Anh
        String genderText = cbGender.getValue();
        if (genderText != null) {
            switch (genderText) {
                case "Nam": emp.setGender("Male"); break;
                case "Nữ": emp.setGender("Female"); break;
                case "Khác": emp.setGender("Other"); break;
                default: emp.setGender(null); break;
            }
        }

        emp.setBirthdate(dpBirthdate.getValue());
        emp.setAddress(txtAddress.getText());
        emp.setPhone(txtPhone.getText());
        emp.setEmail(txtEmail.getText());

        // Ánh xạ vai trò tiếng Việt sang tiếng Anh
        String roleText = cbRole.getValue();
        if (roleText != null) {
            switch (roleText) {
                case "Quản lý": emp.setRole("Manager"); break;
                case "Nhân viên": emp.setRole("Sales"); break;
                case "Khách": emp.setRole("Other"); break;
                default: emp.setRole("Other"); break;
            }
        }

        // Kiểm tra lương và chuyển thành BigDecimal
        try {
            if (!txtSalary.getText().isEmpty()) {
                emp.setSalary(new BigDecimal(txtSalary.getText()));
            } else {
                emp.setSalary(BigDecimal.ZERO); // Nếu không có giá trị, mặc định là 0
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lương không hợp lệ: " + txtSalary.getText());
            alert.showAndWait();
            return;
        }

        // Ánh xạ ca làm tiếng Việt sang tiếng Anh
        String workShiftText = cbWorkShift.getValue();
        if (workShiftText != null) {
            switch (workShiftText) {
                case "Sáng": emp.setWorkShift("Morning"); break;
                case "Chiều": emp.setWorkShift("Afternoon"); break;
                case "Tối": emp.setWorkShift("Evening"); break;
                default: emp.setWorkShift("Morning"); break;
            }
        }

        // Ánh xạ trạng thái tiếng Việt sang tiếng Anh
        String statusText = cbStatus.getValue();
        if (statusText != null) {
            switch (statusText) {
                case "Đang làm": emp.setStatus("Active"); break;
                case "Nghỉ việc": emp.setStatus("Resigned"); break;
                default: emp.setStatus("Active"); break;
            }
        }

        // Lấy giá trị ngày vào làm
        if (dpHiringDate.getValue() != null) {
            emp.setHiringDate(dpHiringDate.getValue().atStartOfDay());
        }

        // Gọi phương thức để thêm nhân viên vào MySQL
        insertEmployee(emp);

        // Tự động reload lại danh sách nhân viên
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }

        // Đóng form sau khi thêm
        ((Stage) txtCode.getScene().getWindow()).close();
    }


    private Runnable onUpdateCallback;

    public void setOnUpdateCallback(Runnable onUpdateCallback) {
        this.onUpdateCallback = onUpdateCallback;
    }







    @FXML
    private void handleCancel() {
        ((Stage) txtCode.getScene().getWindow()).close();
    }


}
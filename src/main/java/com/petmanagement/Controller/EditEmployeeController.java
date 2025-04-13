package com.petmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.petmanagement.model.Employee;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class EditEmployeeController {

    @FXML private TextField txtCode;
    @FXML private TextField txtFullName;
    @FXML private ComboBox<String> comboGender;
    @FXML private DatePicker dateBirth;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> txtRole;
    @FXML private TextField txtAddress;
    @FXML private TextField txtSalary;
    @FXML private ComboBox<String> txtWorkShift;
    @FXML private ComboBox<String> txtStatus;

    private Employee employee;

    public void setEmployee(Employee employee) {
        this.employee = employee;
        loadEmployeeData();
    }

    private void loadEmployeeData() {
        txtCode.setText(employee.getCodeEmployee());
        txtFullName.setText(employee.getFullname());
        comboGender.setValue(employee.getGender());
        dateBirth.setValue(employee.getBirthdate());
        txtPhone.setText(employee.getPhone());
        txtEmail.setText(employee.getEmail());
        txtRole.setValue(employee.getRole());
        txtAddress.setText(employee.getAddress());
        txtSalary.setText(employee.getSalary().toString());
        txtWorkShift.setValue(employee.getWorkShift());
        txtStatus.setValue(employee.getStatus());
    }

    @FXML
    private void handleSave() {
        // Kiểm tra các trường bắt buộc
        if (txtFullName.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Họ tên không được để trống!");
            alert.showAndWait();
            return;
        }
        if (txtPhone.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Số điện thoại không được để trống!");
            alert.showAndWait();
            return;
        }
        if (txtEmail.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Email không được để trống!");
            alert.showAndWait();
            return;
        }

        // Cập nhật thông tin nhân viên từ các trường nhập
        employee.setFullname(txtFullName.getText());
        employee.setGender(comboGender.getValue());
        employee.setBirthdate(dateBirth.getValue());
        employee.setPhone(txtPhone.getText());
        employee.setEmail(txtEmail.getText());
        employee.setRole(txtRole.getValue());
        employee.setAddress(txtAddress.getText());

        // Lấy và kiểm tra lương
        try {
            employee.setSalary(new BigDecimal(txtSalary.getText()));
        } catch (NumberFormatException e) {
            System.out.println("Lương không hợp lệ: " + txtSalary.getText());
            employee.setSalary(BigDecimal.ZERO); // Giá trị mặc định
            Alert alert = new Alert(Alert.AlertType.WARNING, "Lương không hợp lệ, đã đặt về 0!");
            alert.showAndWait();
        }

        employee.setWorkShift(txtWorkShift.getValue());
        employee.setStatus(txtStatus.getValue());

        // Gọi phương thức cập nhật nhân viên vào cơ sở dữ liệu
        updateEmployeeInDatabase(employee);

        // Gọi callback để làm mới danh sách nhân viên trong EmployeeController
        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }

        // Đóng cửa sổ
        ((Stage) txtCode.getScene().getWindow()).close();
    }


    private void updateEmployeeInDatabase(Employee emp) {
        String sql = "UPDATE employees SET fullname=?, gender=?, birthdate=?, address=?, phone=?, email=?, role=?, salary=?, work_shift=?, status=? WHERE code_employee=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emp.getFullname());
            stmt.setString(2, emp.getGender());
            stmt.setDate(3, Date.valueOf(emp.getBirthdate()));
            stmt.setString(4, emp.getAddress());
            stmt.setString(5, emp.getPhone());
            stmt.setString(6, emp.getEmail());
            stmt.setString(7, emp.getRole());
            stmt.setBigDecimal(8, emp.getSalary());
            stmt.setString(9, emp.getWorkShift());
            stmt.setString(10, emp.getStatus());
            stmt.setString(11, emp.getCodeEmployee());

            stmt.executeUpdate();
            System.out.println("Cập nhật nhân viên thành công!");

            // Hiển thị thông báo thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cập nhật nhân viên thành công!");
            alert.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
            // Hiển thị thông báo lỗi
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi khi cập nhật nhân viên: " + e.getMessage());
            alert.showAndWait();
        }
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

    private Runnable onUpdateCallback;

    public void setOnUpdateCallback(Runnable onUpdateCallback) {
        this.onUpdateCallback = onUpdateCallback;
    }


    @FXML
    private void handleCancel() {
        ((Stage) txtCode.getScene().getWindow()).close();
    }


}

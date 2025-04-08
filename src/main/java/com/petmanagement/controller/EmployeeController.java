package com.petmanagement.controller;

import com.petmanagement.model.Employee;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class EmployeeController {

    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, Integer> colId;
    @FXML
    private TableColumn<Employee, String> colCode;
    @FXML
    private TableColumn<Employee, String> colName;
    @FXML
    private TableColumn<Employee, String> colGender;
    @FXML
    private TableColumn<Employee, LocalDate> colBirth;
    @FXML
    private TableColumn<Employee, String> colPhone;
    @FXML
    private TableColumn<Employee, String> colEmail;
    @FXML
    private TableColumn<Employee, String> colRole;
    @FXML
    private TableColumn<Employee, String> colAddress;
    @FXML
    private TableColumn<Employee, java.math.BigDecimal> colSalary;
    @FXML
    private TableColumn<Employee, String> colWorkShift;
    @FXML
    private TableColumn<Employee, String> colStatus;
    @FXML
    private TableColumn<Employee, LocalDateTime> colHiringDate;


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

    private ObservableList<Employee> getAllEmployees() {
        ObservableList<Employee> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM employees";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setEmployeeId(rs.getInt("employee_id"));
                emp.setCodeEmployee(rs.getString("code_employee"));
                emp.setFullname(rs.getString("fullname"));
                emp.setGender(rs.getString("gender"));

                Date birthdate = rs.getDate("birthdate");
                if (birthdate != null) {
                    emp.setBirthdate(birthdate.toLocalDate());
                }

                emp.setAddress(rs.getString("address"));
                emp.setPhone(rs.getString("phone"));
                emp.setEmail(rs.getString("email"));
                emp.setRole(rs.getString("role"));
                emp.setSalary(rs.getBigDecimal("salary"));
                emp.setWorkShift(rs.getString("work_shift"));
                emp.setStatus(rs.getString("status"));

                Timestamp hiringDate = rs.getTimestamp("hiring_date");
                if (hiringDate != null) {
                    emp.setHiringDate(hiringDate.toLocalDateTime());
                }

                list.add(emp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @FXML
    public void initialize() {
        // Gán thuộc tính các cột
        colId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("codeEmployee"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colBirth.setCellValueFactory(new PropertyValueFactory<>("birthdate"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colWorkShift.setCellValueFactory(new PropertyValueFactory<>("workShift"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colHiringDate.setCellValueFactory(new PropertyValueFactory<>("hiringDate"));


        // Load dữ liệu
        employeeTable.setItems(getAllEmployees());
    }



    @FXML
    private void handleAddEmployee(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/add-employee.fxml"));

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Thêm nhân viên");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}

package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.Employee;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @FXML
    private TableColumn<Employee, Void> colActions;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> genderFilter;
    @FXML
    private ComboBox<String> roleFilter;
    @FXML
    private Button btnAdd;
    @FXML
    private final int rowsPerPage = 7;
    @FXML
    private int currentPage = 1;
    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;
    @FXML
    private Label lblPageInfo;
    @FXML
    private ObservableList<Employee> fullEmployeeList = FXCollections.observableArrayList();


    private Connection getConnection() {
        return DBConnection.getConn();
    }

    @FXML
    public void initialize() {
        String role = Session.getRole();
        System.out.println("Role người dùng: " + role);
        if (!"Manager".equalsIgnoreCase(role) && !"Boss".equalsIgnoreCase(role)) {
            btnAdd.setVisible(false);
            for (TableColumn<Employee, ?> col : employeeTable.getColumns()) {
                if (col.getText().equals("Actions")) {
                    col.setVisible(false);
                }
            }
        }
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
        colActions.setCellFactory(column -> new TableCell<Employee, Void>() {
            private final HBox buttons = new HBox();
            {
                Button editButton = new Button("Edit");
                editButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    handleEditEmployee(employee);
                });

                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    handleDeleteEmployee(employee);
                });

                buttons.getChildren().addAll(editButton, deleteButton);
                buttons.setSpacing(10);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        genderFilter.getItems().addAll();
        genderFilter.setOnAction(event -> handleFilter());
        roleFilter.getItems().addAll();
        roleFilter.setOnAction(event -> handleFilter());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        loadData();
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

    public void handleDeleteEmployee(Employee employee) {
        // Xác nhận xóa
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confrim Delete");
        alert.setHeaderText("Do you want to delete this employee?");
        alert.setContentText("Employee: " + employee.getFullname());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM employees WHERE employee_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, employee.getEmployeeId());
                stmt.executeUpdate();

                // Cập nhật bảng sau khi xóa
                employeeTable.setItems(getAllEmployees());
                System.out.println("Delete Success: " + employee.getFullname());
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddEmployee(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/add-employee.fxml"));

            Parent root = loader.load();
            AddEmployeeController addController = loader.getController();
            addController.setOnUpdateCallback(this::loadData);
            Stage stage = new Stage();
            stage.setTitle("Add Employee");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error: " + e.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleEditEmployee(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/edit-employee.fxml"));
            Parent root = loader.load();

            EditEmployeeController editController = loader.getController();
            editController.setEmployee(employee);
            editController.setOnUpdateCallback(this::loadData);

            Stage stage = new Stage();
            stage.setTitle("Edit Employee");
            stage.setScene(new Scene(root));

            stage.setOnHiding(e -> Platform.runLater(() -> {
                loadData();
            }));

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error: " + e.getMessage());
        }
    }

    private void loadData() {
        fullEmployeeList = getAllEmployees();
        currentPage = 1;
        updateTable();
        employeeTable.refresh();

    }

    @FXML
    private void handleSearch() {
        String gender = genderFilter.getValue();
        String role = roleFilter.getValue();
        String keyword = searchField.getText().trim().toLowerCase();

        ObservableList<Employee> filteredList = FXCollections.observableArrayList();

        for (Employee emp : fullEmployeeList) {
            boolean matchesGender = (gender == null || gender.isEmpty() || emp.getGender().equalsIgnoreCase(gender));
            boolean matchesRole = (role == null || role.isEmpty() || emp.getRole().equalsIgnoreCase(role));
            boolean matchesSearch =
                    emp.getCodeEmployee().toLowerCase().contains(keyword)
                            || emp.getFullname().toLowerCase().contains(keyword)
                            || emp.getEmail().toLowerCase().contains(keyword)
                            || emp.getPhone().toLowerCase().contains(keyword);
            if (matchesGender && matchesRole && matchesSearch) {
                filteredList.add(emp);
            }
        }

        employeeTable.setItems(filteredList);
        lblPageInfo.setText("Kết quả lọc: " + filteredList.size() + " nhân viên");
        btnPrev.setDisable(true);
        btnNext.setDisable(true);
    }
    @FXML
    public void handleFilter() {
        handleSearch();
    }


    private void updateTable() {
        int fromIndex = (currentPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, fullEmployeeList.size());

        if (fromIndex <= toIndex) {
            employeeTable.setItems(FXCollections.observableArrayList(fullEmployeeList.subList(fromIndex, toIndex)));
        }

        int totalPages = (int) Math.ceil((double) fullEmployeeList.size() / rowsPerPage);
        lblPageInfo.setText("Pages " + currentPage + " / " + totalPages);

        btnPrev.setDisable(currentPage == 1);
        btnNext.setDisable(currentPage == totalPages || totalPages == 0);
    }


    private int getTotalPages() {
        return (int) Math.ceil((double) fullEmployeeList.size() / rowsPerPage);
    }
    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTable();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < getTotalPages()) {
            currentPage++;
            updateTable();
        }
    }



}

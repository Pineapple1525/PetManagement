package com.petmanagement.controller;

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
    private Button  btnAdd;




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

    @FXML
    public void initialize() {

        String role = Session.getRole();
        System.out.println("Role người dùng: " + role);


// Kiểm tra quyền truy cập
        if (!"Manager".equalsIgnoreCase(role)) {
            // Nếu không có quyền, hiển thị cảnh báo và ẩn bảng
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Truy cập bị từ chối");
            alert.setHeaderText(null);
            alert.setContentText("Bạn không đủ đẳng cấp để xem trang này");
            alert.showAndWait();

            // Ẩn bảng và các điều khiển khác
            employeeTable.setVisible(false);    // Ẩn bảng nhân viên
            searchField.setVisible(false);      // Ẩn trường tìm kiếm
            genderFilter.setVisible(false);     // Ẩn bộ lọc giới tính
            roleFilter.setVisible(false);       // Ẩn bộ lọc vai trò
            btnAdd.setVisible(false);           // Ẩn nút thêm nhân viên (nếu cần)
            return; // Không load dữ liệu
        }


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
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());


        // Thiết lập cột "Hành động"
        colActions.setCellFactory(column -> new TableCell<Employee, Void>() {
            private final HBox buttons = new HBox();

            {
                Button editButton = new Button("Sửa");
                editButton.setOnAction(event -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    handleEditEmployee(employee); // Gọi phương thức với nhân viên đã chọn
                });

                Button deleteButton = new Button("Xóa");
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

        // Load dữ liệu
        employeeTable.setItems(getAllEmployees());
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
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa nhân viên này không?");
        alert.setContentText("Nhân viên: " + employee.getFullname());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String sql = "DELETE FROM employees WHERE employee_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, employee.getEmployeeId());
                stmt.executeUpdate();

                // Cập nhật bảng sau khi xóa
                employeeTable.setItems(getAllEmployees());
                System.out.println("Nhân viên đã được xóa: " + employee.getFullname());
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Lỗi khi xóa nhân viên: " + e.getMessage());
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
            stage.setTitle("Thêm nhân viên");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi khi mở trang thêm nhân viên: " + e.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
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
            editController.setOnUpdateCallback(this::loadData);  // Set the callback to reload data

            // Khởi tạo Stage sau khi thiết lập controller
            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa nhân viên");
            stage.setScene(new Scene(root));

            // Cập nhật lại bảng khi cửa sổ đóng
            stage.setOnHiding(e -> Platform.runLater(() -> {
                loadData();  // Gọi lại phương thức loadData để làm mới bảng
            }));

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi khi mở trang chỉnh sửa: " + e.getMessage());
        }
    }

    private void loadData() {
        System.out.println("Đang tải lại dữ liệu...");
        employeeTable.setItems(getAllEmployees()); // Làm mới dữ liệu bảng
        employeeTable.refresh(); // Cập nhật lại giao diện bảng
    }

 
    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();

        ObservableList<Employee> filteredList = FXCollections.observableArrayList();

        for (Employee emp : getAllEmployees()) {
            if (emp.getCodeEmployee().toLowerCase().contains(keyword) ||
                    emp.getFullname().toLowerCase().contains(keyword)) {
                filteredList.add(emp);
            }
        }

        employeeTable.setItems(filteredList);
    }


}

package com.petmanagement.controller;

import com.petmanagement.connection.DBConnection;
import com.petmanagement.model.Employee;
import com.petmanagement.model.User;
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
import java.util.List;

public class UserController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, Void> colAction;
    @FXML
    private TextField searchField;

    @FXML
    private Button btnAdd;

    @FXML
    private Label lblPageInfo;

    @FXML
    private Button btnPrev, btnNext;

    private final int rowsPerPage = 7;
    private int currentPage = 1;
    private ObservableList<User> fullUserList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {


        String role = Session.getRole();
        System.out.println("Role người dùng: " + role);
        if (!"Manager".equalsIgnoreCase(role) && !"Boss".equalsIgnoreCase(role)) {
            btnAdd.setVisible(false);
            for (TableColumn<User, ?> col : userTable.getColumns()) {
                if (col.getText().equals("Action")) {
                    col.setVisible(false);
                }
            }
        }
        // Định nghĩa các cột trong bảng
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));



        colAction.setCellFactory(column -> new TableCell<User, Void>() {
            private final HBox buttons = new HBox();
            {
                Button editButton = new Button("Edit");
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                buttons.getChildren().addAll(editButton);
                buttons.setSpacing(10);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });


        loadData();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }
    private void loadData() {
        fullUserList = getAllUsers();
        currentPage = 1;
        updateTable();
        userTable.refresh();

    }

    private ObservableList<User> getAllUsers() {
        ObservableList<User> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM users";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setPassword(rs.getString("password"));
                list.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (list.isEmpty()) {
            System.out.println("Không có người dùng nào trong cơ sở dữ liệu.");
        }

        return list;
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();

        fullUserList = getAllUsers(); // luôn lấy lại danh sách gốc từ DB (nếu cần)

        if (!keyword.isEmpty()) {
            ObservableList<User> filtered = FXCollections.observableArrayList();
            for (User user : getAllUsers()) {
                if (user.getUsername().toLowerCase().contains(keyword) ||
                        user.getEmail().toLowerCase().contains(keyword)) {
                    filtered.add(user);
                }
            }
            fullUserList = filtered;
        }

        currentPage = 1;
        updateTable();
    }



    private void updateTable() {
        int total = fullUserList.size();
        int fromIndex = (currentPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, total);

        if (fromIndex >= total || fromIndex < 0) {
            userTable.setItems(FXCollections.observableArrayList());
        } else {
            userTable.setItems(FXCollections.observableArrayList(fullUserList.subList(fromIndex, toIndex)));
        }

        int totalPages = getTotalPages();
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }



    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/edit-user.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Sửa người dùng");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi khi mở trang sửa người dùng: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/add-user.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Thêm người dùng");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi khi mở trang thêm người dùng: " + e.getMessage());
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
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            fullUserList = getAllUsers();
            updateTable();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < getTotalPages()) {
            currentPage++;
            fullUserList = getAllUsers();
            updateTable();
        }
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) fullUserList.size() / rowsPerPage);
    }


}

package com.petmanagement.controller;


import com.petmanagement.model.Pet;
import com.petmanagement.model.PetFoodTracking;
import com.petmanagement.connection.DBConnection;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;


import javafx.geometry.Insets;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PetController {

    @FXML
    private TableView<Pet> petTable;
    @FXML
    private TableColumn<Pet, Void> actionColumn;
    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<Pet, Integer> idColumn;
    @FXML
    private TableColumn<Pet, String> nameColumn;
    @FXML
    private TableColumn<Pet, String> codeColumn;
    @FXML
    private TableColumn<Pet, String> speciesColumn;
    @FXML
    private TableColumn<Pet, String> breedColumn;

    @FXML
    private TableColumn<Pet, String> genderColumn;
    @FXML
    private TableColumn<Pet, String> birthdateColumn;
    @FXML
    private TableColumn<Pet, String> colorColumn;
    @FXML
    private TableColumn<Pet, Double> weightColumn;

    @FXML
    private TableColumn<Pet, String> healthStatusColumn;
    @FXML
    private TableColumn<Pet, String> imageColumn;
    @FXML
    private TableColumn<Pet, String> sourceTypeColumn;
    @FXML
    private TableColumn<Pet, Integer> POIdColumn;

    @FXML
    private TableColumn<Pet, String> breederColumn;
    @FXML
    private TableColumn<Pet, LocalDateTime> importDateColumn;
    @FXML
    private TableColumn<Pet, String> adoptionStatusColumn;
    @FXML
    private TableColumn<Pet, Double> priceColumn;



    private ObservableList<Pet> petList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPetId()));
        nameColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getName()));
        nameColumn.setCellFactory(column -> new TableCell<Pet, String>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setOnMouseClicked(null);
                } else {
                    setText(name);
                    setStyle("-fx-text-fill: blue; -fx-underline: true;");
                    setOnMouseClicked(event -> {
                        Pet selectedPet = getTableView().getItems().get(getIndex());
                        openPetFoodTrackingStage(selectedPet.getPetCode());
                    });
                }
            }
        });


        codeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPetCode()));
        speciesColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSpecies()));
        breedColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBreed()));
        genderColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGender()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        birthdateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getBirthdate() != null
                                ? data.getValue().getBirthdate().format(formatter)
                                : ""
                )
        );
        colorColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getColor()));
        weightColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getWeight()));
        healthStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getHealthStatus()));
        imageColumn.setCellFactory(col -> new TableCell<Pet, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image(item);
                        imageView.setImage(image);
                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                        e.printStackTrace();
                    }
                }
            }
        });
        sourceTypeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSourceType()));
        POIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPreviousOwnerId()));
        breederColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEmployeeId()));
        importDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getImportDate()));
        adoptionStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAdoptionStatus()));

        priceColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrice()));

        setupActionColumn();
        petTable.setItems(petList);
        loadPetsFromDatabase();
        searchField.setOnKeyReleased(event -> searchPets());

    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<Pet, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Button btnEdit = new Button("Sửa");
                    Button btnDelete = new Button("Xóa");
                    HBox pane = new HBox(10, btnEdit, btnDelete);
                    pane.setAlignment(Pos.CENTER);
                    pane.setPadding(new Insets(5));

                    btnEdit.getStyleClass().add("doodle-button");
                    btnDelete.getStyleClass().add("doodle-button");

                    Pet pet = getTableView().getItems().get(getIndex());

                    btnEdit.setOnAction(event -> openPetDialog(pet));
                    btnDelete.setOnAction(event -> deleteProduct(pet));

                    setGraphic(pane);
                }
            }
        });
    }

    private void loadPetsFromDatabase() {


        petList.clear();
        try (Connection conn = new DBConnection().getConn()) {
            String sql = "SELECT * FROM pets";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Pet pet = new Pet();
                    pet.setPetId(rs.getInt("pet_id"));
                    pet.setName(rs.getString("name"));
                    pet.setPetCode(rs.getString("pet_code"));
                    pet.setSpecies(rs.getString("species"));
                    pet.setBreed(rs.getString("breed"));
                    pet.setGender(rs.getString("gender"));

                    pet.setBirthdate(rs.getObject("birthdate", LocalDate.class));

                    pet.setColor(rs.getString("color"));
                    pet.setWeight(rs.getDouble("weight"));
                    pet.setImageUrl(rs.getString("image_url"));
                    pet.setHealthStatus(rs.getString("health_status"));
                    pet.setSourceType(rs.getString("source_type"));
                    pet.setPreviousOwnerId(rs.getInt("previous_owner_id"));
                    pet.setEmployeeId(rs.getString("employee_id"));

                    pet.setImportDate(rs.getObject("import_date", LocalDateTime.class));
                    pet.setPrice(rs.getDouble("price"));
                    petList.add(pet);


                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteProduct(Pet pet) {
        if (pet == null) {
            showAlert("Thông báo", "Vui lòng chọn pet để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa pet này?");
        confirm.setContentText("Mã pet: " + pet.getPetCode());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try (Connection conn = new DBConnection().getConn()) {
                    String deleteSQL = "DELETE FROM pets WHERE pet_id=?";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                        pstmt.setInt(1, pet.getPetId());
                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            showAlert("Thành công", "Xóa pet thành công.");
                            loadPetsFromDatabase();
                        } else {
                            showAlert("Lỗi", "Không tìm thấy pet để xóa.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Lỗi", "Không thể xóa pet: " + e.getMessage());
                }
            }
        });
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openPetDialog(Pet pet) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(pet == null ? "Thêm Thú Cưng" : "Chỉnh Sửa Thú Cưng");

        // Các trường nhập liệu
        TextField txtCode = new TextField();
        TextField txtName = new TextField();
        ComboBox<String> cbSpecies = new ComboBox<>();
        cbSpecies.getItems().addAll("Dog", "Cat", "Bird", "Other");
        TextField txtBreed = new TextField();
        ComboBox<String> cbGender = new ComboBox<>();
        cbGender.getItems().addAll("Male", "Female", "Other");
        DatePicker dpBirthDate = new DatePicker();
        TextField txtColor = new TextField();
        TextField txtWeight = new TextField();
        TextArea txtHealthStatus = new TextArea();
        TextField txtImage = new TextField();
        ComboBox<String> cbSourceType = new ComboBox<>();
        cbSourceType.getItems().addAll("Previous Owner", "Breeding Farm");
        TextField txtPreviousOwnerId = new TextField();
        TextField txtEmployeeId = new TextField();
        ComboBox<String> cbAdoptionStatus = new ComboBox<>();
        cbAdoptionStatus.getItems().addAll("Available", "Adopted", "Reserved");
        TextField txtPrice = new TextField();

        // Nếu có thú cưng, điền thông tin hiện tại
        if (pet != null) {
            txtCode.setText(pet.getPetCode());
            txtName.setText(pet.getName());
            cbSpecies.setValue(pet.getSpecies());
            txtBreed.setText(pet.getBreed());
            cbGender.setValue(pet.getGender());
            dpBirthDate.setValue(pet.getBirthdate());
            txtColor.setText(pet.getColor());
            txtWeight.setText(pet.getWeight() != null ? String.valueOf(pet.getWeight()) : "");
            txtHealthStatus.setText(pet.getHealthStatus());
            txtImage.setText(pet.getImageUrl());
            cbSourceType.setValue(pet.getSourceType());
            txtPreviousOwnerId.setText(pet.getPreviousOwnerId() != null ? pet.getPreviousOwnerId().toString() : "");
            txtEmployeeId.setText(pet.getEmployeeId() != null ? pet.getEmployeeId().toString() : "");
            cbAdoptionStatus.setValue(pet.getAdoptionStatus());
            txtPrice.setText(String.valueOf(pet.getPrice()));
        }

        // Nhóm 1: Thông tin cơ bản
        GridPane group1 = new GridPane();
        group1.setHgap(10);
        group1.setVgap(10);
        group1.add(new Label("Mã thú cưng:"), 0, 0); group1.add(txtCode, 1, 0);
        group1.add(new Label("Tên:"), 0, 1); group1.add(txtName, 1, 1);
        group1.add(new Label("Loài:"), 0, 2); group1.add(cbSpecies, 1, 2);
        group1.add(new Label("Giống:"), 0, 3); group1.add(txtBreed, 1, 3);

        // Nhóm 2: Giới tính, ngày sinh, màu sắc
        GridPane group2 = new GridPane();
        group2.setHgap(10);
        group2.setVgap(10);
        group2.add(new Label("Giới tính:"), 0, 0); group2.add(cbGender, 1, 0);
        group2.add(new Label("Ngày sinh:"), 0, 1); group2.add(dpBirthDate, 1, 1);
        group2.add(new Label("Màu sắc:"), 0, 2); group2.add(txtColor, 1, 2);
        group2.add(new Label("Cân nặng:"), 0, 3); group2.add(txtWeight, 1, 3);

        // Nhóm 3: Tình trạng sức khỏe, ảnh, nguồn gốc
        GridPane group3 = new GridPane();
        group3.setHgap(10);
        group3.setVgap(10);
        group3.add(new Label("Tình trạng sức khỏe:"), 0, 0); group3.add(txtHealthStatus, 1, 0);
        group3.add(new Label("Ảnh:"), 0, 1); group3.add(txtImage, 1, 1);
        group3.add(new Label("Nguồn gốc:"), 0, 2); group3.add(cbSourceType, 1, 2);

        // Nhóm 4: ID chủ cũ, ID nhân viên, tình trạng nhận nuôi
        GridPane group4 = new GridPane();
        group4.setHgap(10);
        group4.setVgap(10);
        group4.add(new Label("ID chủ cũ:"), 0, 0); group4.add(txtPreviousOwnerId, 1, 0);
        group4.add(new Label("ID nhân viên:"), 0, 1); group4.add(txtEmployeeId, 1, 1);
        group4.add(new Label("Tình trạng nhận nuôi:"), 0, 2); group4.add(cbAdoptionStatus, 1, 2);
        group4.add(new Label("Giá:"), 0, 3); group4.add(txtPrice, 1, 3);

        VBox content = new VBox(10,
                createGroup("Thông Tin Cơ Bản", group1),
                createGroup("Giới Tính & Ngày Sinh", group2),
                createGroup("Tình Trạng Sức Khỏe & Hình Ảnh", group3),
                createGroup("Thông Tin Chủ Cũ & Nhân Viên", group4)
        );
        content.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(700);
        scrollPane.setMaxHeight(700);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        dialog.getDialogPane().setContent(scrollPane);


        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/petmanagement/styles/product.css").toExternalForm());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (txtCode.getText().isEmpty() || txtName.getText().isEmpty() || cbSpecies.getValue() == null ||
                        cbGender.getValue() == null || txtPrice.getText().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
                    return null;
                }

                try (Connection conn = new DBConnection().getConn()) {
                    if (pet == null) {
                        String insertSQL = "INSERT INTO pets (pet_code, name, species, breed, gender, birthdate, color, weight, health_status, image_url, source_type, previous_owner_id, employee_id, adoption_status, price) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                            pstmt.setString(1, txtCode.getText());
                            pstmt.setString(2, txtName.getText());
                            pstmt.setString(3, cbSpecies.getValue());
                            pstmt.setString(4, txtBreed.getText());
                            pstmt.setString(5, cbGender.getValue());
                            pstmt.setDate(6, Date.valueOf(dpBirthDate.getValue()));
                            pstmt.setString(7, txtColor.getText());
                            pstmt.setDouble(8, Double.parseDouble(txtWeight.getText()));
                            pstmt.setString(9, txtHealthStatus.getText());
                            pstmt.setString(10, txtImage.getText());
                            pstmt.setString(11, cbSourceType.getValue());
                            pstmt.setInt(12, txtPreviousOwnerId.getText().isEmpty() ? null : Integer.parseInt(txtPreviousOwnerId.getText()));
                            pstmt.setString(13, txtEmployeeId.getText().isEmpty() ? null : txtEmployeeId.getText());

                            pstmt.setString(14, cbAdoptionStatus.getValue());
                            pstmt.setDouble(15, Double.parseDouble(txtPrice.getText()));
                            pstmt.executeUpdate();
                        }
                    } else {
                        String updateSQL = "UPDATE pets SET pet_code=?, name=?, species=?, breed=?, gender=?, birthdate=?, color=?, weight=?, health_status=?, image_url=?, source_type=?, previous_owner_id=?, employee_id=?, adoption_status=?, price=? WHERE pet_id=?";
                        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                            pstmt.setString(1, txtCode.getText());
                            pstmt.setString(2, txtName.getText());
                            pstmt.setString(3, cbSpecies.getValue());
                            pstmt.setString(4, txtBreed.getText());
                            pstmt.setString(5, cbGender.getValue());
                            pstmt.setDate(6, Date.valueOf(dpBirthDate.getValue()));
                            pstmt.setString(7, txtColor.getText());
                            pstmt.setDouble(8, Double.parseDouble(txtWeight.getText()));
                            pstmt.setString(9, txtHealthStatus.getText());
                            pstmt.setString(10, txtImage.getText());
                            pstmt.setString(11, cbSourceType.getValue());
                            pstmt.setInt(12, txtPreviousOwnerId.getText().isEmpty() ? null : Integer.parseInt(txtPreviousOwnerId.getText()));
                            pstmt.setString(13, txtEmployeeId.getText().isEmpty() ? null : txtEmployeeId.getText());

                            pstmt.setString(14, cbAdoptionStatus.getValue());
                            pstmt.setDouble(15, Double.parseDouble(txtPrice.getText()));
                            pstmt.setInt(16, pet.getPetId());
                            pstmt.executeUpdate();
                        }
                    }
                    loadPetsFromDatabase();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Lỗi", "Không thể lưu thú cưng: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void openPetFoodTrackingStage(String petCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petmanagement/pages/pet_food_tracking.fxml"));
            Parent root = loader.load();

            PetFoodTrackingController controller = loader.getController();
            controller.setPetCode(petCode);

            Stage stage = new Stage();
            stage.setTitle("Theo dõi thức ăn thú cưng");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private TitledPane createGroup(String title, Node content) {
        TitledPane tp = new TitledPane(title, content);
        tp.setCollapsible(false);
        return tp;
    }


    @FXML
    private void openAddPetDialog() {
        openPetDialog(null);
    }

    @FXML
    private void openEditPetDialog() {
        Pet selected = petTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một pet để chỉnh sửa.");
            return;
        }
        openPetDialog(selected);
    }
    @FXML
    private void searchPets() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            petTable.setItems(petList);
            return;
        }

        ObservableList<Pet> filteredList = FXCollections.observableArrayList();
        for (Pet pet : petList) {
            if (
                    pet.getPetCode().toLowerCase().contains(keyword) ||
                            pet.getName().toLowerCase().contains(keyword) ||
                            pet.getSpecies().toLowerCase().contains(keyword)

            ) {
                filteredList.add(pet);
            }
        }

        petTable.setItems(filteredList);
        setupActionColumn();
    }




    public List<PetFoodTracking> getByPetCode(String petCode) {
        List<PetFoodTracking> list = new ArrayList<>();
        String sql = "SELECT * FROM pet_food_tracking WHERE pet_code = ?";

        try (Connection conn = DBConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, petCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PetFoodTracking pft = new PetFoodTracking(
                        rs.getInt("food_id"),
                        rs.getString("pet_code"),
                        rs.getString("food_name"),
                        rs.getString("food_type"),
                        rs.getTimestamp("feeding_date").toLocalDateTime(),
                        rs.getString("portion_size")
                );
                list.add(pft);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}


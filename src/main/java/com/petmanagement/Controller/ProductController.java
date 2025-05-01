package com.petmanagement.controller;

import com.petmanagement.model.PetProduct;
import com.petmanagement.connection.DBConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

public class ProductController {

    @FXML
    private TableView<PetProduct> productTable;
    @FXML
    private TableColumn<PetProduct, Void> actionColumn;
    @FXML private TextField searchField;

    @FXML private TableColumn<PetProduct, Integer> idColumn;
    @FXML private TableColumn<PetProduct, String> codeColumn;
    @FXML private TableColumn<PetProduct, String> nameColumn;
    @FXML private TableColumn<PetProduct, String> categoryColumn;
    @FXML private TableColumn<PetProduct, String> brandColumn;
    @FXML private TableColumn<PetProduct, String> descriptionColumn;
    @FXML private TableColumn<PetProduct, BigDecimal> priceColumn;
    @FXML private TableColumn<PetProduct, Integer> stockColumn;
    @FXML private TableColumn<PetProduct, String> imageColumn;
    @FXML private TableColumn<PetProduct, String> supplierColumn;
    @FXML private TableColumn<PetProduct, String> importDateColumn;


    private ObservableList<PetProduct> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        codeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodeProduct()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory().toString()));
        brandColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        priceColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrice()));
        stockColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStockQuantity()));
        imageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImageUrl()));
        supplierColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSupplierName()));
        importDateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getImportDate() != null ? data.getValue().getImportDate().toLocalDateTime().toString() : ""
        ));
        imageColumn.setCellFactory(col -> new TableCell<PetProduct, String>() {
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
        setupActionColumn();
        productTable.setItems(productList);
        loadProductsFromDatabase();
        searchField.setOnKeyReleased(event -> searchProducts());

    }
    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<PetProduct, Void>() {
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

                    PetProduct product = getTableView().getItems().get(getIndex());

                    btnEdit.setOnAction(event -> openProductDialog(product));
                    btnDelete.setOnAction(event -> deleteProduct(product));

                    setGraphic(pane);
                }
            }
        });
    }

    private void loadProductsFromDatabase() {


        productList.clear();
        try (Connection conn = new DBConnection().getConn()) {
            String sql = "SELECT * FROM pet_products";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PetProduct product = new PetProduct();
                    product.setId(rs.getInt("id"));
                    product.setCodeProduct(rs.getString("code_product"));
                    product.setName(rs.getString("name"));
                    product.setCategory(PetProduct.Category.valueOf(rs.getString("category")));
                    product.setBrand(rs.getString("brand"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setStockQuantity(rs.getInt("stock_quantity"));
                    product.setImportDate(rs.getTimestamp("import_date"));
                    product.setImageUrl(rs.getString("image_url"));
                    product.setSupplierName(rs.getString("supplier_name"));
                    productList.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteProduct(PetProduct product) {
        if (product == null) {
            showAlert("Thông báo", "Vui lòng chọn một sản phẩm để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa sản phẩm này?");
        confirm.setContentText("Mã sản phẩm: " + product.getCodeProduct());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try (Connection conn = new DBConnection().getConn()) {
                    String deleteSQL = "DELETE FROM pet_products WHERE id=?";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                        pstmt.setInt(1, product.getId());
                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            showAlert("Thành công", "Xóa sản phẩm thành công.");
                            loadProductsFromDatabase();
                        } else {
                            showAlert("Lỗi", "Không tìm thấy sản phẩm để xóa.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Lỗi", "Không thể xóa sản phẩm: " + e.getMessage());
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

    private void openProductDialog(PetProduct product) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Thêm Sản Phẩm" : "Chỉnh Sửa Sản Phẩm");

        TextField txtCode = new TextField();
        TextField txtName = new TextField();
        ComboBox<PetProduct.Category> cbCat = new ComboBox<>();
        cbCat.getItems().addAll(PetProduct.Category.values());
        TextField txtBrand = new TextField();
        TextField txtPrice = new TextField();
        TextField txtStock = new TextField();
        TextArea txtDesc = new TextArea();
        TextField txtImage = new TextField();
        TextField txtSupplier = new TextField();
        DatePicker dpDate = new DatePicker();

        if (product != null) {
            txtCode.setText(product.getCodeProduct());
            txtName.setText(product.getName());
            cbCat.setValue(product.getCategory());
            txtBrand.setText(product.getBrand());
            txtPrice.setText(product.getPrice().toPlainString());
            txtStock.setText(String.valueOf(product.getStockQuantity()));
            txtDesc.setText(product.getDescription());
            txtImage.setText(product.getImageUrl());
            txtSupplier.setText(product.getSupplierName());
            if (product.getImportDate() != null) {
                dpDate.setValue(product.getImportDate().toLocalDateTime().toLocalDate());
            }
        }

        // Nhóm 1: Thông tin cơ bản
        GridPane group1 = new GridPane();
        group1.setHgap(10);
        group1.setVgap(10);
        group1.add(new Label("Mã sản phẩm:"), 0, 0); group1.add(txtCode, 1, 0);
        group1.add(new Label("Tên:"), 0, 1); group1.add(txtName, 1, 1);
        group1.add(new Label("Danh mục:"), 0, 2); group1.add(cbCat, 1, 2);

        // Nhóm 2: Giá và tồn kho
        GridPane group2 = new GridPane();
        group2.setHgap(10);
        group2.setVgap(10);
        group2.add(new Label("Thương hiệu:"), 0, 0); group2.add(txtBrand, 1, 0);
        group2.add(new Label("Giá:"), 0, 1); group2.add(txtPrice, 1, 1);
        group2.add(new Label("Tồn kho:"), 0, 2); group2.add(txtStock, 1, 2);

        GridPane group3 = new GridPane();
        group3.setHgap(10);
        group3.setVgap(10);
        group3.add(new Label("Mô tả:"), 0, 0); group3.add(txtDesc, 1, 0);
        group3.add(new Label("Ảnh URL:"), 0, 1); group3.add(txtImage, 1, 1);

        // Nhóm 4: Cung cấp & ngày nhập
        GridPane group4 = new GridPane();
        group4.setHgap(10);
        group4.setVgap(10);
        group4.add(new Label("Nhà cung cấp:"), 0, 0); group4.add(txtSupplier, 1, 0);
        group4.add(new Label("Ngày nhập:"), 0, 1); group4.add(dpDate, 1, 1);

        VBox content = new VBox(10,
                createGroup("Thông Tin Cơ Bản", group1),
                createGroup("Giá & Tồn Kho", group2),
                createGroup("Mô Tả & Hình Ảnh", group3),
                createGroup("Nhà Cung Cấp & Ngày Nhập", group4)
        );
        content.setPadding(new Insets(20));
        content.setPrefHeight(500);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/petmanagement/styles/product.css").toExternalForm());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (txtCode.getText().isEmpty() || txtName.getText().isEmpty() || cbCat.getValue() == null ||
                        txtPrice.getText().isEmpty() || txtStock.getText().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin bắt buộc!");
                    return null;
                }

                try (Connection conn = new DBConnection().getConn()) {
                    if (product == null) {
                        String insertSQL = "INSERT INTO pet_products (code_product, name, category, brand, description, price, stock_quantity, import_date, image_url, supplier_name) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                            pstmt.setString(1, txtCode.getText());
                            pstmt.setString(2, txtName.getText());
                            pstmt.setString(3, cbCat.getValue().toString());
                            pstmt.setString(4, txtBrand.getText());
                            pstmt.setString(5, txtDesc.getText());
                            pstmt.setBigDecimal(6, new BigDecimal(txtPrice.getText()));
                            pstmt.setInt(7, Integer.parseInt(txtStock.getText()));
                            pstmt.setTimestamp(8, dpDate.getValue() != null ? Timestamp.valueOf(dpDate.getValue().atStartOfDay()) : null);
                            pstmt.setString(9, txtImage.getText());
                            pstmt.setString(10, txtSupplier.getText());
                            pstmt.executeUpdate();
                        }
                    } else {
                        String updateSQL = "UPDATE pet_products SET code_product=?, name=?, category=?, brand=?, description=?, price=?, stock_quantity=?, import_date=?, image_url=?, supplier_name=? WHERE id=?";
                        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                            pstmt.setString(1, txtCode.getText());
                            pstmt.setString(2, txtName.getText());
                            pstmt.setString(3, cbCat.getValue().toString());
                            pstmt.setString(4, txtBrand.getText());
                            pstmt.setString(5, txtDesc.getText());
                            pstmt.setBigDecimal(6, new BigDecimal(txtPrice.getText()));
                            pstmt.setInt(7, Integer.parseInt(txtStock.getText()));
                            pstmt.setTimestamp(8, dpDate.getValue() != null ? Timestamp.valueOf(dpDate.getValue().atStartOfDay()) : null);
                            pstmt.setString(9, txtImage.getText());
                            pstmt.setString(10, txtSupplier.getText());
                            pstmt.setInt(11, product.getId());
                            pstmt.executeUpdate();
                        }
                    }

                    loadProductsFromDatabase();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Lỗi", "Không thể lưu sản phẩm: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private TitledPane createGroup(String title, Node content) {
        TitledPane tp = new TitledPane(title, content);
        tp.setCollapsible(false);
        return tp;
    }


    @FXML
    private void openAddProductDialog() {
        openProductDialog(null);
    }

    @FXML
    private void openEditProductDialog() {
        PetProduct selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một sản phẩm để chỉnh sửa.");
            return;
        }
        openProductDialog(selected);
    }
    @FXML
    private void searchProducts() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            productTable.setItems(productList);
            return;
        }

        ObservableList<PetProduct> filteredList = FXCollections.observableArrayList();
        for (PetProduct product : productList) {
            if (
                    product.getCodeProduct().toLowerCase().contains(keyword) ||
                            product.getName().toLowerCase().contains(keyword) ||
                            product.getCategory().toString().toLowerCase().contains(keyword) ||
                            product.getBrand().toLowerCase().contains(keyword) ||
                            product.getDescription().toLowerCase().contains(keyword) ||
                            product.getSupplierName().toLowerCase().contains(keyword)
            ) {
                filteredList.add(product);
            }
        }
        productTable.setItems(filteredList);
        setupActionColumn();

    }



}

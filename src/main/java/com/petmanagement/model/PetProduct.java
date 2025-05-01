package com.petmanagement.model;

import javafx.scene.image.ImageView;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PetProduct {

    private int id;
    private String codeProduct;
    private String name;
    private Category category;
    private String brand;
    private BigDecimal price;
    private int stockQuantity;
    private String description;
    private String imageUrl;
    private String supplierName;
    private Timestamp importDate;
    private ImageView image;



    public enum Category {
        Food, Toy, Accessory, Medicine, Other
    }



    public PetProduct(int id, String codeProduct, String name, Category category, String brand,
                      String description, BigDecimal price, int stockQuantity, Timestamp importDate,
                      String imageUrl, String supplierName) {
        this.id = id;
        this.codeProduct = codeProduct;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.importDate = importDate;
        this.imageUrl = imageUrl;
        this.supplierName = supplierName;

    }
    public PetProduct() {
    }

    // Getters và Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodeProduct() {
        return codeProduct;
    }

    public void setCodeProduct(String codeProduct) {
        this.codeProduct = codeProduct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Timestamp getImportDate() {
        return importDate;
    }

    public void setImportDate(Timestamp importDate) {
        this.importDate = importDate;
    }

    @Override
    public String toString() {
        return "PetProduct{" +
                "id=" + id +
                ", codeProduct='" + codeProduct + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", importDate=" + importDate +
                '}';
    }
}

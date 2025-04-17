package com.petmanagement.model;

import java.math.BigDecimal;

public class OrderDetail {
    private int orderDetailId;
    private String orderId; // Mã đơn hàng
    private String productType; // Loại sản phẩm (Pet, Accessory, Food)
    private String codeProduct; // Mã sản phẩm
    private String codePet; // Mã pet
    private String productName; // Tên sản phẩm
    private BigDecimal salePrice; // Giá bán
    private int quantity; // Số lượng
    private BigDecimal weight; // Trọng lượng

    // Constructor
    public OrderDetail(int orderDetailId, String orderId, String productType, String codeProduct, String codePet,
                       String productName, BigDecimal salePrice, int quantity, BigDecimal weight) {
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.productType = productType;
        this.codeProduct = codeProduct;
        this.codePet = codePet;
        this.productName = productName;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.weight = weight;
    }

    public OrderDetail(int orderDetailId, String name, double price, int quantity, double weight, String type) {
    }

    public OrderDetail(String productType, String productItem, int i) {
    }

    // Getter and Setter methods
    public int getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(int orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getCodeProduct() {
        return codeProduct;
    }

    public void setCodeProduct(String codeProduct) {
        this.codeProduct = codeProduct;
    }

    public String getCodePet() {
        return codePet;
    }

    public void setCodePet(String codePet) {
        this.codePet = codePet;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    // Calculate total price for this order detail
    public BigDecimal calculateTotalPrice() {
        return salePrice.multiply(new BigDecimal(quantity));
    }

    // Override toString() method for better display of OrderDetail
    @Override
    public String toString() {
        return "OrderDetail{" +
                "orderDetailId=" + orderDetailId +
                ", orderId='" + orderId + '\'' +
                ", productType='" + productType + '\'' +
                ", codeProduct='" + codeProduct + '\'' +
                ", codePet='" + codePet + '\'' +
                ", productName='" + productName + '\'' +
                ", salePrice=" + salePrice +
                ", quantity=" + quantity +
                ", weight=" + weight +
                '}';
    }


}


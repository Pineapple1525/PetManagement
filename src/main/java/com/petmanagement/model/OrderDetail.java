package com.petmanagement.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class OrderDetail {
    private int orderDetailId;
    private String orderId;
    private String productType;
    private String codeProduct; // Will hold food_id for Food items
    private String codePet;
    private String productName;
    private BigDecimal salePrice;
    private int quantity;
    private BigDecimal weight;
    private Map<String, Object> additionalData; // To hold additional attributes like breed

    public OrderDetail(int orderDetailId, String orderId, String productType, String codeProduct, String codePet,
                       String productName, BigDecimal salePrice, int quantity, double weight) {
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.productType = productType;
        this.codeProduct = codeProduct;
        this.codePet = codePet;
        this.productName = productName;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.weight = BigDecimal.valueOf(weight);
        this.additionalData = new HashMap<>(); // Initialize the map
    }

    // Getters and setters
    public int getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(int orderDetailId) { this.orderDetailId = orderDetailId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public String getCodeProduct() { return codeProduct; }
    public void setCodeProduct(String codeProduct) { this.codeProduct = codeProduct; }

    public String getCodePet() { return codePet; }
    public void setCodePet(String codePet) { this.codePet = codePet; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = BigDecimal.valueOf(weight); }

    public BigDecimal calculateTotalPrice() {
        return salePrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Methods to handle additional data
    public void addAdditionalData(String key, Object value) {
        additionalData.put(key, value);
    }

    public Object getAdditionalData(String key) {
        return additionalData.get(key);
    }
}
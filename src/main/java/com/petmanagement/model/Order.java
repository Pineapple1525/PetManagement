package com.petmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private String codeOrder;
    private String customerName;
    private LocalDateTime orderDate;
    private BigDecimal totalPrice;
    private String status;
    private String paymentMethod;
    private String deliveryStatus;
    private String note;

    public Order(int orderId, String codeOrder, String customerName, LocalDateTime orderDate,
                 BigDecimal totalPrice, String status, String paymentMethod, String deliveryStatus, String note) {
        this.orderId = orderId;
        this.codeOrder = codeOrder;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.deliveryStatus = deliveryStatus;
        this.note = note;
    }

    public Order(String codeOrder, String customerName, LocalDateTime orderDate, BigDecimal totalPrice,
                 String status, String paymentMethod, String deliveryStatus, String note) {
        this.orderId = 0; // Default for new orders
        this.codeOrder = codeOrder;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.deliveryStatus = deliveryStatus;
        this.note = note;
    }


    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCodeOrder() {
        return codeOrder;
    }

    public void setCodeOrder(String codeOrder) {
        this.codeOrder = codeOrder;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
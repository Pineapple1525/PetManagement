package com.petmanagement.model;

import java.time.LocalDate;

public class Customer {
    private int id;
    private String codeCustomer;
    private String name;
    private String phone;
    private String email;
    private LocalDate birthdate;
    private String address;
    private String gender;
    private String customerType;
    private double totalSpent;

    // Constructor đầy đủ tham số
    public Customer(int id, String codeCustomer, String name, String phone, String email,
                    LocalDate birthdate, String address, String gender,
                    String customerType, double totalSpent) {
        this.id = id;
        this.codeCustomer = codeCustomer;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.birthdate = birthdate;
        this.address = address;
        this.gender = gender;
        this.customerType = customerType;
        this.totalSpent = totalSpent;
    }

    // Constructor rỗng (bắt buộc để thêm mới)
    public Customer() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCodeCustomer() {
        return codeCustomer;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public String getAddress() {
        return address;
    }

    public String getGender() {
        return gender;
    }

    public String getCustomerType() {
        return customerType;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCodeCustomer(String codeCustomer) {
        this.codeCustomer = codeCustomer;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }
}

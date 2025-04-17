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
}

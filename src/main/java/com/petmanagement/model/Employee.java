package com.petmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Employee {
    private int employeeId;
    private String codeEmployee;
    private String fullname;
    private String gender;
    private LocalDate birthdate;
    private String address;
    private String phone;
    private String email;
    private String role;
    private BigDecimal salary;
    private String workShift;
    private String status;
    private LocalDateTime hiringDate;
    private String username;
    private String password;


    public Employee() {
    }

    public Employee(int employeeId, String codeEmployee, String fullname, String gender,
                    LocalDate birthdate, String address, String phone, String email,
                    String role, BigDecimal salary, String workShift, String status,
                    LocalDateTime hiringDate, String username, String password) {
        this.employeeId = employeeId;
        this.codeEmployee = codeEmployee;
        this.fullname = fullname;
        this.gender = gender;
        this.birthdate = birthdate;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.salary = salary;
        this.workShift = workShift;
        this.status = status;
        this.hiringDate = hiringDate;
        this.username = username;
        this.password = password;
    }


    // Getters and Setters

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getCodeEmployee() {
        return codeEmployee;
    }

    public void setCodeEmployee(String codeEmployee) {
        this.codeEmployee = codeEmployee;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getWorkShift() {
        return workShift;
    }

    public void setWorkShift(String workShift) {
        this.workShift = workShift;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getHiringDate() {
        return hiringDate;
    }

    public void setHiringDate(LocalDateTime hiringDate) {
        this.hiringDate = hiringDate;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}

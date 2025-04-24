package com.petmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

public class Pet {
    private int petId;
    private String name;
    private String petCode;
    private String species; // Enum: Dog, Cat, Bird, Other
    private String breed;
    private String gender; // Enum: Male, Female, Other
    private LocalDate birthdate;
    private String color;
    private BigDecimal weight;
    private String healthStatus;
    private String imageUrl;
    private String sourceType; // Enum: Previous Owner, Breeding Farm
    private Integer previousOwnerId; // Can be null
    private String breederName;
    private LocalDateTime importDate;
    private String adoptionStatus; // Enum: Available, Adopted, Reserved
    private BigDecimal price;

    // Constructors
    public Pet() {}

    public Pet(int petId, String name, String petCode, String species, String breed, String gender,
               LocalDate birthdate, String color, BigDecimal weight, String healthStatus,
               String imageUrl, String sourceType, Integer previousOwnerId, String breederName,
               LocalDateTime importDate, String adoptionStatus, BigDecimal price) {
        this.petId = petId;
        this.name = name;
        this.petCode = petCode;
        this.species = species;
        this.breed = breed;
        this.gender = gender;
        this.birthdate = birthdate;
        this.color = color;
        this.weight = weight;
        this.healthStatus = healthStatus;
        this.imageUrl = imageUrl;
        this.sourceType = sourceType;
        this.previousOwnerId = previousOwnerId;
        this.breederName = breederName;
        this.importDate = importDate;
        this.adoptionStatus = adoptionStatus;
        this.price = price;
    }

    // Getters and Setters
    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPetCode() {
        return petCode;
    }

    public void setPetCode(String petCode) {
        this.petCode = petCode;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getPreviousOwnerId() {
        return previousOwnerId;
    }

    public void setPreviousOwnerId(Integer previousOwnerId) {
        this.previousOwnerId = previousOwnerId;
    }

    public String getBreederName() {
        return breederName;
    }

    public void setBreederName(String breederName) {
        this.breederName = breederName;
    }

    public LocalDateTime getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDateTime importDate) {
        this.importDate = importDate;
    }

    public String getAdoptionStatus() {
        return adoptionStatus;
    }

    public void setAdoptionStatus(String adoptionStatus) {
        this.adoptionStatus = adoptionStatus;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

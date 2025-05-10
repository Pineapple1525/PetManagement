package com.petmanagement.model;


import java.time.LocalDateTime;

public class PetFoodTracking {
    private int foodId;
    private String petCode;
    private String foodName;
    private String foodType;
    private LocalDateTime feedingDate;
    private String portionSize;

    public PetFoodTracking(int foodId, String petCode, String foodName, String foodType,
                           LocalDateTime feedingDate, String portionSize) {
        this.foodId = foodId;
        this.petCode = petCode;
        this.foodName = foodName;
        this.foodType = foodType;
        this.feedingDate = feedingDate;
        this.portionSize = portionSize;
    }

    public int getFoodId() { return foodId; }
    public String getPetCode() { return petCode; }
    public String getFoodName() { return foodName; }
    public String getFoodType() { return foodType; }
    public LocalDateTime getFeedingDate() { return feedingDate; }
    public String getPortionSize() { return portionSize; }
}

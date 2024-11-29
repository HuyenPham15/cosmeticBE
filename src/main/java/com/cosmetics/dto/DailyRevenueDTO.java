package com.cosmetics.dto;

public class DailyRevenueDTO {
    private int day;
    private Double totalRevenue;

    public DailyRevenueDTO(int day, Double totalRevenue) {
        this.day = day;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
package com.cosmetics.dto;

public class MonthlyRevenueDTO {
    private int month;
    private Double totalRevenue;

    public MonthlyRevenueDTO(int month, Double totalRevenue) {
        this.month = month;
        this.totalRevenue = totalRevenue;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
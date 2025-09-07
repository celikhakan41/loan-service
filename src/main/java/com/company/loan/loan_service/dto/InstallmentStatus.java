package com.company.loan.loan_service.dto;

public enum InstallmentStatus {
    PAID("Payment completed"),
    OVERDUE("Payment overdue"),
    UNPAID("Payment pending");
    
    private final String description;
    
    InstallmentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
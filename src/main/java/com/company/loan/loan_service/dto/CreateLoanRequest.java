package com.company.loan.loan_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanRequest {
    
    @NotNull(message = "Customer ID cannot be null")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;
    
    @NotNull(message = "Loan amount cannot be null")
    @DecimalMin(value = "0.01", message = "Loan amount must be positive")
    @Digits(integer = 17, fraction = 2, message = "Loan amount format is invalid")
    private BigDecimal loanAmount;
    
    @NotNull(message = "Number of installments cannot be null")
    @Pattern(regexp = "^(6|9|12|24)$", message = "Number of installments must be 6, 9, 12, or 24")
    private String numberOfInstallment;
    
    @NotNull(message = "Interest rate cannot be null")
    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1")
    @DecimalMax(value = "0.5", message = "Interest rate cannot exceed 0.5")
    @Digits(integer = 1, fraction = 3, message = "Interest rate format is invalid")
    private BigDecimal interestRate;
    
    public Integer getNumberOfInstallmentAsInteger() {
        return Integer.valueOf(numberOfInstallment);
    }
}
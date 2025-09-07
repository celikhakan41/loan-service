package com.company.loan.loan_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCreditLimitRequest {
    
    @NotNull(message = "Credit limit cannot be null")
    @DecimalMin(value = "0.01", message = "Credit limit must be positive")
    @Digits(integer = 17, fraction = 2, message = "Credit limit format is invalid")
    private BigDecimal creditLimit;
}
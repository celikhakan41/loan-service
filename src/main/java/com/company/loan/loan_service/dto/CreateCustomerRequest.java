package com.company.loan.loan_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {
    
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @NotBlank(message = "Surname cannot be blank")
    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    private String surname;
    
    @NotNull(message = "Credit limit cannot be null")
    @DecimalMin(value = "0.01", message = "Credit limit must be positive")
    @Digits(integer = 17, fraction = 2, message = "Credit limit format is invalid")
    private BigDecimal creditLimit;
}
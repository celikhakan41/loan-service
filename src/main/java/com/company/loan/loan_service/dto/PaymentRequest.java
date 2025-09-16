package com.company.loan.loan_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    
    @NotNull(message = "Payment amount cannot be null")
    @DecimalMin(value = "0.01", message = "Payment amount must be positive")
    @Digits(integer = 17, fraction = 2, message = "Payment amount format is invalid")
    private BigDecimal paymentAmount;
    
    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDate paymentDate;
}
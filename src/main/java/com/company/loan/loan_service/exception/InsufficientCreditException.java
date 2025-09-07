package com.company.loan.loan_service.exception;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class InsufficientCreditException extends BusinessException {
    
    public InsufficientCreditException(BigDecimal availableCredit, BigDecimal requestedAmount) {
        super(String.format("Insufficient credit limit. Available: %s, Requested: %s", 
                           availableCredit, requestedAmount), 
              "INSUFFICIENT_CREDIT", 
              HttpStatus.BAD_REQUEST);
    }
}
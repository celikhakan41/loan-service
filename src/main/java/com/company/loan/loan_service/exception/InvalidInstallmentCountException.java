package com.company.loan.loan_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidInstallmentCountException extends BusinessException {
    
    public InvalidInstallmentCountException(Integer numberOfInstallments) {
        super(String.format("Invalid number of installments: %d. Must be 6, 9, 12, or 24", 
                           numberOfInstallments), 
              "INVALID_INSTALLMENT_COUNT", 
              HttpStatus.BAD_REQUEST);
    }
}
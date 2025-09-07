package com.company.loan.loan_service.exception;

import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends BusinessException {
    
    public CustomerNotFoundException(Long customerId) {
        super(String.format("Customer with ID %d not found", customerId), 
              "CUSTOMER_NOT_FOUND", 
              HttpStatus.NOT_FOUND);
    }
}
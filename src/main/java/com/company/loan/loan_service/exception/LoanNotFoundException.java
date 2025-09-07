package com.company.loan.loan_service.exception;

import org.springframework.http.HttpStatus;

public class LoanNotFoundException extends BusinessException {
    
    public LoanNotFoundException(Long loanId) {
        super(String.format("Loan with ID %d not found", loanId), 
              "LOAN_NOT_FOUND", 
              HttpStatus.NOT_FOUND);
    }
}
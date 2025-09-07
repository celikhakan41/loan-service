package com.company.loan.loan_service.exception;

import org.springframework.http.HttpStatus;

public class PaymentException extends BusinessException {
    
    public PaymentException(String message) {
        super(message, "PAYMENT_ERROR", HttpStatus.BAD_REQUEST);
    }
    
    public static PaymentException noInstallmentsAvailable() {
        return new PaymentException("No unpaid installments available within payment window");
    }
    
    public static PaymentException insufficientPaymentAmount() {
        return new PaymentException("Payment amount is insufficient to pay any complete installment");
    }
    
    public static PaymentException loanAlreadyPaid() {
        return new PaymentException("Loan is already fully paid");
    }

    public static PaymentException invalidPaymentDate() {
        return new PaymentException("Payment date cannot be in the future");
    }

    public static PaymentException invalidPaymentAmount() {
        return new PaymentException("Payment amount must be positive");
    }
}
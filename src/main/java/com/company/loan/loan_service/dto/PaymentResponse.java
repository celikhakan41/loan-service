package com.company.loan.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Integer installmentsPaidCount;
    private BigDecimal totalAmountSpent;
    private Boolean isLoanComplete;
    private List<InstallmentPaymentDetail> paymentDetails;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstallmentPaymentDetail {
        private Long installmentId;
        private BigDecimal originalAmount;
        private BigDecimal effectiveAmount;
        private BigDecimal discount;
        private BigDecimal penalty;
        private String paymentType; // EARLY, ON_TIME, LATE
    }
}
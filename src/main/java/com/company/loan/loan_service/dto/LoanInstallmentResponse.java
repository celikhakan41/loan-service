package com.company.loan.loan_service.dto;

import com.company.loan.loan_service.entity.LoanInstallment;
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
public class LoanInstallmentResponse {
    
    private Long id;
    private Long loanId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private Boolean isPaid;
    private BigDecimal remainingAmount;
    private InstallmentStatus status;
    
    public static LoanInstallmentResponse fromEntity(LoanInstallment installment) {
        InstallmentStatus status = calculateInstallmentStatus(installment);
        
        return LoanInstallmentResponse.builder()
                .id(installment.getId())
                .loanId(installment.getLoan().getId())
                .amount(installment.getAmount())
                .paidAmount(installment.getPaidAmount())
                .dueDate(installment.getDueDate())
                .paymentDate(installment.getPaymentDate())
                .isPaid(installment.getIsPaid())
                .remainingAmount(installment.getRemainingAmount())
                .status(status)
                .build();
    }
    
    private static InstallmentStatus calculateInstallmentStatus(LoanInstallment installment) {
        if (installment.getIsPaid()) {
            return InstallmentStatus.PAID;
        }
        
        LocalDate today = LocalDate.now();
        if (installment.getDueDate().isBefore(today)) {
            return InstallmentStatus.OVERDUE;
        } else {
            return InstallmentStatus.UNPAID;
        }
    }
}
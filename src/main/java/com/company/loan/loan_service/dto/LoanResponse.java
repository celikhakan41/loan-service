package com.company.loan.loan_service.dto;

import com.company.loan.loan_service.entity.Loan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {
    
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal loanAmount;
    private Integer numberOfInstallment;
    private LocalDate createDate;
    private Boolean isPaid;
    private List<LoanInstallmentResponse> installments;
    private BigDecimal interestRate;

    public static LoanResponse fromEntity(Loan loan) {

        LoanResponse response = LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getId())
                .customerName(loan.getCustomer().getName() + " " + loan.getCustomer().getSurname())
                .loanAmount(loan.getLoanAmount())
                .numberOfInstallment(loan.getNumberOfInstallment())
                .createDate(loan.getCreateDate())
                .isPaid(loan.getIsPaid())
                .interestRate(loan.getInterestRate())
                .build();
        
        if (loan.getInstallments() != null) {
            response.setInstallments(
                loan.getInstallments().stream()
                    .map(LoanInstallmentResponse::fromEntity)
                    .collect(Collectors.toList())
            );
        }
        
        return response;
    }
    
    public static LoanResponse fromEntityWithoutInstallments(Loan loan) {

        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getId())
                .customerName(loan.getCustomer().getName() + " " + loan.getCustomer().getSurname())
                .loanAmount(loan.getLoanAmount())
                .numberOfInstallment(loan.getNumberOfInstallment())
                .createDate(loan.getCreateDate())
                .isPaid(loan.getIsPaid())
                .interestRate(loan.getInterestRate())
                .build();
    }
}
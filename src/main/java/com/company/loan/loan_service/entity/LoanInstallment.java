package com.company.loan.loan_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_installments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanInstallment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @NotNull(message = "Loan cannot be null")
    private Loan loan;
    
    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Paid amount cannot be null")
    @DecimalMin(value = "0.0", message = "Paid amount cannot be negative")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    @NotNull(message = "Due date cannot be null")
    private LocalDate dueDate;
    
    @Column
    private LocalDate paymentDate;
    
    @Column(nullable = false)
    @NotNull(message = "Paid status cannot be null")
    private Boolean isPaid = Boolean.FALSE;
    
    @PrePersist
    protected void onCreate() {
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (isPaid == null) {
            isPaid = Boolean.FALSE;
        }
    }

    public BigDecimal getRemainingAmount() {
        return amount.subtract(paidAmount);
    }

    public long getDaysFromDueDate(LocalDate paymentDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, paymentDate);
    }
}
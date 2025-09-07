package com.company.loan.loan_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer cannot be null")
    private Customer customer;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Loan amount cannot be null")
    @DecimalMin(value = "0.01", message = "Loan amount must be positive")
    private BigDecimal loanAmount;

    @Column(nullable = false)
    @NotNull(message = "Number of installments cannot be null")
    @Min(value = 1, message = "Number of installments must be positive")
    private Integer numberOfInstallment;

    @Column(nullable = false)
    @NotNull(message = "Create date cannot be null")
    private LocalDate createDate;

    @Column(nullable = false)
    @NotNull(message = "Paid status cannot be null")
    @Builder.Default
    private Boolean isPaid = Boolean.FALSE;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    //orphanRemoval = true; because if the credit is deleted, the installments should also be cleared.
    @OrderBy("dueDate ASC")
    private List<LoanInstallment> installments;

    @Column(nullable = false, precision = 5, scale = 3)
    @NotNull(message = "Interest rate cannot be null")
    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1")
    @DecimalMax(value = "0.5", message = "Interest rate must not exceed 0.5")
    private BigDecimal interestRate;

    @PrePersist
    protected void onCreate() {
        if (createDate == null) {
            createDate = LocalDate.now();
        }
        if (isPaid == null) {
            isPaid = Boolean.FALSE;
        }
    }

    public boolean isValidInstallmentCount() {
        return numberOfInstallment != null &&
                (numberOfInstallment == 6 || numberOfInstallment == 9 ||
                        numberOfInstallment == 12 || numberOfInstallment == 24);
    }
}
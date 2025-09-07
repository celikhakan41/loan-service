package com.company.loan.loan_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "loans")
@Data
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
    
    @Column(nullable = false)
    @NotNull(message = "Loan amount cannot be null")
    private BigDecimal loanAmount;
    
    @Column(nullable = false)
    @NotNull(message = "Number of installments cannot be null")
    private Integer numberOfInstallment;
    
    @Column(nullable = false)
    @NotNull(message = "Create date cannot be null")
    private LocalDate createDate;
    
    @Column(nullable = false)
    @NotNull(message = "Paid status cannot be null")
    private Boolean isPaid = Boolean.FALSE;
    
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanInstallment> installments;
    
    @PrePersist
    protected void onCreate() {
        if (createDate == null) {
            createDate = LocalDate.now();
        }
        if (isPaid == null) {
            isPaid = Boolean.FALSE;
        }
    }
}
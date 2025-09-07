package com.company.loan.loan_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @Column(nullable = false)
    @NotBlank(message = "Surname cannot be blank")
    @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
    private String surname;
    
    @Column(nullable = false)
    @NotNull(message = "Credit limit cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Credit limit must be positive")
    private BigDecimal creditLimit;
    
    @Column(nullable = false)
    @NotNull(message = "Used credit limit cannot be null")
    @DecimalMin(value = "0.0", message = "Used credit limit cannot be negative")
    private BigDecimal usedCreditLimit = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans;
    
    public BigDecimal getAvailableCreditLimit() {
        return creditLimit.subtract(usedCreditLimit);
    }
    
    public boolean canTakeLoan(BigDecimal loanAmount) {
        return getAvailableCreditLimit().compareTo(loanAmount) >= 0;
    }
}
package com.company.loan.loan_service.config;

import com.company.loan.loan_service.entity.Customer;
import com.company.loan.loan_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final CustomerRepository customerRepository;
    
    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            initializeCustomers();
        }
    }
    
    private void initializeCustomers() {
        log.info("Initializing sample customers...");
        
        // Create sample customers for testing
        Customer customer1 = Customer.builder()
                .name("Muhammed Hakan")
                .surname("Celik")
                .creditLimit(new BigDecimal("50000.00"))
                .usedCreditLimit(BigDecimal.ZERO)
                .build();

        Customer customer2 = Customer.builder()
                .name("Jane")
                .surname("Smith")
                .creditLimit(new BigDecimal("75000.00"))
                .usedCreditLimit(BigDecimal.ZERO)
                .build();

        Customer customer3 = Customer.builder()
                .name("Bob")
                .surname("Johnson")
                .creditLimit(new BigDecimal("30000.00"))
                .usedCreditLimit(BigDecimal.ZERO)
                .build();

        
        customerRepository.save(customer1);
        customerRepository.save(customer2);
        customerRepository.save(customer3);
        
        log.info("Sample customers created successfully");
    }
}
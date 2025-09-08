package com.company.loan.loan_service.config;

import com.company.loan.loan_service.entity.Customer;
import com.company.loan.loan_service.entity.User;
import com.company.loan.loan_service.repository.CustomerRepository;
import com.company.loan.loan_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserDataInitializer {
    
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    @Profile({"dev", "test", "default"})
    public CommandLineRunner initDemoUsers() {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Initializing demo users...");
                
                // Create admin user
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .enabled(true)
                        .roles(Set.of(User.Role.ADMIN))
                        .build();
                userRepository.save(adminUser);
                log.info("Created admin user: {}", adminUser.getUsername());
                
                // Create sample customers first
                Customer customer1 = Customer.builder()
                        .name("John")
                        .surname("Doe")
                        .creditLimit(new BigDecimal("50000.00"))
                        .usedCreditLimit(BigDecimal.ZERO)
                        .build();
                customer1 = customerRepository.save(customer1);
                
                Customer customer2 = Customer.builder()
                        .name("Jane")
                        .surname("Smith")
                        .creditLimit(new BigDecimal("30000.00"))
                        .usedCreditLimit(BigDecimal.ZERO)
                        .build();
                customer2 = customerRepository.save(customer2);
                
                // Create customer users
                User customerUser1 = User.builder()
                        .username("customer1")
                        .password(passwordEncoder.encode("customer123"))
                        .enabled(true)
                        .roles(Set.of(User.Role.CUSTOMER))
                        .customerId(customer1.getId())
                        .build();
                userRepository.save(customerUser1);
                log.info("Created customer user: {} linked to customer ID: {}", 
                        customerUser1.getUsername(), customer1.getId());
                
                User customerUser2 = User.builder()
                        .username("customer2")
                        .password(passwordEncoder.encode("customer123"))
                        .enabled(true)
                        .roles(Set.of(User.Role.CUSTOMER))
                        .customerId(customer2.getId())
                        .build();
                userRepository.save(customerUser2);
                log.info("Created customer user: {} linked to customer ID: {}", 
                        customerUser2.getUsername(), customer2.getId());
                
                log.info("Demo users initialization completed!");
                log.info("Admin login: admin / admin123");
                log.info("Customer1 login: customer1 / customer123 (Customer ID: {})", customer1.getId());
                log.info("Customer2 login: customer2 / customer123 (Customer ID: {})", customer2.getId());
            } else {
                log.info("Demo users already exist, skipping initialization");
            }
        };
    }
}
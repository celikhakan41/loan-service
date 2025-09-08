package com.company.loan.loan_service.controller;

import com.company.loan.loan_service.dto.CreateLoanRequest;
import com.company.loan.loan_service.dto.PaymentRequest;
import com.company.loan.loan_service.entity.Customer;
import com.company.loan.loan_service.repository.CustomerRepository;
import com.company.loan.loan_service.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class LoanControllerIntegrationTest {
    
    @Autowired
    private WebApplicationContext context;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    private String adminToken;
    private String customerToken;
    private Customer testCustomer;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        // Create test customer
        testCustomer = Customer.builder()
                .name("John")
                .surname("Doe")
                .creditLimit(new BigDecimal("50000.00"))
                .usedCreditLimit(BigDecimal.ZERO)
                .build();
        testCustomer = customerRepository.save(testCustomer);
        
        // Generate tokens
        adminToken = "Bearer " + jwtUtil.generateToken("admin", Arrays.asList("ADMIN"), null);
        customerToken = "Bearer " + jwtUtil.generateToken("customer1", Arrays.asList("CUSTOMER"), testCustomer.getId());
    }
    
    @Test
    void createLoan_AdminWithValidRequest_ShouldCreateLoan() throws Exception {
        // Given
        CreateLoanRequest request = new CreateLoanRequest();
        request.setCustomerId(testCustomer.getId());
        request.setLoanAmount(new BigDecimal("10000.00"));
        request.setNumberOfInstallment("12");
        request.setInterestRate(new BigDecimal("0.2"));
        
        // When & Then
        mockMvc.perform(post("/api/loans")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.loanAmount").value(12000.00))
                .andExpect(jsonPath("$.numberOfInstallment").value(12))
                .andExpect(jsonPath("$.isPaid").value(false));
    }
    
    @Test
    void createLoan_CustomerRole_ShouldBeForbidden() throws Exception {
        // Given
        CreateLoanRequest request = CreateLoanRequest.builder()
                .customerId(testCustomer.getId())
                .loanAmount(new BigDecimal("10000.00"))
                .numberOfInstallment("12")
                .interestRate(new BigDecimal("0.2"))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/loans")
                .header("Authorization", customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void createLoan_InvalidInstallmentCount_ShouldReturnBadRequest() throws Exception {
        // Given

        CreateLoanRequest request = CreateLoanRequest.builder()
                .customerId(testCustomer.getId())
                .loanAmount(new BigDecimal("10000.00"))
                .numberOfInstallment("10") // Invalid
                .interestRate(new BigDecimal("0.2"))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/loans")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
    
    @Test
    void getCustomerLoans_AdminRole_ShouldReturnLoans() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/loans/{customerId}", testCustomer.getId())
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void getCustomerLoans_CustomerOwnLoans_ShouldReturnLoans() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/loans/{customerId}", testCustomer.getId())
                .header("Authorization", customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void getCustomerLoans_CustomerOtherLoans_ShouldBeForbidden() throws Exception {
        // Create another customer
        Customer otherCustomer = Customer.builder()
                .name("Jane")
                .surname("Smith")
                .creditLimit(new BigDecimal("30000.00"))
                .usedCreditLimit(BigDecimal.ZERO)
                .build();

        otherCustomer = customerRepository.save(otherCustomer);
        
        // When & Then
        mockMvc.perform(get("/api/loans/{customerId}", otherCustomer.getId())
                .header("Authorization", customerToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void createLoan_InsufficientCredit_ShouldReturnBadRequest() throws Exception {
        // Given - Set used credit limit high
        testCustomer.setUsedCreditLimit(new BigDecimal("45000.00"));
        customerRepository.save(testCustomer);
        

        CreateLoanRequest request = CreateLoanRequest.builder()
                        .customerId(testCustomer.getId())
                        .loanAmount(new BigDecimal("10000.00")) // Total would be 12000 with interest
                        .numberOfInstallment("12")
                        .interestRate(new BigDecimal("0.2"))
                        .build();
        
        // When & Then
        mockMvc.perform(post("/api/loans")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_CREDIT"));
    }
    
    @Test
    void createLoan_ValidationError_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid request (missing required fields)
        CreateLoanRequest request = CreateLoanRequest.builder()
                .customerId(null)
                .loanAmount(new BigDecimal("-5000.00"))
                .numberOfInstallment("15")
                .interestRate(new BigDecimal("0.6"))
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/loans")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors").exists());
    }
    
    @Test
    void processPayment_ValidPayment_ShouldProcessSuccessfully() throws Exception {
        // First create a loan
        CreateLoanRequest loanRequest = CreateLoanRequest.builder()
                        .customerId(testCustomer.getId())
                        .loanAmount(new BigDecimal("12000.00"))
                        .numberOfInstallment("12")
                        .interestRate(new BigDecimal("0.1")) // Minimum valid interest rate
                        .build();
        
        String loanResponse = mockMvc.perform(post("/api/loans")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        // Extract loan ID from response
        Long loanId = objectMapper.readTree(loanResponse).get("id").asLong();
        
        // Now make a payment
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentAmount(new BigDecimal("1100.00"));

        // Yesterday to avoid validation issues
        paymentRequest.setPaymentDate(LocalDate.now().minusDays(1));
        
        // When & Then
        mockMvc.perform(post("/api/loans/{loanId}/payments", loanId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installmentsPaidCount").value(1))
                .andExpect(jsonPath("$.totalAmountSpent").exists())
                .andExpect(jsonPath("$.isLoanComplete").value(false));
    }
}
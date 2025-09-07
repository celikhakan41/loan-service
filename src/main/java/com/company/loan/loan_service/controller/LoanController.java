package com.company.loan.loan_service.controller;

import com.company.loan.loan_service.dto.*;
import com.company.loan.loan_service.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Loan Management", description = "APIs for managing customer loans")
public class LoanController {
    
    private final LoanService loanService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new loan", description = "Creates a new loan for a customer after validating credit limit and business rules")
    @ApiResponse(responseCode = "201", description = "Loan created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or business rule violation")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        log.info("Received loan creation request for customer: {}", request.getCustomerId());
        
        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId == authentication.principal.customerId)")
    @Operation(summary = "Get customer loans", description = "Retrieves all loans for a specific customer with optional filters")
    @ApiResponse(responseCode = "200", description = "Loans retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<List<LoanResponse>> getCustomerLoans(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Filter by payment status") @RequestParam(required = false) Boolean isPaid,
            @Parameter(description = "Filter by number of installments") @RequestParam(required = false) Integer numberOfInstallments) {
        
        log.info("Retrieving loans for customer: {} with filters - isPaid: {}, installments: {}", 
                customerId, isPaid, numberOfInstallments);
        
        List<LoanResponse> loans = loanService.getCustomerLoans(customerId, isPaid, numberOfInstallments);
        return ResponseEntity.ok(loans);
    }
    
    @GetMapping("/{loanId}/installments")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @loanService.isLoanOwnedByCustomer(#loanId, authentication.principal.customerId))")
    @Operation(summary = "Get loan installments", description = "Retrieves all installments for a specific loan")
    @ApiResponse(responseCode = "200", description = "Installments retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public ResponseEntity<List<LoanInstallmentResponse>> getLoanInstallments(
            @Parameter(description = "Loan ID") @PathVariable Long loanId) {
        
        log.info("Retrieving installments for loan: {}", loanId);
        
        List<LoanInstallmentResponse> installments = loanService.getLoanInstallments(loanId);
        return ResponseEntity.ok(installments);
    }
    
    @PostMapping("/{loanId}/payments")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @loanService.isLoanOwnedByCustomer(#loanId, authentication.principal.customerId))")
    @Operation(summary = "Process loan payment", description = "Processes a payment for a specific loan using FIFO algorithm")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payment or business rule violation")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "Loan ID") @PathVariable Long loanId,
            @Valid @RequestBody PaymentRequest request) {
        
        log.info("Processing payment for loan: {} - amount: {}", loanId, request.getPaymentAmount());
        
        PaymentResponse response = loanService.processPayment(loanId, request);
        return ResponseEntity.ok(response);
    }
}
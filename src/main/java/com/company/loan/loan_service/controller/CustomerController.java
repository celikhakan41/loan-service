package com.company.loan.loan_service.controller;

import com.company.loan.loan_service.dto.CreateCustomerRequest;
import com.company.loan.loan_service.dto.UpdateCreditLimitRequest;
import com.company.loan.loan_service.entity.Customer;
import com.company.loan.loan_service.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {
    
    private final CustomerService customerService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new customer", description = "Creates a new customer with specified credit limit")
    @ApiResponse(responseCode = "201", description = "Customer created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        log.info("Creating customer: {} {} with credit limit: {}", 
                request.getName(), request.getSurname(), request.getCreditLimit());
        
        Customer customer = customerService.createCustomer(
            request.getName(), request.getSurname(), request.getCreditLimit());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }
    
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #customerId == authentication.principal.customerId)")
    @Operation(summary = "Get customer by ID", description = "Retrieves customer information by ID")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<Customer> getCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        
        log.info("Retrieving customer: {}", customerId);
        Customer customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(customer);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all customers", description = "Retrieves all customers (Admin only)")
    @ApiResponse(responseCode = "200", description = "Customers retrieved successfully")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        log.info("Retrieving all customers");
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }
    
    @PutMapping("/{customerId}/credit-limit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update customer credit limit", description = "Updates the credit limit for a specific customer")
    @ApiResponse(responseCode = "200", description = "Credit limit updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid credit limit")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    public ResponseEntity<Customer> updateCreditLimit(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Valid @RequestBody UpdateCreditLimitRequest request) {
        
        log.info("Updating credit limit for customer: {} to {}", customerId, request.getCreditLimit());
        Customer customer = customerService.updateCreditLimit(customerId, request.getCreditLimit());
        return ResponseEntity.ok(customer);
    }
}
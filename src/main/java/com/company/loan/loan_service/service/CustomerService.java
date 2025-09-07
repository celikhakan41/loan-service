package com.company.loan.loan_service.service;

import com.company.loan.loan_service.entity.Customer;
import com.company.loan.loan_service.exception.CustomerNotFoundException;
import com.company.loan.loan_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    public Customer createCustomer(String name, String surname, BigDecimal creditLimit) {
        log.info("Creating customer: {} {} with credit limit: {}", name, surname, creditLimit);
        
        Customer customer = new Customer();
        customer.setName(name);
        customer.setSurname(surname);
        customer.setCreditLimit(creditLimit);
        customer.setUsedCreditLimit(BigDecimal.ZERO);
        
        return customerRepository.save(customer);
    }
    
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
    
    @Transactional(readOnly = true)
    public Optional<Customer> findByNameAndSurname(String name, String surname) {
        return customerRepository.findByNameAndSurname(name, surname);
    }
    
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Customer updateCreditLimit(Long customerId, BigDecimal newCreditLimit) {
        log.info("Updating credit limit for customer {} to {}", customerId, newCreditLimit);
        
        Customer customer = getCustomerById(customerId);
        
        if (newCreditLimit.compareTo(customer.getUsedCreditLimit()) < 0) {
            throw new IllegalArgumentException("New credit limit cannot be less than used credit limit");
        }
        
        customer.setCreditLimit(newCreditLimit);
        return customerRepository.save(customer);
    }
    
    @Transactional(readOnly = true)
    public List<Customer> getCustomersOverCreditLimit() {
        return customerRepository.findCustomersOverCreditLimit();
    }
}
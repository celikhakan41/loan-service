package com.company.loan.loan_service.service;

import com.company.loan.loan_service.entity.Customer;
import com.company.loan.loan_service.exception.CustomerNotFoundException;
import com.company.loan.loan_service.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private CustomerService customerService;
    
    private Customer testCustomer;
    
    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John");
        testCustomer.setSurname("Doe");
        testCustomer.setCreditLimit(new BigDecimal("50000.00"));
        testCustomer.setUsedCreditLimit(BigDecimal.ZERO);
    }
    
    @Test
    void createCustomer_ValidData_ShouldCreateSuccessfully() {
        // Given
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        
        // When
        Customer result = customerService.createCustomer("John", "Doe", new BigDecimal("50000.00"));
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getSurname()).isEqualTo("Doe");
        assertThat(result.getCreditLimit()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(result.getUsedCreditLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        
        verify(customerRepository).save(any(Customer.class));
    }
    
    @Test
    void getCustomerById_ExistingCustomer_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        
        // When
        Customer result = customerService.getCustomerById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
    }
    
    @Test
    void getCustomerById_NonExistingCustomer_ShouldThrowException() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> customerService.getCustomerById(1L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer with ID 1 not found");
    }
    
    @Test
    void findByNameAndSurname_ExistingCustomer_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findByNameAndSurname("John", "Doe"))
                .thenReturn(Optional.of(testCustomer));
        
        // When
        Optional<Customer> result = customerService.findByNameAndSurname("John", "Doe");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John");
        assertThat(result.get().getSurname()).isEqualTo("Doe");
    }
    
    @Test
    void getAllCustomers_ShouldReturnAllCustomers() {
        // Given
        List<Customer> customers = List.of(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        
        // When
        List<Customer> result = customerService.getAllCustomers();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCustomer);
    }
    
    @Test
    void updateCreditLimit_ValidUpdate_ShouldUpdateSuccessfully() {
        // Given
        testCustomer.setUsedCreditLimit(new BigDecimal("20000.00"));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);
        
        BigDecimal newCreditLimit = new BigDecimal("75000.00");
        
        // When
        Customer result = customerService.updateCreditLimit(1L, newCreditLimit);
        
        // Then
        assertThat(result.getCreditLimit()).isEqualByComparingTo(newCreditLimit);
        verify(customerRepository).save(testCustomer);
    }
    
    @Test
    void updateCreditLimit_NewLimitBelowUsedCredit_ShouldThrowException() {
        // Given
        testCustomer.setUsedCreditLimit(new BigDecimal("30000.00"));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        
        BigDecimal newCreditLimit = new BigDecimal("25000.00"); // Less than used credit
        
        // When & Then
        assertThatThrownBy(() -> customerService.updateCreditLimit(1L, newCreditLimit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New credit limit cannot be less than used credit limit");
    }
    
    @Test
    void getCustomersOverCreditLimit_ShouldReturnOverLimitCustomers() {
        // Given
        List<Customer> overLimitCustomers = List.of(testCustomer);
        when(customerRepository.findCustomersOverCreditLimit()).thenReturn(overLimitCustomers);
        
        // When
        List<Customer> result = customerService.getCustomersOverCreditLimit();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testCustomer);
    }
}
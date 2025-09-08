package com.company.loan.loan_service.service;

import com.company.loan.loan_service.dto.CreateLoanRequest;
import com.company.loan.loan_service.dto.LoanResponse;
import com.company.loan.loan_service.dto.PaymentRequest;
import com.company.loan.loan_service.dto.PaymentResponse;
import com.company.loan.loan_service.entity.Customer;
import com.company.loan.loan_service.entity.Loan;
import com.company.loan.loan_service.entity.LoanInstallment;
import com.company.loan.loan_service.exception.CustomerNotFoundException;
import com.company.loan.loan_service.exception.InsufficientCreditException;
import com.company.loan.loan_service.exception.InvalidInstallmentCountException;
import com.company.loan.loan_service.exception.LoanNotFoundException;
import com.company.loan.loan_service.repository.CustomerRepository;
import com.company.loan.loan_service.repository.LoanInstallmentRepository;
import com.company.loan.loan_service.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    
    @Mock
    private LoanRepository loanRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private LoanInstallmentRepository installmentRepository;
    
    @InjectMocks
    private LoanService loanService;
    
    private Customer testCustomer;
    private CreateLoanRequest validLoanRequest;
    
    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John");
        testCustomer.setSurname("Doe");
        testCustomer.setCreditLimit(new BigDecimal("50000.00"));
        testCustomer.setUsedCreditLimit(BigDecimal.ZERO);
        
        validLoanRequest = new CreateLoanRequest();
        validLoanRequest.setCustomerId(1L);
        validLoanRequest.setLoanAmount(new BigDecimal("10000.00"));
        validLoanRequest.setNumberOfInstallment("12");
        validLoanRequest.setInterestRate(new BigDecimal("0.2"));
    }
    
    @Test
    void createLoan_ValidRequest_ShouldCreateLoanSuccessfully() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        
        Loan savedLoan = new Loan();
        savedLoan.setId(1L);
        savedLoan.setCustomer(testCustomer);
        savedLoan.setLoanAmount(new BigDecimal("12000.00")); // 10000 * 1.2
        savedLoan.setNumberOfInstallment(12);
        savedLoan.setCreateDate(LocalDate.now());
        savedLoan.setIsPaid(false);
        
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
        when(installmentRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        
        // When
        LoanResponse response = loanService.createLoan(validLoanRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLoanAmount()).isEqualByComparingTo(new BigDecimal("12000.00"));
        
        verify(customerRepository).save(testCustomer);
        verify(loanRepository).save(any(Loan.class));
        verify(installmentRepository).saveAll(anyList());
        
        // Verify customer's used credit limit is updated
        assertThat(testCustomer.getUsedCreditLimit()).isEqualByComparingTo(new BigDecimal("12000.00"));
    }
    
    @Test
    void createLoan_CustomerNotFound_ShouldThrowException() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> loanService.createLoan(validLoanRequest))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer with ID 1 not found");
    }
    
    @Test
    void createLoan_InvalidInstallmentCount_ShouldThrowException() {
        // Given
        validLoanRequest.setNumberOfInstallment("15"); // Invalid installment count
        
        // When & Then
        assertThatThrownBy(() -> loanService.createLoan(validLoanRequest))
                .isInstanceOf(InvalidInstallmentCountException.class)
                .hasMessage("Invalid number of installments: 15. Must be 6, 9, 12, or 24");
    }
    
    @Test
    void createLoan_InsufficientCredit_ShouldThrowException() {
        // Given
        testCustomer.setUsedCreditLimit(new BigDecimal("45000.00"));
        validLoanRequest.setLoanAmount(new BigDecimal("10000.00")); // Total would be 12000 with interest
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        
        // When & Then
        assertThatThrownBy(() -> loanService.createLoan(validLoanRequest))
                .isInstanceOf(InsufficientCreditException.class)
                .hasMessageContaining("Insufficient credit limit");
    }
    
    @Test
    void processPayment_ValidPayment_ShouldProcessSuccessfully() {
        // Given
        Loan loan = Loan.builder()
                .id(1L)
                .customer(testCustomer)
                .loanAmount(new BigDecimal("12000.00"))
                .isPaid(false)
                .build();


        LoanInstallment installment1 = LoanInstallment.builder()
                .id(1L)
                .loan(loan)
                .amount(new BigDecimal("1000.00"))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(LocalDate.now().minusDays(5))
                .isPaid(false)
                .build();
        
        List<LoanInstallment> unpaidInstallments = List.of(installment1);
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentAmount(new BigDecimal("1005.00")); // Includes penalty
        paymentRequest.setPaymentDate(LocalDate.now());
        
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(installmentRepository.findUnpaidInstallmentsWithinPaymentWindow(eq(1L), any(LocalDate.class)))
                .thenReturn(unpaidInstallments);
        when(installmentRepository.countUnpaidInstallmentsForLoan(1L)).thenReturn(0L);
        
        // When
        PaymentResponse response = loanService.processPayment(1L, paymentRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getInstallmentsPaidCount()).isEqualTo(1);
        assertThat(response.getIsLoanComplete()).isTrue();
        verify(installmentRepository).save(installment1);
        verify(loanRepository).save(loan);
    }
    
    @Test
    void processPayment_LoanNotFound_ShouldThrowException() {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentAmount(new BigDecimal("1000.00"));
        paymentRequest.setPaymentDate(LocalDate.now());
        
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> loanService.processPayment(1L, paymentRequest))
                .isInstanceOf(LoanNotFoundException.class)
                .hasMessage("Loan with ID 1 not found");
    }
    
    @Test
    void processPayment_EarlyPayment_ShouldApplyDiscount() {
        // Given
        Loan loan = Loan.builder()
                .id(1L)
                .customer(testCustomer)
                .loanAmount(new BigDecimal("12000.00"))
                .isPaid(false)
                .build();

       LoanInstallment installment = LoanInstallment.builder()
                .id(1L)
                .loan(loan)
                .amount(new BigDecimal("1000.00"))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(LocalDate.now().plusDays(10)) // Due in 10 days
                .isPaid(false)
                .build();


       PaymentRequest paymentRequest = PaymentRequest.builder()
               // Early payment: discount = 1000 * 0.001 * 10 = 10.00
               // Effective amount = 1000 - 10 = 990.00
               .paymentAmount(new BigDecimal("990.00"))
               .paymentDate(LocalDate.now()) // 10 days early
               .build();

        
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(installmentRepository.findUnpaidInstallmentsWithinPaymentWindow(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(installment));
        when(installmentRepository.countUnpaidInstallmentsForLoan(1L)).thenReturn(0L);
        
        // When
        PaymentResponse response = loanService.processPayment(1L, paymentRequest);
        
        // Then
        assertThat(response.getPaymentDetails()).hasSize(1);
        assertThat(response.getPaymentDetails().get(0).getPaymentType()).isEqualTo("EARLY");
        assertThat(response.getPaymentDetails().get(0).getDiscount()).isPositive();
    }
    
    @Test
    void getCustomerLoans_CustomerExists_ShouldReturnLoans() {
        // Given
        when(customerRepository.existsById(1L)).thenReturn(true);
        
        List<Loan> loans = List.of(
            createTestLoan(1L, new BigDecimal("10000.00"), false),
            createTestLoan(2L, new BigDecimal("15000.00"), true)
        );
        
        when(loanRepository.findByCustomerIdWithFilters(1L, null, null)).thenReturn(loans);
        
        // When
        List<LoanResponse> responses = loanService.getCustomerLoans(1L, null, null);
        
        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }
    
    @Test
    void isLoanOwnedByCustomer_ValidOwnership_ShouldReturnTrue() {
        // Given
        Loan loan = Loan.builder()
                .id(1L)
                .customer(testCustomer)
                .build();
        
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        
        // When
        boolean isOwned = loanService.isLoanOwnedByCustomer(1L, 1L);
        
        // Then
        assertThat(isOwned).isTrue();
    }
    
    @Test
    void isLoanOwnedByCustomer_InvalidOwnership_ShouldReturnFalse() {
        // Given
        Loan loan = Loan.builder()
                .id(1L)
                .customer(testCustomer)
                .build();
        
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        
        // When
        boolean isOwned = loanService.isLoanOwnedByCustomer(1L, 2L);
        
        // Then
        assertThat(isOwned).isFalse();
    }
    
    private Loan createTestLoan(Long id, BigDecimal amount, boolean isPaid) {

        return Loan.builder()
                .id(id)
                .customer(testCustomer)
                .loanAmount(amount)
                .numberOfInstallment(12)
                .createDate(LocalDate.now())
                .isPaid(isPaid)
                .build();
    }
}
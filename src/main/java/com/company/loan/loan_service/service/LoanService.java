package com.company.loan.loan_service.service;

import com.company.loan.loan_service.dto.*;
import com.company.loan.loan_service.entity.Customer;
import com.company.loan.loan_service.entity.Loan;
import com.company.loan.loan_service.entity.LoanInstallment;
import com.company.loan.loan_service.exception.*;
import com.company.loan.loan_service.repository.CustomerRepository;
import com.company.loan.loan_service.repository.LoanInstallmentRepository;
import com.company.loan.loan_service.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LoanService {
    
    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final LoanInstallmentRepository installmentRepository;
    
    public LoanResponse createLoan(CreateLoanRequest request) {
        log.info("Creating loan for customer {} with amount {}", request.getCustomerId(), request.getLoanAmount());
        
        validateInstallmentCount(request.getNumberOfInstallment());
        
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));
        
        BigDecimal totalLoanAmount = request.getLoanAmount().multiply(BigDecimal.ONE.add(request.getInterestRate()));
        
        if (!customer.canTakeLoan(totalLoanAmount)) {
            throw new InsufficientCreditException(customer.getAvailableCreditLimit(), totalLoanAmount);
        }

        Loan loan = Loan.builder()
                .customer(customer)
                .loanAmount(totalLoanAmount)
                .numberOfInstallment(request.getNumberOfInstallmentAsInteger())
                .createDate(LocalDate.now())
                .interestRate(request.getInterestRate())
                .build();
        
        loan = loanRepository.save(loan);
        
        generateInstallments(loan, totalLoanAmount);
        
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(totalLoanAmount));
        customerRepository.save(customer);
        
        log.info("Loan created successfully with ID: {}", loan.getId());
        return LoanResponse.fromEntity(loan);
    }
    
    @Transactional(readOnly = true)
    public List<LoanResponse> getCustomerLoans(Long customerId, Boolean isPaid, Integer numberOfInstallments) {
        log.info("Retrieving loans for customer {} with filters - isPaid: {}, installments: {}", 
                customerId, isPaid, numberOfInstallments);
        
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        
        List<Loan> loans = loanRepository.findByCustomerIdWithFilters(customerId, isPaid, numberOfInstallments);
        
        return loans.stream()
                .map(LoanResponse::fromEntityWithoutInstallments)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LoanInstallmentResponse> getLoanInstallments(Long loanId) {
        log.info("Retrieving installments for loan {}", loanId);
        
        if (!loanRepository.existsById(loanId)) {
            throw new LoanNotFoundException(loanId);
        }
        
        List<LoanInstallment> installments = installmentRepository.findByLoanId(loanId);
        
        return installments.stream()
                .map(LoanInstallmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public PaymentResponse processPayment(Long loanId, PaymentRequest request) {
        log.info("Processing payment for loan {} - amount: {}, date: {}", 
                loanId, request.getPaymentAmount(), request.getPaymentDate());
        
        if (request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw PaymentException.invalidPaymentAmount();
        }
        
        if (request.getPaymentDate().isAfter(LocalDate.now())) {
            throw PaymentException.invalidPaymentDate();
        }
        
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LoanNotFoundException(loanId));
        
        if (loan.getIsPaid()) {
            throw PaymentException.loanAlreadyPaid();
        }
        
        return executePaymentAlgorithm(loan, request);
    }
    
    private PaymentResponse executePaymentAlgorithm(Loan loan, PaymentRequest request) {
        // Only allow payments for installments due within 3 calendar months from payment date
        LocalDate maxPaymentWindow = request.getPaymentDate().plusMonths(2).withDayOfMonth(
            request.getPaymentDate().plusMonths(2).lengthOfMonth());
        List<LoanInstallment> unpaidInstallments = installmentRepository
            .findUnpaidInstallmentsWithinPaymentWindow(
                    loan.getId(),
                    maxPaymentWindow);
        
        if (unpaidInstallments.isEmpty()) {
            throw PaymentException.noInstallmentsAvailable();
        }
        
        // Sort by due date (FIFO - earliest first) - create new mutable list
        unpaidInstallments = new ArrayList<>(unpaidInstallments);
        unpaidInstallments.sort((i1, i2) -> i1.getDueDate().compareTo(i2.getDueDate()));
        
        BigDecimal remainingPayment = request.getPaymentAmount();
        List<PaymentResponse.InstallmentPaymentDetail> paymentDetails = new ArrayList<>();
        int installmentsPaid = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;
        
        for (LoanInstallment installment : unpaidInstallments) {
            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            BigDecimal originalAmount = installment.getRemainingAmount();
            PaymentResponse.InstallmentPaymentDetail detail = calculateInstallmentPayment(
                installment, originalAmount, request.getPaymentDate());
            
            if (remainingPayment.compareTo(detail.getEffectiveAmount()) >= 0) {
                // Can pay this installment fully
                installment.setPaidAmount(installment.getAmount());
                installment.setIsPaid(true);
                installment.setPaymentDate(request.getPaymentDate());
                
                remainingPayment = remainingPayment.subtract(detail.getEffectiveAmount());
                totalSpent = totalSpent.add(detail.getEffectiveAmount());
                installmentsPaid++;
                paymentDetails.add(detail);
                
                installmentRepository.save(installment);
                log.info("Installment {} paid fully. Effective amount: {}", 
                        installment.getId(), detail.getEffectiveAmount());
            } else {
                // Cannot pay this installment fully, stop here
                break;
            }
        }
        
        if (installmentsPaid == 0) {
            throw PaymentException.insufficientPaymentAmount();
        }
        
        // Check if loan is complete
        boolean isLoanComplete = installmentRepository.countUnpaidInstallmentsForLoan(loan.getId()) == 0;
        if (isLoanComplete) {
            loan.setIsPaid(true);
            loanRepository.save(loan);
            
            // Free up customer's credit limit
            Customer customer = loan.getCustomer();
            BigDecimal currentUsedCredit = customer.getUsedCreditLimit() != null ? customer.getUsedCreditLimit() : BigDecimal.ZERO;
            BigDecimal loanAmount = loan.getLoanAmount() != null ? loan.getLoanAmount() : BigDecimal.ZERO;
            customer.setUsedCreditLimit(currentUsedCredit.subtract(loanAmount));
            customerRepository.save(customer);
            
            log.info("Loan {} is now fully paid", loan.getId());
        }
        
        PaymentResponse response = new PaymentResponse();
        response.setInstallmentsPaidCount(installmentsPaid);
        response.setTotalAmountSpent(totalSpent);
        response.setIsLoanComplete(isLoanComplete);
        response.setPaymentDetails(paymentDetails);
        
        return response;
    }
    
    private PaymentResponse.InstallmentPaymentDetail calculateInstallmentPayment(
            LoanInstallment installment, BigDecimal originalAmount, LocalDate paymentDate) {
        
        long daysDifference = installment.getDaysFromDueDate(paymentDate);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal penalty = BigDecimal.ZERO;
        String paymentType;
        
        if (daysDifference < 0) {
            // Early payment - discount
            discount = originalAmount.multiply(BigDecimal.valueOf(Math.abs(daysDifference) * 0.001));
            paymentType = "EARLY";
        } else if (daysDifference > 0) {
            // Late payment - penalty
            penalty = originalAmount.multiply(BigDecimal.valueOf(daysDifference * 0.001));
            paymentType = "LATE";
        } else {
            // On time
            paymentType = "ON_TIME";
        }
        
        BigDecimal effectiveAmount = originalAmount.subtract(discount).add(penalty);
        
        PaymentResponse.InstallmentPaymentDetail detail = new PaymentResponse.InstallmentPaymentDetail();
        detail.setInstallmentId(installment.getId());
        detail.setOriginalAmount(originalAmount);
        detail.setEffectiveAmount(effectiveAmount.setScale(2, RoundingMode.HALF_UP));
        detail.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        detail.setPenalty(penalty.setScale(2, RoundingMode.HALF_UP));
        detail.setPaymentType(paymentType);
        
        return detail;
    }
    
    private void generateInstallments(Loan loan, BigDecimal totalAmount) {
        BigDecimal installmentAmount = totalAmount.divide(
            BigDecimal.valueOf(loan.getNumberOfInstallment()), 2, RoundingMode.HALF_UP);
        
        List<LoanInstallment> installments = new ArrayList<>();
        LocalDate currentDueDate = getFirstDayOfNextMonth(loan.getCreateDate());
        
        for (int i = 0; i < loan.getNumberOfInstallment(); i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setDueDate(currentDueDate);
            installment.setIsPaid(false);
            
            installments.add(installment);
            currentDueDate = currentDueDate.plusMonths(1);
        }
        
        installmentRepository.saveAll(installments);
        loan.setInstallments(installments);
    }
    
    private LocalDate getFirstDayOfNextMonth(LocalDate createDate) {
        return createDate.plusMonths(1).withDayOfMonth(1);
    }
    
    
    @Transactional(readOnly = true)
    public boolean isLoanOwnedByCustomer(Long loanId, Long customerId) {
        return loanRepository.findById(loanId)
            .map(loan -> loan.getCustomer().getId().equals(customerId))
            .orElse(false);
    }
    
    private void validateInstallmentCount(String numberOfInstallment) {
        if (numberOfInstallment == null) {
            return;
        }
        
        if (!numberOfInstallment.matches("^(6|9|12|24)$")) {
            throw new InvalidInstallmentCountException(Integer.valueOf(numberOfInstallment));
        }
    }
}
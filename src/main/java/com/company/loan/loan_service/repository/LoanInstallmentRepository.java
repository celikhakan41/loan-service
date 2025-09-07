package com.company.loan.loan_service.repository;

import com.company.loan.loan_service.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    
    @Query("SELECT li FROM LoanInstallment li JOIN FETCH li.loan WHERE li.loan.id = :loanId")
    List<LoanInstallment> findByLoanId(@Param("loanId") Long loanId);

    @Query("SELECT li FROM LoanInstallment li WHERE li.loan.id = :loanId AND li.isPaid = false " +
           "AND li.dueDate <= :maxDueDate ORDER BY li.dueDate ASC")
    List<LoanInstallment> findUnpaidInstallmentsWithinPaymentWindow(
        @Param("loanId") Long loanId,
        @Param("maxDueDate") LocalDate maxDueDate
    );

    @Query("SELECT COUNT(li) FROM LoanInstallment li WHERE li.loan.id = :loanId AND li.isPaid = false")
    long countUnpaidInstallmentsForLoan(@Param("loanId") Long loanId);
    
}
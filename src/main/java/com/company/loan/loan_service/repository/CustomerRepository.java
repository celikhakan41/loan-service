package com.company.loan.loan_service.repository;

import com.company.loan.loan_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByNameAndSurname(String name, String surname);

    @Query("SELECT c FROM Customer c WHERE c.usedCreditLimit > c.creditLimit")
    java.util.List<Customer> findCustomersOverCreditLimit();
}
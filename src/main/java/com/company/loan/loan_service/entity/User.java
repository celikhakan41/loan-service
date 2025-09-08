package com.company.loan.loan_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"password"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity @Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username cannot be blank")
    private String username;
    
    @Column(nullable = false)
    @NotBlank(message = "Password cannot be blank")
    private String password;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", 
                    joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    public enum Role {
        ADMIN, CUSTOMER
    }
}
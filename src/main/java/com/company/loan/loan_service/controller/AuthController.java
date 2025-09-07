package com.company.loan.loan_service.controller;

import com.company.loan.loan_service.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Generates JWT token for valid credentials")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Authentication request for user: {}", loginRequest.getUsername());

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        List<String> roles;
        Long customerId = null;
        
        // Demo users
        if ("admin".equals(username) && "admin123".equals(password)) {
            roles = Arrays.asList("ADMIN");
        } else if (username.startsWith("customer") && "customer123".equals(password)) {
            roles = Arrays.asList("CUSTOMER");
            try {
                customerId = Long.parseLong(username.substring(8));
            } catch (NumberFormatException e) {
                customerId = 1L; // Default customer ID
            }
        } else {
            log.warn("Invalid credentials for user: {}", username);
            return ResponseEntity.status(401).build();
        }
        
        String jwt = jwtUtil.generateToken(username, roles, customerId);
        
        log.info("Successfully authenticated user: {} with roles: {}", username, roles);
        return ResponseEntity.ok(new JwtResponse(jwt, username, roles, customerId));
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username cannot be blank")
        private String username;
        
        @NotBlank(message = "Password cannot be blank")
        private String password;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtResponse {
        private String token;
        private String username;
        private List<String> roles;
        private Long customerId;
    }
}
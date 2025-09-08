package com.company.loan.loan_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private String testSecret = "mySecretKeyForJWTTokenGenerationAndValidation123456789";
    private int testExpiration = 86400000; // 24 hours
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", testExpiration);
    }
    
    @Test
    void generateToken_ValidInputs_ShouldGenerateToken() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ADMIN", "USER");
        Long customerId = 1L;
        
        // When
        String token = jwtUtil.generateToken(username, roles, customerId);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }
    
    @Test
    void getUsernameFromToken_ValidToken_ShouldReturnUsername() {
        // Given
        String expectedUsername = "testuser";
        List<String> roles = Arrays.asList("ADMIN");
        String token = jwtUtil.generateToken(expectedUsername, roles, 1L);
        
        // When
        String actualUsername = jwtUtil.getUsernameFromToken(token);
        
        // Then
        assertThat(actualUsername).isEqualTo(expectedUsername);
    }
    
    @Test
    void getRolesFromToken_ValidToken_ShouldReturnRoles() {
        // Given
        String username = "testuser";
        List<String> expectedRoles = Arrays.asList("ADMIN", "CUSTOMER");
        String token = jwtUtil.generateToken(username, expectedRoles, 1L);
        
        // When
        List<String> actualRoles = jwtUtil.getRolesFromToken(token);
        
        // Then
        assertThat(actualRoles).isEqualTo(expectedRoles);
    }
    
    @Test
    void getCustomerIdFromToken_ValidToken_ShouldReturnCustomerId() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("CUSTOMER");
        Long expectedCustomerId = 123L;
        String token = jwtUtil.generateToken(username, roles, expectedCustomerId);
        
        // When
        Long actualCustomerId = jwtUtil.getCustomerIdFromToken(token);
        
        // Then
        assertThat(actualCustomerId).isEqualTo(expectedCustomerId);
    }
    
    @Test
    void getCustomerIdFromToken_NullCustomerId_ShouldReturnNull() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ADMIN");
        String token = jwtUtil.generateToken(username, roles, null);
        
        // When
        Long actualCustomerId = jwtUtil.getCustomerIdFromToken(token);
        
        // Then
        assertThat(actualCustomerId).isNull();
    }
    
    @Test
    void getExpirationDateFromToken_ValidToken_ShouldReturnExpirationDate() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ADMIN");
        Date beforeGeneration = new Date();
        String token = jwtUtil.generateToken(username, roles, 1L);
        Date afterGeneration = new Date();
        
        // When
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        
        // Then
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(beforeGeneration);
        assertThat(expirationDate).isAfter(afterGeneration);
    }
    
    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ADMIN");
        String token = jwtUtil.generateToken(username, roles, 1L);
        
        // When
        boolean isValid = jwtUtil.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    void validateToken_InvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void validateToken_NullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtil.validateToken(null);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    void isTokenExpired_ValidToken_ShouldReturnFalse() {
        // Given
        String username = "testuser";
        List<String> roles = Arrays.asList("ADMIN");
        String token = jwtUtil.generateToken(username, roles, 1L);
        
        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);
        
        // Then
        assertThat(isExpired).isFalse();
    }
    
    @Test
    void isTokenExpired_InvalidToken_ShouldReturnTrue() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isExpired = jwtUtil.isTokenExpired(invalidToken);
        
        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Given
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 1000); // 1 second expiration
        String username = "testuser";
        List<String> roles = Arrays.asList("ADMIN");
        String token = jwtUtil.generateToken(username, roles, 1L);

        // Wait for token to expire
        Thread.sleep(1500);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
    }
}
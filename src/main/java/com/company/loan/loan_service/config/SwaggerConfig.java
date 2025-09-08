package com.company.loan.loan_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server().url("http://localhost:8080").description("Local Development Server"),
                    new Server().url("https://api.loan-service.com").description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("JWT", createJWTSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }
    
    private Info apiInfo() {
        return new Info()
                .title("Loan Management API")
                .description("Spring Boot REST API for bank employees to manage customer loans with JWT authentication")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Loan Service Team")
                        .email("support@loan-service.com")
                        .url("https://loan-service.com"))
                .license(new License()
                        .name("Apache License 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }
    
    private SecurityScheme createJWTSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token for authentication. Use /api/auth/login to obtain the token.");
    }
}
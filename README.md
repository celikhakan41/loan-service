# 🏦 Spring Boot Loan Management API

A comprehensive REST API for bank employees to manage customer loans with advanced business logic, JWT authentication, and production-ready features.

## 📋 Table of Contents

- [Features](#features)
- [Technical Stack](#technical-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Business Rules](#business-rules)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Production Deployment](#production-deployment)

## ✨ Features

### Core Business Features
- **Loan Creation**: Create loans with credit limit validation and interest rate calculation
- **Payment Processing**: Complex FIFO payment algorithm with discounts/penalties
- **Installment Management**: Automatic installment generation with due date calculations
- **Customer Management**: Full customer lifecycle with credit limit management
- **Role-based Access**: ADMIN and CUSTOMER roles with different permissions

### Technical Features
- **JWT Authentication**: Secure token-based authentication
- **Input Validation**: Comprehensive Bean Validation with custom business rules
- **Exception Handling**: Global exception handler with proper HTTP status codes
- **Transaction Management**: ACID compliance for financial operations
- **API Documentation**: Interactive Swagger UI with detailed endpoint documentation
- **Comprehensive Testing**: Unit tests and integration tests with high coverage

## 🛠 Technical Stack

- **Framework**: Spring Boot 3.5.5
- **Language**: Java 17
- **Database**: H2 (development), easily configurable for PostgreSQL/MySQL
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, MockMVC, AssertJ
- **Build Tool**: Maven

## 🏗 Architecture

The application follows clean architecture principles with clear separation of concerns:

```
src/main/java/com/company/loan/loan_service/
├── config/          # Configuration classes (Security, Swagger, Data initialization)
├── controller/      # REST controllers with validation
├── dto/            # Data Transfer Objects for API requests/responses
├── entity/         # JPA entities representing domain model
├── exception/      # Custom exceptions and global exception handler
├── repository/     # Spring Data JPA repositories
├── security/       # JWT utilities and authentication filters
└── service/        # Business logic and transaction management
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd loan-service
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8081`

### Initial Setup

The application automatically creates sample customers on startup:
- Customer ID 1: Hakan Celik (Credit Limit: $100,000)
- Customer ID 2: Ricardo Quaresma (Credit Limit: $50,000)
- Customer ID 3: Admin User (Credit Limit: $1,000,000)

## 📚 API Documentation

### Swagger UI
Access the interactive API documentation at: `http://localhost:8081/swagger-ui.html`

### Database Console
Access H2 database console at: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:loandb`
- Username: `hakan`
- Password: `celik`

### Core Endpoints

#### Authentication
- `POST /api/auth/login` - Authenticate and get JWT token

#### Loan Management
- `POST /api/loans` - Create a new loan (Admin only)
- `GET /api/loans/{customerId}` - Get customer loans with filters
- `GET /api/loans/{loanId}/installments` - Get loan installments
- `POST /api/loans/{loanId}/payments` - Process loan payment

#### Customer Management
- `POST /api/customers` - Create customer (Admin only)
- `GET /api/customers/{customerId}` - Get customer details
- `PUT /api/customers/{customerId}/credit-limit` - Update credit limit (Admin only)

## 🔐 Authentication

The API uses JWT Bearer token authentication. Get a token by calling the login endpoint:

### Demo Users

**Admin User:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Customer User:**
```json
{
  "username": "customer1",
  "password": "customer123"
}
```
*Note: customer1 corresponds to Customer ID 1, customer2 to Customer ID 2, etc.*

### Using the Token

Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## 📋 Business Rules

### Loan Creation
- **Credit Limit Check**: Total loan amount (principal + interest) must not exceed available credit
- **Installment Options**: Only 6, 9, 12, or 24 installments allowed
- **Interest Rate**: Between 0.1 (10%) and 0.5 (50%)
- **Total Amount**: Principal × (1 + interest rate)

### Installment Generation
- **Equal Amounts**: Total loan amount divided equally among installments
- **Due Dates**: First installment due on 1st of next month, then monthly
- **Precision**: Financial calculations use BigDecimal for accuracy

### Payment Processing (Complex FIFO Algorithm)
1. **Payment Window**: Only installments due within 3 months can be paid
2. **FIFO Order**: Payments applied to earliest due installments first
3. **Whole Installments**: Only complete installments can be paid
4. **Early Payment Discount**: 0.1% per day before due date
5. **Late Payment Penalty**: 0.1% per day after due date

### Example Payment Calculation
```
Installment Amount: $1,000
Due Date: 2024-01-15
Payment Date: 2024-01-10 (5 days early)

Discount = $1,000 × 0.001 × 5 = $5
Effective Amount = $1,000 - $5 = $995
```

## 🗄 Database Schema

### Customer Table
```sql
customers (
  id BIGINT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  surname VARCHAR(50) NOT NULL,
  credit_limit DECIMAL(19,2) NOT NULL,
  used_credit_limit DECIMAL(19,2) NOT NULL
)
```

### Loan Table
```sql
loans (
  id BIGINT PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  loan_amount DECIMAL(19,2) NOT NULL,
  number_of_installment INTEGER NOT NULL,
  create_date DATE NOT NULL,
  is_paid BOOLEAN NOT NULL
)
```

### Loan Installment Table
```sql
loan_installments (
  id BIGINT PRIMARY KEY,
  loan_id BIGINT NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  paid_amount DECIMAL(19,2) NOT NULL,
  due_date DATE NOT NULL,
  payment_date DATE,
  is_paid BOOLEAN NOT NULL
)
```

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
```

### Test Coverage
The application includes comprehensive test coverage:
- **Unit Tests**: Business logic, security utilities, and service layer
- **Integration Tests**: Full API endpoints with authentication
- **Test Data**: Isolated test database with clean state per test

### Sample Test Scenarios
- Loan creation with various business rule violations
- Payment processing with early/late payment calculations
- Authentication and authorization edge cases
- Input validation and error handling

## 🚀 Production Deployment

### Environment Configuration

Create `application-prod.properties`:
```properties
# Database Configuration (PostgreSQL example)
spring.datasource.url=jdbc:postgresql://localhost:5432/loanservice
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# Logging
logging.level.com.company.loan.loan_service=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

### Security Considerations
1. **JWT Secret**: Use a strong, randomly generated secret key
2. **Database**: Replace H2 with production database (PostgreSQL, MySQL)
3. **HTTPS**: Enable SSL/TLS in production
4. **Rate Limiting**: Implement API rate limiting
5. **Monitoring**: Add application performance monitoring

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/loan-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📊 API Usage Examples

### 1. Authenticate
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 2. Create a Loan
```bash
curl -X POST http://localhost:8081/api/loans \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "loanAmount": 10000.00,
    "numberOfInstallment": "12",
    "interestRate": 0.2
  }'
```

### 3. Process Payment
```bash
curl -X POST http://localhost:8081/api/loans/1/payments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentAmount": 1000.00,
    "paymentDate": "2024-01-15"
  }'
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 📞 Support

For support and questions, please contact:
- Email: support@loan-service.com
- Documentation: [Swagger UI](http://localhost:8081/swagger-ui.html)

---

# Bank Account Management System

A console-based bank account management program built with Java and Oracle Database.

## Key Features

### User Management
- **User Registration**: Register new users (ID, name, password)
- **Login/Logout**: User authentication system
- **Admin Mode**: System administration features

### Account Management
- **Account Creation**: Multiple account types available
  - Checking Account (0.1% annual interest)
  - Savings Account (2.5% annual interest)
  - Fixed Deposit (3.0% annual interest)
- **Account Inquiry**: View account list and detailed information
- **Account Alias**: Custom account nicknames for user convenience

### Transaction Features
- **Deposit**: Cash deposit to accounts (Min: ₩1,000 ~ Max: ₩10,000,000)
- **Withdrawal**: Cash withdrawal from accounts (with balance verification)
- **Transfer**: Fund transfer between accounts (with transaction processing)
- **Transaction History**: View detailed transaction records by account

### Additional Features
- **Automatic Interest Calculation**: Monthly interest payment per account
- **Transaction Logging**: Detailed logging of all transactions
- **Security Features**: Account password verification, login attempt limits

## Tech Stack

- **Language**: Java
- **Database**: Oracle Database
- **JDBC**: Oracle JDBC Driver
- **Architecture**: MVC pattern-based design

## Database Design

### Table Structure

#### Users Table
```sql
- user_id (VARCHAR2): User ID (Primary Key)
- user_name (VARCHAR2): User name
- password (VARCHAR2): User password
```

#### Accounts Table
```sql
- account_id (VARCHAR2): Account number (Primary Key)
- owner_id (VARCHAR2): Account owner ID (Foreign Key)
- account_holder_name (VARCHAR2): Account holder name
- account_type (VARCHAR2): Account type
- account_password (VARCHAR2): Account password
- balance (NUMBER): Account balance
- account_alias (VARCHAR2): Account alias
- created_date (DATE): Creation date
- interest_rate (NUMBER): Interest rate
- last_interest_date (DATE): Last interest payment date
```

#### Transactions Table
```sql
- transaction_id (NUMBER): Transaction ID (Primary Key)
- account_id (VARCHAR2): Account number (Foreign Key)
- transaction_type (VARCHAR2): Transaction type
- amount (NUMBER): Transaction amount
- detail (VARCHAR2): Transaction details
- transaction_date (DATE): Transaction date
```

## Installation & Setup

### 1. Prerequisites
- Java JDK 8 or higher
- Oracle Database
- Oracle JDBC Driver

### 2. Database Setup
```sql
-- Create sequence
CREATE SEQUENCE seq_transaction_id START WITH 1 INCREMENT BY 1;

-- Create tables (refer to the structure above)
```

### 3. Update Database Connection
```java
// Modify DB connection info in BankSystem.java constructor
conn = DriverManager.getConnection(
    "jdbc:oracle:thin:@localhost:1521/orcl", // DB connection info
    "your_username",                          // DB username
    "your_password"                           // DB password
);
```

### 4. Run the Program
```bash
javac -cp ".:ojdbc8.jar" BankSystem.java
java -cp ".:ojdbc8.jar" BankSystem
```

## Usage Guide

### Admin Account
- **ID**: admin
- **Password**: password

### Menu Structure
```
0. User Registration
1. Login
2. Logout
3. Create Account
4. View Accounts
5. Deposit
6. Withdrawal
7. Transfer
8. Transaction History
9. Manual Interest Calculation (Admin only)
10. Exit
```

## Security Features

- **Login Attempt Limit**: Access blocked after 3 failed attempts
- **Account Password**: 4-digit numeric authentication
- **Owner Verification**: Prevents unauthorized account access
- **Transaction Processing**: Ensures atomicity for transfers

## Key Highlights

### Automatic Interest Calculation
- Monthly interest payment based on account creation date
- Interest calculated on the same day each month (adjusted to 28th if over 28)
- Manual interest calculation available in admin mode

### User-Friendly Interface
- Cancel any operation by typing 'q'
- Comma-separated number formatting for amounts
- Detailed guidance messages throughout

### Data Integrity
- Foreign key constraints ensure data consistency
- Transaction processing ensures transfer atomicity
- Input validation prevents invalid data entry

## Known Limitations

- Console-based interface (no GUI)
- Single user concurrent access only
- Account deletion feature not implemented
- Additional financial products (loans, credit cards) not supported

## Future Improvements

- [ ] GUI interface implementation
- [ ] Multi-threading support for concurrent access
- [ ] Account closure functionality
- [ ] Transaction search and filtering features
- [ ] Logging system implementation
- [ ] Enhanced encryption

## Developer Information

**Project Name**: Bank Account Management System  
**Development Period**: [Development Period]  
**Purpose**: Learning Java programming and database integration

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

If this project helped you, please give it a Star!

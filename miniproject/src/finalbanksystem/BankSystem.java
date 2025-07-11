package finalbanksystem;

import java.sql.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant; // Instant import 추가
import java.util.*;

public class BankSystem {
    private Scanner scanner = new Scanner(System.in);
    private Connection conn = null;
    private boolean isLoggedIn;
    private String currentUser;
    private DecimalFormat formatter;
    
    private static final double MAX_TRANSACTION_AMOUNT = 10000000;
    private static final double MIN_TRANSACTION_AMOUNT = 1000;

    public BankSystem() {
        try {
            // JDBC Driver 등록
            Class.forName("oracle.jdbc.OracleDriver");
            
            // DB 연결 (본인의 DB 정보로 수정)
            conn = DriverManager.getConnection(
    				"jdbc:oracle:thin:@localhost:1521/orcl", // oracle 접속정보 
    				"jhw1",		    // oracle 본인계정 이름 
    				"1234"			// oracle 본인계정 암호
    			);
            
            isLoggedIn = false;
            currentUser = null;
            formatter = new DecimalFormat("#,###");
            
            System.out.println("데이터베이스 연결 성공!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("데이터베이스 연결 실패!");
            exit();
        }
    }

    // =================================================================
    // 기본 유틸리티 메소드들
    // =================================================================
    
    private boolean isValidAmount(double amount) {
        return amount > 0 && amount <= MAX_TRANSACTION_AMOUNT;
    }

    private boolean isValidAccountPassword(String password) {
        return password != null && password.length() == 4 && password.matches("\\d{4}");
    }

    private boolean isValidUserId(String userId) {
        return userId != null && userId.trim().length() >= 3 && userId.trim().length() <= 20;
    }

    private boolean isValidName(String name) {
        return name != null && name.trim().length() > 0 && name.trim().length() <= 20;
    }

    private String formatAmount(double amount) {
        return formatter.format(amount);
    }

    private boolean isAccountOwner(String accountId) {
        if (!isUserLoggedIn()) return false;
        Account account = findAccountById(accountId);
        if (account == null) return false;
        return account.getOwnerId().equals(currentUser);
    }

    public boolean isAdminLoggedIn() {
        return isLoggedIn && "admin".equals(currentUser);
    }

    public boolean isUserLoggedIn() {
        return isLoggedIn && !"admin".equals(currentUser);
    }

    // DB에서 계좌 조회
    public Account findAccountById(String id) {
        try {
            String sql = "SELECT * FROM accounts WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Account account = new Account();
                account.setId(rs.getString("account_id"));
                account.setOwnerId(rs.getString("owner_id"));
                account.setAccountHolderName(rs.getString("account_holder_name"));
                account.setAccountType(rs.getString("account_type"));
                account.setAccountPassword(rs.getString("account_password"));
                account.setBalance(rs.getDouble("balance"));
                account.setAccountAlias(rs.getString("account_alias"));
                account.setCreatedDate(rs.getDate("created_date"));
                account.setInterestRate(rs.getDouble("interest_rate"));
                account.setLastInterestDate(rs.getDate("last_interest_date"));
                
                rs.close();
                pstmt.close();
                return account;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // DB에서 현재 사용자 이름 조회
    private String getCurrentUserName() {
        try {
            String sql = "SELECT user_name FROM users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentUser);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("user_name");
                rs.close();
                pstmt.close();
                return name;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // =================================================================
    // 사용자 관리 메소드들
    // =================================================================

    // 회원가입
    public void registerUser(Scanner scanner) {
        System.out.println("=== 회원가입 ===");

        String userId;
        while (true) {
            System.out.print("아이디 (3-20자) (또는 'q'를 입력하면 취소): ");
            userId = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(userId)) {
                System.out.println("회원가입이 취소되었습니다.");
                return;
            }

            if (!isValidUserId(userId)) {
                System.out.println("아이디는 3-20자로 입력해주세요.");
                continue;
            }

            // DB에서 중복 체크
            if (isUserIdExists(userId)) {
                System.out.println("이미 존재하는 아이디입니다. 다른 아이디를 입력해주세요.");
                continue;
            }
            
            break;
        }

        String name;
        while (true) {
            System.out.print("이름 (또는 'q'를 입력하면 취소): ");
            name = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(name)) {
                System.out.println("회원가입이 취소되었습니다.");
                return;
            }
            
            if (isValidName(name)) {
                break;
            }
            System.out.println("올바른 이름을 입력해주세요.");
        }

        String password;
        while (true) {
            System.out.print("비밀번호 (최소 4자) (또는 'q'를 입력하면 취소): ");
            password = scanner.nextLine();
            
            if ("q".equalsIgnoreCase(password)) {
                System.out.println("회원가입이 취소되었습니다.");
                return;
            }
            
            if (password.length() >= 4) {
                break;
            }
            System.out.println("비밀번호는 최소 4자 이상이어야 합니다.");
        }

        // DB에 사용자 등록
        try {
            String sql = "INSERT INTO users (user_id, user_name, password) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, password);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("회원가입이 완료되었습니다!");
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("회원가입 중 오류가 발생했습니다.");
        }
    }

    // 사용자 ID 중복 체크
    private boolean isUserIdExists(String userId) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                boolean exists = rs.getInt(1) > 0;
                rs.close();
                pstmt.close();
                return exists;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 로그인
    public boolean login(String userId, String password) {
        try {
            if ("admin".equals(userId) && "password".equals(password)) {
                isLoggedIn = true;
                currentUser = "admin";
                return true;
            }
            
            String sql = "SELECT password FROM users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (dbPassword.equals(password)) {
                    isLoggedIn = true;
                    currentUser = userId;
                    rs.close();
                    pstmt.close();
                    return true;
                }
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void logout() {
        isLoggedIn = false;
        currentUser = null;
        System.out.println("로그아웃 되었습니다.");
    }

    // =================================================================
    // 계좌 관리 메소드들
    // =================================================================

    // 계좌 생성
    public void createAccount(Scanner scanner) {
        if (!isUserLoggedIn()) {
            System.out.println("로그인 후 이용해주세요.");
            return;
        }

        System.out.println("=== 계좌 개설 ===");

        // 계좌주 실명 입력 (현재 로그인한 사용자와 일치해야 함)
        String accountHolderName = inputAccountHolderName(scanner);
        if (accountHolderName == null) {
            System.out.println("계좌 개설이 취소되었습니다.");
            return;
        }
        
        // 계좌 유형 선택
        AccountTypeInfo accountInfo = selectAccountType(scanner);
        if (accountInfo == null) {
            System.out.println("계좌 개설이 취소되었습니다.");
            return;
        }
        
        // 계좌 비밀번호 설정
        String accountPassword = inputAccountPassword(scanner);
        if (accountPassword == null) {
            System.out.println("계좌 개설이 취소되었습니다.");
            return;
        }
        
        // 초기 입금액 설정
        Double initialBalance = inputInitialBalance(scanner);
        if (initialBalance == null) {
            System.out.println("계좌 개설이 취소되었습니다.");
            return;
        }
        
        // 계좌 별명 설정
        String accountAlias = inputAccountAlias(scanner, accountInfo.type, accountHolderName);

        // DB에 계좌 생성
        try {
            String accountId = generateAccountId();
            String sql = "INSERT INTO accounts (account_id, owner_id, account_holder_name, " +
                        "account_type, account_password, balance, account_alias, interest_rate) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accountId);
            pstmt.setString(2, currentUser);
            pstmt.setString(3, accountHolderName);
            pstmt.setString(4, accountInfo.type);
            pstmt.setString(5, accountPassword);
            pstmt.setDouble(6, initialBalance);
            pstmt.setString(7, accountAlias);
            pstmt.setDouble(8, accountInfo.interestRate);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                recordTransaction(accountId, "개설", initialBalance, "계좌 개설 시 초기 입금");
                
                // 생성된 계좌 정보 출력
                Account newAccount = findAccountById(accountId);
                printAccountCreationResult(newAccount);
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("계좌 개설 중 오류가 발생했습니다.");
        }
    }

    // 계좌번호 생성
    private String generateAccountId() {
        String bankCode = "123";
        String branchCode = "456";
        int accountNumber = 100000;

        String newAccountId;
        do {
            accountNumber++;
            String accountDigits = String.format("%06d", accountNumber);
            newAccountId = bankCode + "-" + branchCode + "-" + accountDigits;
        } while (findAccountById(newAccountId) != null);

        return newAccountId;
    }

    // 거래내역 기록
    private void recordTransaction(String accountId, String type, double amount, String detail) {
        try {
            String sql = "INSERT INTO transactions (transaction_id, account_id, transaction_type, " +
                        "amount, detail) VALUES (seq_transaction_id.NEXTVAL, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accountId);
            pstmt.setString(2, type);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, detail);
            
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =================================================================
    // 입력 처리 메소드들 (기존과 동일)
    // =================================================================
    
    private String inputAccountHolderName(Scanner scanner) {
        String userRealName = getCurrentUserName();
        
        while (true) {
            System.out.print("계좌주 실명 (또는 'q'를 입력하면 취소): ");
            String accountHolderName = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(accountHolderName)) {
                return null;
            }

            if (!isValidName(accountHolderName)) {
                System.out.println("올바른 이름을 입력해주세요.");
                continue;
            }

            if (userRealName.equals(accountHolderName)) {
                return accountHolderName;
            } else {
                System.out.println("계좌주 실명이 회원가입시 등록한 이름과 일치하지 않습니다.");
                System.out.println("회원가입 이름: " + userRealName);
                System.out.println("다시 정확히 입력해주세요.");
            }
        }
    }

    private AccountTypeInfo selectAccountType(Scanner scanner) {
        System.out.println("\n계좌 유형을 선택하세요:");
        System.out.println("1. 자유입출금통장 (연 0.1%)");
        System.out.println("2. 적금통장 (연 2.5%)");
        System.out.println("3. 정기예금 (연 3.0%)");

        while (true) {
            System.out.print("선택 (1-3) (또는 'q'를 입력하면 취소): ");
            String input = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(input)) {
                return null;
            }
            
            try {
                int typeChoice = Integer.parseInt(input);
                switch (typeChoice) {
                    case 1: return new AccountTypeInfo("자유입출금통장", 0.1);
                    case 2: return new AccountTypeInfo("적금통장", 2.5);
                    case 3: return new AccountTypeInfo("정기예금", 3.0);
                    default: System.out.println("1, 2, 3 중에서 선택해주세요.");
                }
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    private static class AccountTypeInfo {
        String type;
        double interestRate;
        
        AccountTypeInfo(String type, double interestRate) {
            this.type = type;
            this.interestRate = interestRate;
        }
    }

    private String inputAccountPassword(Scanner scanner) {
        while (true) {
            System.out.print("계좌 비밀번호 (4자리 숫자) (또는 'q'를 입력하면 취소): ");
            String accountPassword = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(accountPassword)) {
                return null;
            }
            
            if (isValidAccountPassword(accountPassword)) {
                return accountPassword;
            }
            System.out.println("4자리 숫자로 입력해주세요.");
        }
    }

    private Double inputInitialBalance(Scanner scanner) {
        while (true) {
            System.out.print("초기 입금액 (최소 1,000원, 최대 " + formatAmount(MAX_TRANSACTION_AMOUNT) + "원) (또는 'q'를 입력하면 취소): ");
            String input = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(input)) {
                return null;
            }
            
            try {
                double initialBalance = Double.parseDouble(input);
                
                if (initialBalance >= 1000 && isValidAmount(initialBalance)) {
                    return initialBalance;
                } else {
                    System.out.println("1,000원 이상 " + formatAmount(MAX_TRANSACTION_AMOUNT) + "원 이하로 입력해주세요.");
                }
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    private String inputAccountAlias(Scanner scanner, String accountType, String accountHolderName) {
        System.out.print("계좌 별명을 설정하시겠습니까? (Y/N): ");
        String aliasChoice = scanner.nextLine().trim().toUpperCase();

        if ("Y".equals(aliasChoice)) {
            System.out.print("계좌 별명을 입력하세요: ");
            String accountAlias = scanner.nextLine().trim();
            return accountAlias.isEmpty() ? accountType + "_" + accountHolderName : accountAlias;
        } else {
            return accountType + "_" + accountHolderName;
        }
    }

    private void printAccountCreationResult(Account newAccount) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("           계좌 개설 완료");
        System.out.println("=".repeat(40));
        System.out.println("계좌번호: " + newAccount.getId());
        System.out.println("계좌주: " + newAccount.getAccountHolderName());
        System.out.println("계좌유형: " + newAccount.getAccountType());
        System.out.println("연 이자율: " + newAccount.getInterestRate() + "%");
        System.out.println("계좌별명: " + newAccount.getAccountAlias());
        System.out.println("초기잔액: " + formatAmount(newAccount.getBalance()) + "원");
        System.out.println("개설일자: " + newAccount.getCreatedDate());
        System.out.println("=".repeat(40));
    }

    // =================================================================
    // 계좌 조회 메소드
    // =================================================================
    
    private void viewAccountInfo(Scanner scanner) {
        if (!isUserLoggedIn()) {
            System.out.println("로그인 후 이용해주세요.");
            return;
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.println("                  내 계좌 목록");
        System.out.println("=".repeat(50));

        try {
            String sql = "SELECT * FROM accounts WHERE owner_id = ? ORDER BY created_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentUser);
            
            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            
            while (rs.next()) {
                System.out.println("계좌번호: " + rs.getString("account_id"));
                System.out.println("계좌주: " + rs.getString("account_holder_name"));
                System.out.println("계좌유형: " + rs.getString("account_type"));
                System.out.println("계좌별명: " + rs.getString("account_alias"));
                System.out.println("잔액: " + formatAmount(rs.getDouble("balance")) + "원");
                System.out.println("연 이자율: " + rs.getDouble("interest_rate") + "%");
                System.out.println("개설일자: " + rs.getDate("created_date"));
                System.out.println("마지막 이자 지급일: " + rs.getDate("last_interest_date"));
                System.out.println("-".repeat(50));
                found = true;
            }

            if (!found) {
                System.out.println("개설된 계좌가 없습니다.");
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        System.out.println("=".repeat(50));
    }

    // =================================================================
    // 거래 기능 메소드들
    // =================================================================

    // 입금
    private void deposit(Scanner scanner) {
        System.out.println("=== 입금 ===");
        
        Account account = inputAndVerifyAccount(scanner, "입금");
        if (account == null) {
            System.out.println("입금이 취소되었습니다.");
            return;
        }

        if (!inputAndVerifyPassword(scanner, account)) {
            System.out.println("입금이 취소되었습니다.");
            return;
        }

        Double amount = inputTransactionAmount(scanner, "입금", 0);
        if (amount == null) {
            System.out.println("입금이 취소되었습니다.");
            return;
        }
        
        // DB에서 잔액 업데이트
        try {
            String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, amount);
            pstmt.setString(2, account.getId());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                recordTransaction(account.getId(), "입금", amount, "직접 입금");
                account.setBalance(account.getBalance() + amount); // 객체도 업데이트
                System.out.println("입금이 완료되었습니다. 현재 잔액: " + formatAmount(account.getBalance()) + "원");
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("입금 처리 중 오류가 발생했습니다.");
        }
    }

    // 출금
    private void withdraw(Scanner scanner) {
        System.out.println("=== 출금 ===");
        
        Account account = inputAndVerifyAccount(scanner, "출금");
        if (account == null) {
            System.out.println("출금이 취소되었습니다.");
            return;
        }

        if (!inputAndVerifyPassword(scanner, account)) {
            System.out.println("출금이 취소되었습니다.");
            return;
        }

        Double amount = inputTransactionAmount(scanner, "출금", account.getBalance());
        if (amount == null) {
            System.out.println("출금이 취소되었습니다.");
            return;
        }
        
        // DB에서 잔액 업데이트
        try {
            String sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, amount);
            pstmt.setString(2, account.getId());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                recordTransaction(account.getId(), "출금", amount, "직접 출금");
                account.setBalance(account.getBalance() - amount); // 객체도 업데이트
                System.out.println("출금이 완료되었습니다. 현재 잔액: " + formatAmount(account.getBalance()) + "원");
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("출금 처리 중 오류가 발생했습니다.");
        }
    }

    // 이체
    private void transfer(Scanner scanner) {
        System.out.println("=== 이체 ===");
        
        Account fromAccount = inputAndVerifyAccount(scanner, "이체");
        if (fromAccount == null) {
            System.out.println("이체가 취소되었습니다.");
            return;
        }

        if (!inputAndVerifyPassword(scanner, fromAccount)) {
            System.out.println("이체가 취소되었습니다.");
            return;
        }

        Account toAccount = inputTargetAccount(scanner, fromAccount.getId());
        if (toAccount == null) {
            System.out.println("이체가 취소되었습니다.");
            return;
        }

        Double amount = inputTransactionAmount(scanner, "이체", fromAccount.getBalance());
        if (amount == null) {
            System.out.println("이체가 취소되었습니다.");
            return;
        }

        // DB에서 트랜잭션 처리
        try {
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            // 출금 계좌에서 차감
            String withdrawSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            PreparedStatement withdrawStmt = conn.prepareStatement(withdrawSql);
            withdrawStmt.setDouble(1, amount);
            withdrawStmt.setString(2, fromAccount.getId());
            withdrawStmt.executeUpdate();
            
            // 입금 계좌에 추가
            String depositSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            PreparedStatement depositStmt = conn.prepareStatement(depositSql);
            depositStmt.setDouble(1, amount);
            depositStmt.setString(2, toAccount.getId());
            depositStmt.executeUpdate();
            
            // 거래내역 기록
            recordTransaction(fromAccount.getId(), "이체", amount, 
                            "-> " + toAccount.getId() + " (" + toAccount.getAccountHolderName() + ")");
            recordTransaction(toAccount.getId(), "이체", amount, 
                            "<- " + fromAccount.getId() + " (" + fromAccount.getAccountHolderName() + ")");
            
            conn.commit(); // 트랜잭션 커밋
            conn.setAutoCommit(true); // 자동 커밋 복원
            
            // 객체도 업데이트
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);
            
            System.out.println("이체가 완료되었습니다.");
            System.out.println("출금 계좌(" + fromAccount.getId() + ") 잔액: " + formatAmount(fromAccount.getBalance()) + "원");
            System.out.println("입금 계좌(" + toAccount.getId() + ") 잔액: " + formatAmount(toAccount.getBalance()) + "원");
            
            withdrawStmt.close();
            depositStmt.close();
            
        } catch (SQLException e) {
            try {
                conn.rollback(); // 롤백
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            System.out.println("이체 처리 중 오류가 발생했습니다.");
        }
    }

    // 거래내역 조회
    private void viewTransactionHistory(Scanner scanner) {
        System.out.println("=== 거래내역 조회 ===");
        
        Account account = inputAndVerifyAccount(scanner, "거래내역 조회");
        if (account == null) {
            System.out.println("거래내역 조회가 취소되었습니다.");
            return;
        }

        if (!inputAndVerifyPassword(scanner, account)) {
            System.out.println("거래내역 조회가 취소되었습니다.");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("                     거래내역");
        System.out.println("=".repeat(60));
        System.out.println("계좌번호: " + account.getId() + " | 현재 잔액: " + formatAmount(account.getBalance()) + "원");
        System.out.println("계좌주: " + account.getAccountHolderName() + " | 계좌별명: " + account.getAccountAlias());
        System.out.println("-".repeat(60));

        try {
            String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, account.getId());
            
            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            
            while (rs.next()) {
                Transaction transaction = new Transaction(
                    rs.getInt("transaction_id"),
                    rs.getString("account_id"),
                    rs.getString("transaction_type"),
                    rs.getDouble("amount"),
                    rs.getString("detail"),
                    rs.getDate("transaction_date")
                );
                System.out.println(transaction);
                found = true;
            }

            if (!found) {
                System.out.println("해당 계좌의 거래내역이 없습니다.");
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        System.out.println("=".repeat(60));
    }

    // =================================================================
    // 입력 처리 유틸리티 메소드들
    // =================================================================
    
    private int getValidMenuChoice(Scanner scanner) {
        while (true) {
            System.out.print("\n메뉴를 선택하세요: ");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                return choice;
            } catch (InputMismatchException e) {
                System.out.println("숫자만 입력해주세요.");
                scanner.nextLine();
            }
        }
    }

    private Account inputAndVerifyAccount(Scanner scanner, String actionName) {
        while (true) {
            System.out.print("계좌번호 (또는 'q'를 입력하면 메인메뉴로): ");
            String accountId = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(accountId)) {
                return null;
            }
            
            if (accountId.isEmpty()) {
                System.out.println("계좌번호를 입력해주세요.");
                continue;
            }
            
            if (!isAccountOwner(accountId)) {
                System.out.println("본인 계좌만 " + actionName + " 가능합니다.");
                System.out.println("다시 입력해주세요.");
                continue;
            }

            Account account = findAccountById(accountId);
            if (account == null) {
                System.out.println("계좌를 찾을 수 없습니다.");
                System.out.println("계좌번호를 확인하고 다시 입력해주세요.");
                continue;
            }

            return account;
        }
    }

    private boolean inputAndVerifyPassword(Scanner scanner, Account account) {
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;
        
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("계좌 비밀번호 (4자리) (또는 'q'를 입력하면 취소): ");
            String password = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(password)) {
                return false;
            }
            
            if (account.checkPassword(password)) {
                return true;
            }
            
            attempts++;
            System.out.println("계좌 비밀번호가 틀렸습니다. (" + attempts + "/" + MAX_ATTEMPTS + ")");
            
            if (attempts >= MAX_ATTEMPTS) {
                System.out.println("비밀번호 입력 횟수를 초과했습니다. 보안을 위해 취소됩니다.");
                return false;
            }
        }
        return false;
    }

    private Account inputTargetAccount(Scanner scanner, String fromAccountId) {
        while (true) {
            System.out.print("입금 계좌번호 (또는 'q'를 입력하면 취소): ");
            String toAccountId = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(toAccountId)) {
                return null;
            }
            
            if (toAccountId.isEmpty()) {
                System.out.println("계좌번호를 입력해주세요.");
                continue;
            }
            
            if (fromAccountId.equals(toAccountId)) {
                System.out.println("동일한 계좌로는 이체할 수 없습니다.");
                System.out.println("다른 계좌번호를 입력해주세요.");
                continue;
            }
            
            Account toAccount = findAccountById(toAccountId);
            if (toAccount == null) {
                System.out.println("입금 계좌를 찾을 수 없습니다.");
                System.out.println("계좌번호를 확인하고 다시 입력해주세요.");
                continue;
            }
            
            return toAccount;
        }
    }

    private Double inputTransactionAmount(Scanner scanner, String transactionType, double currentBalance) {
        while (true) {
            System.out.print(transactionType + " 금액 (" + formatAmount(MIN_TRANSACTION_AMOUNT) + "원 ~ "
                    + formatAmount(MAX_TRANSACTION_AMOUNT) + "원) (또는 'q'를 입력하면 취소): ");
            String input = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(input)) {
                return null;
            }
            
            try {
                double amount = Double.parseDouble(input);

                if (amount < MIN_TRANSACTION_AMOUNT) {
                    System.out.println("최소 " + transactionType + " 금액은 " + formatAmount(MIN_TRANSACTION_AMOUNT) + "원입니다.");
                    continue;
                }

                if (!isValidAmount(amount)) {
                    System.out.println("최대 " + transactionType + " 금액은 " + formatAmount(MAX_TRANSACTION_AMOUNT) + "원입니다.");
                    continue;
                }

                if ((transactionType.equals("출금") || transactionType.equals("이체")) && currentBalance < amount) {
                    System.out.println("잔액이 부족합니다. 현재 잔액: " + formatAmount(currentBalance) + "원");
                    continue;
                }

                return amount;
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
            }
        }
    }

    // =================================================================
    // 이자 계산 메소드
    // =================================================================
    
    public void calculateAllInterests() {
        LocalDate today = LocalDate.now();
        boolean hasInterestPayment = false;

        System.out.println("=== 이자 계산 시스템 시작 (계좌별 개설일 기준) ===");

        try {
            String sql = "SELECT * FROM accounts";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String accountId = rs.getString("account_id");
                Date lastInterestDate = rs.getDate("last_interest_date");
                Date createdDate = rs.getDate("created_date");
                double balance = rs.getDouble("balance");
                double interestRate = rs.getDouble("interest_rate");
                
                // java.sql.Date를 Instant로 변환하는 부분 수정
                LocalDate lastInterestLocalDate = Instant.ofEpochMilli(lastInterestDate.getTime())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                
                int accountInterestDay = getAccountInterestDay(createdDate);
                LocalDate checkDate = lastInterestLocalDate.plusMonths(1).withDayOfMonth(accountInterestDay);

                while (!checkDate.isAfter(today)) {
                    double interest = balance * (interestRate / 100.0 / 12.0);
                    if (interest > 0) {
                        // DB에서 잔액과 마지막 이자 지급일 업데이트
                        String updateSql = "UPDATE accounts SET balance = balance + ?, last_interest_date = ? WHERE account_id = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                        updateStmt.setDouble(1, interest);
                        updateStmt.setDate(2, java.sql.Date.valueOf(checkDate));
                        updateStmt.setString(3, accountId);
                        updateStmt.executeUpdate();
                        updateStmt.close();

                        String detail = String.format("월 이자 지급 (이자율: %.1f%%, 지급일: 매월 %d일)", 
                                                     interestRate, accountInterestDay);
                        recordTransaction(accountId, "이자", interest, detail);

                        System.out.println("[" + accountId + "] " + checkDate + " 이자 지급: " + 
                                         formatAmount(interest) + "원 (매월 " + accountInterestDay + "일)");
                        hasInterestPayment = true;
                        
                        balance += interest; // 다음 계산을 위해 잔액 업데이트
                    }
                    checkDate = checkDate.plusMonths(1);
                }
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!hasInterestPayment) {
            System.out.println("지급할 이자가 없습니다.");
        }
        System.out.println("=== 이자 계산 완료 ===\n");
    }

    private int getAccountInterestDay(Date createdDate) {
        // java.sql.Date를 Instant로 변환하는 부분 수정
        LocalDate createdLocalDate = Instant.ofEpochMilli(createdDate.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDate();
        int createdDay = createdLocalDate.getDayOfMonth();
        return (createdDay > 28) ? 28 : createdDay;
    }

    // =================================================================
    // 메뉴 및 실행 관련 메소드들
    // =================================================================
    
    public void runMenu() {
        System.out.println("=== 은행 시스템을 시작합니다 ===");

        while (true) {
            showMenu();
            int choice = getValidMenuChoice(scanner);
            handleMenuChoice(choice, scanner);
        }
    }

    private void showMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("           은행 시스템 메뉴");
        System.out.println("=".repeat(40));

        if (isLoggedIn) {
            if (isAdminLoggedIn()) {
                System.out.println("관리자 모드 - " + currentUser);
            } else {
                String userName = getCurrentUserName();
                System.out.println("사용자 모드 - " + userName + "님");
            }
        } else {
            System.out.println("로그인이 필요합니다");
        }

        System.out.println("=".repeat(40));
        System.out.println("0. 회원가입");
        System.out.println("1. 로그인");
        System.out.println("2. 로그아웃");
        System.out.println("3. 계좌 개설");
        System.out.println("4. 계좌 조회");
        System.out.println("5. 입금");
        System.out.println("6. 출금");
        System.out.println("7. 이체");
        System.out.println("8. 거래내역 조회");
        
        if (isAdminLoggedIn()) {
            System.out.println("9. 수동 이자 계산 (관리자)");
            System.out.println("10. 종료");
        } else {
            System.out.println("9. 종료");
        }
        System.out.println("=".repeat(40));
        System.out.println("※ 작업 중 'q'를 입력하면 해당 작업을 취소하고 메인메뉴로 돌아갑니다.");
    }

    private void handleMenuChoice(int choice, Scanner scanner) {
        switch (choice) {
            case 0 -> registerUser(scanner);
            case 1 -> handleLogin(scanner);
            case 2 -> handleLogout();
            case 3 -> createAccount(scanner);
            case 4 -> viewAccountInfo(scanner);
            case 5 -> handleUserAction(scanner, this::deposit, "입금");
            case 6 -> handleUserAction(scanner, this::withdraw, "출금");
            case 7 -> handleUserAction(scanner, this::transfer, "이체");
            case 8 -> handleUserAction(scanner, this::viewTransactionHistory, "거래내역 조회");
            case 9 -> handleExitOrAdminAction(scanner);
            case 10 -> handleAdminExit(scanner);
            default -> System.out.println("잘못된 번호입니다. 다시 선택해주세요.");
        }
    }

    private interface UserAction {
        void execute(Scanner scanner);
    }

    private void handleUserAction(Scanner scanner, UserAction action, String actionName) {
        if (isUserLoggedIn()) {
            action.execute(scanner);
        } else {
            System.out.println("사용자 로그인 후 " + actionName + "을 이용해주세요.");
        }
    }

    private void handleLogin(Scanner scanner) {
        if (isLoggedIn) {
            System.out.println("이미 로그인되어 있습니다. 먼저 로그아웃해주세요.");
            return;
        }

        System.out.println("=== 로그인 ===");
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;
        
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("아이디 (또는 'q'를 입력하면 취소): ");
            String id = scanner.nextLine().trim();
            
            if ("q".equalsIgnoreCase(id)) {
                System.out.println("로그인이 취소되었습니다.");
                return;
            }
            
            System.out.print("비밀번호: ");
            String pw = scanner.nextLine();

            if (login(id, pw)) {
                System.out.println("로그인에 성공했습니다!");
                if (isAdminLoggedIn()) {
                    System.out.println("관리자 모드로 접속했습니다.");
                } else {
                    System.out.println("환영합니다, " + getCurrentUserName() + "님!");
                }
                return;
            } else {
                attempts++;
                System.out.println("아이디 또는 비밀번호가 틀렸습니다. (" + attempts + "/" + MAX_ATTEMPTS + ")");
                
                if (attempts >= MAX_ATTEMPTS) {
                    System.out.println("로그인 시도 횟수를 초과했습니다. 보안을 위해 메인메뉴로 돌아갑니다.");
                    return;
                }
                
                System.out.print("다시 시도하시겠습니까? (Y/N): ");
                String retry = scanner.nextLine().trim().toUpperCase();
                if (!"Y".equals(retry)) {
                    System.out.println("로그인이 취소되었습니다.");
                    return;
                }
            }
        }
    }

    private void handleLogout() {
        if (isLoggedIn) {
            logout();
        } else {
            System.out.println("로그인되어 있지 않습니다.");
        }
    }

    private void handleExitOrAdminAction(Scanner scanner) {
        if (isAdminLoggedIn()) {
            manualInterestCalculation();
        } else {
            handleExit(scanner);
        }
    }

    private void handleExit(Scanner scanner) {
        System.out.print("정말 종료하시겠습니까? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if ("Y".equals(confirm)) {
            closeConnection();
            System.out.println("은행 시스템을 종료합니다. 감사합니다!");
            System.exit(0);
        }
    }

    private void handleAdminExit(Scanner scanner) {
        if (isAdminLoggedIn()) {
            System.out.print("정말 종료하시겠습니까? (Y/N): ");
            String confirm = scanner.nextLine().trim().toUpperCase();
            if ("Y".equals(confirm)) {
                closeConnection();
                System.out.println("관리자 모드를 종료합니다.");
                System.exit(0);
            }
        } else {
            System.out.println("잘못된 번호입니다. 다시 선택해주세요.");
        }
    }

    private void manualInterestCalculation() {
        if (!isAdminLoggedIn()) {
            System.out.println("관리자만 사용할 수 있습니다.");
            return;
        }
        
        System.out.println("수동 이자 계산을 시작합니다...");
        calculateAllInterests();
    }

    private void exit() {
        closeConnection();
        System.out.println("시스템을 종료합니다.");
        System.exit(0);
    }

    private void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("데이터베이스 연결이 종료되었습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =================================================================
    // 메인 메소드
    // =================================================================
    
    public static void main(String[] args) {
        BankSystem bankSystem = new BankSystem();

        System.out.println("=== 은행 시스템 초기화 ===");
        bankSystem.calculateAllInterests();

        bankSystem.runMenu();
    }
}
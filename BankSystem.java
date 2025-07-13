package banksystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.function.Predicate;

public class BankSystem {
    private Scanner scanner = new Scanner(System.in);
    private Connection conn = null;
    private String loginId = null;
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");
    
    public BankSystem() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521/orcl",
                "jhw1", "1234"
            );
            System.out.println("은행 시스템 DB 연결 성공!");
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
    }
    
    // 🔧 금액 포맷팅 메소드
    private String formatCurrency(double amount) {
        return currencyFormat.format(amount) + "원";
    }
    
    // 🔧 공통 입력 처리 메소드들
    private String inputWithValidation(String prompt, Predicate<String> validator) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine();
        } while (!validator.test(input));
        return input;
    }
    
    private double inputAmount(String prompt) {
        double amount;
        do {
            try {
                System.out.print(prompt);
                amount = Double.parseDouble(scanner.nextLine());
                if (amount <= 0) {
                    System.out.println("❌ 금액은 0원보다 커야 합니다.");
                    continue;
                }
                if (validateAmount(amount)) break;
            } catch (NumberFormatException e) {
                System.out.println("❌ 올바른 숫자를 입력해주세요.");
            }
        } while (true);
        return amount;
    }
    
    private String inputValidAccountId(String prompt) {
        return inputWithValidation(prompt, accountId -> {
            if (accountId.trim().isEmpty()) {
                System.out.println("❌ 계좌번호를 입력해주세요.");
                return false;
            }
            if (!checkAccountExists(accountId)) {
                System.out.println("❌ 존재하지 않는 계좌번호입니다.");
                return false;
            }
            return true;
        });
    }
    
    private boolean inputAndVerifyAccountPassword(String accountId) {
        return inputWithValidation("계좌 비밀번호 (4자리): ", password -> {
            if (!validateAccountPassword(password)) return false;
            if (!verifyAccountPassword(accountId, password)) {
                System.out.println("❌ 계좌 비밀번호가 일치하지 않습니다.");
                return false;
            }
            return true;
        }) != null;
    }
    
    // 🔧 DB 작업을 위한 유틸리티 메소드
    private void handleDatabaseError(String operation, SQLException e) {
        System.out.println("❌ " + operation + " 처리 중 오류가 발생했습니다: " + e.getMessage());
    }
    
    private String generateAccountName(String accountType, String userId) {
        String sql = "SELECT user_name FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return accountType + " 계좌_" + rs.getString("user_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accountType + " 계좌_" + userId;
    }
    
    private String selectAccountType() {
        String[] accountTypes = {"보통예금", "정기예금", "적금"};
        String[] descriptions = {
            "자유입출금, 일상거래용 [연 0.1%]",
            "목돈 예치, 높은 이자  [연 1.5%]",
            "매월 저축, 목돈 만들기 [연 2.0%]"
        };
        
        System.out.println("\n[계좌 종류 선택]");
        System.out.println("---------------------------------------");
        for (int i = 0; i < accountTypes.length; i++) {
            System.out.println((i + 1) + ". " + accountTypes[i] + " - " + descriptions[i]);
        }
        System.out.println("---------------------------------------");
        
        String choice = inputWithValidation("계좌 종류를 선택하세요 (1-3): ", input -> {
            try {
                int choiceNum = Integer.parseInt(input);
                if (choiceNum >= 1 && choiceNum <= 3) {
                    return true;
                } else {
                    System.out.println("❌ 1번에서 3번 사이의 번호를 선택해주세요.");
                    return false;
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ 올바른 숫자를 입력해주세요.");
                return false;
            }
        });
        
        return accountTypes[Integer.parseInt(choice) - 1];
    }
    
    // 🔧 유효성 검사 메소드들
    private boolean validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            System.out.println("❌ 아이디를 입력해주세요.");
            return false;
        }
        if (userId.length() > 8) {
            System.out.println("❌ 아이디는 8자리까지만 가능합니다.");
            return false;
        }
        if (!userId.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$")) {
            System.out.println("❌ 아이디는 영문과 숫자가 모두 포함되어야 합니다.");
            return false;
        }
        return true;
    }
    
    private boolean validatePassword(String password, String userId) {
        if (password == null || password.trim().isEmpty()) {
            System.out.println("❌ 비밀번호를 입력해주세요.");
            return false;
        }
        if (password.length() < 7 || password.length() > 12) {
            System.out.println("❌ 비밀번호는 7~12자리여야 합니다.");
            return false;
        }
        if (userId != null && password.equals(userId)) {
            System.out.println("❌ 비밀번호는 아이디와 같을 수 없습니다.");
            return false;
        }
        return true;
    }
    
    private boolean validateAccountPassword(String accountPassword) {
        if (accountPassword == null || accountPassword.trim().isEmpty()) {
            System.out.println("❌ 계좌 비밀번호를 입력해주세요.");
            return false;
        }
        if (accountPassword.length() != 4) {
            System.out.println("❌ 계좌 비밀번호는 4자리여야 합니다.");
            return false;
        }
        if (!accountPassword.matches("^[0-9]+$")) {
            System.out.println("❌ 계좌 비밀번호는 숫자만 입력 가능합니다.");
            return false;
        }
        return true;
    }
    
    private boolean validateUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            System.out.println("❌ 이름을 입력해주세요.");
            return false;
        }
        if (userName.length() > 20) {
            System.out.println("❌ 이름은 20자리까지만 가능합니다.");
            return false;
        }
        return true;
    }
    
    private boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("❌ 이메일을 입력해주세요.");
            return false;
        }
        if (email.length() > 100) {
            System.out.println("❌ 이메일은 100자리까지만 가능합니다.");
            return false;
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            System.out.println("❌ 올바른 이메일 형식이 아닙니다. (예: user@domain.com)");
            return false;
        }
        String[] commonDomains = {".com", ".net", ".org", ".edu", ".gov", ".co.kr", ".kr"};
        for (String domain : commonDomains) {
            if (email.toLowerCase().endsWith(domain)) return true;
        }
        System.out.println("❌ 일반적인 도메인을 사용해주세요. (.com, .net, .org, .kr 등)");
        return false;
    }
    
    private boolean validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            System.out.println("❌ 전화번호를 입력해주세요.");
            return false;
        }
        if (!phone.matches("^010-\\d{4}-\\d{4}$")) {
            System.out.println("❌ 전화번호는 010-0000-0000 형식으로 입력해주세요.");
            return false;
        }
        String middlePart = phone.substring(4, 8);
        if (Integer.parseInt(middlePart) < 1000) {
            System.out.println("❌ 유효하지 않은 전화번호입니다. (010-1000-0000 이상이어야 합니다)");
            return false;
        }
        return true;
    }
    
    private boolean validateAccountName(String accountName) {
        if (accountName == null || accountName.trim().isEmpty()) {
            System.out.println("❌ 계좌명을 입력해주세요.");
            return false;
        }
        if (accountName.length() > 30) {
            System.out.println("❌ 계좌명은 30자리까지만 가능합니다.");
            return false;
        }
        return true;
    }
    
    private boolean validateAmount(double amount) {
        if (amount < 1000) {
            System.out.println("❌ 금액은 1000원 이상이어야 합니다.");
            return false;
        }
        if (amount > 999999999) {
            System.out.println("❌ 금액이 너무 큽니다. (최대 9억원)");
            return false;
        }
        return true;
    }
    
    // 🔧 DB 확인 메소드들
    private boolean checkDuplicateUserId(String userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("❌ 이미 존재하는 아이디입니다.");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private boolean verifyAccountPassword(String accountId, String inputPassword) {
        String sql = "SELECT account_password FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("account_password");
                    return dbPassword != null && dbPassword.equals(inputPassword);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean checkAccountExists(String accountId) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean isOwnerAccount(String accountId) {
        String sql = "SELECT user_id FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String accountUserId = rs.getString("user_id");
                    return loginId != null && loginId.equals(accountUserId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private String generateAccountNumber() {
        String sql = "SELECT SEQ_ACCOUNT.NEXTVAL FROM DUAL";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                int seqNum = rs.getInt(1);
                return "110-234-" + String.format("%06d", seqNum);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 🔧 메인 비즈니스 로직 메소드들
    private void accountList() {
        if (loginId == null) {
            System.out.println("\n=== 은행 계좌 관리 시스템 ===");
            System.out.println("계좌 서비스를 이용하려면 로그인해주세요.");
            mainMenu();
            return;
        }
        
        System.out.println("\n[계좌 목록] 사용자: " + loginId);
        System.out.println("-----------------------------------------------------------------------");
        System.out.printf("%-20s%-15s%-15s%-15s%-20s\n", "계좌번호", "계좌명", "계좌종류", "소유자", "잔액");
        System.out.println("-----------------------------------------------------------------------");
        
        String sql = "SELECT a.account_id, a.account_name, a.account_type, a.balance, u.user_name " +
                     "FROM accounts a JOIN users u ON a.user_id = u.user_id WHERE a.user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loginId);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasAccounts = false;
                while (rs.next()) {
                    hasAccounts = true;
                    System.out.printf("%-20s%-15s%-15s%-15s%-20s\n",
                        rs.getString("account_id"),
                        rs.getString("account_name"),
                        rs.getString("account_type"),
                        rs.getString("user_name"),
                        formatCurrency(rs.getDouble("balance")));
                }
                if (!hasAccounts) {
                    System.out.println("보유하신 계좌가 없습니다. 계좌를 생성해보세요!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            exit();
        }
        mainMenu();
    }
    
    private void mainMenu() {
        System.out.println("\n-----------------------------------------------------------------------");
        if (loginId == null) {
            System.out.println("메인메뉴: 1.회원가입 | 2.로그인 | 3.종료");
            System.out.print("메뉴선택: ");
            
            String menuNo = scanner.nextLine();
            switch (menuNo) {
                case "1" -> join();
                case "2" -> login();
                case "3" -> exit();
                default -> {
                    System.out.println("***1번에서 3번의 숫자만 입력이 가능합니다. 다시 입력해 주세요***");
                    mainMenu();
                }
            }
        } else {
            System.out.println("메인메뉴: 1.계좌생성 | 2.계좌조회 | 3.입금 | 4.출금 | 5.이체 | 6.계좌비밀번호변경 | 7.계좌삭제 | 8.로그아웃 | 9.종료");
            System.out.print("메뉴선택: ");
            
            String menuNo = scanner.nextLine();
            switch (menuNo) {
                case "1" -> createAccount();
                case "2" -> readAccount();
                case "3" -> deposit();
                case "4" -> withdraw();
                case "5" -> transfer();
                case "6" -> changeAccountPassword();
                case "7" -> deleteAccount();
                case "8" -> logout();
                case "9" -> exit();
                default -> {
                    System.out.println("***1번에서 9번의 숫자만 입력이 가능합니다. 다시 입력해 주세요***");
                    mainMenu();
                }
            }
        }
    }
    
    private void logout() {
        loginId = null;
        System.out.println("로그아웃되었습니다.");
        accountList();
    }
    
    private void login() {
        System.out.println("[로그인]");
        
        String userId = inputWithValidation("아이디: ", input -> {
            if (input.trim().isEmpty()) {
                System.out.println("❌ 아이디를 입력해주세요.");
                return false;
            }
            return validateUserId(input);
        });
        
        String userPassword = inputWithValidation("비밀번호: ", input -> {
            if (input.trim().isEmpty()) {
                System.out.println("❌ 비밀번호를 입력해주세요.");
                return false;
            }
            return true;
        });
        
        if (printSubMenu().equals("1")) {
            String sql = "SELECT user_password FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String dbPassword = rs.getString("user_password");
                        if (dbPassword != null && dbPassword.equals(userPassword)) {
                            loginId = userId;
                            System.out.println("✅ 로그인 성공!");
                        } else {
                            System.out.println("❌ 비밀번호가 일치하지 않습니다.");
                        }
                    } else {
                        System.out.println("❌ 아이디가 존재하지 않습니다.");
                    }
                }
            } catch (SQLException e) {
                handleDatabaseError("로그인", e);
            }
        }
        accountList();
    }
    
    private void join() {
        System.out.println("[회원가입]");
        
        String userId = inputWithValidation("아이디 (8자리 이하, 영문+숫자 모두 포함): ", 
            input -> validateUserId(input) && checkDuplicateUserId(input));
        
        String userName = inputWithValidation("이름 (20자리 이하): ", this::validateUserName);
        
        String userPassword = inputWithValidation("비밀번호 (7~12자리, 아이디와 달라야 함): ", 
            input -> validatePassword(input, userId));
        
        String userEmail = inputWithValidation("이메일 (필수, 예: user@domain.com): ", this::validateEmail);
        
        String userPhone = inputWithValidation("전화번호 (필수, 010-0000-0000 형식): ", this::validatePhone);
        
        if (printSubMenu().equals("1")) {
            String sql = "INSERT INTO users (user_id, user_name, user_password, user_email, user_phone, join_date) VALUES (?, ?, ?, ?, ?, SYSDATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, userName);
                pstmt.setString(3, userPassword);
                pstmt.setString(4, userEmail);
                pstmt.setString(5, userPhone);
                
                if (pstmt.executeUpdate() > 0) {
                    System.out.println("✅ 회원가입이 완료되었습니다!");
                }
            } catch (SQLException e) {
                handleDatabaseError("회원가입", e);
            }
        }
        accountList();
    }
    
    private void createAccount() {
        if (loginId == null) {
            System.out.println("❌ 계좌 생성은 로그인 후 이용 가능합니다.");
            mainMenu();
            return;
        }
        
        System.out.println("[계좌 생성]");
        
        String accountType = selectAccountType();
        String accountName = generateAccountName(accountType, loginId);
        System.out.println("계좌명: " + accountName + " (자동 생성)");
        
        String accountPassword = inputWithValidation("계좌 비밀번호 (4자리 숫자): ", this::validateAccountPassword);
        double initialBalance = inputAmount("초기 입금액 (1000원 이상): ");
        
        if (printSubMenu().equals("1")) {
            String accountId = generateAccountNumber();
            String sql = "INSERT INTO accounts (account_id, account_name, account_type, account_password, balance, user_id, create_date) VALUES (?, ?, ?, ?, ?, ?, SYSDATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, accountId);
                pstmt.setString(2, accountName);
                pstmt.setString(3, accountType);
                pstmt.setString(4, accountPassword);
                pstmt.setDouble(5, initialBalance);
                pstmt.setString(6, loginId);
                
                if (pstmt.executeUpdate() > 0) {
                    System.out.println("✅ 계좌가 성공적으로 생성되었습니다!");
                    System.out.println("   계좌번호: " + accountId);
                    System.out.println("   계좌명: " + accountName);
                    System.out.println("   계좌종류: " + accountType);
                    System.out.println("   ⚠️  계좌 비밀번호를 잊지 마세요!");
                }
            } catch (SQLException e) {
                handleDatabaseError("계좌 생성", e);
            }
        }
        accountList();
    }
    
    private void readAccount() {
        System.out.println("[계좌 조회]");
        System.out.print("계좌번호: ");
        String accountId = scanner.nextLine();
        
        String sql = "SELECT account_id, account_name, account_type, balance, user_id, create_date FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String userId = rs.getString("user_id");
                    
                    System.out.println("##############");
                    System.out.println("계좌번호: " + rs.getString("account_id"));
                    System.out.println("계좌명: " + rs.getString("account_name"));
                    System.out.println("계좌종류: " + rs.getString("account_type"));
                    System.out.println("잔액: " + formatCurrency(rs.getDouble("balance")));
                    System.out.println("소유자: " + userId);
                    System.out.println("개설일: " + rs.getDate("create_date"));
                    System.out.println("----------------------------------");
                    
                    if (loginId != null && userId.equals(loginId)) {
                    	System.out.println("보조메뉴: 1.삭제 | 2.목록");
                        System.out.print("메뉴선택: ");
                        String menuNo = scanner.nextLine();
                        
                        if (menuNo.equals("1")) {
                            deleteAccountById(accountId);
                        }
                    }
                } else {
                    System.out.println("해당 계좌를 찾을 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            exit();
        }
        accountList();
    }
    
    private void deposit() {
        System.out.println("[입금]");
        
        String accountId = inputValidAccountId("계좌번호: ");
        
        // 본인 계좌인지 확인
        if (isOwnerAccount(accountId)) {
            // 본인 계좌면 비밀번호 확인 필요
            System.out.println("💳 본인 계좌 입금 - 계좌 비밀번호 확인이 필요합니다.");
            if (!inputAndVerifyAccountPassword(accountId)) {
                accountList();
                return;
            }
            System.out.println("✅ 본인 계좌 확인 완료");
        } else {
            // 타인 계좌면 비밀번호 불필요
            System.out.println("📝 타인 계좌 입금 (계좌 비밀번호 불필요)");
        }
        
        double amount = inputAmount("입금액: ");
        
        if (printSubMenu().equals("1")) {
            String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, accountId);
                
                if (pstmt.executeUpdate() > 0) {
                    System.out.println("✅ 입금이 완료되었습니다!");
                    System.out.println("   입금액: " + formatCurrency(amount));
                    System.out.println("   입금계좌: " + accountId);
                    
                    // 입금 후 잔액 확인 (본인 계좌일 때만)
                    if (isOwnerAccount(accountId)) {
                        double newBalance = getCurrentBalance(accountId);
                        if (newBalance != -1) {
                            System.out.println("   현재잔액: " + formatCurrency(newBalance));
                        }
                    }
                } else {
                    System.out.println("❌ 입금 처리 중 오류가 발생했습니다.");
                }
            } catch (SQLException e) {
                handleDatabaseError("입금", e);
            }
        }
        accountList();
    }
    
    private void withdraw() {
        System.out.println("[출금]");
        
        String accountId = inputValidAccountId("계좌번호: ");
        if (!inputAndVerifyAccountPassword(accountId)) {
            accountList();
            return;
        }
        
        double amount = inputAmount("출금액: ");
        
        if (printSubMenu().equals("1")) {
            processWithdrawal(accountId, amount);
        }
        accountList();
    }
    
    private void processWithdrawal(String accountId, double amount) {
        double currentBalance = getCurrentBalance(accountId);
        if (currentBalance == -1) {
            System.out.println("❌ 해당 계좌를 찾을 수 없습니다.");
            return;
        }
        
        if (currentBalance < amount) {
            System.out.println("❌ 잔액이 부족합니다. (현재 잔액: " + formatCurrency(currentBalance) + ")");
            return;
        }
        
        if (updateBalance(accountId, -amount)) {
            System.out.println("✅ 출금이 완료되었습니다!");
            System.out.println("   출금액: " + formatCurrency(amount));
            System.out.println("   잔여잔액: " + formatCurrency(currentBalance - amount));
        }
    }
    
    private double getCurrentBalance(String accountId) {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            handleDatabaseError("잔액 조회", e);
        }
        return -1; // 계좌를 찾을 수 없음을 나타냄
    }
    
    private boolean updateBalance(String accountId, double amountChange) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amountChange);
            pstmt.setString(2, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleDatabaseError("잔액 업데이트", e);
            return false;
        }
    }
    
    private void transfer() {
        System.out.println("[이체]");
        
        String fromAccountId = inputValidAccountId("출금 계좌번호: ");
        if (!inputAndVerifyAccountPassword(fromAccountId)) {
            accountList();
            return;
        }
        
        String toAccountId = inputWithValidation("입금 계좌번호: ", accountId -> {
            if (accountId.trim().isEmpty()) {
                System.out.println("❌ 입금 계좌번호를 입력해주세요.");
                return false;
            }
            if (!checkAccountExists(accountId)) {
                System.out.println("❌ 존재하지 않는 입금 계좌번호입니다.");
                return false;
            }
            if (fromAccountId.equals(accountId)) {
                System.out.println("❌ 출금 계좌와 입금 계좌가 같을 수 없습니다.");
                return false;
            }
            return true;
        });
        
        double amount = inputAmount("이체금액: ");
        
        if (printSubMenu().equals("1")) {
            try {
                conn.setAutoCommit(false);
                
                String checkSql = "SELECT balance FROM accounts WHERE account_id = ?";
                try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                    checkPstmt.setString(1, fromAccountId);
                    try (ResultSet rs = checkPstmt.executeQuery()) {
                        if (rs.next()) {
                            double currentBalance = rs.getDouble("balance");
                            if (currentBalance >= amount) {
                                String withdrawSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
                                String depositSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
                                
                                try (PreparedStatement withdrawPstmt = conn.prepareStatement(withdrawSql);
                                     PreparedStatement depositPstmt = conn.prepareStatement(depositSql)) {
                                    
                                    withdrawPstmt.setDouble(1, amount);
                                    withdrawPstmt.setString(2, fromAccountId);
                                    withdrawPstmt.executeUpdate();
                                    
                                    depositPstmt.setDouble(1, amount);
                                    depositPstmt.setString(2, toAccountId);
                                    int depositRows = depositPstmt.executeUpdate();
                                    
                                    if (depositRows > 0) {
                                        conn.commit();
                                        System.out.println("✅ 이체가 완료되었습니다!");
                                        System.out.println("   이체금액: " + formatCurrency(amount));
                                        System.out.println("   출금계좌: " + fromAccountId);
                                        System.out.println("   입금계좌: " + toAccountId);
                                    } else {
                                        conn.rollback();
                                        System.out.println("❌ 입금 계좌를 찾을 수 없습니다.");
                                    }
                                }
                            } else {
                                System.out.println("❌ 잔액이 부족합니다. (현재 잔액: " + formatCurrency(currentBalance) + ")");
                            }
                        } else {
                            System.out.println("❌ 출금 계좌를 찾을 수 없습니다.");
                        }
                    }
                }
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                handleDatabaseError("이체", e);
            }
        }
        accountList();
    }
    
    private void changeAccountPassword() {
        System.out.println("[계좌 비밀번호 변경]");
        
        String accountId = inputWithValidation("계좌번호: ", input -> {
            if (input.trim().isEmpty()) {
                System.out.println("❌ 계좌번호를 입력해주세요.");
                return false;
            }
            if (!checkAccountExists(input)) {
                System.out.println("❌ 존재하지 않는 계좌번호입니다.");
                return false;
            }
            if (!isOwnerAccount(input)) {
                System.out.println("❌ 본인 소유의 계좌만 비밀번호를 변경할 수 있습니다.");
                return false;
            }
            return true;
        });
        
        if (inputAndVerifyAccountPassword(accountId)) {
            String newAccountPassword = inputWithValidation("새 계좌 비밀번호 (4자리 숫자): ", this::validateAccountPassword);
            
            if (printSubMenu().equals("1")) {
                String sql = "UPDATE accounts SET account_password = ? WHERE account_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newAccountPassword);
                    pstmt.setString(2, accountId);
                    if (pstmt.executeUpdate() > 0) {
                        System.out.println("✅ 계좌 비밀번호가 변경되었습니다!");
                    }
                } catch (SQLException e) {
                    handleDatabaseError("계좌 비밀번호 변경", e);
                }
            }
        }
        accountList();
    }
    
    private void updateAccount(String accountId) {
        System.out.println("[계좌 비밀번호 변경]");
        
        if (inputAndVerifyAccountPassword(accountId)) {
            String newAccountPassword;
            do {
                System.out.print("새 계좌 비밀번호 (4자리 숫자): ");
                newAccountPassword = scanner.nextLine();
            } while (!validateAccountPassword(newAccountPassword));
            
            if (printSubMenu().equals("1")) {
                String sql = "UPDATE accounts SET account_password = ? WHERE account_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newAccountPassword);
                    pstmt.setString(2, accountId);
                    if (pstmt.executeUpdate() > 0) {
                        System.out.println("✅ 계좌 비밀번호가 변경되었습니다!");
                    }
                } catch (SQLException e) {
                    handleDatabaseError("계좌 비밀번호 변경", e);
                }
            }
        }
        accountList();
    }
    
    private void deleteAccountById(String accountId) {
        if (!inputAndVerifyAccountPassword(accountId)) {
            accountList();
            return;
        }
        
        String sql = "DELETE FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✅ 계좌가 삭제되었습니다!");
            }
        } catch (SQLException e) {
            handleDatabaseError("계좌 삭제", e);
        }
    }
    
    private void deleteAccount() {
        System.out.println("[계좌 삭제]");
        
        String accountId = inputWithValidation("삭제할 계좌번호: ", input -> {
            if (input.trim().isEmpty()) {
                System.out.println("❌ 계좌번호를 입력해주세요.");
                return false;
            }
            if (!checkAccountExists(input)) {
                System.out.println("❌ 존재하지 않는 계좌번호입니다.");
                return false;
            }
            if (!isOwnerAccount(input)) {
                System.out.println("❌ 본인 소유의 계좌만 삭제할 수 있습니다.");
                return false;
            }
            return true;
        });
        
        if (printSubMenu().equals("1")) {
            deleteAccountById(accountId);
        }
        accountList();
    }
    
    private String printSubMenu() {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("보조메뉴: 1.확인 | 2.취소");
        System.out.print("메뉴선택: ");
        return scanner.nextLine();
    }
    
    private void exit() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("은행 시스템이 정상적으로 종료되었습니다.");
        System.exit(0);
    }
    
    public static void main(String[] args) {
        BankSystem bankSystem = new BankSystem();
        bankSystem.accountList();
    }
}
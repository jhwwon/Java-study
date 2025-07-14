package mondayproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class BankSystem {
	//필드 선언
    private Scanner scanner = new Scanner(System.in);
    private Connection conn = null;
    private String loginId = null;
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 시스템 생성자 - DB 연결 초기화
     */
    public BankSystem() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521/orcl", "jhw1", "1234"
            );
            System.out.println("은행 계좌 시스템 DB 연결 성공!");
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
    }
    
    /**
     * 시스템 종료 - DB 연결 해제 및 프로그램 종료
     */
    private void exit() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println("❌ DB 연결 종료 중 오류: " + e.getMessage());
        }
        System.out.println("은행 시스템이 정상적으로 종료되었습니다.");
        System.exit(0);
    }
    
    // ========================================
    // 유틸리티 메소드
    // ========================================
    
    /**
     * 금액을 통화 형식으로 포맷팅
     */
    private String formatCurrency(double amount) {
        return currencyFormat.format(amount) + "원";
    }
    
    /**
     * 새로운 계좌번호 생성
     */
    private String newAccNum() {
        String sql = "SELECT '110-234-' || LPAD(SEQ_ACCOUNT.NEXTVAL, 6, '0') FROM DUAL";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            System.out.println("❌ 계좌번호 생성 오류: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 새로운 거래번호 생성
     */
    private String newTxId() {
        String sql = "SELECT 'T' || LPAD(SEQ_TRANSACTION.NEXTVAL, 8, '0') FROM DUAL";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            System.out.println("❌ 거래번호 생성 오류: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 사용자 ID로 사용자 이름 조회
     */
    private String getUserName(String userId) {
        String sql = "SELECT user_name FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("user_name");
            }
        } catch (SQLException e) {
            System.out.println("❌ 사용자 이름 조회 오류: " + e.getMessage());
        }
        return userId;
    }
    
    /**
     * 거래 유형에 따른 상대방 정보 표시 형식 결정
     */
    private String getCounterpartDisplay(String transactionType, String counterpartName, 
                                       String depositorName, String counterpartAccount) {
        switch (transactionType) {
            case "이체입금":
                return counterpartName != null ? "보낸사람: " + counterpartName : 
                       counterpartAccount != null ? "보낸계좌: " + counterpartAccount : "-";
            case "이체출금":
                return counterpartName != null ? "받는사람: " + counterpartName : 
                       counterpartAccount != null ? "받는계좌: " + counterpartAccount : "-";
            case "입금":
                return depositorName != null ? "입금자: " + depositorName : "-";
            default:
                return "-";
        }
    }
    
    // ========================================
    // 기본 입력 및 확인 메소드
    // ========================================
    
    /**
     * 기본 입력 메소드 - 빈 값 입력 방지
     */
    private String input(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
        } while (input.isEmpty());
        return input;
    }
    
    /**
     * 작업 확인 메소드
     */
    private boolean confirmAction() {
        System.out.println("보조메뉴: 1.확인 | 2.취소");
        System.out.print("메뉴선택: ");
        return "1".equals(scanner.nextLine());
    }
    
    // ========================================
    // 회원가입 입력 및 검증 메소드
    // ========================================
    
    /**
     * 사용자 ID 입력 및 검증
     */
    private String inputUserId() {
        String userId;
        do {
            userId = input("아이디 (4~8자리, 영문+숫자): ");
            if (validateUserId(userId) && checkUserIdDuplicate(userId)) {
                return userId;
            }
        } while (true);
    }
    
    /**
     * 사용자 이름 입력 및 검증
     */
    private String inputUserName() {
        String userName;
        do {
            userName = input("이름: ");
            if (validateUserName(userName)) {
                return userName;
            }
        } while (true);
    }
    
    /**
     * 사용자 비밀번호 입력 및 검증
     */
    private String inputUserPassword(String userId) {
        String password;
        do {
            password = input("비밀번호 (7~12자리, 영문+숫자): ");
            if (validateUserPassword(password, userId)) {
                return password;
            }
        } while (true);
    }
    
    /**
     * 이메일 입력 및 검증
     */
    private String inputEmail() {
        String email;
        do {
            email = input("이메일: ");
            if (validateEmail(email)) {
                return email;
            }
        } while (true);
    }
    
    /**
     * 전화번호 입력 및 검증
     */
    private String inputPhone() {
        String phone;
        do {
            phone = input("전화번호 (010-0000-0000): ");
            if (validatePhone(phone)) {
                return phone;
            }
        } while (true);
    }
    
    // ========================================
    // 회원가입 유효성 검사 메소드
    // ========================================
    
    /**
     * 사용자 ID 유효성 검사
     */
    private boolean validateUserId(String userId) {
        if (userId.length() < 4 || userId.length() > 8) {
            System.out.println("❌ 아이디는 4~8자리여야 합니다.");
            return false;
        }
        if (!userId.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$")) {
            System.out.println("❌ 아이디는 영문과 숫자가 모두 포함되어야 합니다.");
            return false;
        }
        return true;
    }
    
    /**
     * 사용자 이름 유효성 검사
     */
    private boolean validateUserName(String userName) {
        if (userName.length() > 20) {
            System.out.println("❌ 이름은 20자리까지만 가능합니다.");
            return false;
        }
        return true;
    }
    
    /**
     * 사용자 비밀번호 유효성 검사
     */
    private boolean validateUserPassword(String password, String userId) {
        if (password.length() < 7 || password.length() > 12) {
            System.out.println("❌ 비밀번호는 7~12자리여야 합니다.");
            return false;
        }
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$")) {
            System.out.println("❌ 비밀번호는 영문과 숫자가 모두 포함되어야 합니다.");
            return false;
        }
        if (password.equals(userId)) {
            System.out.println("❌ 비밀번호는 아이디와 같을 수 없습니다.");
            return false;
        }
        return true;
    }
    
    /**
     * 이메일 유효성 검사
     */
    private boolean validateEmail(String email) {
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
    
    /**
     * 전화번호 유효성 검사
     */
    private boolean validatePhone(String phone) {
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
    
    /**
     * 사용자 ID 중복 확인
     */
    private boolean checkUserIdDuplicate(String userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("❌ 이미 존재하는 아이디입니다.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 아이디 중복 확인 오류: " + e.getMessage());
            return false;
        }
        return true;
    }
    
    // ========================================
    // 계좌 관련 입력 및 검증 메소드
    // ========================================
    
    /**
     * 계좌 비밀번호 입력 및 검증
     */
    private String inputAccountPassword() {
        String password;
        do {
            password = input("계좌 비밀번호 (4자리 숫자): ");
            if (validateAccountPassword(password)) {
                return password;
            }
        } while (true);
    }
    
    /**
     * 계좌 비밀번호 유효성 검사
     */
    private boolean validateAccountPassword(String password) {
        if (password.length() != 4) {
            System.out.println("❌ 계좌 비밀번호는 4자리여야 합니다.");
            return false;
        }
        if (!password.matches("^[0-9]+$")) {
            System.out.println("❌ 계좌 비밀번호는 숫자만 입력 가능합니다.");
            return false;
        }
        return true;
    }
    
    /**
     * 금액 입력 및 검증
     */
    private double inputAmount(String prompt) {
        double amount;
        do {
            try {
                System.out.print(prompt);
                amount = Double.parseDouble(scanner.nextLine());
                if (amount < 1000) {
                    System.out.println("❌ 금액은 1,000원 이상이어야 합니다.");
                    continue;
                }
                if (amount > 5000000) {
                    System.out.println("❌ 금액이 너무 큽니다. (최대 500만원)");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("❌ 올바른 숫자를 입력해주세요.");
            }
        } while (true);
    }
    
    /**
     * 계좌번호 입력 및 검증
     */
    private String inputAccountId(String prompt, boolean ownOnly) {
        String accountId;
        do {
            accountId = input(prompt);
            if (!accountExists(accountId)) {
                System.out.println("❌ 존재하지 않는 계좌번호입니다.");
                continue;
            }
            if (ownOnly && !isMyAccount(accountId)) {
                System.out.println("❌ 본인 소유의 계좌만 이용할 수 있습니다.");
                continue;
            }
            return accountId;
        } while (true);
    }
    
    /**
     * 계좌 비밀번호 확인
     */
    private boolean checkPassword(String accountId) {
        String password;
        do {
            System.out.print("계좌 비밀번호 (4자리): ");
            password = scanner.nextLine();
            if (password.length() != 4 || !password.matches("\\d{4}")) {
                System.out.println("❌ 계좌 비밀번호는 4자리 숫자여야 합니다.");
                continue;
            }
            if (verifyPassword(accountId, password)) return true;
            System.out.println("❌ 계좌 비밀번호가 일치하지 않습니다.");
        } while (true);
    }
    
    // ========================================
    // 계좌 관련 조회 및 검증 메소드
    // ========================================
    
    /**
     * 계좌 존재 여부 확인
     */
    private boolean accountExists(String accountId) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("❌ 계좌 조회 오류: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 본인 계좌 여부 확인
     */
    private boolean isMyAccount(String accountId) {
        String sql = "SELECT user_id FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return loginId != null && loginId.equals(rs.getString("user_id"));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 계좌 소유자 확인 오류: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 계좌 비밀번호 검증
     */
    private boolean verifyPassword(String accountId, String password) {
        String sql = "SELECT account_password FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return password.equals(rs.getString("account_password"));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 비밀번호 확인 오류: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 계좌 소유자명 조회
     */
    private String getAccountHolderName(String accountId) {
        String sql = "SELECT u.user_name FROM accounts a " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_name");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 예금주명 조회 오류: " + e.getMessage());
        }
        return "미상";
    }
    
    /**
     * 계좌 잔액 조회
     */
    private double getBalance(String accountId) {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.out.println("❌ 잔액 조회 오류: " + e.getMessage());
        }
        return -1;
    }
    
    // ========================================
    // 거래내역 관리
    // ========================================
    
    /**
     * 거래내역 저장
     */
    private void saveTransaction(String accountId, String type, double amount, double balanceAfter, 
                               String counterpartAccount, String counterpartName, String depositorName, String memo) {
        String sql = "INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, " +
                     "balance_after, counterpart_account, counterpart_name, depositor_name, " +
                     "transaction_memo, transaction_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newTxId());
            pstmt.setString(2, accountId);
            pstmt.setString(3, type);
            pstmt.setDouble(4, amount);
            pstmt.setDouble(5, balanceAfter);
            pstmt.setString(6, counterpartAccount);
            pstmt.setString(7, counterpartName);
            pstmt.setString(8, depositorName);
            pstmt.setString(9, memo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ 거래내역 저장 오류: " + e.getMessage());
        }
    }
    
    // ========================================
    // 사용자 관리 (회원가입/로그인)
    // ========================================
    
    /**
     * 로그인 처리
     */
    private void login() {
        System.out.println("[로그인]");
        String userId = input("아이디: ");
        String password = input("비밀번호: ");
        
        if (confirmAction()) {
            String sql = "SELECT user_password FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && password.equals(rs.getString("user_password"))) {
                        loginId = userId;
                        System.out.println("✅ 로그인 성공!");
                    } else {
                        System.out.println("❌ 아이디 또는 비밀번호가 일치하지 않습니다.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("❌ 로그인 오류: " + e.getMessage());
            }
        }
        list();
    }
    
    /**
     * 로그아웃 처리
     */
    private void logout() {
        loginId = null;
        System.out.println("로그아웃되었습니다.");
        list();
    }
    
    /**
     * 회원가입 처리
     */
    private void join() {
        System.out.println("[회원가입]");
        String userId = inputUserId();
        String userName = inputUserName();
        String password = inputUserPassword(userId);
        String email = inputEmail();
        String phone = inputPhone();
        
        if (confirmAction()) {
            String sql = "INSERT INTO users (user_id, user_name, user_password, user_email, user_phone, join_date) " +
                         "VALUES (?, ?, ?, ?, ?, SYSDATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, userName);
                pstmt.setString(3, password);
                pstmt.setString(4, email);
                pstmt.setString(5, phone);
                
                if (pstmt.executeUpdate() > 0) {
                    System.out.println("✅ 회원가입이 완료되었습니다!");
                }
            } catch (SQLException e) {
                System.out.println("❌ 회원가입 오류: " + e.getMessage());
            }
        }
        list();
    }
    
    // ========================================
    // 계좌 관리 기능
    // ========================================
    
    /**
     * 계좌 생성
     */
    private void createAccount() {
        if (loginId == null) {
            System.out.println("❌ 계좌 생성은 로그인 후 이용 가능합니다.");
            menu();
            return;
        }
        
        System.out.println("[계좌 생성]");
        String[] types = {"보통예금", "정기예금", "적금"};
        
        System.out.println("\n[계좌 종류 선택]");
        System.out.println("---------------------------------------");
        System.out.println("1. 보통예금 - 자유입출금, 일상거래용 [연 0.1%]");
        System.out.println("2. 정기예금 - 목돈 예치, 높은 이자 [연 1.5%]");
        System.out.println("3. 적금 - 매월 저축, 목돈 만들기 [연 2.0%]");
        System.out.println("---------------------------------------");
        
        int choice;
        do {
            try {
                System.out.print("계좌 종류 선택 (1-3): ");
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= 3) break;
            } catch (NumberFormatException e) {}
            System.out.println("❌ 1~3번을 선택해주세요.");
        } while (true);
        
        String accountType = types[choice - 1];
        String accountName = accountType + " 계좌_" + getUserName(loginId);
        String password = inputAccountPassword();
        double initialBalance = inputAmount("초기 입금액 (1,000원 이상): ");
        
        if (confirmAction()) {
            String accountId = newAccNum();
            String sql = "INSERT INTO accounts (account_id, account_name, account_type, account_password, " +
                         "balance, user_id, create_date) VALUES (?, ?, ?, ?, ?, ?, SYSDATE)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, accountId);
                pstmt.setString(2, accountName);
                pstmt.setString(3, accountType);
                pstmt.setString(4, password);
                pstmt.setDouble(5, initialBalance);
                pstmt.setString(6, loginId);
                
                if (pstmt.executeUpdate() > 0) {
                    saveTransaction(accountId, "입금", initialBalance, initialBalance, null, null, 
                                  getUserName(loginId), "계좌개설");
                    System.out.println("✅ 계좌가 성공적으로 생성되었습니다!");
                    System.out.println("   계좌번호: " + accountId);
                }
            } catch (SQLException e) {
                System.out.println("❌ 계좌 생성 오류: " + e.getMessage());
            }
        }
        list();
    }
    
    /**
     * 계좌 조회
     */
    private void readAccount() {
        System.out.println("[계좌 조회]");
        String accountId = input("계좌번호: ");
        
        String sql = "SELECT a.*, u.user_name FROM accounts a JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("계좌번호: " + rs.getString("account_id"));
                    System.out.println("계좌명: " + rs.getString("account_name"));
                    System.out.println("계좌종류: " + rs.getString("account_type"));
                    System.out.println("잔액: " + formatCurrency(rs.getDouble("balance")));
                    System.out.println("소유자: " + rs.getString("user_name"));
                    System.out.println("개설일: " + rs.getDate("create_date"));
                    
                    if (loginId != null && loginId.equals(rs.getString("user_id"))) {
                        System.out.println("보조메뉴: 1.삭제 | 2.목록");
                        if ("1".equals(scanner.nextLine())) {
                            deleteAccount(accountId);
                        }
                    }
                } else {
                    System.out.println("해당 계좌를 찾을 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 계좌 조회 오류: " + e.getMessage());
        }
        list();
    }
    
    /**
     * 계좌 삭제
     */
    private void deleteAccount(String accountId) {
        if (accountId == null) {
            System.out.println("[계좌 삭제]");
            accountId = inputAccountId("삭제할 계좌번호: ", true);
        }
        
        if (!checkPassword(accountId) || !confirmAction()) {
            list();
            return;
        }
        
        String sql = "DELETE FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✅ 계좌가 삭제되었습니다!");
            }
        } catch (SQLException e) {
            System.out.println("❌ 계좌 삭제 오류: " + e.getMessage());
        }
        list();
    }
    
    /**
     * 계좌 비밀번호 변경
     */
    private void changePassword() {
        System.out.println("[계좌 비밀번호 변경]");
        String accountId = inputAccountId("계좌번호: ", true);
        
        if (checkPassword(accountId)) {
            String newPassword = inputAccountPassword();
            
            if (confirmAction()) {
                String sql = "UPDATE accounts SET account_password = ? WHERE account_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newPassword);
                    pstmt.setString(2, accountId);
                    if (pstmt.executeUpdate() > 0) {
                        System.out.println("✅ 계좌 비밀번호가 변경되었습니다!");
                    }
                } catch (SQLException e) {
                    System.out.println("❌ 비밀번호 변경 오류: " + e.getMessage());
                }
            }
        }
        list();
    }
    
    // ========================================
    // 거래 업무 기능
    // ========================================
    
    /**
     * 입금 처리
     */
    private void deposit() {
        System.out.println("[입금]");
        String accountId = inputAccountId("계좌번호: ", false);
        String depositorName;
        
        if (isMyAccount(accountId)) {
            System.out.println("💳 본인 계좌 입금 - 계좌 비밀번호 확인이 필요합니다.");
            if (!checkPassword(accountId)) {
                list();
                return;
            }
            depositorName = getUserName(loginId);
        } else {
            depositorName = input("입금자명: ");
        }
        
        double amount = inputAmount("입금액: ");
        System.out.print("입금 메모 (선택사항): ");
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty()) memo = null;
        
        if (confirmAction()) {
            String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, accountId);
                
                if (pstmt.executeUpdate() > 0) {
                    double newBalance = getBalance(accountId);
                    saveTransaction(accountId, "입금", amount, newBalance, null, null, depositorName, memo);
                    
                    System.out.println("✅ 입금이 완료되었습니다!");
                    System.out.println("   입금액: " + formatCurrency(amount));
                    if (isMyAccount(accountId)) {
                        System.out.println("   현재잔액: " + formatCurrency(newBalance));
                    }
                }
            } catch (SQLException e) {
                System.out.println("❌ 입금 오류: " + e.getMessage());
            }
        }
        list();
    }
    
    /**
     * 출금 처리
     */
    private void withdraw() {
        System.out.println("[출금]");
        String accountId = inputAccountId("계좌번호: ", true);
        
        if (!checkPassword(accountId)) {
            list();
            return;
        }
        
        double currentBalance = getBalance(accountId);
        double amount = inputAmount("출금액: ");
        
        if (currentBalance < amount) {
            System.out.println("❌ 잔액이 부족합니다. (현재 잔액: " + formatCurrency(currentBalance) + ")");
            list();
            return;
        }
        
        System.out.print("출금 메모 (선택사항): ");
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty()) memo = null;
        
        if (confirmAction()) {
            String sql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, accountId);
                
                if (pstmt.executeUpdate() > 0) {
                    double newBalance = currentBalance - amount;
                    saveTransaction(accountId, "출금", amount, newBalance, null, null, null, memo);
                    
                    System.out.println("✅ 출금이 완료되었습니다!");
                    System.out.println("   출금액: " + formatCurrency(amount));
                    System.out.println("   잔여잔액: " + formatCurrency(newBalance));
                }
            } catch (SQLException e) {
                System.out.println("❌ 출금 오류: " + e.getMessage());
            }
        }
        list();
    }
    
    /**
     * 이체 처리
     */
    private void transfer() {
        System.out.println("[이체]");
        String fromAccountId = inputAccountId("출금 계좌번호: ", true);
        
        if (!checkPassword(fromAccountId)) {
            list();
            return;
        }
        
        String toAccountId;
        do {
            toAccountId = inputAccountId("입금 계좌번호: ", false);
            if (!fromAccountId.equals(toAccountId)) break;
            System.out.println("❌ 출금 계좌와 입금 계좌가 같을 수 없습니다.");
        } while (true);
        
        double currentBalance = getBalance(fromAccountId);
        double amount = inputAmount("이체금액: ");
        
        if (currentBalance < amount) {
            System.out.println("❌ 잔액이 부족합니다. (현재 잔액: " + formatCurrency(currentBalance) + ")");
            list();
            return;
        }
        
        System.out.print("이체 메모 (선택사항): ");
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty()) memo = null;
        
        if (confirmAction()) {
            try {
                conn.setAutoCommit(false);
                
                // 출금
                String withdrawSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(withdrawSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setString(2, fromAccountId);
                    pstmt.executeUpdate();
                }
                
                // 입금
                String depositSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(depositSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setString(2, toAccountId);
                    pstmt.executeUpdate();
                }
                
                // 거래내역 저장
                double fromBalance = getBalance(fromAccountId);
                double toBalance = getBalance(toAccountId);
                String fromName = getAccountHolderName(fromAccountId);
                String toName = getAccountHolderName(toAccountId);
                
                saveTransaction(fromAccountId, "이체출금", amount, fromBalance, toAccountId, toName, null, memo);
                saveTransaction(toAccountId, "이체입금", amount, toBalance, fromAccountId, fromName, null, memo);
                
                conn.commit();
                
                System.out.println("✅ 이체가 완료되었습니다!");
                System.out.println("   이체금액: " + formatCurrency(amount));
                System.out.println("   출금계좌: " + fromAccountId + " (" + fromName + ")");
                System.out.println("   입금계좌: " + toAccountId + " (" + toName + ")");
                
            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ex) {}
                System.out.println("❌ 이체 오류: " + e.getMessage());
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException e) {}
            }
        }
        list();
    }
    
    /**
     * 거래내역 조회
     */
    private void history() {
        System.out.println("[거래내역 조회]");
        String accountId = inputAccountId("계좌번호: ", true);
        
        if (!checkPassword(accountId)) {
            list();
            return;
        }
        
        System.out.println("\n[거래내역] 계좌번호: " + accountId + " (" + getAccountHolderName(accountId) + ")");
        System.out.println("==========================================================================================================");
        System.out.printf("%-12s %-8s %-15s %-15s %-20s %-12s %s%n", 
                "거래번호", "거래구분", "거래금액", "거래후잔액", "상대방정보", "메모", "거래일시");
        System.out.println("==========================================================================================================");
        
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasTransactions = false;
                while (rs.next()) {
                    hasTransactions = true;
                    
                    String transactionType = rs.getString("transaction_type");
                    String counterpartAccount = rs.getString("counterpart_account");
                    String counterpartName = rs.getString("counterpart_name");
                    String depositorName = rs.getString("depositor_name");
                    String memo = rs.getString("transaction_memo");
                    
                    String counterpartDisplay = getCounterpartDisplay(transactionType, counterpartName, 
                                                                    depositorName, counterpartAccount);
                    
                    if (memo == null) memo = "-";
                    String displayMemo = memo.length() > 10 ? memo.substring(0, 10) + ".." : memo;
                    
                    System.out.printf("%-12s %-8s %-15s %-15s %-20s %-12s %s%n",
                        rs.getString("transaction_id"),
                        transactionType,
                        formatCurrency(rs.getDouble("amount")),
                        formatCurrency(rs.getDouble("balance_after")),
                        counterpartDisplay,
                        displayMemo,
                        dateFormat.format(rs.getTimestamp("transaction_date"))
                    );
                }
                
                if (!hasTransactions) {
                    System.out.println("거래내역이 없습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 거래내역 조회 오류: " + e.getMessage());
        }
        
        list();
    }
    
    /**
     * 계좌 목록 표시
     */
    private void list() {
        if (loginId == null) {
            System.out.println("\n=== 은행 계좌 관리 시스템 ===");
            System.out.println("계좌 서비스를 이용하려면 로그인해주세요.");
            menu();
            return;
        }
        
        System.out.println("\n[계좌 목록] 사용자: " + getUserName(loginId) + " (" + loginId + ")");
        System.out.println("====================================================================================");
        System.out.println("계좌번호\t\t계좌명\t\t\t계좌종류\t\t소유자\t\t잔액");
        System.out.println("====================================================================================");
        
        String sql = "SELECT a.*, u.user_name FROM accounts a JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loginId);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasAccounts = false;
                while (rs.next()) {
                    hasAccounts = true;
                    
                    String accountName = rs.getString("account_name");
                    String displayAccountName = accountName.length() > 12 ? accountName.substring(0, 12) + ".." : accountName;
                    
                    System.out.println(
                        rs.getString("account_id") + "\t" +
                        displayAccountName + "\t\t" +
                        rs.getString("account_type") + "\t\t" +
                        rs.getString("user_name") + "\t\t" +
                        formatCurrency(rs.getDouble("balance"))
                    );
                }
                if (!hasAccounts) {
                    System.out.println("보유하신 계좌가 없습니다. 계좌를 생성해보세요!");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 계좌 목록 조회 오류: " + e.getMessage());
        }
        menu();
    }
    
    /**
     * 메뉴 표시 및 처리
     */
    private void menu() {
        System.out.println("\n" + "─".repeat(80));
        if (loginId == null) {
            System.out.println("메인메뉴: 1.회원가입 | 2.로그인 | 3.종료");
            System.out.print("메뉴선택: ");
            
            String menuNo = scanner.nextLine();
            switch (menuNo) {
                case "1" -> join();
                case "2" -> login();
                case "3" -> exit();
                default -> {
                    System.out.println("❌ 1~3번의 숫자만 입력이 가능합니다.");
                    menu();
                }
            }
        } else {
            System.out.println("✅계좌관리: 1.계좌생성 | 2.계좌조회 | 8.계좌해지");
            System.out.println("✅거래업무: 3.입금 | 4.출금 | 5.이체 | 6.거래내역조회");
            System.out.println("✅기타메뉴: 7.계좌비밀번호변경 | 9.로그아웃 | 0.종료");
            System.out.print("메뉴선택: ");
            
            String menuNo = scanner.nextLine();
            switch (menuNo) {
                case "1" -> createAccount();
                case "2" -> readAccount();
                case "3" -> deposit();
                case "4" -> withdraw();
                case "5" -> transfer();
                case "6" -> history();
                case "7" -> changePassword();
                case "8" -> deleteAccount(null);
                case "9" -> logout();
                case "0" -> exit();
                default -> {
                    System.out.println("❌ 0~9번의 숫자만 입력이 가능합니다.");
                    menu();
                }
            }
        }
    }
    
    /**
     * 프로그램 시작점
     */
    public static void main(String[] args) {
        BankSystem bankSystem = new BankSystem();
        bankSystem.list();
    }
}
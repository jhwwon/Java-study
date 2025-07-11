package miniproject.project;

import java.sql.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * 은행 시스템 - 게시판 스타일 구조
 * 사용자 입력, 데이터베이스 연결, 비즈니스 로직을 하나의 클래스에서 처리
 */
public class BankSystem {
    // 사용자 입력을 받기 위한 Scanner
    private Scanner scanner = new Scanner(System.in);
    
    // 데이터베이스 연결 객체
    private Connection conn = null;
    
    // 로그인한 사용자 정보
    private String loginUserId = null;
    private String loginUserName = null;
    
    // 거래 한도 상수
    private static final double MAX_TRANSACTION_AMOUNT = 10_000_000; // 천만원
    private static final double MIN_TRANSACTION_AMOUNT = 1_000;      // 천원
    
    // 금액 포맷터 (천단위 콤마)
    private DecimalFormat formatter = new DecimalFormat("#,###");
    
    /**
     * 기본 생성자 - 데이터베이스 연결
     */
    public BankSystem() {
        try {
            // JDBC Driver 등록
            Class.forName("oracle.jdbc.OracleDriver");
            
            // 데이터베이스 연결
            conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521/orcl", // Oracle 접속정보
                "jhw1",     // 계정명
                "1234"      // 비밀번호
            );
            System.out.println("데이터베이스 연결 성공!");
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
    }
    
    /**
     * 계좌 목록을 보여주는 기능
     */
    private void accountList() {
        String accountListTitle = "[계좌 목록]";
        if (loginUserId != null) {
            accountListTitle = accountListTitle + " 사용자: " + loginUserName;
        }
        
        System.out.println();
        System.out.println(accountListTitle);
        System.out.println("-----------------------------------------------------------------------");
        System.out.printf("%-20s%-15s%-20s%-15s%-15s\n", 
                          "계좌번호", "계좌주", "계좌유형", "잔액", "이자율");
        System.out.println("-----------------------------------------------------------------------");
        
        try {
            String sql = "SELECT account_id, account_holder_name, account_type, balance, interest_rate " +
                        "FROM accounts";
            
            if (loginUserId != null) { // 로그인한 경우 본인 계좌만 조회
                sql += " WHERE owner_id = '" + loginUserId + "'";
            }
            sql += " ORDER BY created_date DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            boolean hasAccounts = false;
            while (rs.next()) {
                hasAccounts = true;
                System.out.printf("%-20s%-15s%-20s%-15s%-15s\n",
                                rs.getString("account_id"),
                                rs.getString("account_holder_name"),
                                rs.getString("account_type"),
                                formatter.format(rs.getDouble("balance")) + "원",
                                rs.getDouble("interest_rate") + "%");
            }
            
            if (!hasAccounts) {
                System.out.println("등록된 계좌가 없습니다.");
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            exit();
        }
        
        mainMenu(); // 메인 메뉴 호출
    }
    
    /**
     * 메인 메뉴를 생성하는 기능
     */
    private void mainMenu() {
        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        
        if (loginUserId == null) { // 로그인 안 했을 경우
            System.out.println("메인메뉴: 1.회원가입 | 2.로그인 | 3.전체계좌조회 | 4.종료");
            System.out.print("메뉴선택: ");
            
            String menuNo = scanner.nextLine();
            switch (menuNo) {
                case "1" -> join();
                case "2" -> login();
                case "3" -> accountList();
                case "4" -> exit();
                default -> {
                    System.out.println("***1번에서 4번의 숫자만 입력 가능합니다. 다시 입력해 주세요***");
                    mainMenu();
                }
            }
        } else { // 로그인 했을 경우
            if ("admin".equals(loginUserId)) { // 관리자인 경우
                System.out.println("관리자메뉴: 1.전체계좌조회 | 2.이자계산 | 3.로그아웃 | 4.종료");
                System.out.print("메뉴선택: ");
                
                String menuNo = scanner.nextLine();
                switch (menuNo) {
                    case "1" -> accountList();
                    case "2" -> calculateInterest();
                    case "3" -> logout();
                    case "4" -> exit();
                    default -> {
                        System.out.println("***1번에서 4번의 숫자만 입력 가능합니다. 다시 입력해 주세요***");
                        mainMenu();
                    }
                }
            } else { // 일반 사용자인 경우
                System.out.println("사용자메뉴: 1.계좌개설 | 2.입금 | 3.출금 | 4.이체 | 5.거래내역 | 6.로그아웃 | 7.종료");
                System.out.print("메뉴선택: ");
                
                String menuNo = scanner.nextLine();
                switch (menuNo) {
                    case "1" -> createAccount();
                    case "2" -> deposit();
                    case "3" -> withdraw();
                    case "4" -> transfer();
                    case "5" -> transactionHistory();
                    case "6" -> logout();
                    case "7" -> exit();
                    default -> {
                        System.out.println("***1번에서 7번의 숫자만 입력 가능합니다. 다시 입력해 주세요***");
                        mainMenu();
                    }
                }
            }
        }
    }
    
    /**
     * 회원가입 기능
     */
    private void join() {
        System.out.println("[회원가입]");
        System.out.print("아이디 (3-20자): ");
        String userId = scanner.nextLine();
        System.out.print("이름: ");
        String userName = scanner.nextLine();
        System.out.print("비밀번호 (최소 4자): ");
        String userPassword = scanner.nextLine();
        
        // 입력값 검증
        if (!User.isValidUserId(userId)) {
            System.out.println("아이디는 3-20자로 입력해주세요.");
            mainMenu();
            return;
        }
        
        if (!User.isValidName(userName)) {
            System.out.println("올바른 이름을 입력해주세요.");
            mainMenu();
            return;
        }
        
        if (userPassword.length() < 4) {
            System.out.println("비밀번호는 최소 4자 이상이어야 합니다.");
            mainMenu();
            return;
        }
        
        // 보조메뉴 출력
        if (printSubMenu().equals("1")) {
            try {
                // 중복 확인
                if (isUserIdExists(userId)) {
                    System.out.println("이미 존재하는 아이디입니다.");
                    mainMenu();
                    return;
                }
                
                // 사용자 등록
                String sql = "INSERT INTO users (user_id, user_name, password) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, userId);
                pstmt.setString(2, userName);
                pstmt.setString(3, userPassword);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("회원가입이 완료되었습니다!");
                }
                
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                exit();
            }
        }
        
        mainMenu();
    }
    
    /**
     * 로그인 기능
     */
    private void login() {
        System.out.println("[로그인]");
        System.out.print("아이디: ");
        String userId = scanner.nextLine();
        System.out.print("비밀번호: ");
        String userPassword = scanner.nextLine();
        
        // 보조메뉴 출력
        if (printSubMenu().equals("1")) {
            try {
                // 관리자 계정 확인
                if ("admin".equals(userId) && "password".equals(userPassword)) {
                    loginUserId = "admin";
                    loginUserName = "관리자";
                    System.out.println("관리자 로그인 성공!");
                    mainMenu();
                    return;
                }
                
                // 일반 사용자 확인
                String sql = "SELECT user_name, password FROM users WHERE user_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, userId);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    String userName = rs.getString("user_name");
                    
                    if (dbPassword != null && dbPassword.equals(userPassword)) {
                        loginUserId = userId;
                        loginUserName = userName;
                        System.out.println("로그인 성공! 환영합니다, " + userName + "님!");
                    } else {
                        System.out.println("비밀번호가 일치하지 않습니다.");
                    }
                } else {
                    System.out.println("아이디가 존재하지 않습니다.");
                }
                
                rs.close();
                pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
                exit();
            }
        }
        
        mainMenu();
    }
    
    /**
     * 로그아웃 기능
     */
    private void logout() {
        loginUserId = null;
        loginUserName = null;
        System.out.println("로그아웃 되었습니다.");
        mainMenu();
    }
    
    /**
     * 계좌 개설 기능
     */
    private void createAccount() {
        System.out.println("[계좌 개설]");
        System.out.print("계좌주 실명: ");
        String accountHolderName = scanner.nextLine();
        
        // 실명 확인
        if (!loginUserName.equals(accountHolderName)) {
            System.out.println("계좌주 실명이 회원가입시 등록한 이름과 일치하지 않습니다.");
            System.out.println("회원가입 이름: " + loginUserName);
            mainMenu();
            return;
        }
        
        // 계좌 유형 선택
        System.out.println("계좌 유형을 선택하세요:");
        System.out.println("1. 자유입출금통장 (연 0.1%)");
        System.out.println("2. 적금통장 (연 2.5%)");
        System.out.println("3. 정기예금 (연 3.0%)");
        System.out.print("선택: ");
        
        String typeChoice = scanner.nextLine();
        String accountType;
        double interestRate;
        
        switch (typeChoice) {
            case "1" -> { accountType = "자유입출금통장"; interestRate = 0.1; }
            case "2" -> { accountType = "적금통장"; interestRate = 2.5; }
            case "3" -> { accountType = "정기예금"; interestRate = 3.0; }
            default -> {
                System.out.println("잘못된 선택입니다.");
                mainMenu();
                return;
            }
        }
        
        System.out.print("계좌 비밀번호 (4자리 숫자): ");
        String accountPassword = scanner.nextLine();
        
        if (!Account.isValidPassword(accountPassword)) {
            System.out.println("계좌 비밀번호는 4자리 숫자여야 합니다.");
            mainMenu();
            return;
        }
        
        System.out.print("초기 입금액: ");
        String balanceStr = scanner.nextLine();
        double initialBalance;
        
        try {
            initialBalance = Double.parseDouble(balanceStr);
            if (initialBalance < MIN_TRANSACTION_AMOUNT || initialBalance > MAX_TRANSACTION_AMOUNT) {
                System.out.println("초기 입금액은 " + formatter.format(MIN_TRANSACTION_AMOUNT) + 
                                 "원 ~ " + formatter.format(MAX_TRANSACTION_AMOUNT) + "원 사이여야 합니다.");
                mainMenu();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("올바른 금액을 입력해주세요.");
            mainMenu();
            return;
        }
        
        System.out.print("계좌 별명 (선택사항): ");
        String accountAlias = scanner.nextLine();
        if (accountAlias.trim().isEmpty()) {
            accountAlias = accountType + "_" + accountHolderName;
        }
        
        // 보조메뉴 출력
        if (printSubMenu().equals("1")) {
            try {
                // 계좌번호 생성
                String accountId = generateAccountId();
                
                // 계좌 생성
                String sql = "INSERT INTO accounts (account_id, owner_id, account_holder_name, " +
                           "account_type, account_password, balance, account_alias, interest_rate) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, accountId);
                pstmt.setString(2, loginUserId);
                pstmt.setString(3, accountHolderName);
                pstmt.setString(4, accountType);
                pstmt.setString(5, accountPassword);
                pstmt.setDouble(6, initialBalance);
                pstmt.setString(7, accountAlias);
                pstmt.setDouble(8, interestRate);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    // 개설 거래내역 기록
                    recordTransaction(accountId, "개설", initialBalance, "계좌 개설 시 초기 입금");
                    
                    System.out.println("계좌가 성공적으로 개설되었습니다!");
                    System.out.println("계좌번호: " + accountId);
                    System.out.println("계좌유형: " + accountType);
                    System.out.println("초기잔액: " + formatter.format(initialBalance) + "원");
                }
                
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                exit();
            }
        }
        
        mainMenu();
    }
    
    /**
     * 입금 기능
     */
    private void deposit() {
        System.out.println("[입금]");
        System.out.print("계좌번호: ");
        String accountId = scanner.nextLine();
        
        // 본인 계좌 확인
        if (!isMyAccount(accountId)) {
            System.out.println("본인 계좌만 이용 가능합니다.");
            mainMenu();
            return;
        }
        
        System.out.print("계좌 비밀번호: ");
        String password = scanner.nextLine();
        
        System.out.print("입금액: ");
        String amountStr = scanner.nextLine();
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < MIN_TRANSACTION_AMOUNT || amount > MAX_TRANSACTION_AMOUNT) {
                System.out.println("입금액은 " + formatter.format(MIN_TRANSACTION_AMOUNT) + 
                                 "원 ~ " + formatter.format(MAX_TRANSACTION_AMOUNT) + "원 사이여야 합니다.");
                mainMenu();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("올바른 금액을 입력해주세요.");
            mainMenu();
            return;
        }
        
        // 보조메뉴 출력
        if (printSubMenu().equals("1")) {
            try {
                // 계좌 정보 조회 및 비밀번호 확인
                String selectSql = "SELECT account_password, balance FROM accounts WHERE account_id = ?";
                PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
                selectPstmt.setString(1, accountId);
                
                ResultSet rs = selectPstmt.executeQuery();
                if (rs.next()) {
                    String dbPassword = rs.getString("account_password");
                    double currentBalance = rs.getDouble("balance");
                    
                    if (!password.equals(dbPassword)) {
                        System.out.println("계좌 비밀번호가 틀렸습니다.");
                        rs.close();
                        selectPstmt.close();
                        mainMenu();
                        return;
                    }
                    
                    // 잔액 업데이트
                    String updateSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
                    PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                    updatePstmt.setDouble(1, amount);
                    updatePstmt.setString(2, accountId);
                    
                    int rows = updatePstmt.executeUpdate();
                    if (rows > 0) {
                        recordTransaction(accountId, "입금", amount, "직접 입금");
                        System.out.println("입금이 완료되었습니다.");
                        System.out.println("현재 잔액: " + formatter.format(currentBalance + amount) + "원");
                    }
                    
                    updatePstmt.close();
                } else {
                    System.out.println("계좌를 찾을 수 없습니다.");
                }
                
                rs.close();
                selectPstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                exit();
            }
        }
        
        mainMenu();
    }
    
    /**
     * 출금 기능
     */
    private void withdraw() {
        System.out.println("[출금]");
        System.out.print("계좌번호: ");
        String accountId = scanner.nextLine();
        
        // 본인 계좌 확인
        if (!isMyAccount(accountId)) {
            System.out.println("본인 계좌만 이용 가능합니다.");
            mainMenu();
            return;
        }
        
        System.out.print("계좌 비밀번호: ");
        String password = scanner.nextLine();
        
        System.out.print("출금액: ");
        String amountStr = scanner.nextLine();
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < MIN_TRANSACTION_AMOUNT || amount > MAX_TRANSACTION_AMOUNT) {
                System.out.println("출금액은 " + formatter.format(MIN_TRANSACTION_AMOUNT) + 
                                 "원 ~ " + formatter.format(MAX_TRANSACTION_AMOUNT) + "원 사이여야 합니다.");
                mainMenu();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("올바른 금액을 입력해주세요.");
            mainMenu();
            return;
        }
        
        // 보조메뉴 출력
        if (printSubMenu().equals("1")) {
            try {
                // 계좌 정보 조회 및 비밀번호 확인
                String selectSql = "SELECT account_password, balance FROM accounts WHERE account_id = ?";
                PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
                selectPstmt.setString(1, accountId);
                
                ResultSet rs = selectPstmt.executeQuery();
                if (rs.next()) {
                    String dbPassword = rs.getString("account_password");
                    double currentBalance = rs.getDouble("balance");
                    
                    if (!password.equals(dbPassword)) {
                        System.out.println("계좌 비밀번호가 틀렸습니다.");
                        rs.close();
                        selectPstmt.close();
                        mainMenu();
                        return;
                    }
                    
                    if (currentBalance < amount) {
                        System.out.println("잔액이 부족합니다. 현재 잔액: " + formatter.format(currentBalance) + "원");
                        rs.close();
                        selectPstmt.close();
                        mainMenu();
                        return;
                    }
                    
                    // 잔액 업데이트
                    String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
                    PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                    updatePstmt.setDouble(1, amount);
                    updatePstmt.setString(2, accountId);
                    
                    int rows = updatePstmt.executeUpdate();
                    if (rows > 0) {
                        recordTransaction(accountId, "출금", amount, "직접 출금");
                        System.out.println("출금이 완료되었습니다.");
                        System.out.println("현재 잔액: " + formatter.format(currentBalance - amount) + "원");
                    }
                    
                    updatePstmt.close();
                } else {
                    System.out.println("계좌를 찾을 수 없습니다.");
                }
                
                rs.close();
                selectPstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                exit();
            }
        }
        
        mainMenu();
    }
    
    /**
     * 이체 기능
     */
    private void transfer() {
        System.out.println("[이체]");
        System.out.print("출금 계좌번호: ");
        String fromAccountId = scanner.nextLine();
        
        // 본인 계좌 확인
        if (!isMyAccount(fromAccountId)) {
            System.out.println("본인 계좌만 이용 가능합니다.");
            mainMenu();
            return;
        }
        
        System.out.print("계좌 비밀번호: ");
        String password = scanner.nextLine();
        
        System.out.print("입금 계좌번호: ");
        String toAccountId = scanner.nextLine();
        
        if (fromAccountId.equals(toAccountId)) {
            System.out.println("동일한 계좌로는 이체할 수 없습니다.");
            mainMenu();
            return;
        }
        
        System.out.print("이체액: ");
        String amountStr = scanner.nextLine();
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < MIN_TRANSACTION_AMOUNT || amount > MAX_TRANSACTION_AMOUNT) {
                System.out.println("이체액은 " + formatter.format(MIN_TRANSACTION_AMOUNT) + 
                                 "원 ~ " + formatter.format(MAX_TRANSACTION_AMOUNT) + "원 사이여야 합니다.");
                mainMenu();
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("올바른 금액을 입력해주세요.");
            mainMenu();
            return;
        }
        
        // 보조메뉴 출력
        if (printSubMenu().equals("1")) {
            try {
                conn.setAutoCommit(false); // 트랜잭션 시작
                
                // 출금 계좌 확인
                String fromSelectSql = "SELECT account_password, balance, account_holder_name FROM accounts WHERE account_id = ?";
                PreparedStatement fromSelectPstmt = conn.prepareStatement(fromSelectSql);
                fromSelectPstmt.setString(1, fromAccountId);
                ResultSet fromRs = fromSelectPstmt.executeQuery();
                
                if (!fromRs.next()) {
                    System.out.println("출금 계좌를 찾을 수 없습니다.");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    fromRs.close();
                    fromSelectPstmt.close();
                    mainMenu();
                    return;
                }
                
                String dbPassword = fromRs.getString("account_password");
                double fromBalance = fromRs.getDouble("balance");
                String fromHolderName = fromRs.getString("account_holder_name");
                
                if (!password.equals(dbPassword)) {
                    System.out.println("계좌 비밀번호가 틀렸습니다.");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    fromRs.close();
                    fromSelectPstmt.close();
                    mainMenu();
                    return;
                }
                
                if (fromBalance < amount) {
                    System.out.println("잔액이 부족합니다. 현재 잔액: " + formatter.format(fromBalance) + "원");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    fromRs.close();
                    fromSelectPstmt.close();
                    mainMenu();
                    return;
                }
                
                // 입금 계좌 확인
                String toSelectSql = "SELECT account_holder_name, balance FROM accounts WHERE account_id = ?";
                PreparedStatement toSelectPstmt = conn.prepareStatement(toSelectSql);
                toSelectPstmt.setString(1, toAccountId);
                ResultSet toRs = toSelectPstmt.executeQuery();
                
                if (!toRs.next()) {
                    System.out.println("입금 계좌를 찾을 수 없습니다.");
                    conn.rollback();
                    conn.setAutoCommit(true);
                    fromRs.close();
                    fromSelectPstmt.close();
                    toRs.close();
                    toSelectPstmt.close();
                    mainMenu();
                    return;
                }
                
                String toHolderName = toRs.getString("account_holder_name");
                double toBalance = toRs.getDouble("balance");
                
                // 출금 처리
                String fromUpdateSql = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
                PreparedStatement fromUpdatePstmt = conn.prepareStatement(fromUpdateSql);
                fromUpdatePstmt.setDouble(1, amount);
                fromUpdatePstmt.setString(2, fromAccountId);
                fromUpdatePstmt.executeUpdate();
                
                // 입금 처리
                String toUpdateSql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
                PreparedStatement toUpdatePstmt = conn.prepareStatement(toUpdateSql);
                toUpdatePstmt.setDouble(1, amount);
                toUpdatePstmt.setString(2, toAccountId);
                toUpdatePstmt.executeUpdate();
                
                // 거래내역 기록
                recordTransaction(fromAccountId, "이체", amount, 
                                "-> " + toAccountId + " (" + toHolderName + ")");
                recordTransaction(toAccountId, "이체", amount, 
                                "<- " + fromAccountId + " (" + fromHolderName + ")");
                
                conn.commit(); // 트랜잭션 커밋
                conn.setAutoCommit(true);
                
                System.out.println("이체가 완료되었습니다.");
                System.out.println("출금 계좌 잔액: " + formatter.format(fromBalance - amount) + "원");
                System.out.println("입금 계좌 잔액: " + formatter.format(toBalance + amount) + "원");
                
                // 자원 정리
                fromRs.close();
                fromSelectPstmt.close();
                toRs.close();
                toSelectPstmt.close();
                fromUpdatePstmt.close();
                toUpdatePstmt.close();
                
            } catch (SQLException e) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                exit();
            }
        }
        
        mainMenu();
    }
    
    /**
     * 거래내역 조회 기능
     */
    private void transactionHistory() {
        System.out.println("[거래내역 조회]");
        System.out.print("계좌번호: ");
        String accountId = scanner.nextLine();
        
        // 본인 계좌 확인
        if (!isMyAccount(accountId)) {
            System.out.println("본인 계좌만 이용 가능합니다.");
            mainMenu();
            return;
        }
        
        System.out.print("계좌 비밀번호: ");
        String password = scanner.nextLine();
        
        try {
            // 계좌 정보 조회 및 비밀번호 확인
            String accountSql = "SELECT account_password, account_holder_name, account_alias, balance " +
                               "FROM accounts WHERE account_id = ?";
            PreparedStatement accountPstmt = conn.prepareStatement(accountSql);
            accountPstmt.setString(1, accountId);
            
            ResultSet accountRs = accountPstmt.executeQuery();
            if (accountRs.next()) {
                String dbPassword = accountRs.getString("account_password");
                
                if (!password.equals(dbPassword)) {
                    System.out.println("계좌 비밀번호가 틀렸습니다.");
                    accountRs.close();
                    accountPstmt.close();
                    mainMenu();
                    return;
                }
                
                String holderName = accountRs.getString("account_holder_name");
                String alias = accountRs.getString("account_alias");
                double balance = accountRs.getDouble("balance");
                
                System.out.println();
                System.out.println("=".repeat(80));
                System.out.println("                           거래내역");
                System.out.println("=".repeat(80));
                System.out.println("계좌번호: " + accountId + " | 현재 잔액: " + formatter.format(balance) + "원");
                System.out.println("계좌주: " + holderName + " | 계좌별명: " + alias);
                System.out.println("-".repeat(80));
                
                // 거래내역 조회
                String transactionSql = "SELECT transaction_type, amount, detail, transaction_date " +
                                       "FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
                PreparedStatement transactionPstmt = conn.prepareStatement(transactionSql);
                transactionPstmt.setString(1, accountId);
                
                ResultSet transactionRs = transactionPstmt.executeQuery();
                boolean hasTransactions = false;
                
                while (transactionRs.next()) {
                    hasTransactions = true;
                    String type = transactionRs.getString("transaction_type");
                    double amount = transactionRs.getDouble("amount");
                    String detail = transactionRs.getString("detail");
                    Timestamp timestamp = transactionRs.getTimestamp("transaction_date");
                    
                    System.out.printf("[%s] %s | %s원 | %s\n",
                                    timestamp.toString(),
                                    type,
                                    formatter.format(amount),
                                    detail);
                }
                
                if (!hasTransactions) {
                    System.out.println("해당 계좌의 거래내역이 없습니다.");
                }
                
                System.out.println("=".repeat(80));
                
                transactionRs.close();
                transactionPstmt.close();
            } else {
                System.out.println("계좌를 찾을 수 없습니다.");
            }
            
            accountRs.close();
            accountPstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            exit();
        }
        
        mainMenu();
    }
    
    /**
     * 이자 계산 기능 (관리자)
     */
    private void calculateInterest() {
        System.out.println("[이자 계산]");
        
        if (printSubMenu().equals("1")) {
            try {
                LocalDate today = LocalDate.now();
                boolean hasInterestPayment = false;
                
                System.out.println("=== 이자 계산 시스템 시작 ===");
                
                // 모든 계좌 조회
                String sql = "SELECT account_id, balance, interest_rate, created_date, last_interest_date " +
                           "FROM accounts";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    String accountId = rs.getString("account_id");
                    double balance = rs.getDouble("balance");
                    double interestRate = rs.getDouble("interest_rate");
                    Date createdDate = rs.getDate("created_date");
                    Date lastInterestDate = rs.getDate("last_interest_date");
                    
                    // 이자 지급일 계산
                    LocalDate lastInterestLocalDate = lastInterestDate.toLocalDate();
                    int interestDay = getAccountInterestDay(createdDate);
                    LocalDate checkDate = lastInterestLocalDate.plusMonths(1).withDayOfMonth(interestDay);
                    
                    while (!checkDate.isAfter(today)) {
                        if (balance > 0 && interestRate > 0) {
                            double interest = balance * (interestRate / 100.0 / 12.0);
                            
                            // 잔액 업데이트
                            String updateSql = "UPDATE accounts SET balance = balance + ?, last_interest_date = ? WHERE account_id = ?";
                            PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
                            updatePstmt.setDouble(1, interest);
                            updatePstmt.setDate(2, java.sql.Date.valueOf(checkDate));
                            updatePstmt.setString(3, accountId);
                            updatePstmt.executeUpdate();
                            updatePstmt.close();
                            
                            // 거래내역 기록
                            String detail = String.format("월 이자 지급 (이자율: %.1f%%, 지급일: 매월 %d일)", 
                                                         interestRate, interestDay);
                            recordTransaction(accountId, "이자", interest, detail);
                            
                            System.out.println("[" + accountId + "] " + checkDate + " 이자 지급: " + 
                                             formatter.format(interest) + "원");
                            hasInterestPayment = true;
                            
                            balance += interest; // 다음 계산을 위해 잔액 업데이트
                        }
                        checkDate = checkDate.plusMonths(1);
                    }
                }
                
                if (!hasInterestPayment) {
                    System.out.println("지급할 이자가 없습니다.");
                }
                System.out.println("=== 이자 계산 완료 ===");
                
                rs.close();
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                exit();
            }
        }
        
        mainMenu();
    }
    
    /**
     * 보조메뉴 출력
     */
    private String printSubMenu() {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("보조메뉴: 1.확인 | 2.취소");
        System.out.print("메뉴선택: ");
        return scanner.nextLine();
    }
    
    // ========== 유틸리티 메소드 ==========
    
    /**
     * 사용자 ID 중복 확인
     */
    private boolean isUserIdExists(String userId) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            boolean exists = false;
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
            
            rs.close();
            pstmt.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 본인 계좌 확인
     */
    private boolean isMyAccount(String accountId) {
        if (loginUserId == null) return false;
        
        try {
            String sql = "SELECT owner_id FROM accounts WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accountId);
            
            ResultSet rs = pstmt.executeQuery();
            boolean isOwner = false;
            if (rs.next()) {
                String ownerId = rs.getString("owner_id");
                isOwner = loginUserId.equals(ownerId);
            }
            
            rs.close();
            pstmt.close();
            return isOwner;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 계좌번호 자동 생성
     */
    private String generateAccountId() {
        String bankCode = "123";
        String branchCode = "456";
        int accountNumber = 100000;
        
        String newAccountId;
        do {
            accountNumber++;
            String accountDigits = String.format("%06d", accountNumber);
            newAccountId = bankCode + "-" + branchCode + "-" + accountDigits;
        } while (accountExists(newAccountId));
        
        return newAccountId;
    }
    
    /**
     * 계좌 존재 확인
     */
    private boolean accountExists(String accountId) {
        try {
            String sql = "SELECT COUNT(*) FROM accounts WHERE account_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, accountId);
            
            ResultSet rs = pstmt.executeQuery();
            boolean exists = false;
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
            
            rs.close();
            pstmt.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 거래내역 기록
     */
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
    
    /**
     * 계좌별 이자 지급일 계산
     */
    private int getAccountInterestDay(Date createdDate) {
        LocalDate created = createdDate.toLocalDate();
        int day = created.getDayOfMonth();
        return (day > 28) ? 28 : day; // 28일 이후는 28일로 조정
    }
    
    /**
     * 프로그램 종료
     */
    private void exit() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("데이터베이스 연결이 종료되었습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("은행 시스템이 정상적으로 종료되었습니다.");
        System.exit(0);
    }
    
    /**
     * 메인 메소드 - 프로그램 시작점
     */
    public static void main(String[] args) {
        System.out.println("=== 은행 시스템에 오신 것을 환영합니다! ===");
        BankSystem bankSystem = new BankSystem();
        
        // 메인 메뉴로 바로 시작
        bankSystem.mainMenu();
    }
}
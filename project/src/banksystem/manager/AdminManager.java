package banksystem.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import banksystem.entity.InterestPayment;
import banksystem.entity.Transaction;
import banksystem.entity.InterestInfo;  // ✅ 이미 추가되어 있음
import banksystem.helper.InputHelper;
import banksystem.helper.ValidationHelper;
import banksystem.util.InterestCalculator;
import banksystem.util.BankUtils;

public class AdminManager {
    private Connection conn;
    private ValidationHelper validator;
    private InputHelper inputHelper;
    private Scanner scanner;
    
    // 하드코딩된 관리자 계정 정보
    private static final String ADMIN_ID = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NAME = "시스템관리자";

    public AdminManager(Connection conn, ValidationHelper validator, InputHelper inputHelper, Scanner scanner) {
        this.conn = conn;
        this.validator = validator;
        this.inputHelper = inputHelper;
        this.scanner = scanner;
    }

    // 관리자 존재 여부 확인 
    public boolean checkAdminExists(String adminId) {
        return ADMIN_ID.equals(adminId);
    }

    // 관리자 비밀번호 확인 
    public boolean checkAdminPassword(String adminId, String password) {
        return ADMIN_ID.equals(adminId) && ADMIN_PASSWORD.equals(password);
    }

    // 관리자 ID로 관리자 이름 조회 
    public String getAdminName(String adminId) {
        if (ADMIN_ID.equals(adminId)) {
            return ADMIN_NAME;
        }
        return adminId; 
    }

    // 관리자 로그인 처리 
    public String adminLogin() {
        System.out.println("[관리자 로그인]");

        // 아이디 검증
        String adminId;
        do {
            adminId = inputHelper.input("관리자 아이디: ");
            if (checkAdminExists(adminId)) {
                break;
            } else {
                System.out.println("존재하지 않는 관리자 아이디입니다.");
                System.out.println("관리자 아이디를 다시 확인해주시거나 시스템 관리자에게 문의하세요.");
                System.out.println("시스템관리자: 내선 999");
            }
        } while (true);

        // 비밀번호 검증
        String password;
        do {
            password = inputHelper.input("관리자 비밀번호: ");
            if (checkAdminPassword(adminId, password)) {
                break;
            } else {
                System.out.println("비밀번호가 일치하지 않습니다.");
                System.out.println("관리자 비밀번호를 다시 확인해주시거나 시스템 관리자에게 문의하세요.");
                System.out.println("시스템관리자: 내선 999");
            }
        } while (true);

        // 최종 확인
        if (inputHelper.confirmAction()) {
            System.out.println("✅ 관리자 로그인 성공!");
            return adminId;
        }
        return null;
    }

    // 전체 계좌 목록 조회 (관리자용)
    public void viewAllAccounts() {
        System.out.println("\n[전체 계좌 목록 - 관리자 조회]");
        System.out.println("============================================================================================================================");
        System.out.println("계좌번호\t\t계좌명\t\t\t계좌종류\t\t소유자\t\t잔액\t\t\t이자율\t\t계좌 개설일자");
        System.out.println("============================================================================================================================");

        String sql = "SELECT a.*, u.user_name FROM accounts a " +
                    "JOIN users u ON a.user_id = u.user_id " +
                    "ORDER BY a.create_date DESC";

        int accountCount = 0;
        int savingsCount = 0;      // 보통예금 수
        int fixedCount = 0;        // 정기예금 수
        int installmentCount = 0;  // 적금 수
        int todayNewAccounts = 0;  // 오늘 신규 계좌
        
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accountCount++;
                    
                    String accountName = rs.getString("account_name");
                    String displayAccountName = accountName.length() > 12 ? 
                        accountName.substring(0, 12) + ".." : accountName;

                    double balance = rs.getDouble("balance");
                    double interestRate = rs.getDouble("interest_rate");
                    String accountType = rs.getString("account_type");
                    java.sql.Date createDate = rs.getDate("create_date");
                    
                    // 계좌 종류별 카운트
                    switch (accountType) {
                        case "보통예금" -> savingsCount++;
                        case "정기예금" -> fixedCount++;
                        case "적금" -> installmentCount++;
                    }
                    
                    // 오늘 개설된 계좌 카운트
                    if (createDate != null && createDate.toString().equals(today.toString())) {
                        todayNewAccounts++;
                    }

                    System.out.println(
                        rs.getString("account_id") + "\t" +
                        displayAccountName + "\t\t" +
                        accountType + "\t\t" +
                        rs.getString("user_name") + "\t\t" +
                        formatCurrency(balance) + "\t\t" +
                        String.format("%.1f%%", interestRate * 100) + "\t\t" +
                        createDate
                    );
                }

                if (accountCount == 0) {
                    System.out.println("등록된 계좌가 없습니다.");
                } else {
                	System.out.println("============================================================================================================================");
                    System.out.println("   계좌 관리 현황");
                    System.out.println("   총 계좌 수: " + accountCount + "개");
                    System.out.println("   계좌 종류별 현황:");
                    System.out.println("     - 보통예금: " + savingsCount + "개 (" + String.format("%.1f%%", (double)savingsCount/accountCount*100) + ")");
                    System.out.println("     - 정기예금: " + fixedCount + "개 (" + String.format("%.1f%%", (double)fixedCount/accountCount*100) + ")");
                    System.out.println("     - 적금: " + installmentCount + "개 (" + String.format("%.1f%%", (double)installmentCount/accountCount*100) + ")");
                    if (todayNewAccounts > 0) {
                        System.out.println("    오늘 신규 개설: " + todayNewAccounts + "개");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("전체 계좌 목록을 불러올 수 없습니다.");
            System.out.println("시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            System.out.println("시스템관리자: 내선 999");
        }
    }

    // 특정 사용자의 계좌 조회 (관리자용)
    public void viewUserAccounts() {
        System.out.println("[사용자별 계좌 조회]");
        String userId = inputHelper.input("조회할 사용자 아이디: ");

        // 사용자 존재 여부 확인
        String sql = "SELECT user_name FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("존재하지 않는 사용자입니다.");
                    System.out.println("사용자 아이디를 다시 확인해주세요.");
                    return;
                }
                
                String userName = rs.getString("user_name");
                System.out.println("\n[" + userName + "(" + userId + ") 계좌 목록]");
                System.out.println("============================================================================================================================");
                System.out.println("계좌번호\t\t계좌명\t\t\t계좌종류\t\t잔액\t\t개설일");
                System.out.println("============================================================================================================================");
                
                // 해당 사용자의 계좌 목록 조회
                String accountSql = "SELECT * FROM accounts WHERE user_id = ? ORDER BY create_date DESC";
                try (PreparedStatement accountPstmt = conn.prepareStatement(accountSql)) {
                    accountPstmt.setString(1, userId);
                    try (ResultSet accountRs = accountPstmt.executeQuery()) {
                        boolean hasAccounts = false;
                        double totalBalance = 0;
                        
                        while (accountRs.next()) {
                            hasAccounts = true;
                            
                            String accountName = accountRs.getString("account_name");
                            String displayAccountName = accountName.length() > 12 ? 
                                accountName.substring(0, 12) + ".." : accountName;

                            double balance = accountRs.getDouble("balance");
                            totalBalance += balance;

                            System.out.println(
                                accountRs.getString("account_id") + "\t" +
                                displayAccountName + "\t\t" +
                                accountRs.getString("account_type") + "\t\t" +
                                formatCurrency(balance) + "\t" +
                                accountRs.getDate("create_date")
                            );
                        }
                        
                        if (!hasAccounts) {
                            System.out.println("해당 사용자의 계좌가 없습니다.");
                        } else {
                        	System.out.println("============================================================================================================================");
                            System.out.println("해당 사용자 계좌 잔액 합계: " + formatCurrency(totalBalance));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("사용자 계좌 조회 중 오류가 발생했습니다.");
            System.out.println("시스템 오류입니다. 시스템 관리자에게 연락해주세요.");
            System.out.println("시스템관리자: 내선 999");
        }
    }

    // 이자 지급 대상 계좌 조회
    public void viewInterestTargets() {
        System.out.println("\n[이자 지급 대상 조회]");
        System.out.println("============================================================================================================================");
        System.out.println("계좌번호\t\t소유자\t\t계좌종류\t\t원금\t\t\t이자율\t경과일\t예상이자");
        System.out.println("============================================================================================================================");

        String sql = "SELECT a.*, u.user_name FROM accounts a " +
                    "JOIN users u ON a.user_id = u.user_id " +
                    "ORDER BY a.account_type, a.create_date";

        int targetCount = 0;
        double totalInterest = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String accountId = rs.getString("account_id");
                    
                    // ✅ 수정: InterestCalculator.InterestInfo → InterestInfo
                    InterestInfo interestInfo = 
                        InterestCalculator.calculateAccountInterest(conn, accountId);
                    
                    if (interestInfo != null && interestInfo.getInterestAmount() > 0) {
                        targetCount++;
                        totalInterest += interestInfo.getInterestAmount();
                        
                        System.out.println(
                            accountId + "\t" +
                            rs.getString("user_name") + "\t\t" +
                            rs.getString("account_type") + "\t\t" +
                            formatCurrency(interestInfo.getPrincipal()) + "\t\t" +
                            String.format("%.1f%%", interestInfo.getInterestRate() * 100) + "\t" +
                            interestInfo.getDays() + "일\t" +
                            formatCurrency(interestInfo.getInterestAmount())
                        );
                    }
                }
                
                if (targetCount == 0) {
                    System.out.println("현재 이자 지급 대상 계좌가 없습니다.");
                } else {
                	System.out.println("============================================================================================================================");
                    System.out.println("총 지급 대상: " + targetCount + "개 계좌");
                    System.out.println("총 지급 예정 이자: " + formatCurrency(totalInterest));
                }
            }
        } catch (SQLException e) {
            System.out.println("이자 지급 대상 조회 중 오류가 발생했습니다.");
            System.out.println("시스템 오류입니다. 시스템 관리자에게 연락해주세요.");
            System.out.println("시스템관리자: 내선 999");
        }
    }

    // 이자 일괄 지급 실행
    public void executeInterestPayment(String adminId) {
        System.out.println("\n[이자 일괄 지급 실행]");
        
        // 1. 이자 지급 대상 목록 수집
        List<InterestInfo> targets = getInterestTargets();  // ✅ 수정
        
        if (targets.isEmpty()) {
            System.out.println("현재 이자 지급 대상 계좌가 없습니다.");
            return;
        }
        
        // 2. 지급 예정 정보 표시
        System.out.println("============================================================================================================================");
        System.out.printf("총 %d개 계좌에 이자를 지급합니다.%n", targets.size());
        
        double totalInterest = targets.stream()
            .mapToDouble(InterestInfo::getInterestAmount)  // ✅ 수정
            .sum();
        System.out.println("총 지급 예정 이자: " + formatCurrency(totalInterest));
        System.out.println("============================================================================================================================");
        
        // 3. 최종 확인
        if (!inputHelper.confirmAction()) {
            System.out.println("이자 지급이 취소되었습니다.");
            return;
        }
        
        // 4. 이자 일괄 지급 실행
        int successCount = 0;
        int failCount = 0;
        
        System.out.println("\n이자 지급을 실행합니다...");
        
        for (InterestInfo target : targets) {  // ✅ 수정
            try {
                conn.setAutoCommit(false);
                
                // 4-1. 계좌 잔액 업데이트
                if (!updateAccountBalance(target.getAccountId(), 
                        target.getPrincipal() + target.getInterestAmount())) {
                    throw new SQLException("계좌 잔액 업데이트 실패");
                }
                
                // 4-2. 마지막 이자 지급일 업데이트
                if (!updateLastInterestDate(target.getAccountId())) {
                    throw new SQLException("이자 지급일 업데이트 실패");
                }
                
                // 4-3. 이자 지급 내역 저장
                InterestPayment payment = new InterestPayment(
                    BankUtils.generatePaymentId(conn),
                    target.getAccountId(),
                    target.getInterestAmount(),
                    adminId
                );
                
                if (!saveInterestPayment(payment)) {
                    throw new SQLException("이자 지급 내역 저장 실패");
                }
                
                // 4-4. 거래 내역 생성 (이자 입금)
                Transaction transaction = new Transaction();
                transaction.setTransactionId(BankUtils.generateTransactionId(conn));
                transaction.setAccountId(target.getAccountId());
                transaction.setTransactionType("이자입금");
                transaction.setAmount(target.getInterestAmount());
                transaction.setBalanceAfter(target.getPrincipal() + target.getInterestAmount());
                transaction.setTransactionMemo("정기 이자 지급");
                
                if (!saveTransaction(transaction)) {
                    throw new SQLException("거래 내역 저장 실패");
                }
                
                conn.commit();
                successCount++;
                
                System.out.printf("✅ %s: %s 지급 완료%n", 
                    target.getAccountId(), formatCurrency(target.getInterestAmount()));
                
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("롤백 처리 중 오류가 발생했습니다.");
                    System.out.println("시스템관리자: 내선 999 (즉시 연락 필요)");
                }
                failCount++;
                System.out.printf("%s: 지급 실패%n", target.getAccountId());
                System.out.println("시스템 오류로 인해 이자 지급에 실패했습니다.");
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("시스템 설정 복구 중 오류가 발생했습니다.");
                    System.out.println("시스템관리자: 내선 999 (즉시 연락 필요)");
                }
            }
        }
        
        // 5. 결과 보고
        System.out.println("============================================================================================================================");
        System.out.println("✅ 이자 지급 완료!");
        System.out.printf("성공: %d건, 실패: %d건%n", successCount, failCount);
        System.out.println("총 지급 금액: " + formatCurrency(
            targets.stream()
                .limit(successCount)
                .mapToDouble(InterestInfo::getInterestAmount)  // ✅ 수정
                .sum()
        ));
        
        if (failCount > 0) {
            System.out.println("실패한 계좌가 있습니다. 시스템 관리자에게 연락해주세요.");
            System.out.println("시스템관리자: 내선 999");
        }
    }

    // 이자 지급 내역 조회
    public void viewInterestHistory() {
        System.out.println("\n[이자 지급 내역 조회]");
        System.out.println("============================================================================================================================");
        System.out.println("지급번호\t\t계좌번호\t\t소유자\t\t지급일\t\t\t이자금액\t\t관리자");
        System.out.println("============================================================================================================================");

        String sql = "SELECT ip.*, u.user_name " +
                     "FROM interest_payments ip " +
                     "JOIN accounts acc ON ip.account_id = acc.account_id " +
                     "JOIN users u ON acc.user_id = u.user_id " +
                     "ORDER BY ip.payment_date DESC";

        double totalPaid = 0;
        int recordCount = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recordCount++;
                    double interestAmount = rs.getDouble("interest_amount");
                    totalPaid += interestAmount;
                    
                    // 관리자 ID에서 실제 이름 표시
                    String adminId = rs.getString("admin_id");
                    String adminDisplayName = getAdminName(adminId);

                    System.out.println(
                        rs.getString("payment_id") + "\t" +
                        rs.getString("account_id") + "\t" +
                        rs.getString("user_name") + "\t\t" +
                        rs.getTimestamp("payment_date") + "\t" +
                        formatCurrency(interestAmount) + "\t\t" +
                        adminDisplayName
                    );
                }

                if (recordCount == 0) {
                    System.out.println("이자 지급 내역이 없습니다.");
                } else {
                	System.out.println("============================================================================================================================");
                    System.out.println("총 지급 건수: " + recordCount + "건");
                    System.out.println("총 지급 금액: " + formatCurrency(totalPaid));
                }
            }
        } catch (SQLException e) {
            System.out.println("이자 지급 내역을 불러올 수 없습니다.");
            System.out.println("시스템 오류가 발생했습니다. 시스템 관리자에게 연락해주세요.");
            System.out.println("시스템관리자: 내선 999");
        }
    }

    // 이자 지급 대상 목록 수집
    private List<InterestInfo> getInterestTargets() {  // ✅ 수정
        List<InterestInfo> targets = new ArrayList<>();  // ✅ 수정
        
        String sql = "SELECT account_id FROM accounts ORDER BY account_type, create_date";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String accountId = rs.getString("account_id");
                    InterestInfo interestInfo =   // ✅ 수정
                        InterestCalculator.calculateAccountInterest(conn, accountId);
                    
                    if (interestInfo != null && interestInfo.getInterestAmount() > 0) {
                        targets.add(interestInfo);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("이자 지급 대상 수집 중 오류가 발생했습니다.");
            System.out.println("시스템 오류입니다. 시스템 관리자에게 연락해주세요.");
            System.out.println("시스템관리자: 내선 999");
        }
        
        return targets;
    }

    // 계좌 잔액 업데이트
    private boolean updateAccountBalance(String accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 마지막 이자 지급일 업데이트
    private boolean updateLastInterestDate(String accountId) {
        String sql = "UPDATE accounts SET last_interest_date = SYSDATE WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 이자 지급 내역 저장
    private boolean saveInterestPayment(InterestPayment payment) {
        String sql = "INSERT INTO interest_payments (payment_id, account_id, payment_date, " +
                    "interest_amount, admin_id) VALUES (?, ?, SYSDATE, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, payment.getPaymentId());
            pstmt.setString(2, payment.getAccountId());
            pstmt.setDouble(3, payment.getInterestAmount());
            pstmt.setString(4, payment.getAdminId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 거래 내역 저장
    private boolean saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (transaction_id, transaction_date, account_id, " +
                    "transaction_type, amount, balance_after, transaction_memo) " +
                    "VALUES (?, SYSDATE, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setString(2, transaction.getAccountId());
            pstmt.setString(3, transaction.getTransactionType());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setDouble(5, transaction.getBalanceAfter());
            pstmt.setString(6, transaction.getTransactionMemo());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // 금액 포맷팅 유틸리티 메소드
    private String formatCurrency(double amount) {
        return String.format("%,.0f원", amount);
    }
}
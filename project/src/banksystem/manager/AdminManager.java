package banksystem.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import banksystem.entity.Admin;
import banksystem.entity.InterestPayment;
import banksystem.entity.Transaction;
import banksystem.helper.InputHelper;
import banksystem.helper.ValidationHelper;
import banksystem.util.InterestCalculator;
import banksystem.util.BankUtils;

public class AdminManager {
    private Connection conn;
    private ValidationHelper validator;
    private InputHelper inputHelper;
    private Scanner scanner;

    public AdminManager(Connection conn, ValidationHelper validator, InputHelper inputHelper, Scanner scanner) {
        this.conn = conn;
        this.validator = validator;
        this.inputHelper = inputHelper;
        this.scanner = scanner;
    }

    // 관리자 존재 여부 확인
    public boolean checkAdminExists(String adminId) {
        String sql = "SELECT COUNT(*) FROM admins WHERE admin_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("관리자 조회 오류: " + e.getMessage());
            return false;
        }
    }

    // 관리자 비밀번호 확인
    public boolean checkAdminPassword(String adminId, String password) {
        String sql = "SELECT admin_password FROM admins WHERE admin_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return password.equals(rs.getString("admin_password"));
                }
            }
        } catch (SQLException e) {
            System.out.println("관리자 비밀번호 확인 오류: " + e.getMessage());
        }
        return false;
    }

    // 관리자 ID로 관리자 이름 조회
    public String getAdminName(String adminId) {
        String sql = "SELECT admin_name FROM admins WHERE admin_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, adminId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("admin_name");
            }
        } catch (SQLException e) {
            System.out.println("관리자 이름 조회 오류: " + e.getMessage());
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
                System.out.println("존재하지 않는 관리자 아이디입니다. 다시 입력해주세요.");
            }
        } while (true);

        // 비밀번호 검증
        String password;
        do {
            password = inputHelper.input("관리자 비밀번호: ");
            if (checkAdminPassword(adminId, password)) {
                break;
            } else {
                System.out.println("비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
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

        double totalBalance = 0;
        int accountCount = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accountCount++;
                    
                    String accountName = rs.getString("account_name");
                    String displayAccountName = accountName.length() > 12 ? 
                        accountName.substring(0, 12) + ".." : accountName;

                    double balance = rs.getDouble("balance");
                    totalBalance += balance;
                    
                    double interestRate = rs.getDouble("interest_rate");

                    System.out.println(
                        rs.getString("account_id") + "\t" +
                        displayAccountName + "\t\t" +
                        rs.getString("account_type") + "\t\t" +
                        rs.getString("user_name") + "\t\t" +
                        formatCurrency(balance) + "\t\t" +
                        String.format("%.1f%%", interestRate * 100) + "\t\t" +
                        rs.getDate("create_date")
                    );
                }

                if (accountCount == 0) {
                    System.out.println("등록된 계좌가 없습니다.");
                } else {
                	System.out.println("============================================================================================================================");
                    System.out.println("총 계좌 수: " + accountCount + "개");
                    System.out.println("전체 잔액 합계: " + formatCurrency(totalBalance));
                }
            }
        } catch (SQLException e) {
            System.out.println("전체 계좌 목록 조회 오류: " + e.getMessage());
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
            System.out.println("사용자 계좌 조회 오류: " + e.getMessage());
        }
    }

    // ==================== 이자 관련 기능들 (새로 추가) ====================

    /**
     * 이자 지급 대상 계좌 조회
     */
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
                    
                    // 이자 계산
                    InterestCalculator.InterestInfo interestInfo = 
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
            System.out.println("이자 지급 대상 조회 오류: " + e.getMessage());
        }
    }

    /**
     * 이자 일괄 지급 실행
     */
    public void executeInterestPayment(String adminId) {
        System.out.println("\n[이자 일괄 지급 실행]");
        
        // 1. 이자 지급 대상 목록 수집
        List<InterestCalculator.InterestInfo> targets = getInterestTargets();
        
        if (targets.isEmpty()) {
            System.out.println("현재 이자 지급 대상 계좌가 없습니다.");
            return;
        }
        
        // 2. 지급 예정 정보 표시
        System.out.println("============================================================================================================================");
        System.out.printf("총 %d개 계좌에 이자를 지급합니다.%n", targets.size());
        
        double totalInterest = targets.stream()
            .mapToDouble(InterestCalculator.InterestInfo::getInterestAmount)
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
        
        for (InterestCalculator.InterestInfo target : targets) {
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
                    System.out.println("롤백 오류: " + ex.getMessage());
                }
                failCount++;
                System.out.printf("❌ %s: 지급 실패 - %s%n", 
                    target.getAccountId(), e.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("자동커밋 설정 오류: " + e.getMessage());
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
                .mapToDouble(InterestCalculator.InterestInfo::getInterestAmount)
                .sum()
        ));
    }

    /**
     * 이자 지급 내역 조회
     */
    public void viewInterestHistory() {
        System.out.println("\n[이자 지급 내역 조회]");
        System.out.println("============================================================================================================================");
        System.out.println("지급번호\t\t계좌번호\t\t소유자\t\t지급일\t\t\t이자금액\t\t관리자");
        System.out.println("============================================================================================================================");

        String sql = "SELECT ip.*, u.user_name, a.admin_name " +
                    "FROM interest_payments ip " +
                    "JOIN accounts acc ON ip.account_id = acc.account_id " +
                    "JOIN users u ON acc.user_id = u.user_id " +
                    "JOIN admins a ON ip.admin_id = a.admin_id " +
                    "ORDER BY ip.payment_date DESC";

        double totalPaid = 0;
        int recordCount = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    recordCount++;
                    double interestAmount = rs.getDouble("interest_amount");
                    totalPaid += interestAmount;

                    System.out.println(
                        rs.getString("payment_id") + "\t" +
                        rs.getString("account_id") + "\t" +
                        rs.getString("user_name") + "\t\t" +
                        rs.getTimestamp("payment_date") + "\t" +
                        formatCurrency(interestAmount) + "\t\t" +
                        rs.getString("admin_name")
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
            System.out.println("이자 지급 내역 조회 오류: " + e.getMessage());
        }
    }

    // ==================== 이자 관련 헬퍼 메소드들 ====================

    /**
     * 이자 지급 대상 목록 수집
     */
    private List<InterestCalculator.InterestInfo> getInterestTargets() {
        List<InterestCalculator.InterestInfo> targets = new ArrayList<>();
        
        String sql = "SELECT account_id FROM accounts ORDER BY account_type, create_date";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String accountId = rs.getString("account_id");
                    InterestCalculator.InterestInfo interestInfo = 
                        InterestCalculator.calculateAccountInterest(conn, accountId);
                    
                    if (interestInfo != null && interestInfo.getInterestAmount() > 0) {
                        targets.add(interestInfo);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("이자 지급 대상 수집 오류: " + e.getMessage());
        }
        
        return targets;
    }

    /**
     * 계좌 잔액 업데이트
     */
    private boolean updateAccountBalance(String accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("잔액 업데이트 오류: " + e.getMessage());
            return false;
        }
    }

    /**
     * 마지막 이자 지급일 업데이트
     */
    private boolean updateLastInterestDate(String accountId) {
        String sql = "UPDATE accounts SET last_interest_date = SYSDATE WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("이자 지급일 업데이트 오류: " + e.getMessage());
            return false;
        }
    }

    /**
     * 이자 지급 내역 저장
     */
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
            System.out.println("이자 지급 내역 저장 오류: " + e.getMessage());
            return false;
        }
    }

    /**
     * 거래 내역 저장
     */
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
            System.out.println("거래 내역 저장 오류: " + e.getMessage());
            return false;
        }
    }

    // 금액 포맷팅 유틸리티 메소드
    private String formatCurrency(double amount) {
        return String.format("%,.0f원", amount);
    }
}
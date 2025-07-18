package banksystem.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import banksystem.entity.Transaction;
import banksystem.helper.InputHelper;
import banksystem.util.BankUtils;

public class TransactionManager {
    private Connection conn;
    private InputHelper inputHelper;
    private AccountManager accountManager;
    private Scanner scanner;

    // 거래 한도 상수 정의
    private static final double DEPOSIT_DAILY_LIMIT = 10000000;   // 입금 1일 1천만원
    private static final double DEPOSIT_SINGLE_LIMIT = 5000000;   // 입금 1회 5백만원
    
    private static final double WITHDRAW_DAILY_LIMIT = 5000000;   // 출금 1일 500만원
    private static final double WITHDRAW_SINGLE_LIMIT = 1000000;  // 출금 1회 100만원 
    
    private static final double TRANSFER_DAILY_LIMIT = 5000000;   // 이체 1일 5백만원
    private static final double TRANSFER_SINGLE_LIMIT = 2000000;  // 이체 1회 2백만원

    public TransactionManager(Connection conn, InputHelper inputHelper,
                             AccountManager accountManager, Scanner scanner) {
        this.conn = conn;
        this.inputHelper = inputHelper;
        this.accountManager = accountManager;  
        this.scanner = scanner;
    }
    
    // AccountManager 설정 메소드
    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    // 오늘 특정 거래 유형의 총 금액 조회
    private double getTodayTransactionAmount(String accountId, String transactionType) {
        String sql = "SELECT NVL(SUM(amount), 0) FROM transactions " +
                    "WHERE account_id = ? AND transaction_type = ? " +
                    "AND TRUNC(transaction_date) = TRUNC(SYSDATE)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.setString(2, transactionType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("일일 거래금액 조회 오류: " + e.getMessage());
        }
        return 0;
    }

 // 거래 한도 체크 (사용자 표시용 거래 유형 개선)
    private boolean checkTransactionLimit(String accountId, String transactionType, double amount) {
        double singleLimit, dailyLimit;
        String displayType; // 사용자에게 표시할 거래 유형
        
        // 거래 유형별 한도 설정 및 표시 유형 설정
        switch (transactionType) {
            case "입금":
                singleLimit = DEPOSIT_SINGLE_LIMIT;
                dailyLimit = DEPOSIT_DAILY_LIMIT;
                displayType = "입금";
                break;
            case "출금":
                singleLimit = WITHDRAW_SINGLE_LIMIT;
                dailyLimit = WITHDRAW_DAILY_LIMIT;
                displayType = "출금";
                break;
            case "이체출금":
                singleLimit = TRANSFER_SINGLE_LIMIT;
                dailyLimit = TRANSFER_DAILY_LIMIT;
                displayType = "이체"; // 사용자에게는 "이체"로 표시
                break;
            default:
                return true; // 기타 거래는 제한 없음
        }

        // 1회 한도 체크
        if (amount > singleLimit) {
            System.out.println("❌ 1회 " + displayType + " 한도를 초과했습니다.");
            System.out.println("   1회 한도: " + BankUtils.formatCurrency(singleLimit));
            System.out.println("   요청 금액: " + BankUtils.formatCurrency(amount));
            return false;
        }

        // 1일 한도 체크
        double todayAmount = getTodayTransactionAmount(accountId, transactionType);
        if (todayAmount + amount > dailyLimit) {
            System.out.println("❌ 1일 " + displayType + " 한도를 초과했습니다.");
            System.out.println("   1일 한도: " + BankUtils.formatCurrency(dailyLimit));
            System.out.println("   오늘 사용액: " + BankUtils.formatCurrency(todayAmount));
            System.out.println("   요청 금액: " + BankUtils.formatCurrency(amount));
            System.out.println("   잔여 한도: " + BankUtils.formatCurrency(dailyLimit - todayAmount));
            
            // 거래 유형별 추가 안내 메시지
            System.out.println("오늘은 최대 " + BankUtils.formatCurrency(dailyLimit - todayAmount) + "까지 더 " + displayType + " 가능합니다.");
            
            return false;
        }

        return true;
    }

    // Transaction 객체를 DB에 저장
    public boolean saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (transaction_id, transaction_date, account_id, transaction_type, amount, "
                + "balance_after, counterpart_account, counterpart_name, depositor_name, "
                + "transaction_memo) VALUES (?, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getTransactionId());
            pstmt.setString(2, transaction.getAccountId());
            pstmt.setString(3, transaction.getTransactionType());
            pstmt.setDouble(4, transaction.getAmount());
            pstmt.setDouble(5, transaction.getBalanceAfter());
            pstmt.setString(6, transaction.getCounterpartAccount());
            pstmt.setString(7, transaction.getCounterpartName());
            pstmt.setString(8, transaction.getDepositorName());
            pstmt.setString(9, transaction.getTransactionMemo());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("거래내역 저장 오류: " + e.getMessage());
            return false;
        }
    }

    // 입금 처리 (한도 체크 후 재입력 가능)
    public void deposit(String loginId) {
        System.out.println("[입금]");
        String accountId = inputHelper.inputAccountId("계좌번호: ", false, loginId);
        String depositorName;

        if (accountManager.isMyAccount(accountId, loginId)) {
            System.out.println("본인 계좌 입금 - 로그인 인증으로 확인되었습니다.");
            depositorName = null;
        } else {
            System.out.println("타인 계좌 입금");
            depositorName = inputHelper.input("입금자명: ");
        }

        double amount;
        // 한도 체크를 통과할 때까지 반복
        do {
            amount = inputHelper.inputAmount("입금액: ");
            
            // 한도 체크 - 실패하면 다시 입력받기
            if (checkTransactionLimit(accountId, "입금", amount)) {
                break; // 한도 체크 통과시 반복문 종료
            }
            
            // 한도 초과시 재입력 여부 확인
            System.out.println("다시 입력하시겠습니까? (1: 예, 2: 아니오)");
            System.out.print("선택: ");
            String choice = scanner.nextLine();
            
            if (!"1".equals(choice)) {
                System.out.println("입금이 취소되었습니다.");
                return; // 사용자가 재입력을 원하지 않으면 메소드 종료
            }
            
        } while (true);

        System.out.print("입금 메모 (선택사항): ");
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty())
            memo = null;

        if (inputHelper.confirmAction()) {
            double currentBalance = accountManager.getBalance(accountId);
            double newBalance = currentBalance + amount;

            if (accountManager.updateAccountBalance(accountId, newBalance)) {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(BankUtils.generateTransactionId(conn));
                transaction.setAccountId(accountId);
                transaction.setTransactionType("입금");
                transaction.setAmount(amount);
                transaction.setBalanceAfter(newBalance);
                transaction.setDepositorName(depositorName);
                transaction.setTransactionMemo(memo);

                saveTransaction(transaction);

                System.out.println("✅ 입금이 완료되었습니다!");
                System.out.println("   입금액: " + BankUtils.formatCurrency(amount));
                if (accountManager.isMyAccount(accountId, loginId)) {
                    System.out.println("   현재잔액: " + BankUtils.formatCurrency(newBalance));
                }
            }
        }
    }

    // 출금 처리 (한도 체크 후 재입력 가능)
    public void withdraw(String loginId) {
        System.out.println("[출금]");
        String accountId = inputHelper.inputAccountId("계좌번호: ", true, loginId);

        if (!accountManager.checkPassword(accountId)) {
            return;
        }

        double currentBalance = accountManager.getBalance(accountId);
        double amount;
        
        // 잔액 체크 및 한도 체크를 통과할 때까지 반복
        do {
            amount = inputHelper.inputAmount("출금액: ");

            // 1. 잔액 체크
            if (currentBalance < amount) {
                System.out.println("잔액이 부족합니다. (현재 잔액: " + BankUtils.formatCurrency(currentBalance) + ")");
                
                // 재입력 여부 확인
                System.out.println("다시 입력하시겠습니까? (1: 예, 2: 아니오)");
                System.out.print("선택: ");
                String choice = scanner.nextLine();
                
                if (!"1".equals(choice)) {
                    System.out.println("출금이 취소되었습니다.");
                    return;
                }
                continue;
            }

            // 2. 한도 체크
            if (checkTransactionLimit(accountId, "출금", amount)) {
                break; // 모든 체크 통과시 반복문 종료
            }
            
            // 한도 초과시 재입력 여부 확인
            System.out.println("다시 입력하시겠습니까? (1: 예, 2: 아니오)");
            System.out.print("선택: ");
            String choice = scanner.nextLine();
            
            if (!"1".equals(choice)) {
                System.out.println("출금이 취소되었습니다.");
                return;
            }
            
        } while (true);

        System.out.print("출금 메모 (선택사항): ");
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty())
            memo = null;

        if (inputHelper.confirmAction()) {
            double newBalance = currentBalance - amount;

            if (accountManager.updateAccountBalance(accountId, newBalance)) {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(BankUtils.generateTransactionId(conn));
                transaction.setAccountId(accountId);
                transaction.setTransactionType("출금");
                transaction.setAmount(amount);
                transaction.setBalanceAfter(newBalance);
                transaction.setTransactionMemo(memo);

                saveTransaction(transaction);

                System.out.println("✅ 출금이 완료되었습니다!");
                System.out.println("   출금액: " + BankUtils.formatCurrency(amount));
                System.out.println("   잔여잔액: " + BankUtils.formatCurrency(newBalance));
            }
        }
    }

    // 이체 처리 (한도 체크 후 재입력 가능)
    public void transfer(String loginId) {
        System.out.println("[이체]");
        String fromAccountId = inputHelper.inputAccountId("출금 계좌번호: ", true, loginId);

        if (!accountManager.checkPassword(fromAccountId)) {
            return;
        }

        String toAccountId;
        do {
            toAccountId = inputHelper.inputAccountId("입금 계좌번호: ", false, loginId);
            if (!fromAccountId.equals(toAccountId))
                break;
            System.out.println("출금 계좌와 입금 계좌가 같을 수 없습니다.");
        } while (true);

        double currentBalance = accountManager.getBalance(fromAccountId);
        double amount;
        
        // 잔액 체크 및 한도 체크를 통과할 때까지 반복
        do {
            amount = inputHelper.inputAmount("이체금액: ");

            // 1. 잔액 체크
            if (currentBalance < amount) {
                System.out.println("잔액이 부족합니다. (현재 잔액: " + BankUtils.formatCurrency(currentBalance) + ")");
                
                // 재입력 여부 확인
                System.out.println("다시 입력하시겠습니까? (1: 예, 2: 아니오)");
                System.out.print("선택: ");
                String choice = scanner.nextLine();
                
                if (!"1".equals(choice)) {
                    System.out.println("이체가 취소되었습니다.");
                    return;
                }
                continue;
            }

            // 2. 한도 체크 (이체출금으로 체크)
            if (checkTransactionLimit(fromAccountId, "이체출금", amount)) {
                break; // 모든 체크 통과시 반복문 종료
            }
            
            // 한도 초과시 재입력 여부 확인
            System.out.println("다시 입력하시겠습니까? (1: 예, 2: 아니오)");
            System.out.print("선택: ");
            String choice = scanner.nextLine();
            
            if (!"1".equals(choice)) {
                System.out.println("이체가 취소되었습니다.");
                return;
            }
            
        } while (true);

        System.out.print("이체 메모 (선택사항): ");
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty())
            memo = null;

        if (inputHelper.confirmAction()) {
            try {
                conn.setAutoCommit(false);

                // 출금 처리
                double fromBalance = currentBalance - amount;
                if (!accountManager.updateAccountBalance(fromAccountId, fromBalance)) {
                    throw new SQLException("출금 처리 실패");
                }

                // 입금 처리
                double toCurrentBalance = accountManager.getBalance(toAccountId);
                double toBalance = toCurrentBalance + amount;
                if (!accountManager.updateAccountBalance(toAccountId, toBalance)) {
                    throw new SQLException("입금 처리 실패");
                }

                // 거래내역 저장
                String fromName = accountManager.getAccountHolderName(fromAccountId);
                String toName = accountManager.getAccountHolderName(toAccountId);

                // 출금 거래내역
                Transaction fromTransaction = new Transaction();
                fromTransaction.setTransactionId(BankUtils.generateTransactionId(conn));
                fromTransaction.setAccountId(fromAccountId);
                fromTransaction.setTransactionType("이체출금");
                fromTransaction.setAmount(amount);
                fromTransaction.setBalanceAfter(fromBalance);
                fromTransaction.setCounterpartAccount(toAccountId);
                fromTransaction.setCounterpartName(toName);
                fromTransaction.setTransactionMemo(memo);

                // 입금 거래내역
                Transaction toTransaction = new Transaction();
                toTransaction.setTransactionId(BankUtils.generateTransactionId(conn));
                toTransaction.setAccountId(toAccountId);
                toTransaction.setTransactionType("이체입금");
                toTransaction.setAmount(amount);
                toTransaction.setBalanceAfter(toBalance);
                toTransaction.setCounterpartAccount(fromAccountId);
                toTransaction.setCounterpartName(fromName);
                toTransaction.setTransactionMemo(memo);

                saveTransaction(fromTransaction);
                saveTransaction(toTransaction);

                conn.commit();

                System.out.println("✅ 이체가 완료되었습니다!");
                System.out.println("   이체금액: " + BankUtils.formatCurrency(amount));
                System.out.println("   출금계좌: " + fromAccountId + " (" + fromName + ")");
                System.out.println("   입금계좌: " + toAccountId + " (" + toName + ")");

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("롤백 오류: " + ex.getMessage());
                }
                System.out.println("이체 오류: " + e.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("자동커밋 설정 오류: " + e.getMessage());
                }
            }
        }
    }
    
    // 특정 계좌의 오늘 거래 사용량 조회 (입금/출금/이체별)
    public double[] getTodayUsageByAccount(String accountId) {
        double depositUsage = getTodayTransactionAmount(accountId, "입금");
        double withdrawUsage = getTodayTransactionAmount(accountId, "출금");
        double transferUsage = getTodayTransactionAmount(accountId, "이체출금");
        
        return new double[]{depositUsage, withdrawUsage, transferUsage};
    }
    
    //전체 거래 한도 정보 조회 (1회 한도, 1일 한도) 
    public String[] getTransactionLimits() {
        // ANSI 이스케이프 코드로 굵게 표시
        String BOLD = "\033[1m";
        String RESET = "\033[0m";
        
        String depositLimits = String.format(BOLD + "입금: 1회 최대 %s | 1일 최대 %s" + RESET, 
            BankUtils.formatCurrency(DEPOSIT_SINGLE_LIMIT), 
            BankUtils.formatCurrency(DEPOSIT_DAILY_LIMIT));
            
        String withdrawLimits = String.format(BOLD + "출금: 1회 최대 %s | 1일 최대 %s" + RESET, 
            BankUtils.formatCurrency(WITHDRAW_SINGLE_LIMIT), 
            BankUtils.formatCurrency(WITHDRAW_DAILY_LIMIT));
            
        String transferLimits = String.format(BOLD + "이체: 1회 최대 %s | 1일 최대 %s" + RESET, 
            BankUtils.formatCurrency(TRANSFER_SINGLE_LIMIT), 
            BankUtils.formatCurrency(TRANSFER_DAILY_LIMIT));
        
        return new String[]{depositLimits, withdrawLimits, transferLimits};
    }
    
    // 특정 계좌의 1일 잔여한도 계산 (입금/출금/이체별)
    public double[] getRemainingDailyLimits(String accountId) {
        double[] usage = getTodayUsageByAccount(accountId);
        
        double remainingDeposit = DEPOSIT_DAILY_LIMIT - usage[0];
        double remainingWithdraw = WITHDRAW_DAILY_LIMIT - usage[1];
        double remainingTransfer = TRANSFER_DAILY_LIMIT - usage[2];
        
        // 음수가 되지 않도록 보정 (한도 초과 시 0으로 표시)
        remainingDeposit = Math.max(0, remainingDeposit);
        remainingWithdraw = Math.max(0, remainingWithdraw);
        remainingTransfer = Math.max(0, remainingTransfer);
        
        return new double[]{remainingDeposit, remainingWithdraw, remainingTransfer};
    }

    // 계좌별 오늘 사용량을 포맷팅된 문자열로 반환
    public String getFormattedUsageByAccount(String accountId) {
        double[] usage = getTodayUsageByAccount(accountId);
        double[] remaining = getRemainingDailyLimits(accountId);
        
        // 1일 사용금액 (잔여한도 포함)만 표시
        return String.format("[1일 사용금액] 입금 %s(잔여한도:%s) / 출금 %s(잔여한도:%s) / 이체 %s(잔여한도:%s)",
            formatToWon(usage[0]), formatToWon(remaining[0]),
            formatToWon(usage[1]), formatToWon(remaining[1]),
            formatToWon(usage[2]), formatToWon(remaining[2]));
    }
    
    // 금액을 원 단위로 포맷팅 
    private String formatToWon(double amount) {
        return String.format("%,.0f원", amount);
    }

    // 거래내역 조회 
    public void history(String loginId) {
        System.out.println("[거래내역 조회]");
        String accountId = inputHelper.inputAccountId("계좌번호: ", true, loginId);

        if (!accountManager.checkPassword(accountId)) {
            return;
        }

        // 전체 거래내역 표시
        displayAllTransactions(accountId);
    }

    public void displayAllTransactions(String accountId) {
        System.out.println("\n[거래내역] 계좌번호: " + accountId + " (" + accountManager.getAccountHolderName(accountId) + ")");
        System.out.println("============================================================================================================================");
        
        int totalCount = getTotalTransactionCount(accountId);
        
        if (totalCount == 0) {
            System.out.println("거래내역이 없습니다.");
            return;
        }
        
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasTransactions = false;
                int index = totalCount; 
                
                while (rs.next()) {
                    hasTransactions = true;
                    
                    String transactionType = rs.getString("transaction_type");
                    String counterpartAccount = rs.getString("counterpart_account");
                    String counterpartName = rs.getString("counterpart_name");
                    String depositorName = rs.getString("depositor_name");
                    String memo = rs.getString("transaction_memo");

                    String counterpartDisplay = BankUtils.getCounterpartDisplay(transactionType, counterpartName, 
                                                                              depositorName, counterpartAccount);

                    if (memo == null)
                        memo = "-";

                    System.out.println(index + "번째 거래");
                    System.out.println("거래번호: " + rs.getString("transaction_id"));
                    System.out.println("거래구분: " + transactionType);
                    System.out.println("상대방정보: " + counterpartDisplay);
                    System.out.println("거래일시: " + BankUtils.formatDate(rs.getTimestamp("transaction_date")));
                    System.out.println("메모: " + memo);
                    System.out.println("거래금액: " + BankUtils.formatCurrency(rs.getDouble("amount")));
                    System.out.println("거래후잔액: " + BankUtils.formatCurrency(rs.getDouble("balance_after")));
                    System.out.println("============================================================================================================================");
                    
                    index--; 
                }
                
                if (hasTransactions) {
                    System.out.println("총 " + totalCount + "건의 거래내역이 조회되었습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("거래내역을 불러올 수 없습니다.");
            System.out.println("시스템 점검 중이거나 일시적인 오류일 수 있습니다.");
            System.out.println("인터넷뱅킹에서 확인하시거나 고객센터로 연락해주세요.");
            System.out.println("고객센터: 1588-0000");
        }
    }

    // 전체 거래 건수 조회 메서드 추가
    private int getTotalTransactionCount(String accountId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("거래 건수 조회 중 오류가 발생했습니다.");
        }
        return 0;
    }
}
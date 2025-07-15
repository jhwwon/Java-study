package banksystem.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import banksystem.entity.Transaction;
import banksystem.helper.InputHelper;
import banksystem.helper.ValidationHelper;
import banksystem.util.BankUtils;

public class TransactionManager {
    private Connection conn;
    private ValidationHelper validator;
    private InputHelper inputHelper;
    private AccountManager accountManager;
    private UserManager userManager;
    private Scanner scanner;

    public TransactionManager(Connection conn, ValidationHelper validator, InputHelper inputHelper,
                             AccountManager accountManager, UserManager userManager, Scanner scanner) {
        this.conn = conn;
        this.validator = validator;
        this.inputHelper = inputHelper;
        this.accountManager = accountManager;
        this.userManager = userManager;
        this.scanner = scanner;
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
            System.out.println("❌ 거래내역 저장 오류: " + e.getMessage());
            return false;
        }
    }

    // 전체 거래내역 갯수 조회
    public int getTotalTransactionCount(String accountId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 거래내역 개수 조회 오류: " + e.getMessage());
        }
        return 0;
    }

    // 거래내역 페이징 표시
    public void displayTransactionHistory(String accountId) {
        final int PAGE_SIZE = 3; // 페이지당 거래 수
        int currentPage = 1;

        while (true) {
            // 전체 거래 수 조회
            int totalCount = getTotalTransactionCount(accountId);
            if (totalCount == 0) {
                System.out.println("\n[거래내역] 계좌번호: " + accountId + " (" + accountManager.getAccountHolderName(accountId) + ")");
                System.out.println("거래내역이 없습니다.");
                return;
            }

            int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

            // 현재 페이지가 범위를 벗어나면 조정
            if (currentPage > totalPages)
                currentPage = totalPages;
            if (currentPage < 1)
                currentPage = 1;

            // 페이지 데이터 표시
            displayTransactionPage(accountId, currentPage, PAGE_SIZE, totalCount, totalPages);

            System.out.println("\n📄 페이지 이동:");
            System.out.println("1.이전페이지 | 2.다음페이지 | 3.첫페이지 | 4.마지막페이지 | 5.페이지이동 | 0.돌아가기");
            System.out.print("선택: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": // 이전 페이지
                    if (currentPage > 1) {
                        currentPage--;
                    } else {
                        System.out.println("❌ 첫 번째 페이지입니다.");
                    }
                    break;
                case "2": // 다음 페이지
                    if (currentPage < totalPages) {
                        currentPage++;
                    } else {
                        System.out.println("❌ 마지막 페이지입니다.");
                    }
                    break;
                case "3": // 첫 페이지
                    currentPage = 1;
                    break;
                case "4": // 마지막 페이지
                    currentPage = totalPages;
                    break;
                case "5": // 페이지 이동
                    currentPage = inputHelper.inputPageNumber(totalPages);
                    break;
                case "0": // 돌아가기
                    return;
                default:
                    System.out.println("❌ 0~5번을 선택해주세요.");
            }
        }
    }

    // 특정 페이지의 거래내역 표시 (최신 거래가 가장 큰 번호)
    public void displayTransactionPage(String accountId, int currentPage, int pageSize, int totalCount, int totalPages) {
        System.out.println("\n[거래내역] 계좌번호: " + accountId + " (" + accountManager.getAccountHolderName(accountId) + ")");
        System.out.println(currentPage + "/" + totalPages + " 페이지] 총 " + totalCount + "건");
        System.out.println("====================================================================================");

        // Oracle 페이징 쿼리 (ROWNUM 사용)
        String sql = "SELECT * FROM (" + "  SELECT ROWNUM rn, t.* FROM ("
                + "    SELECT * FROM transactions WHERE account_id = ? " + "    ORDER BY transaction_date DESC" + // 최신순 정렬 유지
                "  ) t WHERE ROWNUM <= ?" + ") WHERE rn > ?";

        int endRow = currentPage * pageSize;
        int startRow = (currentPage - 1) * pageSize;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            pstmt.setInt(2, endRow);
            pstmt.setInt(3, startRow);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasTransactions = false;

                while (rs.next()) {
                    hasTransactions = true;

                    // 최신 거래가 가장 큰 번호가 되도록 계산
                    // 전체에서 현재 행의 위치를 계산 (1부터 시작)
                    int currentRowInTotal = startRow + rs.getInt("rn") - startRow;
                    // 역순으로 번호 부여 (가장 최신이 가장 큰 번호)
                    int displayIndex = totalCount - currentRowInTotal + 1;

                    String transactionType = rs.getString("transaction_type");
                    String counterpartAccount = rs.getString("counterpart_account");
                    String counterpartName = rs.getString("counterpart_name");
                    String depositorName = rs.getString("depositor_name");
                    String memo = rs.getString("transaction_memo");

                    String counterpartDisplay = BankUtils.getCounterpartDisplay(transactionType, counterpartName, 
                                                                              depositorName, counterpartAccount);

                    if (memo == null)
                        memo = "-";

                    // 순번 표시 - 최신 거래가 가장 큰 번호
                    System.out.println(displayIndex + "번째 거래");
                    System.out.println("거래번호: " + rs.getString("transaction_id"));
                    System.out.println("거래구분: " + transactionType);
                    System.out.println("상대방정보: " + counterpartDisplay);
                    System.out.println("거래일시: " + BankUtils.formatDate(rs.getTimestamp("transaction_date")));
                    System.out.println("메모: " + memo);
                    System.out.println("거래금액: " + BankUtils.formatCurrency(rs.getDouble("amount")));
                    System.out.println("거래후잔액: " + BankUtils.formatCurrency(rs.getDouble("balance_after")));
                    System.out.println("------------------------------------------------------------------------------------");
                }

                if (!hasTransactions) {
                    System.out.println("해당 페이지에 거래내역이 없습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 거래내역 조회 오류: " + e.getMessage());
        }
    }

    // 입금 처리
    public void deposit(String loginId) {
        System.out.println("[입금]");
        String accountId = inputHelper.inputAccountId("계좌번호: ", false, loginId);
        String depositorName;

        if (accountManager.isMyAccount(accountId, loginId)) {
            System.out.println("💳 본인 계좌 입금 - 계좌 비밀번호 확인이 필요합니다.");
            if (!inputHelper.checkPassword(accountId)) {
                return;
            }
            depositorName = userManager.getUserName(loginId);
        } else {
            depositorName = inputHelper.input("입금자명: ");
        }

        double amount = inputHelper.inputAmount("입금액: ");
        System.out.print("입금 메모 (선택사항): ");
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty())
            memo = null;

        if (inputHelper.confirmAction()) {
            double currentBalance = accountManager.getBalance(accountId);
            double newBalance = currentBalance + amount;

            if (accountManager.updateAccountBalance(accountId, newBalance)) {
                // Transaction 객체 생성하여 거래내역 저장
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

    // 출금 처리
    public void withdraw(String loginId) {
        System.out.println("[출금]");
        String accountId = inputHelper.inputAccountId("계좌번호: ", true, loginId);

        if (!inputHelper.checkPassword(accountId)) {
            return;
        }

        double currentBalance = accountManager.getBalance(accountId);
        double amount = inputHelper.inputAmount("출금액: ");

        if (currentBalance < amount) {
            System.out.println("❌ 잔액이 부족합니다. (현재 잔액: " + BankUtils.formatCurrency(currentBalance) + ")");
            return;
        }

        System.out.print("출금 메모 (선택사항): ");
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty())
            memo = null;

        if (inputHelper.confirmAction()) {
            double newBalance = currentBalance - amount;

            if (accountManager.updateAccountBalance(accountId, newBalance)) {
                // Transaction 객체 생성하여 거래내역 저장
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

    // 이체 처리
    public void transfer(String loginId) {
        System.out.println("[이체]");
        String fromAccountId = inputHelper.inputAccountId("출금 계좌번호: ", true, loginId);

        if (!inputHelper.checkPassword(fromAccountId)) {
            return;
        }

        String toAccountId;
        do {
            toAccountId = inputHelper.inputAccountId("입금 계좌번호: ", false, loginId);
            if (!fromAccountId.equals(toAccountId))
                break;
            System.out.println("❌ 출금 계좌와 입금 계좌가 같을 수 없습니다.");
        } while (true);

        double currentBalance = accountManager.getBalance(fromAccountId);
        double amount = inputHelper.inputAmount("이체금액: ");

        if (currentBalance < amount) {
            System.out.println("❌ 잔액이 부족합니다. (현재 잔액: " + BankUtils.formatCurrency(currentBalance) + ")");
            return;
        }

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
                }
                System.out.println("❌ 이체 오류: " + e.getMessage());
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                }
            }
        }
    }

    // 거래내역 조회 (페이징 기능 포함)
    public void history(String loginId) {
        System.out.println("[거래내역 조회]");
        String accountId = inputHelper.inputAccountId("계좌번호: ", true, loginId);

        if (!inputHelper.checkPassword(accountId)) {
            return;
        }

        // 페이징 시작
        displayTransactionHistory(accountId);
    }
}
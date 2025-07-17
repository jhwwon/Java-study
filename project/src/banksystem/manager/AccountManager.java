package banksystem.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Date;

import banksystem.entity.Account;
import banksystem.entity.Transaction;  // Transaction import 추가
import banksystem.entity.User;
import banksystem.helper.InputHelper;
import banksystem.helper.ValidationHelper;
import banksystem.util.BankUtils;
import banksystem.util.InterestCalculator;

public class AccountManager {
	private Connection conn;
	private ValidationHelper validator;
	private InputHelper inputHelper;
	private UserManager userManager;
	private Scanner scanner;
	private TransactionManager transactionManager;  // 2단계: TransactionManager 참조 추가

	// 2단계: 생성자에 TransactionManager 매개변수 추가
	public AccountManager(Connection conn, ValidationHelper validator, InputHelper inputHelper, UserManager userManager,
			Scanner scanner, TransactionManager transactionManager) {
		this.conn = conn;
		this.validator = validator;
		this.inputHelper = inputHelper;
		this.userManager = userManager;
		this.scanner = scanner;
		this.transactionManager = transactionManager;  // TransactionManager 참조 설정
	}

	// Account 객체를 DB에 저장
	public boolean saveAccount(Account account) {
	    String sql = "INSERT INTO accounts (account_id, account_name, account_type, account_password, " +
	            "balance, user_id, create_date, interest_rate, last_interest_date) " +
	            "VALUES (?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE)";
	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setString(1, account.getAccountId());
	        pstmt.setString(2, account.getAccountName());
	        pstmt.setString(3, account.getAccountType());
	        pstmt.setString(4, account.getAccountPassword());
	        pstmt.setDouble(5, account.getBalance());
	        pstmt.setString(6, account.getUserId());
	        pstmt.setDouble(7, account.getInterestRate()); // 이자율 추가
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) {
	        System.out.println("계좌 생성 오류: " + e.getMessage());
	        return false;
	    }
	}

	// Account ID로 Account 객체 조회
	public Account getAccountById(String accountId) {
		String sql = "SELECT * FROM accounts WHERE account_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Account account = new Account();
					account.setAccountId(rs.getString("account_id"));
					account.setAccountName(rs.getString("account_name"));
					account.setAccountType(rs.getString("account_type"));
					account.setAccountPassword(rs.getString("account_password"));
					account.setBalance(rs.getDouble("balance"));
					account.setUserId(rs.getString("user_id"));
					account.setCreateDate(rs.getDate("create_date"));
					return account;
				}
			}
		} catch (SQLException e) {
			System.out.println("계좌 조회 오류: " + e.getMessage());
		}
		return null;
	}

	// Account 잔액 업데이트
	public boolean updateAccountBalance(String accountId, double newBalance) {
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

	// Account 비밀번호 업데이트
	public boolean updateAccountPassword(String accountId, String newPassword) {
		String sql = "UPDATE accounts SET account_password = ? WHERE account_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newPassword);
			pstmt.setString(2, accountId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("비밀번호 변경 오류: " + e.getMessage());
			return false;
		}
	}

	// Account 삭제
	public boolean deleteAccount(String accountId) {
		String sql = "DELETE FROM accounts WHERE account_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("계좌 삭제 오류: " + e.getMessage());
			return false;
		}
	}

	// 계좌 존재 여부 확인
	public boolean accountExists(String accountId) {
		String sql = "SELECT COUNT(*) FROM accounts WHERE account_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.out.println("계좌 조회 오류: " + e.getMessage());
		}
		return false;
	}

	// 본인 계좌 여부 확인
	public boolean isMyAccount(String accountId, String loginId) {
		Account account = getAccountById(accountId);
		return account != null && loginId != null && loginId.equals(account.getUserId());
	}

	// 계좌 비밀번호 검증
	public boolean verifyPassword(String accountId, String password) {
		Account account = getAccountById(accountId);
		return account != null && password.equals(account.getAccountPassword());
	}

	// 계좌 소유자명 조회
	public String getAccountHolderName(String accountId) {
		String sql = "SELECT u.user_name FROM accounts a " + "JOIN users u ON a.user_id = u.user_id "
				+ "WHERE a.account_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("user_name");
				}
			}
		} catch (SQLException e) {
			System.out.println("예금주명 조회 오류: " + e.getMessage());
		}
		return "미상";
	}

	// 계좌 잔액 조회
	public double getBalance(String accountId) {
		Account account = getAccountById(accountId);
		return account != null ? account.getBalance() : -1;
	}

	// 계좌 생성
	public void createAccount(String loginId) {
		System.out.println("[계좌 신규 개설]");
		String[] types = { "보통예금", "정기예금", "적금" };

		System.out.println("\n[계좌 종류 선택]");
		System.out.println("============================================================================================================================");
		System.out.println("1. 보통예금 - [연 0.1%]");
		System.out.println("2. 정기예금 - [연 1.5%]");
		System.out.println("3. 적   금 - [연 2.0%]");
		System.out.println("============================================================================================================================");
		int choice;
		do {
			try {
				System.out.print("계좌 종류 선택 (1-3): ");
				choice = Integer.parseInt(scanner.nextLine());
				if (choice >= 1 && choice <= 3)
					break;
			} catch (NumberFormatException e) {
			}
			System.out.println("1 ~ 3번을 선택해주세요.");
		} while (true);

		String accountType = types[choice - 1];
		String accountName = accountType + " 계좌_" + userManager.getUserName(loginId);
		String password = inputHelper.inputAccountPassword();
		double initialBalance = inputHelper.inputAmount("초기 입금액 (1,000원 이상): ");

		if (inputHelper.confirmAction()) {
			String accountId = BankUtils.generateAccountNumber(conn);

			// 계좌 종류에 따른 이자율 자동 설정
			double interestRate = InterestCalculator.getInterestRateByType(accountType);
			
			// Account 객체 생성
			Account account = new Account(accountId, accountName, accountType, password, initialBalance, loginId);
			account.setInterestRate(interestRate); // 이자율 설정
	        account.setLastInterestDate(new Date()); // 마지막 이자 지급일을 현재 날짜로 설정
			
			// 트랜잭션으로 계좌 생성과 초기 입금 거래내역을 함께 처리
			try {
				conn.setAutoCommit(false);  // 트랜잭션 시작
				
				// 1. 계좌 생성
				if (saveAccount(account)) {
					// 2. 초기 입금 거래내역 생성 및 저장
					if (transactionManager != null) {
						Transaction initialTransaction = new Transaction();
						initialTransaction.setTransactionId(BankUtils.generateTransactionId(conn));
						initialTransaction.setAccountId(accountId);
						initialTransaction.setTransactionType("입금");
						initialTransaction.setAmount(initialBalance);
						initialTransaction.setBalanceAfter(initialBalance);
						initialTransaction.setTransactionMemo("계좌 개설 초기 입금");
						
						// TransactionManager를 통해 거래내역 저장
						if (transactionManager.saveTransaction(initialTransaction)) {
							conn.commit();  // 모두 성공시 커밋
							
							System.out.println("✅ 계좌가 성공적으로 개설되었습니다!");
							System.out.println("   계좌번호: " + accountId);
							System.out.println("   계좌종류: " + accountType);
							System.out.println("   적용 이자율: " + String.format("%.1f%%", interestRate * 100));
						} else {
							throw new SQLException("거래내역 저장 실패");
						}
					} else {
						// TransactionManager가 null이면 계좌만 생성
						conn.commit();
						System.out.println("✅ 계좌가 성공적으로 개설되었습니다!");
						System.out.println("   계좌번호: " + accountId);
						System.out.println("   계좌종류: " + accountType);
						System.out.println("   적용 이자율: " + String.format("%.1f%%", interestRate * 100));
					}
				} else {
					throw new SQLException("계좌 생성 실패");
				}
				
			} catch (SQLException e) {
				try {
					conn.rollback();  // 실패시 롤백
					System.out.println("❌ 계좌 개설 중 오류가 발생했습니다: " + e.getMessage());
				} catch (SQLException ex) {
					System.out.println("롤백 오류: " + ex.getMessage());
				}
			} finally {
				try {
					conn.setAutoCommit(true);  // 자동커밋 복원
				} catch (SQLException e) {
					System.out.println("자동커밋 설정 오류: " + e.getMessage());
				}
			}
		}
	}

	// 계좌 조회
	public void readAccount(String loginId) {
		System.out.println("[계좌 조회]");
		String accountId = inputHelper.input("계좌번호: ");

		Account account = getAccountById(accountId);
		if (account != null) {
			User accountHolder = userManager.getUserById(account.getUserId());

			System.out.println("계좌번호: " + account.getAccountId());
			System.out.println("계좌명: " + account.getAccountName());
			System.out.println("계좌종류: " + account.getAccountType());
			System.out.println("잔액: " + BankUtils.formatCurrency(account.getBalance()));
			System.out.println("소유자: " + (accountHolder != null ? accountHolder.getUserName() : "미상"));
			System.out.println("개설일: " + account.getCreateDate());
	        
	        // 메뉴로 돌아가기
	        System.out.println("\n엔터키를 누르면 메뉴로 돌아갑니다.");
	        scanner.nextLine();
	        
		} else {
			System.out.println("해당 계좌를 찾을 수 없습니다.");
	        System.out.println("\n엔터키를 누르면 메뉴로 돌아갑니다.");
	        scanner.nextLine();
		}
	}

	// 계좌 삭제
	public void deleteAccountMenu(String loginId, String accountId) {
		if (accountId == null) {
			System.out.println("[계좌 해지]");
			accountId = inputHelper.inputAccountId("해지할 계좌번호: ", true, loginId);
		}

		if (!checkPassword(accountId) || !inputHelper.confirmAction()) {
			return;
		}

		if (deleteAccount(accountId)) {
			System.out.println("✅ 계좌가 해지되었습니다!");
		}
	}

	// 계좌 비밀번호 변경
	public void changePassword(String loginId) {
		System.out.println("[계좌 비밀번호 변경]");
		String accountId = inputHelper.inputAccountId("계좌번호: ", true, loginId);

		if (checkPassword(accountId)) {
			// 현재 계좌 정보 조회
			Account account = getAccountById(accountId);
			String currentPassword = account != null ? account.getAccountPassword() : null;
			String newPassword;

			do {
				// 여기서 직접 새 비밀번호 입력받기
				System.out.print("새 계좌 비밀번호 (4자리 숫자): ");
				newPassword = scanner.nextLine();

				// 유효성 검사
				if (!validator.validateAccountPassword(newPassword)) {
					continue; // 유효성 검사 실패시 다시 입력
				}

				if (currentPassword != null && currentPassword.equals(newPassword)) {
					System.out.println("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
				} else {
					break;
				}
			} while (true);

			if (inputHelper.confirmAction()) {
				if (updateAccountPassword(accountId, newPassword)) {
					System.out.println("✅ 계좌 비밀번호가 변경되었습니다!");
				}
			}
		}
	}

	// 2단계: 계좌 목록 조회 메소드 수정 (한도 정보 표시 추가)
	public void listAccounts(String loginId) {
		System.out.println("\n[계좌 목록] 사용자: " + userManager.getUserName(loginId) + " (" + loginId + ")");
		
		// 2단계: 거래 한도 안내 표시 (제목도 굵게)
		if (transactionManager != null) {
			System.out.println("============================================================================================================================");
			// "거래 한도 안내" 제목도 굵게 표시
			String BOLD = "\033[1m";
			String RESET = "\033[0m";
			System.out.println("\n" + "💡 " + BOLD + "거래 한도 안내" + RESET);
			
			String[] limits = transactionManager.getTransactionLimits();
			for (String limit : limits) {
				System.out.println(limit);
			}
		}
		
		System.out.println("============================================================================================================================");
		System.out.println("계좌번호\t\t계좌명\t\t\t계좌종류\t\t소유자\t\t잔액");
		System.out.println("============================================================================================================================");

		String sql = "SELECT a.*, u.user_name FROM accounts a JOIN users u ON a.user_id = u.user_id "
				+ "WHERE a.user_id = ?";

		double totalBalance = 0; // 전체 잔액 합계 변수

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, loginId);
			try (ResultSet rs = pstmt.executeQuery()) {
				boolean hasAccounts = false;
				while (rs.next()) {
					hasAccounts = true;

					String accountId = rs.getString("account_id");
					String accountName = rs.getString("account_name");
					String displayAccountName = accountName.length() > 12 ? accountName.substring(0, 12) + ".."
							: accountName;

					double balance = rs.getDouble("balance");
					totalBalance += balance; // 각 계좌 잔액을 합계에 누적

					System.out.println(accountId + "\t" + displayAccountName + "\t\t"
							+ rs.getString("account_type") + "\t\t" + rs.getString("user_name") + "\t\t"
							+ BankUtils.formatCurrency(balance));
					
					// 2단계: 계좌별 오늘 사용량 표시
					if (transactionManager != null) {
						String usageInfo = transactionManager.getFormattedUsageByAccount(accountId);
						System.out.println(usageInfo);
						System.out.println(); // 빈 줄 추가로 가독성 향상
					}
				}

				if (!hasAccounts) {
					System.out.println("보유하신 계좌가 없습니다. 계좌를 개설하세요!");
				} else {
					// 전체 잔액 합계 표시 (오른쪽 정렬)
					System.out.println("============================================================================================================================");
					System.out.println("\t\t\t\t\t\t\t"+"전체 계좌 잔액 합계: " + BankUtils.formatCurrency(totalBalance));
				}
			}
		} catch (SQLException e) {
			System.out.println("계좌 목록 조회 오류: " + e.getMessage());
		}
	}

	// 계좌 비밀번호 확인 (InputHelper에서 사용)
	private boolean checkPassword(String accountId) {
		String password;
		do {
			System.out.print("계좌 비밀번호 (4자리 숫자): ");
			password = scanner.nextLine();
			if (password.length() != 4 || !password.matches("\\d{4}")) {
				System.out.println("계좌 비밀번호는 4자리 숫자여야 합니다.");
				continue;
			}
			if (verifyPassword(accountId, password))
				return true;
			System.out.println("계좌 비밀번호가 일치하지 않습니다.");
		} while (true);
	}
}
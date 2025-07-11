package basicbank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

// Account 클래스 임포트 제거됨
// User 클래스 임포트 제거됨

// 전체 시스템을 관리하는 클래스
public class BasicBankSystem {
	// 사용자로부터 입력받기 위한 스캐너
	private Scanner scanner = new Scanner(System.in);
	
	// DB 연결 객체
	private Connection conn = null;
	
	// 로그인한 사용자 정보
	private boolean isLoggedIn;
	private String currentUser;
	private boolean isAdmin;
	
	// 기본 생성자 - DB 연결 초기화
	public BasicBankSystem() {
		this.isLoggedIn = false;
		this.currentUser = null;
		this.isAdmin = false;
		
		try {
			// JDBC Driver 등록
			Class.forName("oracle.jdbc.OracleDriver");
			
			// DB 연결
			conn = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521/orcl",
				"jhw1",		// Oracle 계정 이름
				"1234"		// Oracle 계정 비밀번호
			);
			
			// 테이블 생성 (존재하지 않을 경우)
			initializeTables();
			
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
	}
	
	/**
	 * 필요한 테이블들을 생성하는 메소드
	 */
	private void initializeTables() {
		try {
			// ACCOUNTS 테이블 생성
			String createAccountsTable = """
				CREATE TABLE accounts (
					account_id VARCHAR2(20) PRIMARY KEY,
					account_name VARCHAR2(50) NOT NULL,
					balance NUMBER(15,2) DEFAULT 0
				)
			""";
			
			// BANK_USERS 테이블 생성
			String createUsersTable = """
				CREATE TABLE bank_users (
					user_id VARCHAR2(30) PRIMARY KEY,
					user_name VARCHAR2(50) NOT NULL,
					user_password VARCHAR2(50) NOT NULL
				)
			""";
			
			PreparedStatement pstmt1 = conn.prepareStatement(createAccountsTable);
			PreparedStatement pstmt2 = conn.prepareStatement(createUsersTable);
			
			try {
				pstmt1.executeUpdate();
			} catch (SQLException e) {
				// 테이블이 이미 존재하는 경우 무시
			}
			
			try {
				pstmt2.executeUpdate();
			} catch (SQLException e) {
				// 테이블이 이미 존재하는 경우 무시
			}
			
			pstmt1.close();
			pstmt2.close();
			
			// 기본 관리자 계정 생성 (존재하지 않을 경우)
			createDefaultAdmin();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 기본 관리자 계정 생성
	 */
	private void createDefaultAdmin() {
		try {
			String checkAdmin = "SELECT COUNT(*) FROM bank_users WHERE user_id = 'admin'";
			PreparedStatement pstmt = conn.prepareStatement(checkAdmin);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next() && rs.getInt(1) == 0) {
				String insertAdmin = "INSERT INTO bank_users (user_id, user_name, user_password) VALUES ('admin', 'Administrator', 'password')";
				PreparedStatement insertPstmt = conn.prepareStatement(insertAdmin);
				insertPstmt.executeUpdate();
				insertPstmt.close();
			}
			
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 새 은행 계좌를 생성하는 메소드
	 */
	public void createAccount(String name, double initialBalance) {
		try {
			String accountId = generateAccountId();
			String sql = "INSERT INTO accounts (account_id, account_name, balance) VALUES (?, ?, ?)";
			
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, accountId);
			pstmt.setString(2, name);
			pstmt.setDouble(3, initialBalance);
			
			int rows = pstmt.executeUpdate();
			if (rows > 0) {
				System.out.println("계좌 생성 완료! 계좌번호: " + accountId);
			}
			
			pstmt.close();
		} catch (SQLException e) {
			System.out.println("계좌 생성 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 스캐너를 통한 계좌 생성
	 */
	private void createAccount(Scanner scanner) {
		System.out.print("계좌주 이름: ");
		String name = scanner.nextLine();
		System.out.print("초기 잔액: ");
		
		try {
			double initialBalance = Double.parseDouble(scanner.nextLine());
			createAccount(name, initialBalance);
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		}
	}
	
	/**
	 * 모든 계좌 목록을 출력하는 메소드
	 */
	public void displayAllAccounts() {
		try {
			String sql = "SELECT account_id, account_name FROM accounts ORDER BY account_id";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			boolean hasAccounts = false;
			System.out.println("\n[등록된 계좌 목록]");
			System.out.println("-----------------------------");
			
			while (rs.next()) {
				hasAccounts = true;
				System.out.printf("계좌번호: %-10s | 이름: %s\n",
					rs.getString("account_id"),
					rs.getString("account_name"));
			}
			
			if (!hasAccounts) {
				System.out.println("등록된 계좌가 없습니다.");
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			System.out.println("계좌 목록 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 특정 계좌의 상세 정보를 출력하는 메소드
	 */
	public void displayAccountDetails(String accountId) {
		try {
			String sql = "SELECT * FROM accounts WHERE account_id = ?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, accountId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				System.out.println("\n[계좌 상세 정보]");
				System.out.println("계좌번호: " + rs.getString("account_id"));
				System.out.println("이름: " + rs.getString("account_name"));
				System.out.println("잔액: " + String.format("%.0f원", rs.getDouble("balance")));
			} else {
				System.out.println("해당 계좌를 찾을 수 없습니다.");
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			System.out.println("계좌 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 스캐너를 통한 계좌 상세 조회
	 */
	private void displayAccountDetails(Scanner scanner) {
		System.out.print("계좌번호: ");
		String accountId = scanner.nextLine();
		displayAccountDetails(accountId);
	}
	
	/**
	 * 입금 기능
	 */
	public void deposit(String accountId, double amount) {
		try {
			String selectSql = "SELECT balance FROM accounts WHERE account_id = ?";
			PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
			selectPstmt.setString(1, accountId);
			ResultSet rs = selectPstmt.executeQuery();
			
			if (rs.next()) {
				double currentBalance = rs.getDouble("balance");
				double newBalance = currentBalance + amount;
				
				String updateSql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
				PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
				updatePstmt.setDouble(1, newBalance);
				updatePstmt.setString(2, accountId);
				
				int rows = updatePstmt.executeUpdate();
				if (rows > 0) {
					System.out.println("입금 완료. 현재 잔액: " + String.format("%.0f원", newBalance));
				}
				
				updatePstmt.close();
			} else {
				System.out.println("해당 계좌를 찾을 수 없습니다.");
			}
			
			rs.close();
			selectPstmt.close();
		} catch (SQLException e) {
			System.out.println("입금 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 스캐너를 통한 입금
	 */
	private void deposit(Scanner scanner) {
		System.out.print("계좌번호: ");
		String accountId = scanner.nextLine();
		System.out.print("입금 금액: ");
		
		try {
			double amount = Double.parseDouble(scanner.nextLine());
			if (amount <= 0) {
				System.out.println("0보다 큰 금액을 입력해주세요.");
				return;
			}
			deposit(accountId, amount);
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		}
	}
	
	/**
	 * 출금 기능
	 */
	public void withdraw(String accountId, double amount) {
		try {
			String selectSql = "SELECT balance FROM accounts WHERE account_id = ?";
			PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
			selectPstmt.setString(1, accountId);
			ResultSet rs = selectPstmt.executeQuery();
			
			if (rs.next()) {
				double currentBalance = rs.getDouble("balance");
				
				if (currentBalance >= amount) {
					double newBalance = currentBalance - amount;
					
					String updateSql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
					PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
					updatePstmt.setDouble(1, newBalance);
					updatePstmt.setString(2, accountId);
					
					int rows = updatePstmt.executeUpdate();
					if (rows > 0) {
						System.out.println("출금 완료. 현재 잔액: " + String.format("%.0f원", newBalance));
					}
					
					updatePstmt.close();
				} else {
					System.out.println("잔액이 부족합니다. 현재 잔액: " + String.format("%.0f원", currentBalance));
				}
			} else {
				System.out.println("해당 계좌를 찾을 수 없습니다.");
			}
			
			rs.close();
			selectPstmt.close();
		} catch (SQLException e) {
			System.out.println("출금 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 스캐너를 통한 출금
	 */
	private void withdraw(Scanner scanner) {
		System.out.print("계좌번호: ");
		String accountId = scanner.nextLine();
		System.out.print("출금 금액: ");
		
		try {
			double amount = Double.parseDouble(scanner.nextLine());
			if (amount <= 0) {
				System.out.println("0보다 큰 금액을 입력해주세요.");
				return;
			}
			withdraw(accountId, amount);
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		}
	}
	
	/**
	 * 이체 기능
	 */
	public void transfer(String fromAccountId, String toAccountId, double amount) {
		try {
			// 트랜잭션 시작
			conn.setAutoCommit(false);
			
			// 출금 계좌 확인
			String fromSelectSql = "SELECT balance FROM accounts WHERE account_id = ?";
			PreparedStatement fromSelectPstmt = conn.prepareStatement(fromSelectSql);
			fromSelectPstmt.setString(1, fromAccountId);
			ResultSet fromRs = fromSelectPstmt.executeQuery();
			
			// 입금 계좌 확인
			String toSelectSql = "SELECT balance FROM accounts WHERE account_id = ?";
			PreparedStatement toSelectPstmt = conn.prepareStatement(toSelectSql);
			toSelectPstmt.setString(1, toAccountId);
			ResultSet toRs = toSelectPstmt.executeQuery();
			
			if (fromRs.next() && toRs.next()) {
				double fromBalance = fromRs.getDouble("balance");
				double toBalance = toRs.getDouble("balance");
				
				if (fromBalance >= amount) {
					// 출금 계좌에서 차감
					String fromUpdateSql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
					PreparedStatement fromUpdatePstmt = conn.prepareStatement(fromUpdateSql);
					fromUpdatePstmt.setDouble(1, fromBalance - amount);
					fromUpdatePstmt.setString(2, fromAccountId);
					fromUpdatePstmt.executeUpdate();
					
					// 입금 계좌에 추가
					String toUpdateSql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
					PreparedStatement toUpdatePstmt = conn.prepareStatement(toUpdateSql);
					toUpdatePstmt.setDouble(1, toBalance + amount);
					toUpdatePstmt.setString(2, toAccountId);
					toUpdatePstmt.executeUpdate();
					
					// 트랜잭션 커밋
					conn.commit();
					
					System.out.println("이체 완료.");
					System.out.println("출금 계좌 잔액: " + String.format("%.0f원", fromBalance - amount));
					System.out.println("입금 계좌 잔액: " + String.format("%.0f원", toBalance + amount));
					
					fromUpdatePstmt.close();
					toUpdatePstmt.close();
				} else {
					System.out.println("출금 계좌의 잔액이 부족합니다.");
					conn.rollback();
				}
			} else {
				System.out.println("계좌 정보를 확인해주세요.");
				conn.rollback();
			}
			
			fromRs.close();
			toRs.close();
			fromSelectPstmt.close();
			toSelectPstmt.close();
			
			// 자동 커밋 복원
			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			try {
				conn.rollback();
				conn.setAutoCommit(true);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			System.out.println("이체 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 스캐너를 통한 이체
	 */
	private void transfer(Scanner scanner) {
		System.out.print("출금 계좌번호: ");
		String fromAccountId = scanner.nextLine();
		System.out.print("입금 계좌번호: ");
		String toAccountId = scanner.nextLine();
		System.out.print("이체 금액: ");
		
		try {
			double amount = Double.parseDouble(scanner.nextLine());
			if (amount <= 0) {
				System.out.println("0보다 큰 금액을 입력해주세요.");
				return;
			}
			transfer(fromAccountId, toAccountId, amount);
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		}
	}
	
	/**
	 * 계좌 삭제 기능
	 */
	public void deleteAccount(String accountId) {
		try {
			String sql = "DELETE FROM accounts WHERE account_id = ?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, accountId);
			
			int rows = pstmt.executeUpdate();
			if (rows > 0) {
				System.out.println("계좌 삭제 완료");
			} else {
				System.out.println("해당 계좌를 찾을 수 없습니다.");
			}
			
			pstmt.close();
		} catch (SQLException e) {
			System.out.println("계좌 삭제 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 스캐너를 통한 계좌 삭제
	 */
	private void deleteAccount(Scanner scanner) {
		System.out.print("삭제할 계좌번호: ");
		String accountId = scanner.nextLine();
		deleteAccount(accountId);
	}
	
	/**
	 * 로그인 기능
	 */
	public boolean login(String username, String password) {
		try {
			String sql = "SELECT user_password FROM bank_users WHERE user_id = ?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				String dbPassword = rs.getString("user_password");
				if (dbPassword != null && dbPassword.equals(password)) {
					isLoggedIn = true;
					currentUser = username;
					isAdmin = username.equals("admin");
					return true;
				}
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			System.out.println("로그인 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 로그아웃 기능
	 */
	public void logout() {
		isLoggedIn = false;
		currentUser = null;
		isAdmin = false;
		System.out.println("로그아웃 되었습니다.");
	}
	
	/**
	 * 사용자 가입 기능
	 */
	private void join() {
		System.out.println("\n[새 사용자 가입]");
		System.out.print("사용자 ID: ");
		String userId = scanner.nextLine();
		System.out.print("사용자 이름: ");
		String userName = scanner.nextLine();
		System.out.print("비밀번호: ");
		String userPassword = scanner.nextLine();
		
		if (printSubMenu().equals("1")) {
			try {
				String sql = "INSERT INTO bank_users (user_id, user_name, user_password) VALUES (?, ?, ?)";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, userId);
				pstmt.setString(2, userName);
				pstmt.setString(3, userPassword);
				
				int rows = pstmt.executeUpdate();
				if (rows > 0) {
					System.out.println("사용자 가입이 완료되었습니다.");
				}
				
				pstmt.close();
			} catch (SQLException e) {
				System.out.println("사용자 가입 중 오류 발생: " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("예상치 못한 오류 발생: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 보조메뉴 출력
	 */
	private String printSubMenu() {
		System.out.println("보조메뉴: 1.확인 | 2.취소");
		System.out.print("메뉴선택: ");
		return scanner.nextLine();
	}
	
	/**
	 * 계좌번호 생성
	 */
	private String generateAccountId() {
		try {
			String sql = "SELECT COUNT(*) FROM accounts";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				int count = rs.getInt(1);
				rs.close();
				pstmt.close();
				return String.valueOf(count + 1);
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return "1";
	}
	
	/**
	 * 관리자 로그인
	 */
	private void loginAdmin(Scanner scanner) {
		System.out.println("\n[관리자 로그인]");
		System.out.print("관리자 ID: ");
		String username = scanner.nextLine();
		System.out.print("비밀번호: ");
		String password = scanner.nextLine();
		
		if (login(username, password) && isAdmin) {
			System.out.println("관리자 로그인 성공");
		} else {
			System.out.println("관리자 로그인 실패");
		}
	}
	
	/**
	 * 일반 사용자 로그인
	 */
	private void loginUser(Scanner scanner) {
		System.out.println("\n[사용자 로그인]");
		System.out.print("사용자 ID: ");
		String username = scanner.nextLine();
		System.out.print("비밀번호: ");
		String password = scanner.nextLine();
		
		if (login(username, password)) {
			System.out.println("사용자 로그인 성공");
		} else {
			System.out.println("로그인 실패");
		}
	}
	
	/**
	 * 메인 메뉴 실행
	 */
	public void runMenu() {
		while (true) {
			String loginStatus = isLoggedIn ?
				(" - 로그인: " + currentUser + (isAdmin ? " (관리자)" : " (사용자)")) : "";
			
			System.out.println("\n====================");
			System.out.println("은행 계좌 관리 시스템" + loginStatus);
			System.out.println("====================");
			
			if (!isLoggedIn) {
				System.out.println("1. 관리자 로그인");
				System.out.println("2. 사용자 로그인");
				System.out.println("3. 사용자 가입");
				System.out.println("4. 계좌 생성");
				System.out.println("5. 계좌 목록 보기");
				System.out.println("6. 계좌 상세정보 보기");
				System.out.println("7. 입금");
				System.out.println("8. 출금");
				System.out.println("9. 이체");
				System.out.println("10. 계좌 삭제");
				System.out.println("11. 프로그램 종료");
			} else {
				System.out.println("1. 계좌 생성");
				System.out.println("2. 계좌 목록 보기");
				System.out.println("3. 계좌 상세정보 보기");
				System.out.println("4. 입금");
				System.out.println("5. 출금");
				System.out.println("6. 이체");
				if (isAdmin) {
					System.out.println("7. 계좌 삭제");
				}
				System.out.println("8. 로그아웃");
				System.out.println("9. 프로그램 종료");
			}
			
			System.out.print("선택: ");
			String choice = scanner.nextLine();
			
			try {
				if (!isLoggedIn) {
					switch (choice) {
						case "1" -> loginAdmin(scanner);
						case "2" -> loginUser(scanner);
						case "3" -> join();
						case "4" -> createAccount(scanner);
						case "5" -> displayAllAccounts();
						case "6" -> displayAccountDetails(scanner);
						case "7" -> deposit(scanner);
						case "8" -> withdraw(scanner);
						case "9" -> transfer(scanner);
						case "10" -> {
							if (isAdmin) deleteAccount(scanner);
							else System.out.println("관리자만 사용할 수 있는 기능입니다.");
						}
						case "11" -> exit();
						default -> System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
					}
				} else {
					switch (choice) {
						case "1" -> createAccount(scanner);
						case "2" -> displayAllAccounts();
						case "3" -> displayAccountDetails(scanner);
						case "4" -> deposit(scanner);
						case "5" -> withdraw(scanner);
						case "6" -> transfer(scanner);
						case "7" -> {
							if (isAdmin) deleteAccount(scanner);
							else System.out.println("관리자만 사용할 수 있는 기능입니다.");
						}
						case "8" -> logout();
						case "9" -> exit();
						default -> System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
					}
				}
			} catch (Exception e) {
				System.out.println("메뉴 처리 중 오류 발생: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 프로그램 종료
	 */
	private void exit() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("은행 관리 시스템이 정상적으로 종료되었습니다.");
		System.exit(0);
	}
	
	/**
	 * 메인 메소드
	 */
	public static void main(String[] args) {
		BasicBankSystem bankSystem = new BasicBankSystem();
		bankSystem.runMenu();
	}
}

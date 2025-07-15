package mondayproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat; //날짜/시간을 특정 형식으로 포맷팅하는 클래스
import java.util.Scanner;
import java.util.Date;

public class BankSystem {
	private Scanner scanner = new Scanner(System.in);
	private Connection conn = null; // 아직 DB와 연결되지 않은 상태
	private String loginId = null;  // 로그인하지 않은 상태
	private DecimalFormat currencyFormat = new DecimalFormat("#,###");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public BankSystem() {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/orcl", "jhw1", "1234");
			conn.setAutoCommit(true); // sql문을 실행하는 즉시 DB에 저장
			System.out.println("은행 계좌 시스템 DB 연결 성공!");
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
	}

	private void exit() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			System.out.println("❌ DB 연결 종료 중 오류: " + e.getMessage());
		}
		System.out.println("은행 시스템이 정상적으로 종료되었습니다.");
		System.exit(0);
	}

	/**
	 * 유틸리티 메소드
	 */

	// 금액을 통화 형식으로 포맷팅
	private String formatCurrency(double amount) {
		return currencyFormat.format(amount) + "원";
	}

	// 새로운 계좌번호 생성 (기본 형식은 110-234-000000에서 1씩 증가)
	private String newAccNum() {
		String sql = "SELECT '110-234-' || LPAD(SEQ_ACCOUNT.NEXTVAL, 6, '0') FROM DUAL";
		try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
			if (rs.next())
				return rs.getString(1);
		} catch (SQLException e) {
			System.out.println("❌ 계좌번호 생성 오류: " + e.getMessage());
		}
		return null;
	}

	// 새로운 거래번호 생성 (기본 형식은 T00000000에서 1씩 증가)
	private String newTxId() {
		String sql = "SELECT 'T' || LPAD(SEQ_TRANSACTION.NEXTVAL, 8, '0') FROM DUAL";
		try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
			if (rs.next())
				return rs.getString(1);
		} catch (SQLException e) {
			System.out.println("❌ 거래번호 생성 오류: " + e.getMessage());
		}
		return null;
	}

	// 사용자 ID로 사용자 이름 조회
	private String getUserName(String userId) {
		String sql = "SELECT user_name " + "FROM users " + "WHERE user_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next())
					return rs.getString("user_name");
			}
		} catch (SQLException e) {
			System.out.println("❌ 사용자 이름 조회 오류: " + e.getMessage());
		}
		return userId;
	}

	// 거래 유형에 따른 상대방 정보 표시 형식 결정
	private String getCounterpartDisplay(String transactionType, String counterpartName, String depositorName,
			String counterpartAccount) {
		switch (transactionType) {
		case "이체입금":
			if (counterpartName != null) {
				return "보낸사람: " + counterpartName;
			} else if (counterpartAccount != null) {
				return "보낸계좌: " + counterpartAccount;
			} else {
				return "-";
			}
		case "이체출금":
			if (counterpartName != null) {
				return "받는사람: " + counterpartName;
			} else if (counterpartAccount != null) {
				return "받는계좌: " + counterpartAccount;
			} else {
				return "-";
			}
		case "입금":
			if (depositorName != null) {
				return "입금자: " + depositorName;
			} else {
				return "-";
			}
		default:
			return "-";
		}
	}

	/**
	 * 기본 입력 및 확인 메소드
	 */

	// 기본 입력 메소드 - 빈 값 입력 방지
	private String input(String prompt) {
		String input;
		do {
			System.out.print(prompt);
			input = scanner.nextLine().trim();
		} while (input.isEmpty());
		return input;
	}

	// 보조 메뉴
	private boolean confirmAction() {
		System.out.println("보조메뉴: 1.확인 | 2.취소");
		System.out.print("메뉴선택: ");
		return "1".equals(scanner.nextLine());
	}

	/**
	 * 회원가입 입력 및 검증 메소드
	 */
	// 사용자 ID 입력 및 검증
	private String inputUserId() {
		String userId;
		do {
			userId = input("아이디 (4~8자리, 영문+숫자): ");
			if (validateUserId(userId) && checkUserIdDuplicate(userId)) {
				return userId;
			}
		} while (true);
	}

	// 사용자 이름 입력 및 검증
	private String inputUserName() {
		String userName;
		do {
			userName = input("이름: ");
			if (validateUserName(userName)) {
				return userName;
			}
		} while (true);
	}

	// 사용자 비밀번호 입력 및 검증
	private String inputUserPassword(String userId) {
		String password;
		do {
			password = input("비밀번호 (7~12자리, 영문+숫자): ");
			if (validateUserPassword(password, userId)) {
				return password;
			}
		} while (true);
	}

	// 이메일 입력 및 검증
	private String inputEmail() {
		String email;
		do {
			email = input("이메일: ");
			if (validateEmail(email)) {
				return email;
			}
		} while (true);
	}

	// 전화번호 입력 및 검증
	private String inputPhone() {
		String phone;
		do {
			phone = input("전화번호 (010-0000-0000): ");
			if (validatePhone(phone)) {
				return phone;
			}
		} while (true);
	}

	/**
	 * 회원가입 유효성 검사 메소드
	 */
	// 사용자 ID 유효성 검사
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

	// 사용자 이름 유효성 검사
	private boolean validateUserName(String userName) {
		if (userName.length() > 20) {
			System.out.println("❌ 이름은 20자리까지만 가능합니다.");
			return false;
		}
		return true;
	}

	// 사용자 비밀번호 유효성 검사
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

	// 이메일 유효성 검사
	private boolean validateEmail(String email) {
		if (email.length() > 100) {
			System.out.println("❌ 이메일은 100자리까지만 가능합니다.");
			return false;
		}
		if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			System.out.println("❌ 올바른 이메일 형식이 아닙니다. (예: user@domain.com)");
			return false;
		}
		String[] commonDomains = { ".com", ".net", ".org", ".edu", ".gov", ".co.kr", ".kr" };
		for (String domain : commonDomains) {
			if (email.toLowerCase().endsWith(domain))
				return true;
		}
		System.out.println("❌ 일반적인 도메인을 사용해주세요. (.com, .net, .org, .kr 등)");
		return false;
	}

	// 전화번호 유효성 검사
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

	// 사용자 ID 중복 확인
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

	/**
	 * 로그인 관련 메소드
	 */
	// 사용자 존재 여부 확인
	private boolean checkUserExists(String userId) {
		String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next() && rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.out.println("❌ 사용자 조회 오류: " + e.getMessage());
			return false;
		}
	}

	// 사용자 비밀번호 확인
	private boolean checkUserPassword(String userId, String password) {
		String sql = "SELECT user_password FROM users WHERE user_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return password.equals(rs.getString("user_password"));
				}
			}
		} catch (SQLException e) {
			System.out.println("❌ 비밀번호 확인 오류: " + e.getMessage());
		}
		return false;
	}

	/**
	 * User 객체 관련 메소드
	 */
	// User 객체를 DB에 저장
	private boolean saveUser(User user) {
		String sql = "INSERT INTO users (user_id, user_name, user_password, user_email, user_phone, join_date) "
				+ "VALUES (?, ?, ?, ?, ?, SYSDATE)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, user.getUserId());
			pstmt.setString(2, user.getUserName());
			pstmt.setString(3, user.getUserPassword());
			pstmt.setString(4, user.getUserEmail());
			pstmt.setString(5, user.getUserPhone());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("❌ 회원가입 오류: " + e.getMessage());
			return false;
		}
	}

	// User ID로 User 객체 조회
	private User getUserById(String userId) {
		String sql = "SELECT * FROM users WHERE user_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					User user = new User();
					user.setUserId(rs.getString("user_id"));
					user.setUserName(rs.getString("user_name"));
					user.setUserPassword(rs.getString("user_password"));
					user.setUserEmail(rs.getString("user_email"));
					user.setUserPhone(rs.getString("user_phone"));
					user.setJoinDate(rs.getDate("join_date"));
					return user;
				}
			}
		} catch (SQLException e) {
			System.out.println("❌ 사용자 조회 오류: " + e.getMessage());
		}
		return null;
	}

	// User 객체 업데이트
	private boolean updateUser(User user) {
		StringBuilder sql = new StringBuilder("UPDATE users SET ");
		boolean hasChanges = false;

		// 동적 SQL 생성
		if (user.getUserPassword() != null) {
			sql.append("user_password = ?");
			hasChanges = true;
		}

		if (user.getUserEmail() != null) {
			if (hasChanges)
				sql.append(", ");
			sql.append("user_email = ?");
			hasChanges = true;
		}

		if (user.getUserPhone() != null) {
			if (hasChanges)
				sql.append(", ");
			sql.append("user_phone = ?");
			hasChanges = true;
		}

		if (!hasChanges) {
			System.out.println("ℹ️ 변경된 항목이 없습니다.");
			return false;
		}

		sql.append(" WHERE user_id = ?");

		try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
			int paramIndex = 1;

			if (user.getUserPassword() != null) {
				pstmt.setString(paramIndex++, user.getUserPassword());
			}
			if (user.getUserEmail() != null) {
				pstmt.setString(paramIndex++, user.getUserEmail());
			}
			if (user.getUserPhone() != null) {
				pstmt.setString(paramIndex++, user.getUserPhone());
			}
			pstmt.setString(paramIndex, user.getUserId());

			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("❌ 회원정보 수정 오류: " + e.getMessage());
			return false;
		}
	}

	/**
	 * 회원정보 수정 관련 메소드
	 */
	// 이메일 중복 확인 (수정 시 본인 제외)
	private boolean checkEmailDuplicate(String email) {
		String sql = "SELECT COUNT(*) FROM users WHERE user_email = ? AND user_id != ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, email);
			pstmt.setString(2, loginId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next() && rs.getInt(1) > 0) {
					System.out.println("❌ 이미 사용 중인 이메일입니다.");
					return false;
				}
			}
		} catch (SQLException e) {
			System.out.println("❌ 이메일 중복 확인 오류: " + e.getMessage());
			return false;
		}
		return true;
	}

	// 전화번호 중복 확인 (수정 시 본인 제외)
	private boolean checkPhoneDuplicate(String phone) {
		String sql = "SELECT COUNT(*) FROM users WHERE user_phone = ? AND user_id != ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, phone);
			pstmt.setString(2, loginId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next() && rs.getInt(1) > 0) {
					System.out.println("❌ 이미 사용 중인 전화번호입니다.");
					return false;
				}
			}
		} catch (SQLException e) {
			System.out.println("❌ 전화번호 중복 확인 오류: " + e.getMessage());
			return false;
		}
		return true;
	}

	// 현재 사용자 비밀번호 확인
	private boolean verifyCurrentPassword() {
		System.out.print("현재 비밀번호를 입력하세요: ");
		String password = scanner.nextLine();

		User currentUser = getUserById(loginId);
		if (currentUser != null && password.equals(currentUser.getUserPassword())) {
			return true;
		} else {
			System.out.println("❌ 현재 비밀번호가 일치하지 않습니다.");
			return false;
		}
	}

	// 현재 회원정보 조회 및 출력
	private void displayCurrentUserInfo() {
		User currentUser = getUserById(loginId);
		if (currentUser != null) {
			System.out.println("\n[현재 회원정보]");
			System.out.println("─────────────────────────────────────");
			System.out.println("아이디: " + currentUser.getUserId());
			System.out.println("이름: " + currentUser.getUserName());
			System.out.println("이메일: " + currentUser.getUserEmail());
			System.out.println("전화번호: " + currentUser.getUserPhone());
			System.out.println("가입일: " + currentUser.getJoinDate());
			System.out.println("─────────────────────────────────────");
		}
	}

	// 통합 회원정보 수정 메소드
	private void modifyUserInfo() {
		System.out.println("[회원정보 수정]");

		// 1. 현재 비밀번호 확인
		if (!verifyCurrentPassword()) {
			list();
			return;
		}

		// 2. 현재 정보 표시
		displayCurrentUserInfo();

		System.out.println("\n📝 변경하지 않을 항목은 '-'를 입력하세요. (기존 값 유지)");
		System.out.println("─────────────────────────────────────");

		// 3. 새로운 정보 입력
		String newPassword = inputNewUserPassword();
		String newEmail = inputNewUserEmail();
		String newPhone = inputNewUserPhone();

		// 4. 변경사항 미리보기
		displayChangePreview(newPassword, newEmail, newPhone);

		// 5. 최종 확인 후 업데이트
		if (confirmAction()) {
			updateUserInfo(newPassword, newEmail, newPhone);
		}

		list();
	}

	// 새 비밀번호 입력 (유효성 검사 포함)
	private String inputNewUserPassword() {
		String input;
		do {
			System.out.print("새 비밀번호 (7~12자리, 영문+숫자) 또는 '-' (기존 유지): ");
			input = scanner.nextLine().trim();

			if ("-".equals(input)) {
				return null; // 변경하지 않음을 의미
			}

			if (validateUserPassword(input, loginId)) {
				return input;
			}
			// 유효성 검사 실패시 다시 입력
		} while (true);
	}

	// 새 이메일 입력 (유효성 검사 포함)
	private String inputNewUserEmail() {
		String input;
		do {
			System.out.print("새 이메일 또는 '-' (기존 유지): ");
			input = scanner.nextLine().trim();

			if ("-".equals(input)) {
				return null; // 변경하지 않음을 의미
			}

			if (validateEmail(input) && checkEmailDuplicate(input)) {
				return input;
			}
			// 유효성 검사 실패시 다시 입력
		} while (true);
	}

	// 새 전화번호 입력 (유효성 검사 포함)
	private String inputNewUserPhone() {
		String input;
		do {
			System.out.print("새 전화번호 (010-0000-0000) 또는 '-' (기존 유지): ");
			input = scanner.nextLine().trim();

			if ("-".equals(input)) {
				return null; // 변경하지 않음을 의미
			}

			if (validatePhone(input) && checkPhoneDuplicate(input)) {
				return input;
			}
			// 유효성 검사 실패시 다시 입력
		} while (true);
	}

	// 변경사항 미리보기
	private void displayChangePreview(String newPassword, String newEmail, String newPhone) {
		// 현재 사용자 정보 조회
		User currentUser = getUserById(loginId);

		System.out.println("\n[변경사항 미리보기]");
		System.out.println("─────────────────────────────────────");

		if (newPassword != null) {
			System.out.println("비밀번호: 변경됨 (새로운 비밀번호로 설정)");
		} else {
			System.out.println("비밀번호: 기존 비밀번호 (기존 비밀번호 유지)");
		}

		if (newEmail != null) {
			System.out.println("이메일: " + newEmail + " (변경됨)");
		} else {
			System.out.println("이메일: " + (currentUser != null ? currentUser.getUserEmail() : "정보 없음") + " (기존 이메일 유지)");
		}

		if (newPhone != null) {
			System.out.println("전화번호: " + newPhone + " (변경됨)");
		} else {
			System.out
					.println("전화번호: " + (currentUser != null ? currentUser.getUserPhone() : "정보 없음") + " (기존 전화번호 유지)");
		}

		System.out.println("─────────────────────────────────────");
	}

	// 사용자 정보 일괄 업데이트
	private void updateUserInfo(String newPassword, String newEmail, String newPhone) {
		// User 객체 생성하여 변경할 정보만 설정
		User updateUser = new User();
		updateUser.setUserId(loginId);
		updateUser.setUserPassword(newPassword);
		updateUser.setUserEmail(newEmail);
		updateUser.setUserPhone(newPhone);

		if (updateUser(updateUser)) {
			System.out.println("✅ 회원정보가 성공적으로 수정되었습니다!");

			// 변경된 항목들 안내
			if (newPassword != null)
				System.out.println("   - 비밀번호 변경 완료");
			if (newEmail != null)
				System.out.println("   - 이메일 변경 완료: " + newEmail);
			if (newPhone != null)
				System.out.println("   - 전화번호 변경 완료: " + newPhone);
		}
	}

	/**
	 * Account 객체 관련 메소드
	 */
	// Account 객체를 DB에 저장
	private boolean saveAccount(Account account) {
		String sql = "INSERT INTO accounts (account_id, account_name, account_type, account_password, "
				+ "balance, user_id, create_date) VALUES (?, ?, ?, ?, ?, ?, SYSDATE)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, account.getAccountId());
			pstmt.setString(2, account.getAccountName());
			pstmt.setString(3, account.getAccountType());
			pstmt.setString(4, account.getAccountPassword());
			pstmt.setDouble(5, account.getBalance());
			pstmt.setString(6, account.getUserId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("❌ 계좌 생성 오류: " + e.getMessage());
			return false;
		}
	}

	// Account ID로 Account 객체 조회
	private Account getAccountById(String accountId) {
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
			System.out.println("❌ 계좌 조회 오류: " + e.getMessage());
		}
		return null;
	}

	// Account 잔액 업데이트
	private boolean updateAccountBalance(String accountId, double newBalance) {
		String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setDouble(1, newBalance);
			pstmt.setString(2, accountId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("❌ 잔액 업데이트 오류: " + e.getMessage());
			return false;
		}
	}

	// Account 비밀번호 업데이트
	private boolean updateAccountPassword(String accountId, String newPassword) {
		String sql = "UPDATE accounts SET account_password = ? WHERE account_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newPassword);
			pstmt.setString(2, accountId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("❌ 비밀번호 변경 오류: " + e.getMessage());
			return false;
		}
	}

	// Account 삭제
	private boolean deleteAccount(String accountId) {
		String sql = "DELETE FROM accounts WHERE account_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, accountId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("❌ 계좌 삭제 오류: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Transaction 객체 관련 메소드
	 */
	// Transaction 객체를 DB에 저장
	private boolean saveTransaction(Transaction transaction) {
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

	/**
	 * 계좌 관련 입력 및 검증 메소드
	 */
	// 계좌 비밀번호 입력 및 검증
	private String inputAccountPassword() {
		String password;
		do {
			password = input("계좌 비밀번호 (4자리 숫자): ");
			if (validateAccountPassword(password)) {
				return password;
			}
		} while (true);
	}

	// 계좌 비밀번호 유효성 검사
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

	// 금액 입력 및 검증
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

	// 계좌번호 입력 및 검증
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

	// 계좌 비밀번호 확인
	private boolean checkPassword(String accountId) {
		String password;
		do {
			System.out.print("계좌 비밀번호 (4자리): ");
			password = scanner.nextLine();
			if (password.length() != 4 || !password.matches("\\d{4}")) {
				System.out.println("❌ 계좌 비밀번호는 4자리 숫자여야 합니다.");
				continue;
			}
			if (verifyPassword(accountId, password))
				return true;
			System.out.println("❌ 계좌 비밀번호가 일치하지 않습니다.");
		} while (true);
	}

	/**
	 * 계좌 관련 조회 및 검증 메소드
	 */

	// 계좌 존재 여부 확인
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

	// 본인 계좌 여부 확인
	private boolean isMyAccount(String accountId) {
		Account account = getAccountById(accountId);
		return account != null && loginId != null && loginId.equals(account.getUserId());
	}

	// 계좌 비밀번호 검증
	private boolean verifyPassword(String accountId, String password) {
		Account account = getAccountById(accountId);
		return account != null && password.equals(account.getAccountPassword());
	}

	// 계좌 소유자명 조회
	private String getAccountHolderName(String accountId) {
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
			System.out.println("❌ 예금주명 조회 오류: " + e.getMessage());
		}
		return "미상";
	}

	// 계좌 잔액 조회
	private double getBalance(String accountId) {
		Account account = getAccountById(accountId);
		return account != null ? account.getBalance() : -1;
	}

	/**
	 * 페이징 관련 메소드
	 */

	// 전체 거래내역 갯수 조회
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
			System.out.println("❌ 거래내역 개수 조회 오류: " + e.getMessage());
		}
		return 0;
	}

	// 페이지 번호 입력
	private int inputPageNumber(int totalPages) {
		int pageNumber;
		do {
			try {
				System.out.print("이동할 페이지 번호 (1~" + totalPages + "): ");
				pageNumber = Integer.parseInt(scanner.nextLine());

				if (pageNumber >= 1 && pageNumber <= totalPages) {
					return pageNumber;
				} else {
					System.out.println("❌ 1~" + totalPages + " 범위의 페이지 번호를 입력해주세요.");
				}
			} catch (NumberFormatException e) {
				System.out.println("❌ 올바른 숫자를 입력해주세요.");
			}
		} while (true);
	}

	// 거래내역 페이징 표시
	private void displayTransactionHistory(String accountId) {
		final int PAGE_SIZE = 3; // 페이지당 거래 수
		int currentPage = 1;

		while (true) {
			// 전체 거래 수 조회
			int totalCount = getTotalTransactionCount(accountId);
			if (totalCount == 0) {
				System.out.println("\n[거래내역] 계좌번호: " + accountId + " (" + getAccountHolderName(accountId) + ")");
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
				currentPage = inputPageNumber(totalPages);
				break;
			case "0": // 돌아가기
				return;
			default:
				System.out.println("❌ 0~5번을 선택해주세요.");
			}
		}
	}

	// 특정 페이지의 거래내역 표시 (최신 거래가 가장 큰 번호)
	private void displayTransactionPage(String accountId, int currentPage, int pageSize, int totalCount,
			int totalPages) {
		System.out.println("\n[거래내역] 계좌번호: " + accountId + " (" + getAccountHolderName(accountId) + ")");
		System.out.println("📊 [" + currentPage + "/" + totalPages + " 페이지] 총 " + totalCount + "건");
		System.out.println("========================================================");

		// Oracle 페이징 쿼리 (ROWNUM 사용)
		String sql = "SELECT * FROM (" + "  SELECT ROWNUM rn, t.* FROM ("
				+ "    SELECT * FROM transactions WHERE account_id = ? " + "    ORDER BY transaction_date DESC" + // 최신순
																													// 정렬
																													// 유지
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

					String counterpartDisplay = getCounterpartDisplay(transactionType, counterpartName, depositorName,
							counterpartAccount);

					if (memo == null)
						memo = "-";

					// 순번 표시 - 최신 거래가 가장 큰 번호
					System.out.println("📋 " + displayIndex + "번째 거래");
					System.out.println("거래번호: " + rs.getString("transaction_id"));
					System.out.println("거래구분: " + transactionType);
					System.out.println("상대방정보: " + counterpartDisplay);
					System.out.println("거래일시: " + dateFormat.format(rs.getTimestamp("transaction_date")));
					System.out.println("메모: " + memo);
					System.out.println("거래금액: " + formatCurrency(rs.getDouble("amount")));
					System.out.println("거래후잔액: " + formatCurrency(rs.getDouble("balance_after")));
					System.out.println("────────────────────────────────────────────────────────────────");
				}

				if (!hasTransactions) {
					System.out.println("해당 페이지에 거래내역이 없습니다.");
				}
			}
		} catch (SQLException e) {
			System.out.println("❌ 거래내역 조회 오류: " + e.getMessage());
		}
	}

	// ========================================
	// 사용자 관리 (회원가입/로그인)
	// ========================================

	// 로그인 처리
	private void login() {
		System.out.println("[로그인]");

		// 1. 아이디 검증
		String userId;
		do {
			userId = input("아이디: ");
			if (checkUserExists(userId)) {
				break;
			} else {
				System.out.println("❌ 존재하지 않는 아이디입니다. 다시 입력해주세요.");
			}
		} while (true);

		// 2. 비밀번호 검증
		String password;
		do {
			password = input("비밀번호: ");
			if (checkUserPassword(userId, password)) {
				break;
			} else {
				System.out.println("❌ 비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
			}
		} while (true);

		// 3. 최종 확인
		if (confirmAction()) {
			loginId = userId;
			System.out.println("✅ 로그인 성공!");
		}

		list();
	}

	// 로그아웃 처리
	private void logout() {
		loginId = null;
		System.out.println("로그아웃되었습니다.");
		list();
	}

	// 회원가입 처리
	private void join() {
		System.out.println("[회원가입]");
		String userId = inputUserId();
		String userName = inputUserName();
		String password = inputUserPassword(userId);
		String email = inputEmail();
		String phone = inputPhone();

		if (confirmAction()) {
			// User 객체 생성
			User user = new User();
			user.setUserId(userId);
			user.setUserName(userName);
			user.setUserPassword(password);
			user.setUserEmail(email);
			user.setUserPhone(phone);
			user.setJoinDate(new Date());

			if (saveUser(user)) {
				System.out.println("✅ 회원가입이 완료되었습니다!");
			}
		}
		list();
	}

	// ========================================
	// 계좌 관리 기능
	// ========================================

	// 계좌 생성
	private void createAccount() {
		if (loginId == null) {
			System.out.println("❌ 계좌 생성은 로그인 후 이용 가능합니다.");
			menu();
			return;
		}

		System.out.println("[계좌 생성]");
		String[] types = { "보통예금", "정기예금", "적금" };

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
				if (choice >= 1 && choice <= 3)
					break;
			} catch (NumberFormatException e) {
			}
			System.out.println("❌ 1~3번을 선택해주세요.");
		} while (true);

		String accountType = types[choice - 1];
		String accountName = accountType + " 계좌_" + getUserName(loginId);
		String password = inputAccountPassword();
		double initialBalance = inputAmount("초기 입금액 (1,000원 이상): ");

		if (confirmAction()) {
			String accountId = newAccNum();

			// Account 객체 생성
			Account account = new Account(accountId, accountName, accountType, password, initialBalance, loginId);

			if (saveAccount(account)) {
				// Transaction 객체 생성하여 거래내역 저장
				Transaction transaction = new Transaction();
				transaction.setTransactionId(newTxId());
				transaction.setAccountId(accountId);
				transaction.setTransactionType("입금");
				transaction.setAmount(initialBalance);
				transaction.setBalanceAfter(initialBalance);
				transaction.setDepositorName(getUserName(loginId));
				transaction.setTransactionMemo("계좌개설");

				saveTransaction(transaction);

				System.out.println("✅ 계좌가 성공적으로 생성되었습니다!");
				System.out.println("   계좌번호: " + accountId);
			}
		}
		list();
	}

	// 계좌 조회
	private void readAccount() {
		System.out.println("[계좌 조회]");
		String accountId = input("계좌번호: ");

		Account account = getAccountById(accountId);
		if (account != null) {
			User accountHolder = getUserById(account.getUserId());

			System.out.println("계좌번호: " + account.getAccountId());
			System.out.println("계좌명: " + account.getAccountName());
			System.out.println("계좌종류: " + account.getAccountType());
			System.out.println("잔액: " + formatCurrency(account.getBalance()));
			System.out.println("소유자: " + (accountHolder != null ? accountHolder.getUserName() : "미상"));
			System.out.println("개설일: " + account.getCreateDate());

			if (loginId != null && loginId.equals(account.getUserId())) {
				System.out.println("보조메뉴: 1.삭제 | 2.목록");
				if ("1".equals(scanner.nextLine())) {
					deleteAccountMenu(accountId);
				}
			}
		} else {
			System.out.println("해당 계좌를 찾을 수 없습니다.");
		}
		list();
	}

	// 계좌 삭제
	private void deleteAccountMenu(String accountId) {
		if (accountId == null) {
			System.out.println("[계좌 삭제]");
			accountId = inputAccountId("삭제할 계좌번호: ", true);
		}

		if (!checkPassword(accountId) || !confirmAction()) {
			list();
			return;
		}

		if (deleteAccount(accountId)) {
			System.out.println("✅ 계좌가 삭제되었습니다!");
		}
		list();
	}

	// 계좌 비밀번호 변경
	private void changePassword() {
		System.out.println("[계좌 비밀번호 변경]");
		String accountId = inputAccountId("계좌번호: ", true);

		if (checkPassword(accountId)) {
			// 현재 계좌 정보 조회
			Account account = getAccountById(accountId);
			String currentPassword = account != null ? account.getAccountPassword() : null;
			String newPassword;

			do {
				newPassword = inputAccountPassword();
				if (currentPassword != null && currentPassword.equals(newPassword)) {
					System.out.println("❌ 새 비밀번호는 현재 비밀번호와 달라야 합니다.");
				} else {
					break;
				}
			} while (true);

			if (confirmAction()) {
				if (updateAccountPassword(accountId, newPassword)) {
					System.out.println("✅ 계좌 비밀번호가 변경되었습니다!");
				}
			}
		}
		list();
	}

	// ========================================
	// 거래 업무 기능
	// ========================================

	// 입금 처리
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
		if (memo.isEmpty())
			memo = null;

		if (confirmAction()) {
			double currentBalance = getBalance(accountId);
			double newBalance = currentBalance + amount;

			if (updateAccountBalance(accountId, newBalance)) {
				// Transaction 객체 생성하여 거래내역 저장
				Transaction transaction = new Transaction();
				transaction.setTransactionId(newTxId());
				transaction.setAccountId(accountId);
				transaction.setTransactionType("입금");
				transaction.setAmount(amount);
				transaction.setBalanceAfter(newBalance);
				transaction.setDepositorName(depositorName);
				transaction.setTransactionMemo(memo);

				saveTransaction(transaction);

				System.out.println("✅ 입금이 완료되었습니다!");
				System.out.println("   입금액: " + formatCurrency(amount));
				if (isMyAccount(accountId)) {
					System.out.println("   현재잔액: " + formatCurrency(newBalance));
				}
			}
		}
		list();
	}

	// 출금 처리
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
		if (memo.isEmpty())
			memo = null;

		if (confirmAction()) {
			double newBalance = currentBalance - amount;

			if (updateAccountBalance(accountId, newBalance)) {
				// Transaction 객체 생성하여 거래내역 저장
				Transaction transaction = new Transaction();
				transaction.setTransactionId(newTxId());
				transaction.setAccountId(accountId);
				transaction.setTransactionType("출금");
				transaction.setAmount(amount);
				transaction.setBalanceAfter(newBalance);
				transaction.setTransactionMemo(memo);

				saveTransaction(transaction);

				System.out.println("✅ 출금이 완료되었습니다!");
				System.out.println("   출금액: " + formatCurrency(amount));
				System.out.println("   잔여잔액: " + formatCurrency(newBalance));
			}
		}
		list();
	}

	// 이체 처리
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
			if (!fromAccountId.equals(toAccountId))
				break;
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
		if (memo.isEmpty())
			memo = null;

		if (confirmAction()) {
			try {
				conn.setAutoCommit(false);

				// 출금 처리
				double fromBalance = currentBalance - amount;
				if (!updateAccountBalance(fromAccountId, fromBalance)) {
					throw new SQLException("출금 처리 실패");
				}

				// 입금 처리
				double toCurrentBalance = getBalance(toAccountId);
				double toBalance = toCurrentBalance + amount;
				if (!updateAccountBalance(toAccountId, toBalance)) {
					throw new SQLException("입금 처리 실패");
				}

				// 거래내역 저장
				String fromName = getAccountHolderName(fromAccountId);
				String toName = getAccountHolderName(toAccountId);

				// 출금 거래내역
				Transaction fromTransaction = new Transaction();
				fromTransaction.setTransactionId(newTxId());
				fromTransaction.setAccountId(fromAccountId);
				fromTransaction.setTransactionType("이체출금");
				fromTransaction.setAmount(amount);
				fromTransaction.setBalanceAfter(fromBalance);
				fromTransaction.setCounterpartAccount(toAccountId);
				fromTransaction.setCounterpartName(toName);
				fromTransaction.setTransactionMemo(memo);

				// 입금 거래내역
				Transaction toTransaction = new Transaction();
				toTransaction.setTransactionId(newTxId());
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
				System.out.println("   이체금액: " + formatCurrency(amount));
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
		list();
	}

	// 거래내역 조회 (페이징 기능 포함)
	private void history() {
		System.out.println("[거래내역 조회]");
		String accountId = inputAccountId("계좌번호: ", true);

		if (!checkPassword(accountId)) {
			list();
			return;
		}

		// 페이징 시작
		displayTransactionHistory(accountId);
		list();
	}

	// 계좌 목록 표시
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

		String sql = "SELECT a.*, u.user_name FROM accounts a JOIN users u ON a.user_id = u.user_id "
				+ "WHERE a.user_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, loginId);
			try (ResultSet rs = pstmt.executeQuery()) {
				boolean hasAccounts = false;
				while (rs.next()) {
					hasAccounts = true;

					String accountName = rs.getString("account_name");
					String displayAccountName = accountName.length() > 12 ? accountName.substring(0, 12) + ".."
							: accountName;

					System.out.println(rs.getString("account_id") + "\t" + displayAccountName + "\t\t"
							+ rs.getString("account_type") + "\t\t" + rs.getString("user_name") + "\t\t"
							+ formatCurrency(rs.getDouble("balance")));
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

	// 콘솔창 메뉴 표시
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
			System.out.println("✅기타메뉴: 7.계좌비밀번호변경 | 10.회원정보수정 | 9.로그아웃 | 0.종료");
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
			case "8" -> deleteAccountMenu(null);
			case "9" -> logout();
			case "10" -> modifyUserInfo();
			case "0" -> exit();
			default -> {
				System.out.println("❌ 0~10번의 숫자만 입력이 가능합니다.");
				menu();
			}
			}
		}
	}

	public static void main(String[] args) {
		BankSystem bankSystem = new BankSystem();
		bankSystem.list();
	}
}
package banksystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import banksystem.helper.InputHelper;
import banksystem.helper.ValidationHelper;
import banksystem.manager.AccountManager;
import banksystem.manager.AdminManager;
import banksystem.manager.SchedulerManager;
import banksystem.manager.TransactionManager;
import banksystem.manager.UserManager;

public class BankSystem {
	private Scanner scanner = new Scanner(System.in);
	private Connection conn = null;
	private String loginId = null;
	private String adminLoginId = null;

	// helper 및 manager 객체들
	private ValidationHelper validator;
	private InputHelper inputHelper;
	private UserManager userManager;
	private AccountManager accountManager;
	private TransactionManager transactionManager;
	private AdminManager adminManager;
	private SchedulerManager schedulerManager;

	public BankSystem() {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/orcl", "jhw1", "1234");
			conn.setAutoCommit(true);
			System.out.println("은행 계좌 시스템 DB 연결 성공!" + "\n");

			// 기존 manager 객체 초기화
			validator = new ValidationHelper(conn);
			inputHelper = new InputHelper(scanner, validator, conn);
			userManager = new UserManager(conn, validator, inputHelper, scanner);

			transactionManager = new TransactionManager(conn, inputHelper, null, scanner);
			accountManager = new AccountManager(conn, validator, inputHelper, userManager, scanner, transactionManager);
			setAccountManagerToTransactionManager();
			inputHelper.setAccountManager(accountManager);
			adminManager = new AdminManager(conn, validator, inputHelper, scanner);

			// 스케줄러 초기화 및 시작
			schedulerManager = new SchedulerManager(conn, adminManager);
			schedulerManager.start();

			// 시스템 종료 시 스케줄러도 함께 정리
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\n시스템이 종료됩니다...");
				if (schedulerManager != null) {
					schedulerManager.stop();
				}
				System.out.println("안전하게 종료되었습니다.");
			}));

		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
	}

	private void setAccountManagerToTransactionManager() {
		if (transactionManager != null && accountManager != null) {
			transactionManager.setAccountManager(accountManager);
		}
	}

	private void exit() {
		try {
			// 종료 시 스케줄러 정리
			if (schedulerManager != null) {
				schedulerManager.stop();
			}

			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			System.out.println("DB 연결 종료 중 오류: " + e.getMessage());
		}
		System.out.println("은행 시스템이 정상적으로 종료되었습니다.");
		System.exit(0);
	}

	private void logout() {
		loginId = null;
		adminLoginId = null;
		System.out.println("로그아웃되었습니다.");
		list();
	}

	private void list() {
		if (loginId == null && adminLoginId == null) {
			System.out.println("\n-- 은행 계좌 관리 시스템 --");
			System.out.println("계좌 서비스를 이용하려면 로그인해주세요.");

			// 스케줄러 상태 표시
			if (schedulerManager != null && schedulerManager.isRunning()) {
				System.out.println("✅ 자동 이자 지급: " + schedulerManager.getNextExecutionInfo());
			}

			menu();
			return;
		}

		if (adminLoginId != null) {
			System.out.println("\n[관리자 모드] " + adminManager.getAdminName(adminLoginId) + "님");

			// 스케줄러 상태만 표시
			if (schedulerManager != null) {
				System.out.println(schedulerManager.getNextExecutionInfo());
			}

		} else {
			accountManager.listAccounts(loginId);
		}
		menu();
	}

	private void menu() {
		System.out.println(
				"============================================================================================================================");

		if (loginId == null && adminLoginId == null) {
			System.out.println("✅ 메인메뉴: 1.회원가입 | 2.사용자 로그인 | 3.관리자 로그인 | 4.종료");
			System.out.print("메뉴선택: ");

			String menuNo = scanner.nextLine();
			switch (menuNo) {
			case "1" -> {
				userManager.join();
				list();
			}
			case "2" -> {
				String userId = userManager.login();
				if (userId != null) {
					loginId = userId;
				}
				list();
			}
			case "3" -> {
				String adminId = adminManager.adminLogin();
				if (adminId != null) {
					adminLoginId = adminId;
				}
				list();
			}
			case "4" -> exit();
			default -> {
				System.out.println("1 ~ 4번의 숫자만 입력이 가능합니다.");
				menu();
			}
			}

		} else if (adminLoginId != null) {
			// 관리자 메뉴에 스케줄러 관리 추가
			System.out.println("✅ 계좌관리\t\t✅ 이자관리\t\t✅ 시스템관리\t\t✅ 기타");
			System.out.println(
					"============================================================================================================================");
			System.out.println("1. 전체계좌조회\t\t3. 수동이자지급\t\t5. 스케줄러상태\t\t6. 로그아웃");
			System.out.println("2. 사용자별계좌조회\t\t4. 이자지급내역조회\t\t0. 종료");
			System.out.println("============================================================================================================================");
			System.out.print("메뉴선택: ");

			String menuNo = scanner.nextLine();
			switch (menuNo) {
			case "1" -> {
				adminManager.viewAllAccounts();
				list();
			}
			case "2" -> {
				adminManager.viewUserAccounts();
				list();
			}
			case "3" -> { 
				executeManualInterestPayment();
				list();
			}
			case "4" -> {
				adminManager.viewInterestHistory();
				list();
			}
			case "5" -> { 
				showSchedulerStatus();
				list();
			}
			case "6" -> logout();
			case "0" -> exit();
			default -> {
				System.out.println("0 ~ 6번의 숫자만 입력이 가능합니다.");
				menu();
			}
			}

		} else {
			System.out.println("✅ 계좌관리\t✅ 거래업무\t✅ 기타설정\t✅ 시스템");
			System.out.println(
					"============================================================================================================================");
			System.out.println("1. 계좌생성\t4. 입금\t\t8. 계좌비밀번호변경\t10. 로그아웃");
			System.out.println("2. 계좌조회\t5. 출금\t\t9. 회원정보수정\t0. 종료");
			System.out.println("3. 계좌해지\t6. 이체");
			System.out.println("\t\t7. 거래내역조회");
			System.out.println("============================================================================================================================");
			System.out.print("메뉴선택: ");

			String menuNo = scanner.nextLine();
			switch (menuNo) {
			case "1" -> {
				accountManager.createAccount(loginId);
				list();
			}
			case "2" -> {
				accountManager.readAccount(loginId);
				list();
			}
			case "3" -> {
				accountManager.deleteAccountMenu(loginId, null);
				list();
			}
			case "4" -> {
				transactionManager.deposit(loginId);
				list();
			}
			case "5" -> {
				transactionManager.withdraw(loginId);
				list();
			}
			case "6" -> {
				transactionManager.transfer(loginId);
				list();
			}
			case "7" -> {
				transactionManager.history(loginId);
				menu();
			}
			case "8" -> {
				accountManager.changePassword(loginId);
				list();
			}
			case "9" -> {
				userManager.modifyUserInfo(loginId);
				list();
			}
			case "10" -> logout();
			case "0" -> exit();
			default -> {
				System.out.println("0 ~ 10번의 숫자만 입력이 가능합니다.");
				menu();
			}
			}
		}
	}

	// 스케줄러 상태 표시
	private void showSchedulerStatus() {
		System.out.println("\n[스케줄러 상태 정보]");
		System.out.println(
				"============================================================================================================================");

		if (schedulerManager != null) {
			if (schedulerManager.isRunning()) {
				System.out.println("상태: 실행 중");
				System.out.println(schedulerManager.getNextExecutionInfo());
				System.out.println("실행 시간: 매월 마지막 날 오후 2시");
				System.out.println("실행 내용: 모든 계좌 이자 일괄 지급");
			} else {
				System.out.println("상태: 중지됨");
			}
		} else {
			System.out.println("상태: 스케줄러가 초기화되지 않음");
		}

		System.out.println("============================================================================================================================");
		System.out.println("엔터키를 누르면 메뉴로 돌아갑니다.");
		scanner.nextLine();
	}

	// 수동 이자 지급 
	private void executeManualInterestPayment() {
		System.out.println("\n[수동 이자 지급]");
		System.out.println(
				"============================================================================================================================");
		System.out.println("   주의사항:");
		System.out.println("   - 자동 스케줄러가 현재 실행 중입니다.");

		if (schedulerManager != null && schedulerManager.isRunning()) {
			System.out.println("   - " + schedulerManager.getNextExecutionInfo());
		}

		System.out.println("   - 수동 지급은 긴급상황이나 테스트 목적으로만 사용하세요.");
		System.out.println("   - 정기 이자 지급은 자동 스케줄러가 처리합니다.");
		System.out.println(
				"============================================================================================================================");

		System.out.println("\n수동 이자 지급을 실행하는 이유를 선택하세요:");
		System.out.println("1. 긴급 상황 (고객 민원, 시스템 오류 등)");
		System.out.println("2. 테스트 목적");
		System.out.println("3. 특별 지급 (이벤트, 보상 등)");
		System.out.println("4. 스케줄러 문제로 인한 수동 실행");
		System.out.println("5. 취소");
		System.out.print("선택 (1 - 5): ");

		String choice = scanner.nextLine();
		String reason = "";

		switch (choice) {
		case "1" -> reason = "MANUAL_EMERGENCY";
		case "2" -> reason = "MANUAL_TEST";
		case "3" -> reason = "MANUAL_SPECIAL";
		case "4" -> reason = "MANUAL_SCHEDULER_ISSUE";
		case "5" -> {
			System.out.println("수동 이자 지급이 취소되었습니다.");
			return;
		}
		default -> {
			System.out.println("잘못된 선택입니다. 취소됩니다.");
			return;
		}
		}

		System.out.println("\n정말로 수동 이자 지급을 실행하시겠습니까?");
		if (inputHelper.confirmAction()) {
			System.out.println("수동 이자 지급을 실행합니다... (사유: " + reason + ")");
			adminManager.executeInterestPayment(adminLoginId + "_" + reason);
			System.out.println("✅ 수동 이자 지급이 완료되었습니다!");
		} else {
			System.out.println("수동 이자 지급이 취소되었습니다.");
		}
	}

	public static void main(String[] args) {
		BankSystem bankSystem = new BankSystem();
		bankSystem.list();
	}
}
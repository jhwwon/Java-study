package banksystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import banksystem.helper.InputHelper;
import banksystem.helper.ValidationHelper;
import banksystem.manager.AccountManager;
import banksystem.manager.AdminManager;
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

    public BankSystem() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/orcl", "jhw1", "1234");
            conn.setAutoCommit(true);
            System.out.println("은행 계좌 시스템 DB 연결 성공!");
            
            // 3단계: helper 및 manager 객체 초기화 순서 조정
            validator = new ValidationHelper(conn);
            inputHelper = new InputHelper(scanner, validator, conn);
            userManager = new UserManager(conn, validator, inputHelper, scanner);
            
            // 1. TransactionManager를 먼저 생성
            transactionManager = new TransactionManager(conn, inputHelper, null, scanner);
            
            // 2. AccountManager 생성 시 TransactionManager를 전달
            accountManager = new AccountManager(conn, validator, inputHelper, userManager, scanner, transactionManager);
            
            // 3. TransactionManager에 AccountManager 참조 설정 (상호 참조)
            setAccountManagerToTransactionManager();
            
            // 4. InputHelper에 AccountManager 설정
            inputHelper.setAccountManager(accountManager);
            
            // 5. AdminManager 생성
            adminManager = new AdminManager(conn, validator, inputHelper, scanner);
            
        } catch (Exception e) {
            e.printStackTrace();
            exit();
        }
    }
    
    /**
     * 3단계: TransactionManager에 AccountManager 참조 설정
     * (TransactionManager 생성 시에는 AccountManager가 아직 없으므로 나중에 설정)
     */
    private void setAccountManagerToTransactionManager() {
        if (transactionManager != null && accountManager != null) {
            // 4단계: TransactionManager에 AccountManager 참조 설정
            transactionManager.setAccountManager(accountManager);
        }
    }

    private void exit() {
        try {
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

    // 계좌 목록 표시
    private void list() {
        if (loginId == null && adminLoginId == null) {
            System.out.println("\n-- 은행 계좌 관리 시스템 --");
            System.out.println("계좌 서비스를 이용하려면 로그인해주세요.");
            menu();
            return;
        }
        
        if(adminLoginId != null) {
        	System.out.println("\n[관리자 모드] " + adminManager.getAdminName(adminLoginId)  + "님");
        	adminManager.viewAllAccounts();   //관리자는 전체 계좌 목록 표시
        } else {  //일반 사용자 로그인 상태
        	accountManager.listAccounts(loginId);
        }
        menu();
    }

 // 콘솔창 메뉴 표시
    private void menu() {
    	System.out.println("============================================================================================================================");
        
        if (loginId == null && adminLoginId == null) {
            // 로그인되지 않은 상태
            System.out.println("메인메뉴: 1.회원가입 | 2.사용자 로그인 | 3.관리자 로그인 | 4.종료");
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
                    if(adminId != null) {
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
            // 관리자 로그인 상태
            System.out.println("✅관리자메뉴: 1.전체계좌조회 | 2.사용자별계좌조회 | 3.이자일괄지급 | 4.이자지급내역조회 | 5. 로그아웃 | 0. 종료");
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
                    // 이자 일괄 지급 기능 (수정됨)
                    adminManager.executeInterestPayment(adminLoginId);
                    list();
                }
                case "4" -> {
                    // 이자 지급 내역 조회 기능 (수정됨)
                    adminManager.viewInterestHistory();
                    list();
                }
                case "5" -> logout();
                case "0" -> exit();
                default -> {
                    System.out.println("0~5번의 숫자만 입력이 가능합니다.");
                    menu();
                }
            }
            
        } else {
            // 일반 사용자 로그인 상태
            System.out.println("✅계좌관리: 1.계좌생성 | 2.계좌조회 | 3.계좌해지");
            System.out.println("✅거래업무: 4.입금 | 5.출금 | 6.이체 | 7.거래내역조회");
            System.out.println("✅기타메뉴: 8.계좌비밀번호변경 | 9.회원정보수정 | 10.로그아웃 | 0.종료");
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
                    list();
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
                    System.out.println("0~10번의 숫자만 입력이 가능합니다.");
                    menu();
                }
            }
        }
    }

    // main 메소드가 클래스 안에 있어야 합니다!
    public static void main(String[] args) {
        BankSystem bankSystem = new BankSystem();
        bankSystem.list();
    }
}
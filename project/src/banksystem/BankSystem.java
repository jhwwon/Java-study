package banksystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import banksystem.helper.InputHelper;
import banksystem.helper.ValidationHelper;
import banksystem.manager.AccountManager;
import banksystem.manager.TransactionManager;
import banksystem.manager.UserManager;

public class BankSystem {
    private Scanner scanner = new Scanner(System.in);
    private Connection conn = null;
    private String loginId = null;
    
    // 헬퍼 및 매니저 객체들
    private ValidationHelper validator;
    private InputHelper inputHelper;
    private UserManager userManager;
    private AccountManager accountManager;
    private TransactionManager transactionManager;

    public BankSystem() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/orcl", "jhw1", "1234");
            conn.setAutoCommit(true);
            System.out.println("은행 계좌 시스템 DB 연결 성공!");
            
            // 헬퍼 및 매니저 객체 초기화
            validator = new ValidationHelper(conn);
            inputHelper = new InputHelper(scanner, validator, conn);
            userManager = new UserManager(conn, validator, inputHelper, scanner);
            accountManager = new AccountManager(conn, validator, inputHelper, userManager, scanner);
            transactionManager = new TransactionManager(conn, validator, inputHelper, accountManager, userManager, scanner);
            
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

    // 로그아웃 처리
    private void logout() {
        loginId = null;
        System.out.println("로그아웃되었습니다.");
        list();
    }

    // 계좌 목록 표시
    private void list() {
        if (loginId == null) {
            System.out.println("\n-- 은행 계좌 관리 시스템 --");
            System.out.println("계좌 서비스를 이용하려면 로그인해주세요.");
            menu();
            return;
        }

        accountManager.listAccounts(loginId);
        menu();
    }

    // 콘솔창 메뉴 표시
    private void menu() {
        System.out.println("====================================================================================");
        if (loginId == null) {
            System.out.println("메인메뉴: 1.회원가입 | 2.로그인 | 3.종료");
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
                case "1" -> {
                    accountManager.createAccount(loginId);
                    list();
                }
                case "2" -> {
                    accountManager.readAccount(loginId);
                    list();
                }
                case "3" -> {
                    transactionManager.deposit(loginId);
                    list();
                }
                case "4" -> {
                    transactionManager.withdraw(loginId);
                    list();
                }
                case "5" -> {
                    transactionManager.transfer(loginId);
                    list();
                }
                case "6" -> {
                    transactionManager.history(loginId);
                    list();
                }
                case "7" -> {
                    accountManager.changePassword(loginId);
                    list();
                }
                case "8" -> {
                    accountManager.deleteAccountMenu(loginId, null);
                    list();
                }
                case "9" -> logout();
                case "10" -> {
                    userManager.modifyUserInfo(loginId);
                    list();
                }
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
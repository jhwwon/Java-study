package banksystem.helper;

import java.sql.Connection;
import java.util.Scanner;
import banksystem.manager.AccountManager;  

public class InputHelper {
    private Scanner scanner;
    private ValidationHelper validator;
    private Connection conn;
    private AccountManager accountManager;

    public InputHelper(Scanner scanner, ValidationHelper validator, Connection conn) {
        this.scanner = scanner;
        this.validator = validator;
        this.conn = conn;
    }
    
    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    // 기본 입력 메소드 - 빈 값 입력 방지
    public String input(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();  // 사용자로부터 한줄 입력받은 후 앞뒤 공백 제거
        } while (input.isEmpty());
        return input;
    }

    public boolean confirmAction() {
        System.out.println("보조메뉴: 1.확인 | 2.취소");
        System.out.print("메뉴선택: ");
        return "1".equals(scanner.nextLine());
    }

    // 사용자 ID 입력 및 검증
    public String inputUserId() {
        String userId;
        do {
            userId = input("아이디 (4~8자리, 영문+숫자): ");
            if (validator.validateUserId(userId) && validator.checkUserIdDuplicate(userId)) {
                return userId;
            }
        } while (true);
    }

    // 이름 입력 및 검증
    public String inputName() {
        String userName;
        do {
            userName = input("이름: ");
            if (validator.validateUserName(userName)) {
                return userName;
            }
        } while (true);
    }

    // 비밀번호 입력 및 검증
    public String inputPassword(String userId) {
        String password;
        do {
            password = input("비밀번호 (7~12자리, 영문+숫자): ");
            if (validator.validateUserPassword(password, userId)) {
                return password;
            }
        } while (true);
    }

    // 이메일 입력 및 검증
    public String inputEmail() {
        String email;
        do {
            email = input("이메일: ");
            if (validator.validateEmail(email)) {
                return email;
            }
        } while (true);
    }

    // 전화번호 입력 및 검증
    public String inputPhone() {
        String phone;
        do {
            phone = input("전화번호: ");
            if (validator.validatePhone(phone)) {
                return phone;
            }
        } while (true);
    }

    // 계좌 비밀번호 입력 및 검증
    public String inputAccountPassword() {
        String password;
        do {
            password = input("계좌 비밀번호 (4자리 숫자): ");
            if (validator.validateAccountPassword(password)) {
                return password;
            }
        } while (true);
    }

    // 금액 입력 및 검증
    public double inputAmount(String prompt) {
        double amount;
        do {
            try {
                System.out.print(prompt);
                amount = Double.parseDouble(scanner.nextLine());
                if (amount < 1000) {
                    System.out.println("금액은 1,000원 이상이어야 합니다.");
                    continue;
                }
                if (amount > 5000000) {
                    System.out.println("금액이 너무 큽니다. (최대 500만원)");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
            }
        } while (true);
    }

    // 계좌번호 입력 및 검증
    public String inputAccountId(String prompt, boolean ownOnly, String loginId) {
        String accountId;
        do {
            accountId = input(prompt);
            
            // accountManager가 설정되지 않았으면 그냥 반환 (기존 동작)
            if (accountManager == null) {
                return accountId;
            }
            
            // 1. 계좌 존재 여부 확인
            if (!accountManager.accountExists(accountId)) {
                System.out.println("존재하지 않는 계좌번호입니다.");
                continue;
            }
            
            // 2. 본인 계좌 여부 확인 (ownOnly가 true일 때만)
            if (ownOnly && !accountManager.isMyAccount(accountId, loginId)) {
                System.out.println("본인 계좌만 입력 가능합니다.");
                continue;
            }
            
            // 모든 검증 통과
            return accountId;
            
        } while (true);
    }

    // 계좌 비밀번호 확인
    public boolean checkPassword(String accountId) {
        String password;
        do {
            System.out.print("계좌 비밀번호 (4자리): ");
            password = scanner.nextLine();
            if (password.length() != 4 || !password.matches("\\d{4}")) {
                System.out.println("계좌 비밀번호는 4자리 숫자여야 합니다.");
                continue;
            }
            // 실제 검증은 AccountManager
            return true;  
        } while (true);
    }

    // 새 비밀번호 입력 (유효성 검사 포함)
    public String inputNewUserPassword(String loginId) {
        String input;
        do {
            System.out.print("새 비밀번호 (7~12자리, 영문+숫자) 또는 엔터 (기존 유지): ");
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {  
                return null;
            }

            if (validator.validateUserPassword(input, loginId)) {
                return input;
            }
        } while (true);
    }

    // 새 이메일 입력 (유효성 검사 포함)
    public String inputNewUserEmail(String loginId) {
        String input;
        do {
            System.out.print("새 이메일 또는 엔터 (기존 유지): ");
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {  
                return null; 
            }

            if (validator.validateEmail(input) && validator.checkEmailDuplicate(input, loginId)) {
                return input;
            }
        } while (true);
    }

    // 새 전화번호 입력 (유효성 검사 포함)
    public String inputNewUserPhone(String loginId) {
        String input;
        do {
            System.out.print("새 전화번호 (010-0000-0000) 또는 엔터 (기존 유지): ");
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {  
                return null;
            }

            if (validator.validatePhone(input) && validator.checkPhoneDuplicate(input, loginId)) {
                return input;
            }
        } while (true);
    }
}
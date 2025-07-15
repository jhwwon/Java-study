package banksystem.helper;

import java.sql.Connection;
import java.util.Scanner;

public class InputHelper {
    private Scanner scanner;
    private ValidationHelper validator;
    private Connection conn;

    public InputHelper(Scanner scanner, ValidationHelper validator, Connection conn) {
        this.scanner = scanner;
        this.validator = validator;
        this.conn = conn;
    }

    // 기본 입력 메소드 - 빈 값 입력 방지
    public String input(String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
        } while (input.isEmpty());
        return input;
    }

    // 보조 메뉴
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

    // 사용자 이름 입력 및 검증
    public String inputUserName() {
        String userName;
        do {
            userName = input("이름: ");
            if (validator.validateUserName(userName)) {
                return userName;
            }
        } while (true);
    }

    // 사용자 비밀번호 입력 및 검증
    public String inputUserPassword(String userId) {
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
            phone = input("전화번호 (010-0000-0000): ");
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

    // 계좌번호 입력 및 검증 (AccountManager 의존성 필요)
    public String inputAccountId(String prompt, boolean ownOnly, String loginId) {
        String accountId;
        do {
            accountId = input(prompt);
            // 실제 검증은 AccountManager에서 수행
            return accountId; // 단순 반환, 검증은 호출하는 곳에서 처리
        } while (true);
    }

    // 계좌 비밀번호 확인 (AccountManager 의존성 필요)
    public boolean checkPassword(String accountId) {
        String password;
        do {
            System.out.print("계좌 비밀번호 (4자리): ");
            password = scanner.nextLine();
            if (password.length() != 4 || !password.matches("\\d{4}")) {
                System.out.println("❌ 계좌 비밀번호는 4자리 숫자여야 합니다.");
                continue;
            }
            // 실제 검증은 AccountManager에서 수행
            return true; // 임시 반환, 실제 검증은 호출하는 곳에서 처리
        } while (true);
    }

    // 새 비밀번호 입력 (유효성 검사 포함)
    public String inputNewUserPassword(String loginId) {
        String input;
        do {
            System.out.print("새 비밀번호 (7~12자리, 영문+숫자) 또는 '-' (기존 유지): ");
            input = scanner.nextLine().trim();

            if ("-".equals(input)) {
                return null; // 변경하지 않음을 의미
            }

            if (validator.validateUserPassword(input, loginId)) {
                return input;
            }
            // 유효성 검사 실패시 다시 입력
        } while (true);
    }

    // 새 이메일 입력 (유효성 검사 포함)
    public String inputNewUserEmail(String loginId) {
        String input;
        do {
            System.out.print("새 이메일 또는 '-' (기존 유지): ");
            input = scanner.nextLine().trim();

            if ("-".equals(input)) {
                return null; // 변경하지 않음을 의미
            }

            if (validator.validateEmail(input) && validator.checkEmailDuplicate(input, loginId)) {
                return input;
            }
            // 유효성 검사 실패시 다시 입력
        } while (true);
    }

    // 새 전화번호 입력 (유효성 검사 포함)
    public String inputNewUserPhone(String loginId) {
        String input;
        do {
            System.out.print("새 전화번호 (010-0000-0000) 또는 '-' (기존 유지): ");
            input = scanner.nextLine().trim();

            if ("-".equals(input)) {
                return null; // 변경하지 않음을 의미
            }

            if (validator.validatePhone(input) && validator.checkPhoneDuplicate(input, loginId)) {
                return input;
            }
            // 유효성 검사 실패시 다시 입력
        } while (true);
    }

    // 페이지 번호 입력
    public int inputPageNumber(int totalPages) {
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
}
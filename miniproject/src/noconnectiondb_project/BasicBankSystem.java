package noconnectiondb_project;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.io.*;

// 계좌 정보를 저장
class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private double balance;
    private String password; // 계좌 비밀번호 추가
    
    public Account(String id, String name, double initialBalance) {
        this.id = id;
        this.name = name;
        this.balance = initialBalance;
        this.password = "12345"; // 기본 비밀번호
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public double getBalance() {return balance;}
    public void setBalance(double balance) {this.balance = balance;}
    
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
}

// 전체 시스템을 관리
public class BasicBankSystem {
    private List<Account> accounts;
    private boolean isLoggedIn;
    private String currentUser;
    private boolean isAdmin;
    
    public BasicBankSystem() {
        this.accounts = new ArrayList<>();
        this.isLoggedIn = false;
        this.currentUser = null;
        this.isAdmin = false;
    }
    
    // 새 은행 계좌를 생성하는 메소드
    public void createAccount(String name, double initialBalance) {
        if (initialBalance < 0) {
            System.out.println("초기 잔액은 0 이상이어야 합니다.");
            return;
        }
        
        String accountId = generateAccountId();
        Account account = new Account(accountId, name, initialBalance);
        accounts.add(account);
        System.out.println("계좌 생성 완료! 계좌번호: " + accountId);
    }
    
    // 모든 계좌 목록 출력
    public void displayAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("등록된 계좌가 없습니다.");
        } else {
            System.out.println("\n=== 전체 계좌 목록 ===");
            for (Account account : accounts) {
                System.out.println("계좌번호: " + account.getId() + ", 이름: " + account.getName());
            }
        }
    }
    
    // 특정 계좌 상세정보 출력
    public void displayAccountDetails(String accountId) {
        Account foundAccount = findAccountById(accountId);
        if (foundAccount != null) {
            System.out.println("\n=== 계좌 상세정보 ===");
            System.out.println("계좌번호: " + foundAccount.getId());
            System.out.println("이름: " + foundAccount.getName());
            System.out.println("잔액: " + foundAccount.getBalance() + "원");
        } else {
            System.out.println("해당 계좌를 찾을 수 없습니다.");
        }
    }
    
    private void displayAccountDetails(Scanner scanner) {
        System.out.print("계좌번호를 입력하세요: ");
        String accountId = scanner.nextLine();
        displayAccountDetails(accountId);
    }
    
    // 계좌 찾기
    private Account findAccountById(String accountId) {
        for (Account account : accounts) {
            if (account.getId().equals(accountId)) {
                return account;
            }
        }
        return null;
    }
    
    // 계좌번호 생성
    private String generateAccountId() {
        return String.format("ACC%03d", accounts.size() + 1);
    }
    
    // 입금
    public void deposit(String accountId, double amount) {
        if (amount <= 0) {
            System.out.println("입금 금액은 0보다 커야 합니다.");
            return;
        }
        
        Account account = findAccountById(accountId);
        if (account != null) {
            account.setBalance(account.getBalance() + amount);
            System.out.println("입금 완료. 현재 잔액: " + account.getBalance() + "원");
        } else {
            System.out.println("해당 계좌를 찾을 수 없습니다.");
        }
    }
    
    private void deposit(Scanner scanner) {
        try {
            System.out.print("계좌번호를 입력하세요: ");
            String accountId = scanner.nextLine();
            System.out.print("입금 금액을 입력하세요: ");
            double amount = Double.parseDouble(scanner.nextLine());
            
            deposit(accountId, amount);
        } catch (NumberFormatException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
        }
    }
    
    // 출금
    public void withdraw(String accountId, double amount) {
        if (amount <= 0) {
            System.out.println("출금 금액은 0보다 커야 합니다.");
            return;
        }
        
        Account account = findAccountById(accountId);
        if (account != null) {
            if (account.getBalance() >= amount) {
                account.setBalance(account.getBalance() - amount);
                System.out.println("출금 완료. 현재 잔액: " + account.getBalance() + "원");
            } else {
                System.out.println("잔액이 부족합니다. 현재 잔액: " + account.getBalance() + "원");
            }
        } else {
            System.out.println("해당 계좌를 찾을 수 없습니다.");
        }
    }
    
    private void withdraw(Scanner scanner) {
        try {
            System.out.print("계좌번호를 입력하세요: ");
            String accountId = scanner.nextLine();
            System.out.print("출금 금액을 입력하세요: ");
            double amount = Double.parseDouble(scanner.nextLine());
            
            withdraw(accountId, amount);
        } catch (NumberFormatException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
        }
    }
    
    // 이체
    public void transfer(String fromAccountId, String toAccountId, double amount) {
        if (amount <= 0) {
            System.out.println("이체 금액은 0보다 커야 합니다.");
            return;
        }
        
        if (fromAccountId.equals(toAccountId)) {
            System.out.println("동일한 계좌로는 이체할 수 없습니다.");
            return;
        }
        
        Account fromAccount = findAccountById(fromAccountId);
        Account toAccount = findAccountById(toAccountId);
        
        if (fromAccount == null) {
            System.out.println("출금 계좌를 찾을 수 없습니다.");
            return;
        }
        
        if (toAccount == null) {
            System.out.println("입금 계좌를 찾을 수 없습니다.");
            return;
        }
        
        if (fromAccount.getBalance() >= amount) {
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);
            System.out.println("이체 완료!");
            System.out.println("출금 계좌(" + fromAccountId + ") 잔액: " + fromAccount.getBalance() + "원");
            System.out.println("입금 계좌(" + toAccountId + ") 잔액: " + toAccount.getBalance() + "원");
        } else {
            System.out.println("출금 계좌의 잔액이 부족합니다. 현재 잔액: " + fromAccount.getBalance() + "원");
        }
    }
    
    private void transfer(Scanner scanner) {
        try {
            System.out.print("출금 계좌번호를 입력하세요: ");
            String fromAccountId = scanner.nextLine();
            System.out.print("입금 계좌번호를 입력하세요: ");
            String toAccountId = scanner.nextLine();
            System.out.print("이체 금액을 입력하세요: ");
            double amount = Double.parseDouble(scanner.nextLine());
            
            transfer(fromAccountId, toAccountId, amount);
        } catch (NumberFormatException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
        }
    }
    
    // 계좌 삭제
    public void deleteAccount(String accountId) {
        if (!isAdmin) {
            System.out.println("관리자만 계좌를 삭제할 수 있습니다.");
            return;
        }
        
        Account account = findAccountById(accountId);
        if (account != null) {
            if (account.getBalance() > 0) {
                System.out.println("잔액이 있는 계좌는 삭제할 수 없습니다. 현재 잔액: " + account.getBalance() + "원");
                return;
            }
            accounts.remove(account);
            System.out.println("계좌 삭제 완료");
        } else {
            System.out.println("해당 계좌를 찾을 수 없습니다.");
        }
    }
    
    private void deleteAccount(Scanner scanner) {
        System.out.print("삭제할 계좌번호를 입력하세요: ");
        String accountId = scanner.nextLine();
        deleteAccount(accountId);
    }
    
    // 로그인
    public boolean login(String username, String password) {
        // 관리자 로그인
        if (username.equals("admin") && password.equals("password")) {
            isLoggedIn = true;
            currentUser = "admin";
            isAdmin = true;
            return true;
        }
        
        // 일반 사용자 로그인 (계좌 소유자 이름으로 로그인)
        for (Account account : accounts) {
            if (account.getName().equals(username) && account.getPassword().equals(password)) {
                isLoggedIn = true;
                currentUser = username;
                isAdmin = false;
                return true;
            }
        }
        
        return false;
    }
    
    // 로그아웃
    public void logout() {
        isLoggedIn = false;
        currentUser = null;
        isAdmin = false;
        System.out.println("로그아웃되었습니다.");
    }
    
    // 권한 확인
    public boolean isAdminLoggedIn() {
        return isLoggedIn && isAdmin;
    }
    
    public boolean isUserLoggedIn() {
        return isLoggedIn && !isAdmin;
    }
    
    // 메인 메뉴
    public void runMenu() {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            displayMainMenu();
            
            try {
                System.out.print("선택하세요: ");
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1:
                        loginAdmin(scanner);
                        break;
                    case 2:
                        loginUser(scanner);
                        break;
                    case 3:
                        if (isLoggedIn) {
                            createAccount(scanner);
                        } else {
                            System.out.println("로그인이 필요합니다.");
                        }
                        break;
                    case 4:
                        if (isLoggedIn) {
                            displayAllAccounts();
                        } else {
                            System.out.println("로그인이 필요합니다.");
                        }
                        break;
                    case 5:
                        if (isLoggedIn) {
                            displayAccountDetails(scanner);
                        } else {
                            System.out.println("로그인이 필요합니다.");
                        }
                        break;
                    case 6:
                        if (isLoggedIn) {
                            deposit(scanner);
                        } else {
                            System.out.println("로그인이 필요합니다.");
                        }
                        break;
                    case 7:
                        if (isLoggedIn) {
                            withdraw(scanner);
                        } else {
                            System.out.println("로그인이 필요합니다.");
                        }
                        break;
                    case 8:
                        if (isLoggedIn) {
                            transfer(scanner);
                        } else {
                            System.out.println("로그인이 필요합니다.");
                        }
                        break;
                    case 9:
                        if (isLoggedIn) {
                            deleteAccount(scanner);
                        } else {
                            System.out.println("로그인이 필요합니다.");
                        }
                        break;
                    case 10:
                        if (isLoggedIn) {
                            logout();
                        } else {
                            System.out.println("로그인 상태가 아닙니다.");
                        }
                        break;
                    case 11:
                        saveDataToFile();
                        System.out.println("프로그램을 종료합니다.");
                        scanner.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
                }
            } catch (NumberFormatException e) {
                System.out.println("올바른 숫자를 입력해주세요.");
            }
        }
    }
    
    private void displayMainMenu() {
        System.out.println("\n==============================");
        System.out.println("     은행 계좌 관리 시스템");
        System.out.println("==============================");
        
        if (isLoggedIn) {
            System.out.println("현재 사용자: " + currentUser + (isAdmin ? " (관리자)" : " (일반사용자)"));
        } else {
            System.out.println("로그인 상태: 로그아웃");
        }
        
        System.out.println("------------------------------");
        System.out.println("1. 관리자 로그인");
        System.out.println("2. 일반 사용자 로그인");
        System.out.println("3. 계좌 생성");
        System.out.println("4. 계좌 목록 보기");
        System.out.println("5. 계좌 상세정보 보기");
        System.out.println("6. 입금");
        System.out.println("7. 출금");
        System.out.println("8. 이체");
        System.out.println("9. 계좌 삭제");
        System.out.println("10. 로그아웃");
        System.out.println("11. 프로그램 종료");
        System.out.println("------------------------------");
    }
    
    private void loginAdmin(Scanner scanner) {
        if (isLoggedIn) {
            System.out.println("이미 로그인 상태입니다.");
            return;
        }
        
        System.out.print("관리자 ID를 입력하세요: ");
        String username = scanner.nextLine();
        System.out.print("비밀번호를 입력하세요: ");
        String password = scanner.nextLine();
        
        if (login(username, password)) {
            System.out.println("관리자 로그인 성공!");
        } else {
            System.out.println("로그인 실패. ID 또는 비밀번호를 확인해주세요.");
        }
    }
    
    private void loginUser(Scanner scanner) {
        if (isLoggedIn) {
            System.out.println("이미 로그인 상태입니다.");
            return;
        }
        
        System.out.print("사용자 이름을 입력하세요: ");
        String username = scanner.nextLine();
        System.out.print("비밀번호를 입력하세요: ");
        String password = scanner.nextLine();
        
        if (login(username, password)) {
            System.out.println("사용자 로그인 성공!");
        } else {
            System.out.println("로그인 실패. 이름 또는 비밀번호를 확인해주세요.");
        }
    }
    
    private void createAccount(Scanner scanner) {
        try {
            System.out.print("계좌주 이름을 입력하세요: ");
            String name = scanner.nextLine();
            
            if (name.trim().isEmpty()) {
                System.out.println("계좌주 이름은 비어있을 수 없습니다.");
                return;
            }
            
            System.out.print("초기 잔액을 입력하세요: ");
            double initialBalance = Double.parseDouble(scanner.nextLine());
            
            createAccount(name, initialBalance);
        } catch (NumberFormatException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
        }
    }
    
    // 데이터 저장
    public void saveDataToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("bank_data.dat"))) {
            oos.writeObject(accounts);
            System.out.println("데이터 저장 완료!");
        } catch (IOException e) {
            System.out.println("데이터 저장 중 오류 발생: " + e.getMessage());
        }
    }
    
    // 데이터 불러오기
    @SuppressWarnings("unchecked")
    public void loadDataFromFile() {
        File file = new File("bank_data.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                accounts = (List<Account>) ois.readObject();
                System.out.println("저장된 데이터를 불러왔습니다.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("데이터 불러오기 중 오류 발생: " + e.getMessage());
                System.out.println("새로운 데이터로 시작합니다.");
            }
        } else {
            System.out.println("저장된 데이터 파일이 없습니다. 새로운 데이터로 시작합니다.");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("은행 시스템을 시작합니다...");
        BasicBankSystem bankSystem = new BasicBankSystem();
        bankSystem.loadDataFromFile();
        
        // 시스템 종료 시 자동 저장을 위한 셧다운 훅
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n시스템을 종료합니다. 데이터를 저장중...");
            bankSystem.saveDataToFile();
        }));
        
        bankSystem.runMenu();
    }
}
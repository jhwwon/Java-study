package miniproject.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;

//계좌 정보를 저장
class Account implements Serializable {
	private String id;
	private String name;
	private double balance;
	
	public Account(String id, String name, double initialBalance) {
		this.id = id;
		this.name = name;
		this.balance = initialBalance;
	}

	public String getId() {return id;}
	public void setId(String id) {this.id = id;}

	public String getName() {return name;}
	public void setName(String name) {this.name = name;}

	public double getBalance() {return balance;}
	public void setBalance(double balance) {this.balance = balance;}
}

//전체 시스템을 관리
public class BankSystem {  //은행 시스템이 처음 시작될 때는 항상 같은 상태를 유지해야 하기 때문에 매개변수가 없는 기본 생성자를 생성
	//매개변수가 있으면 매번 시작될 때마다 값이 달라짐 
	private List<Account> accounts; //모든 계좌 정보를 저장하는 ArrayList(Account 객체만 저장가능)
	private boolean isLoggedIn;  //현재 로그인 상태를 나타내는 boolean 변수
	private String currentUser;  //현재 로그인한 사용자정보
	
	public BankSystem() {   //매개변수가 없는 기본 생성자
		this.accounts = new ArrayList<>();  //빈 계좌 리스트로 초기화
		this.isLoggedIn = false;  //로그아웃 상태로 초기화
		this.currentUser = null;  //현재 사용자 없음으로 초기화
	}
	
	//새 은행 계좌를 생성하는 메소드(큰 요청)
	//계좌 소유자의 이름과 계좌 개설 시 입금할 초기 금액을 받는 매개변수
	public void createAccount(String name, double initialBalance) {
		String accountId = generateAccountId();
		Account account = new Account(accountId, name, initialBalance);
		accounts.add(account);
		System.out.println("계좌 생성 완료! 계좌번호: " + accountId);
	}
	
	//은행 시스템에 등록된 모든 계좌의 기본 정보를 화면에 출력하는 메소드
	public void displayAllAccounts() {
		if(accounts.isEmpty()) {
			System.out.println("등록된 계좌가 없습니다.");
		} else {
			for(Account account : accounts) {
				System.out.println("계좌번호: " +account.getId() + ", 이름: " + account.getName());
			}
		}
	}
	
	//특정 계좌번호를 입력받아 해당 계좌의 상세 정보를 화면에 출력하는 메소드
	public void displayAccountDetails(String accountId) {
		Account foundAccount = findAccountById(accountId);
		if(foundAccount != null) {
			System.out.println("계좌번호: " + foundAccount.getId());
			System.out.println("이름: " + foundAccount.getName());
			System.out.println("잔액: " + foundAccount.getBalance() + "원");
		}
	}
	
	private void displayAccountDetails(Scanner scanner) {
		System.out.println("계좌번호: ");
		String accountId = scanner.nextLine();
		
		displayAccountDetails(accountId);
	}
	
	//계좌번호를 받아서 해당하는 계좌 객체를 찾아 변환하는 멕메소드
	private Account findAccountById(String accountId) {
		for(Account account : accounts) {
			if(account.getId().equals(accountId)) {
				return account;
			}
		}
		return null;
	}
	
	//계좌번호만 생성하는 메소드(작은 요청 createAccount() 메소드와 비교했을 때)
	private String generateAccountId() {
		return String.valueOf(accounts.size() + 1);
	}
	
	public void deposit(String accountId, double amount) {
		Account account = findAccountById(accountId);
		if(account != null) {
			account.setBalance(account.getBalance() + amount);
			System.out.println("입금 완료. 현재 잔액: " + account.getBalance() + "원");
		} else {
			System.out.println("해당 계좌를 찾을 수 없습니다.");
		}
	}
	
	private void deposit(Scanner scanner) {
		System.out.println("계좌번호: ");
		String accountId = scanner.next();
		System.out.println("입금 금액: ");
		double amount = scanner.nextDouble();
		scanner.nextLine();
		
		deposit(accountId, amount);
	}
	
	public void withdraw(String accountId, double amount) {
		Account account = findAccountById(accountId);
		if(account != null && account.getBalance() >= amount) {
			account.setBalance(account.getBalance() - amount);
			System.out.println("출금 완료. 현재 잔액: " + account.getBalance() + "원");
		} else if(account == null) {
			System.out.println("해당 계좌를 찾을 수 없습니다.");
		} else {
			System.out.println("잔액이 부족합니다.");
		}
	}
	
	private void withdraw(Scanner scanner) {
		System.out.println("계좌번호: ");
		String accountId = scanner.next();
		System.out.println("출금 금액: ");
		double amount = scanner.nextDouble();
		scanner.nextLine();
		
		withdraw(accountId, amount);
	}
	
	public void transfer(String fromAccountId, String toAccountId, double amount) {
		Account fromAccount = findAccountById(fromAccountId);
		Account toAccount = findAccountById(toAccountId);
		
		if(fromAccount != null && toAccount != null && fromAccount.getBalance() >= amount) {
			fromAccount.setBalance(fromAccount.getBalance() - amount);
			toAccount.setBalance(toAccount.getBalance() + amount);
			System.out.println("이제 완료. 출금 계좌 잔액: " + fromAccount.getBalance() + "원");
			System.out.println("입금 계좌 잔액: " + toAccount.getBalance() + "원");
		} else if(fromAccount == null || toAccount == null) {
			System.out.println("계좌 정보를 확인해주세요.");
		} else {
			System.out.println("출금 계좌의 잔액이 부족합니다.");
		}
	}
	
	private void transfer(Scanner scanner) {
		System.out.println("출금 계좌번호: ");
		String fromAccountId = scanner.next();
		System.out.println("입금 계좌번호: ");
		String toAccountId = scanner.next();
		System.out.println("이체 금액: ");
		double amount = scanner.nextDouble();
		scanner.nextLine();
		
		transfer(fromAccountId, toAccountId, amount);
	}
	
	public void deleteAccount(String accountId) {
		Account account = findAccountById(accountId);
		if(account != null) {
			accounts.remove(account);
			System.out.println("계좌 삭제 완료");
		} else {
			System.out.println("해당 계좌를 찾을 수 없습니다.");
		}
	}
	
	private void deleteAccount(Scanner scanner) {
		System.out.println("삭제할 계좌번회: ");
		String accountId = scanner.next();
		
		deleteAccount(accountId);
	}
	
	public boolean login(String username, String password) {
		if((username.equals("admin") && password.equals("password")) || 
				(username.equals(currentUser) && password.equals("12345"))) {
			isLoggedIn = true;
			return true;
		}
		return false;
	}
	
	public void logout() {
		isLoggedIn = false;
		currentUser = null;
	}
	
	public boolean isAdminLoggedIn() {
		return isLoggedIn && currentUser.equals("admin");
	}
	
	public boolean isUserLoggedIn() {
		return isLoggedIn && !currentUser.equals("admin");
	}
	
	public void runMenu() {
		Scanner scanner = new Scanner(System.in);
		while(true) {
			System.out.println("\n은행 계좌 관리 시스템");
			System.out.println("-----------------");
			System.out.println("1. 로그인 (관리자)");
			System.out.println("2. 로그인 (일반 사용자)");
			System.out.println("3. 계좌 생성");
			System.out.println("4. 계좌 목록 보기");
			System.out.println("5. 계좌 상세정보 보기");
			System.out.println("6. 입금");
			System.out.println("7. 출금");
			System.out.println("8. 이체");
			System.out.println("9. 계좌 삭제");
			System.out.println("10. 프로그램 종료");
			
			System.out.print("선택: ");
			int choice = scanner.nextInt();
			scanner.nextLine();
			
			switch(choice) {
			case 1:
				loginAdmin(scanner);
				break;
			case 2:
				loginUser(scanner);
				break;
			case 3:
				createAccount(scanner);
				break;
			case 4:
				displayAllAccounts();
				break;
			case 5:
				displayAccountDetails(scanner);
				break;
			case 6:
				deposit(scanner);
				break;
			case 7:
				withdraw(scanner);
				break;
			case 8:
				transfer(scanner);
				break;
			case 9:
				deleteAccount(scanner);
				break;
			case 10:
				System.exit(0);
				break;
			default:
				System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
			}
		}
	}
	
	private void loginAdmin(Scanner scanner) {
		System.out.println("관리자 ID: ");
		String username = scanner.nextLine();
		System.out.println("비밀번호: ");
		String password = scanner.nextLine();
		
		if(login(username, password)) {
			currentUser = "admin";
			System.out.println("관리자 로그인 성공");
		} else {
			System.out.println("로그인 실패");
		}
	}
	
	private void loginUser(Scanner scanner) {
		System.out.println("사용자 이름: ");
		String username = scanner.nextLine();
		System.out.println("비밀번호: ");
		String password = scanner.nextLine();
		
		if(login(username, password)) {
			currentUser = username;
			System.out.println("사용자 로그인 성공");
		} else {
			System.out.println("로그인 실패");
		}
	}
	
	public void createAccount(Scanner scanner) {
		System.out.println("계좌주 이름: ");
		String name = scanner.nextLine();
		System.out.println("초기 잔액: ");
		double initialBalance = scanner.nextDouble();
		scanner.nextLine();
		
		createAccount(name, initialBalance);
	}
	
	public void saveDataToFile() {
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("bank_data.dat"))) {
			oos.writeObject(accounts);
			System.out.println("데이터 저장 완료");
		} catch(IOException e) {
			System.out.println("데이터 저장 중 오류 발생: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadDataFromFile() {
		File file = new File("bank_data.dat");
		if(file.exists()) {
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				accounts = (List<Account>) ois.readObject();
				System.out.println("데이터 불러오기 완료");
			} catch(IOException | ClassNotFoundException e) {
				System.out.println("데이터 불러오기 중 오류 발생: " + e.getMessage());
			}
		} else {
			System.out.println("저장된 데이터 파일이 없습니다.");
		}
	}
	
	public static void main(String[] args) {
		BankSystem bankSystem = new BankSystem();
		bankSystem.loadDataFromFile();  //실행하기 전에 저장했던 파일을 가져오기
		bankSystem.runMenu();
	}
}

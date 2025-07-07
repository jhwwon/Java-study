package project;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.InputMismatchException;  // 잘못된 타입의 데이터를 입력했을 경우 발행하는 예외를 처리
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

// 사용자 정보 저장
@Getter 
@Setter 
@AllArgsConstructor
class User implements Serializable {
	private String id;
	private String name;
	private String password;
}

// 수입 정보 저장
@Getter 
@Setter 
@AllArgsConstructor
class Income implements Serializable {
	private int amount;
	private String userId;
	private String date;
	private String category;
}

// 지출 정보 저장
@Getter 
@Setter 
@AllArgsConstructor
class Expense implements Serializable {
	private int amount;
	private String userId;
	private String date;
	private String category;
}

// 예산 정보 저장
@Getter 
@Setter 
@AllArgsConstructor
class Budget implements Serializable {
	private String userId;
	private int budgetAmount;
}

// 카테고리 정보 저장
@Getter
@Setter 
@AllArgsConstructor
class Category implements Serializable {
	private String categoryName;
	private String userId;
}

public class PersonalFinanceManager {
	private List<User> users;
	private List<Income> incomes;
	private List<Expense> expenses;
	private List<Budget> budgets;
	private List<Category> categories;
	private String currentUser;
	
	public PersonalFinanceManager() {
		this.users = new ArrayList<>();
		this.incomes = new ArrayList<>();
		this.expenses = new ArrayList<>();
		this.budgets = new ArrayList<>();
		this.categories = new ArrayList<>();
		this.currentUser = null;
	}
	
	// 안전한 정수 입력
	private int getInt(Scanner scanner, String prompt) {
		while(true) {
			try {
				System.out.print(prompt);
				int value = scanner.nextInt();
				scanner.nextLine();
				return value;
			} catch (InputMismatchException e) {
				System.out.println("숫자만 입력해주세요.");
				scanner.nextLine();
			}
		}
	}
	
	// 양수 입력
	private int getPositiveInt(Scanner scanner, String prompt) {
		while(true) {
			int value = getInt(scanner, prompt);
			if(value > 0) return value;
			System.out.println("1 이상의 양수를 입력해주세요.");
		}
	}
	
	// 빈 문자열 방지 입력
	private String getString(Scanner scanner, String prompt) {
		while(true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();  // 메소드 체이닝: 여러 메소드 호출을 하나의 명령문으로 
			if(!input.isEmpty()) return input;
			System.out.println("빈 값은 입력할 수 없습니다.");
		}
	}
	
	// 날짜 형식 검증
	private String getDate(Scanner scanner, String prompt) {
		while(true) {
			String date = getString(scanner, prompt);
			if(date.matches("\\d{4}-\\d{2}-\\d{2}")) return date;
			System.out.println("날짜 형식: 2025-07-07");
		}
	}
	
	// 금액 포맷팅
	private String formatMoney(int amount) {
		if(amount < 0) {
			return "-" + formatMoney(-amount);
		}
		
		String amountStr = String.valueOf(amount);
		StringBuilder formatted = new StringBuilder();
		
		int len = amountStr.length();
		for(int i = 0; i < len; i++) {
			if(i > 0 && (len - i) % 3 == 0) {
				formatted.append(",");
			}
			formatted.append(amountStr.charAt(i));
		}
		return formatted.toString() + "원";
	}
	
	// 로그인 확인
	private boolean checkLogin() {
		if(currentUser == null) {
			System.out.println("로그인 후 사용가능합니다.");
			return false;
		}
		return true;
	}
	
	// Helper 메소드
	// 특정 사용자의 전체 수입 합계 계산 
	private int getTotalIncomeForUser(String userId) {
	 	int total = 0;
		for(Income income : incomes) {
			if(income.getUserId().equals(userId)) {
				total += income.getAmount();
			}
		}
		return total;
	}
	
	// 특정 사용자의 전체 지출 합계 계산
	private int getTotalExpenseForUser(String userId) {
		int total = 0;
		for(Expense expense : expenses) {
			if(expense.getUserId().equals(userId)) {
				total += expense.getAmount();
			}
		}
		return total;
	}
	
	// 특정 사용자의 특정 기간 동안의 수입 합계 계산
	private int getIncomeForPeriod(String userId, String period) {
		int total = 0;
		for(Income income : incomes) {
			if(income.getUserId().equals(userId) && income.getDate().startsWith(period)) {
				total += income.getAmount();
			}
		}
		return total;
	}
	
	// 특정 사용자의 특정 기간 동안의 지출 합계 계산
	private int getExpenseForPeriod(String userId, String period) {
		int total = 0;
		for(Expense expense : expenses) {
			if(expense.getUserId().equals(userId) && expense.getDate().startsWith(period)) {
				total += expense.getAmount();
			}
		}
		return total;
	}
	
	// 특정 사용자의 특정 카테고리별 수입 합계 계산
	private int getIncomeForCategory(String userId, String category) {
		int total = 0;
		for(Income income : incomes) {
			if(income.getUserId().equals(userId) && income.getCategory().equals(category)) {
				total += income.getAmount();
			}
		}
		return total;
	}
	
	// 특정 사용자의 특정 카테고리별 지출 합계 계산
	private int getExpenseForCategory(String userId, String category) {
		int total = 0;
		for(Expense expense : expenses) {
			if(expense.getUserId().equals(userId) && expense.getCategory().equals(category)) {
				total += expense.getAmount();
			}
		}
		return total;
	}
	
	// 1. 사용자 관리 기능 구현
	// 사용자 등록
	public void registerUser(String id, String name, String password) {
		if(findUserById(id) != null) {
			System.out.println("이미 존재하는 ID입니다.");
			return;
		}
		
		users.add(new User(id, name, password));
		System.out.println("회원가입 완료!");
	}
	
	// 사용자 삭제
	public void removeUser(String id) {
		User user = findUserById(id);
		if(user != null) {
			users.remove(user);
			System.out.println("사용자 삭제 완료!");
		} else {
			System.out.println("사용자를 찾을 수 없습니다.");
		}
	}
	
	// 사용자 목록
	public void displayAllUsers() {
		if(users.isEmpty()) {
			System.out.println("등록된 사용자가 없습니다.");
			return;
		}
		
		System.out.println("\n사용자 목록");
		for(User user : users) {
			System.out.println("ID: " + user.getId() + " | 이름: " + user.getName());
		}
	}
	
	// 2. 수입 관리 기능 구현
	// 수입 추가
	public void addIncome(int amount, String date, String category) {
		if(!checkLogin()) return;
		
		incomes.add(new Income(amount, currentUser, date, category));
		
		// 카테고리 자동 추가
		if(findCategoryByDetails(category, currentUser) == null) {
			categories.add(new Category(category, currentUser));
		}
		
		System.out.println("수입 추가 완료!");
	}
	
	// 수입 삭제
	public void removeIncome(String date, int amount) {
		if(!checkLogin()) return;
		
		Income income = findIncomeByDetails(date, amount);
		if(income != null) {
			incomes.remove(income);
			System.out.println("수입 삭제 완료!");
		} else {
			System.out.println("해당 수입을 찾을 수 없습니다.");
		}
	}
	
	// 수입 조회
	public void displayMyIncomes() {
		if(!checkLogin()) return;
		
		System.out.println("\n수입 내역");
		
		List<Income> userIncomes = new ArrayList<>();
		for(Income income : incomes) {
			if(income.getUserId().equals(currentUser)) {
				userIncomes.add(income);
			}
		}
		
		if(userIncomes.isEmpty()) {
			System.out.println("등록된 수입이 없습니다.");
		} else {
			for(Income income : userIncomes) {
				System.out.println(formatMoney(income.getAmount()) + " | " + income.getDate() + " | " + income.getCategory());
			}
		}
	}
	
	// 3. 지출 관리 기능 구현
	// 지출 추가
	public void addExpense(int amount, String date, String category) {
		if(!checkLogin()) return;
		
		expenses.add(new Expense(amount, currentUser, date, category));
		
		// 카테고리 자동 추가
		if(findCategoryByDetails(category, currentUser) == null) {
			categories.add(new Category(category, currentUser));
		}
		
		System.out.println("지출 추가 완료!");
	}
	
	// 지출 삭제
	public void removeExpense(String date, int amount) {
		if(!checkLogin()) return;
		
		Expense expense = findExpenseByDetails(date, amount);
		if(expense != null) {
			expenses.remove(expense);
			System.out.println("지출 삭제 완료!");
		} else {
			System.out.println("해당 지출을 찾을 수 없습니다.");
		}
	}
	
	// 지출 조회
	public void displayMyExpenses() {
		if(!checkLogin()) return;
		
		System.out.println("\n지출 내역");
		
		List<Expense> userExpenses = new ArrayList<>();
		for(Expense expense : expenses) {
			if(expense.getUserId().equals(currentUser)) {
				userExpenses.add(expense);
			}
		}
		
		if(userExpenses.isEmpty()) {
			System.out.println("등록된 지출이 없습니다.");
		} else {
			for(Expense expense : userExpenses) {
				System.out.println(formatMoney(expense.getAmount()) + " | " + expense.getDate() + " | " + expense.getCategory());
			}
		}
	}
	
	// 4. 예산 관리 기능 구현
	// 예산 설정
	public void setBudget(int budgetAmount) {
		if(!checkLogin()) return;
		
		Budget existingBudget = findBudgetByUserId(currentUser);
		if(existingBudget != null) {
			existingBudget.setBudgetAmount(budgetAmount);
		} else {
			budgets.add(new Budget(currentUser, budgetAmount));
		}
		System.out.println("예산 설정 완료!");
	}
	
	// 예산 조회 + 예산 초과 알림 
	public void displayMyBudget() {
		if(!checkLogin()) return;
		
		Budget budget = findBudgetByUserId(currentUser);
		if(budget == null) {
			System.out.println("설정된 예산이 없습니다.");
			return;
		}
		
		int totalExpense = getTotalExpenseForUser(currentUser);
		int remaining = budget.getBudgetAmount() - totalExpense;
		int usagePercent = (totalExpense * 100) / budget.getBudgetAmount();
		
		System.out.println("\n예산 현황");
		System.out.println("설정 예산: " + formatMoney(budget.getBudgetAmount()));
		System.out.println("사용 금액: " + formatMoney(totalExpense));
		System.out.println("남은 예산: " + formatMoney(remaining));
		System.out.println("예산 사용률: " + usagePercent + "%");
		
		System.out.println("\n예산 알림 상태");
		if(usagePercent >= 100) {
			System.out.println("예산 초과!");
			System.out.println("초과금액: " + formatMoney(totalExpense - budget.getBudgetAmount()));
			System.out.println("지출을 줄이시기 바랍니다.");
		} else if(usagePercent >= 80) {
			System.out.println("예산 경고! (80% 이상 사용)");
			System.out.println("남은 예산: " + formatMoney(remaining));
			System.out.println("지출 관리에 주의하세요.");
		} else if(usagePercent >= 50) {
			System.out.println("예산 절반 사용");
			System.out.println("남은 예산: " + formatMoney(remaining));
			System.out.println("적절한 수준입니다.");
		} else {
			System.out.println("예산 안전 (50% 미만 사용)");
			System.out.println("남은 예산: " + formatMoney(remaining));
			System.out.println("훌륭한 관리입니다!");
		}
	}
	
	// 5. 카테고리 관리 기능 구현
	// 카테고리 추가
	public void addCategory(String categoryName) {
		if(!checkLogin()) return;
		
		if(findCategoryByDetails(categoryName, currentUser) != null) {
			System.out.println("이미 존재하는 카테고리입니다.");
			return;
		}
		
		categories.add(new Category(categoryName, currentUser));
		System.out.println("카테고리 추가 완료!");
	}
	
	// 카테고리 삭제
	public void removeCategory(String categoryName) {
		if(!checkLogin()) return;
		
		Category category = findCategoryByDetails(categoryName, currentUser);
		if(category != null) {
			categories.remove(category);
			System.out.println("카테고리 삭제 완료!");
		} else {
			System.out.println("해당 카테고리를 찾을 수 없습니다.");
		}
	}
	
	// 카테고리 조회
	public void displayCategories() {
		if(!checkLogin()) return;
		
		System.out.println("\n내 카테고리 목록");
		
		List<Category> userCategories = new ArrayList<>();
		for(Category category : categories) {
			if(category.getUserId().equals(currentUser)) {
				userCategories.add(category);
			}
		}
		
		if(userCategories.isEmpty()) {
			System.out.println("등록된 카테고리가 없습니다.");
		} else {
			for(Category category : userCategories) {
				System.out.println("- " + category.getCategoryName());
			}
		}
	}
	
	// 6. 검색 기능 구현
	// 날짜 검색
	public void searchByDate(String searchDate) {
		if(!checkLogin()) return;
		
		System.out.println("\n" + searchDate + " 내역");
		int totalIncome = 0, totalExpense = 0;
		boolean found = false;
		
		for(Income income : incomes) {
			if(income.getUserId().equals(currentUser) && income.getDate().equals(searchDate)) {
				System.out.println("수입: " + formatMoney(income.getAmount()) + " [" + income.getCategory() + "]");
				totalIncome += income.getAmount();
				found = true;
			}
		}
		
		for(Expense expense : expenses) {
			if(expense.getUserId().equals(currentUser) && expense.getDate().equals(searchDate)) {
				System.out.println("지출: " + formatMoney(expense.getAmount()) + " [" + expense.getCategory() + "]");
				totalExpense += expense.getAmount();
				found = true;
			}
		}
		
		if(!found) {
			System.out.println("해당 날짜의 내역이 없습니다.");
		} else {
			System.out.println("총 수입: " + formatMoney(totalIncome) + ", 총 지출: " + formatMoney(totalExpense));
		}
	}
	
	// 카테고리 검색
	public void searchByCategory(String searchCategory) {
		if(!checkLogin()) return;
		
		System.out.println("\n[" + searchCategory + "] 카테고리 내역");
		int totalIncome = 0, totalExpense = 0;
		boolean found = false;
		
		for(Income income : incomes) {
			if(income.getUserId().equals(currentUser) && income.getCategory().equals(searchCategory)) {
				System.out.println("수입: " + formatMoney(income.getAmount()) + " [" + income.getDate() + "]");
				totalIncome += income.getAmount();
				found = true;
			}
		}
		
		for(Expense expense : expenses) {
			if(expense.getUserId().equals(currentUser) && expense.getCategory().equals(searchCategory)) {
				System.out.println("지출: " + formatMoney(expense.getAmount()) + " [" + expense.getDate() + "]");
				totalExpense += expense.getAmount();
				found = true;
			}
		}
		
		if(!found) {
			System.out.println("해당 카테고리의 내역이 없습니다.");
		} else {
			System.out.println("총 수입: " + formatMoney(totalIncome) + ", 총 지출: " + formatMoney(totalExpense));
		}
	}
	
	// 금액 범위 검색
	public void searchByAmount(int minAmount, int maxAmount) {
		if(!checkLogin()) return;
		
		if(minAmount > maxAmount) {
			System.out.println("최소 금액이 최대 금액보다 클 수 없습니다.");
			return;
		}
		
		System.out.println("\n" + formatMoney(minAmount) + " ~ " + formatMoney(maxAmount) + " 범위 내역");
		int totalIncome = 0, totalExpense = 0;
		boolean found = false;
		
		for(Income income : incomes) {
			if(income.getUserId().equals(currentUser) && 
			   income.getAmount() >= minAmount && income.getAmount() <= maxAmount) {
				System.out.println("수입: " + formatMoney(income.getAmount()) + " [" + income.getDate() + " | " + income.getCategory() + "]");
				totalIncome += income.getAmount();
				found = true;
			}
		}
		
		for(Expense expense : expenses) {
			if(expense.getUserId().equals(currentUser) && 
			   expense.getAmount() >= minAmount && expense.getAmount() <= maxAmount) {
				System.out.println("지출: " + formatMoney(expense.getAmount()) + " [" + expense.getDate() + " | " + expense.getCategory() + "]");
				totalExpense += expense.getAmount();
				found = true;
			}
		}
		
		if(!found) {
			System.out.println("해당 금액 범위의 내역이 없습니다.");
		} else {
			System.out.println("총 수입: " + formatMoney(totalIncome) + ", 총 지출: " + formatMoney(totalExpense));
		}
	}
	
	// 7. 통계 기능 구현
	// 총 통계 
	public void calculateTotal() {
		if(!checkLogin()) return;
		
		int totalIncome = getTotalIncomeForUser(currentUser);
		int totalExpense = getTotalExpenseForUser(currentUser);
		
		System.out.println("\n총 통계");
		System.out.println("총 수입: " + formatMoney(totalIncome));
		System.out.println("총 지출: " + formatMoney(totalExpense));
		System.out.println("잔액: " + formatMoney(totalIncome - totalExpense));
	}
	
	// 월별 통계
	public void getMonthlyStatistics(String yearMonth) {
		if(!checkLogin()) return;
		
		if(!yearMonth.matches("\\d{4}-\\d{2}")) {
			System.out.println("년월 형식: 2025-07");
			return;
		}
		
		int monthlyIncome = getIncomeForPeriod(currentUser, yearMonth);
		int monthlyExpense = getExpenseForPeriod(currentUser, yearMonth);
		
		System.out.println("\n" + yearMonth + " 월별 통계");
		System.out.println("월 수입: " + formatMoney(monthlyIncome));
		System.out.println("월 지출: " + formatMoney(monthlyExpense));
		System.out.println("월 잔액: " + formatMoney(monthlyIncome - monthlyExpense));
	}
	
	// 카테고리별 통계 
	public void getCategoryStatistics() {
		if(!checkLogin()) return;
		
		System.out.println("\n카테고리별 통계");
		boolean found = false;
		
		for(Category category : categories) {
			if(category.getUserId().equals(currentUser)) {
				int categoryIncomeTotal = getIncomeForCategory(currentUser, category.getCategoryName());
				int categoryExpenseTotal = getExpenseForCategory(currentUser, category.getCategoryName());
				
				// 수입이나 지출이 있는 카테고리만 표시
				if(categoryIncomeTotal > 0 || categoryExpenseTotal > 0) {
					System.out.println("\n[" + category.getCategoryName() + "]");
					if(categoryIncomeTotal > 0) {
						System.out.println("  수입: " + formatMoney(categoryIncomeTotal));
					}
					if(categoryExpenseTotal > 0) {
						System.out.println("  지출: " + formatMoney(categoryExpenseTotal));
					}
					int netAmount = categoryIncomeTotal - categoryExpenseTotal;
					System.out.println("  순액: " + formatMoney(netAmount));
					found = true;
				}
			}
		}
		
		if(!found) System.out.println("카테고리별 내역이 없습니다.");
	}
	
	// 로그인
	public boolean login(String id, String password) {
		User user = findUserById(id);
		if(user != null && user.getPassword().equals(password)) {
			currentUser = id;
			return true;
		}
		return false;
	}
	
	// 로그아웃
	public void logout() {
		currentUser = null;
	}
	
	// 찾기 메소드들
	// ID로 사용자 검색
	private User findUserById(String id) {
		for(User user : users) {
			if(user.getId().equals(id)) return user;
		}
		return null;
	}
	
	// 날짜와 금액으로 특정 수입 검색
	private Income findIncomeByDetails(String date, int amount) {
		for(Income income : incomes) {
			if(income.getUserId().equals(currentUser) && income.getDate().equals(date) && income.getAmount() == amount) {
				return income;
			}
		}
		return null;
	}
	
	// 날짜와 금액으로 특정 지출 검색
	private Expense findExpenseByDetails(String date, int amount) {
		for(Expense expense : expenses) {
			if(expense.getUserId().equals(currentUser) && expense.getDate().equals(date) && expense.getAmount() == amount) {
				return expense;
			}
		}
		return null;
	}
	
	// 사용자 ID로 예산정보 검색
	private Budget findBudgetByUserId(String userId) {
		for(Budget budget : budgets) {
			if(budget.getUserId().equals(userId)) return budget;
		}
		return null;
	}
	
	// 사용자의 특정 카테고리명에 해당하는 카테고리를 검색
	private Category findCategoryByDetails(String categoryName, String userId) {
		for(Category category : categories) {
			if(category.getCategoryName().equals(categoryName) && category.getUserId().equals(userId)) {
				return category;
			}
		}
		return null;
	}
	
	// 데이터 저장/불러오기
	public void saveDataToFile() {
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("finance_data.dat"))) {
			oos.writeObject(users);
			oos.writeObject(incomes);
			oos.writeObject(expenses);
			oos.writeObject(budgets);
			oos.writeObject(categories);
			System.out.println("데이터 저장 완료");
		} catch (IOException e) {
			System.out.println("데이터 저장 오류: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadDataFromFile() {
		File file = new File("finance_data.dat");
		if(file.exists()) {
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				users = (List<User>) ois.readObject();
				incomes = (List<Income>) ois.readObject();
				expenses = (List<Expense>) ois.readObject();
				budgets = (List<Budget>) ois.readObject();
				categories = (List<Category>) ois.readObject();
				System.out.println("데이터 불러오기 완료");
			} catch(IOException | ClassNotFoundException e) {
				System.out.println("데이터 불러오기 오류: " + e.getMessage());
			}
		}
	}
	
	// 메인 메뉴
	public void runMenu() {
		Scanner scanner = new Scanner(System.in);
		while(true) {
			System.out.println("\n개인 금융 관리 시스템");
			if(currentUser != null) {
				System.out.println("현재 사용자: " + currentUser);
			}
			System.out.println("1. 계정 관리(로그인/로그아웃)");
			System.out.println("2. 회원가입");
			System.out.println("3. 수입 관리");
			System.out.println("4. 지출 관리");
			System.out.println("5. 예산 관리");
			System.out.println("6. 카테고리 관리");
			System.out.println("7. 검색");
			System.out.println("8. 통계");
			System.out.println("9. 데이터 저장 및 불러오기");
			System.out.println("10. 프로그램 종료");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1: loginMenu(scanner); break;
			case 2: userMenu(scanner); break;
			case 3: incomeMenu(scanner); break;
			case 4: expenseMenu(scanner); break;
			case 5: budgetMenu(scanner); break;
			case 6: categoryMenu(scanner); break;
			case 7: searchMenu(scanner); break;
			case 8: statisticsMenu(scanner); break;
			case 9: dataMenu(scanner); break;
			case 10:
				saveDataToFile();
				System.out.println("프로그램을 종료합니다.");
				System.exit(0);
				break;
			default:
				System.out.println("1-10 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	// 로그인 메뉴
	private void loginMenu(Scanner scanner) {
		while(true) {
			if(currentUser == null) {
				System.out.println("\n로그인 메뉴");
				System.out.println("1. 로그인");
				System.out.println("2. 이전 메뉴로");
				
				int choice = getInt(scanner, "선택: ");
				
				switch(choice) {
				case 1:
					String id = getString(scanner, "사용자 ID: ");
					String password = getString(scanner, "비밀번호: ");
					
					if(login(id, password)) {
						System.out.println("로그인 성공!");
					} else {
						System.out.println("로그인 실패! ID 또는 비밀번호를 확인해주세요.");
					}
					break;
				case 2:
					return;
				default:
					System.out.println("1-2 사이의 숫자를 입력해주세요.");
				}
			} else {
				System.out.println("\n계정 관리 (현재 사용자: " + currentUser + ")");
				System.out.println("1. 로그아웃");
				System.out.println("2. 이전 메뉴로");
				
				int choice = getInt(scanner, "선택: ");
				
				switch(choice) {
				case 1:
					logout();
					System.out.println("로그아웃 완료!");
					break;
				case 2:
					return;
				default:
					System.out.println("1-2 사이의 숫자를 입력해주세요.");
				}
			}
		}
	}
	
	// 사용자 관리 메뉴
	private void userMenu(Scanner scanner) {
		while(true) {
			System.out.println("\n사용자 관리");
			System.out.println("1. 사용자 등록");
			System.out.println("2. 사용자 삭제");
			System.out.println("3. 사용자 목록");
			System.out.println("4. 이전 메뉴로");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1:
				String id = getString(scanner, "사용자 ID: ");
				String name = getString(scanner, "이름: ");
				String password = getString(scanner, "비밀번호: ");
				registerUser(id, name, password);
				break;
			case 2:
				String removeId = getString(scanner, "삭제할 사용자 ID: ");
				removeUser(removeId);
				break;
			case 3:
				displayAllUsers();
				break;
			case 4:
				return;
			default:
				System.out.println("1-4 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	// 수입 관리 메뉴
	private void incomeMenu(Scanner scanner) {
		while(true) {
			System.out.println("\n수입 관리");
			System.out.println("1. 수입 추가");
			System.out.println("2. 수입 삭제");
			System.out.println("3. 수입 목록");
			System.out.println("4. 이전 메뉴로");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1:
				int amount = getPositiveInt(scanner, "금액: ");
				String date = getDate(scanner, "날짜 (예: 2025-07-07): ");
				String category = getString(scanner, "카테고리: ");
				addIncome(amount, date, category);
				break;
			case 2:
				String removeDate = getDate(scanner, "날짜: ");
				int removeAmount = getPositiveInt(scanner, "금액: ");
				removeIncome(removeDate, removeAmount);
				break;
			case 3:
				displayMyIncomes();
				break;
			case 4:
				return;
			default:
				System.out.println("1-4 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	// 지출 관리 메뉴
	private void expenseMenu(Scanner scanner) {
		while(true) {
			System.out.println("\n지출 관리");
			System.out.println("1. 지출 추가");
			System.out.println("2. 지출 삭제");
			System.out.println("3. 지출 목록");
			System.out.println("4. 이전 메뉴로");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1:
				int amount = getPositiveInt(scanner, "금액: ");
				String date = getDate(scanner, "날짜 (예: 2025-07-07): ");
				String category = getString(scanner, "카테고리: ");
				addExpense(amount, date, category);
				break;
			case 2:
				String removeDate = getDate(scanner, "날짜: ");
				int removeAmount = getPositiveInt(scanner, "금액: ");
				removeExpense(removeDate, removeAmount);
				break;
			case 3:
				displayMyExpenses();
				break;
			case 4:
				return;
			default:
				System.out.println("1-4 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	// 예산 관리 메뉴
	private void budgetMenu(Scanner scanner) {
		while(true) {
			System.out.println("\n예산 관리");
			System.out.println("1. 예산 설정");
			System.out.println("2. 예산 현황");
			System.out.println("3. 이전 메뉴로");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1:
				int budgetAmount = getPositiveInt(scanner, "예산 금액: ");
				setBudget(budgetAmount);
				break;
			case 2:
				displayMyBudget();
				break;
			case 3:
				return;
			default:
				System.out.println("1-3 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	// 카테고리 관리 메뉴
	private void categoryMenu(Scanner scanner) {
		while(true) {
			System.out.println("\n카테고리 관리");
			System.out.println("1. 카테고리 추가");
			System.out.println("2. 카테고리 삭제");
			System.out.println("3. 카테고리 목록");
			System.out.println("4. 이전 메뉴로");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1:
				String categoryName = getString(scanner, "카테고리명: ");
				addCategory(categoryName);
				break;
			case 2:
				String removeCategoryName = getString(scanner, "삭제할 카테고리명: ");
				removeCategory(removeCategoryName);
				break;
			case 3:
				displayCategories();
				break;
			case 4:
				return;
			default:
				System.out.println("1-4 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	// 검색 메뉴
	private void searchMenu(Scanner scanner) {
		while(true) {
			System.out.println("\n검색 메뉴");
			System.out.println("1. 날짜로 검색");
			System.out.println("2. 카테고리로 검색");
			System.out.println("3. 금액 범위로 검색");
			System.out.println("4. 이전 메뉴로");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1:
				String date = getDate(scanner, "검색할 날짜 (예: 2025-07-07): ");
				searchByDate(date);
				break;
			case 2:
				String category = getString(scanner, "검색할 카테고리: ");
				searchByCategory(category);
				break;
			case 3:
				int minAmount = getInt(scanner, "최소 금액: ");
				int maxAmount = getInt(scanner, "최대 금액: ");
				if(minAmount >= 0 && maxAmount >= 0) {
					searchByAmount(minAmount, maxAmount);
				} else {
					System.out.println("0 이상의 금액을 입력해주세요.");
				}
				break;
			case 4:
				return;
			default:
				System.out.println("1-4 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	// 통계 메뉴
	private void statisticsMenu(Scanner scanner) {
		while(true) {
			System.out.println("\n통계 메뉴");
			System.out.println("1. 총 수입/지출");
			System.out.println("2. 월별 통계");
			System.out.println("3. 카테고리별 통계");
			System.out.println("4. 이전 메뉴로");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1:
				calculateTotal();
				break;
			case 2:
				String yearMonth = getString(scanner, "년월 (예: 2025-07): ");
				getMonthlyStatistics(yearMonth);
				break;
			case 3:
				getCategoryStatistics();
				break;
			case 4:
				return;
			default:
				System.out.println("1-4 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	// 데이터 관리 메뉴
	private void dataMenu(Scanner scanner) {
		while(true) {
			System.out.println("\n데이터 관리");
			System.out.println("1. 데이터 저장");
			System.out.println("2. 데이터 불러오기");
			System.out.println("3. 이전 메뉴로");
			
			int choice = getInt(scanner, "선택: ");
			
			switch(choice) {
			case 1:
				saveDataToFile();
				break;
			case 2:
				loadDataFromFile();
				break;
			case 3:
				return;
			default:
				System.out.println("1-3 사이의 숫자를 입력해주세요.");
			}
		}
	}
	
	public static void main(String[] args) {
		PersonalFinanceManager system = new PersonalFinanceManager();
		system.loadDataFromFile();
		system.runMenu();
	}
}
package noconnectiondb_project;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import lombok.*;

//제품 정보 저장
@Data
class Product implements Serializable {
	private String id;
	private String name;
	private int price;
	private int stock;

	public Product(String id, String name, int price, int stock) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.stock = stock; // 재고 수량
	}
}

//고객 정보 저장
@Data
class Customer implements Serializable {
	private String id;
	private String name;

	public Customer(String id, String name) {
		this.id = id;
		this.name = name;
	}
}

//주문 정보 저장
@Data
class Order implements Serializable {
	private String id;
	private String customerId;
	private String productId;
	private String status; // 주문 상태 (ORDERED, PAID, SHIPPED, DELIVERED 등)

	public Order(String id, String customerId, String productId) {
		this.id = id;
		this.customerId = customerId;
		this.productId = productId;
		this.status = "ORDERED"; // 주문 생성시 기본 상태
	}
}

//결제 정보 저장
@Data
class Payment implements Serializable {
	private String orderId;
	private int amount;

	public Payment(String orderId, int amount) {
		this.orderId = orderId;
		this.amount = amount;
	}
}

//배송 정보 저장
@Data
class Shipping implements Serializable {
	private String orderId; // 배송할 주문의 ID
	private String status; // 배송 상태 (PREPARING, SHIPPED, DELIVERED 등)

	public Shipping(String orderId) {
		this.orderId = orderId;
		this.status = "PREPARING"; // 배송 생성 시 기본 상태
	}
}

//전체적인 시스템 관리
public class ShoppingMallSystem {
	private List<Product> products; // Product 클래스로 만든 실제 상품 객체들을 여러 개 담을 수 있는 리스트
	private List<Customer> customers;
	private List<Order> orders;
	private List<Payment> payments;
	private List<Shipping> shippings;
	private boolean isLoggedIn;
	private String currentUser;

	public ShoppingMallSystem() {
		this.products = new ArrayList<>(); // ArrayList 객체를 생성하고 그 ArrayList의 주소를 product 변수에 저장, 이 product를 통해서 상품들을
											// 추가/삭제/조회 가능
		this.customers = new ArrayList<>();
		this.orders = new ArrayList<>();
		this.payments = new ArrayList<>();
		this.shippings = new ArrayList<>();
		this.isLoggedIn = false;
		this.currentUser = null;
	}

	// 외부에서 상품의 id, name, price, stock 정보를 받아서 상품을 추가하는 public 메소드
	// 매개변수: 메소드가 작업을 하기 위해 외부에서 받아야 하는 정보
	public void addProduct(String id, String name, int price, int stock) {
		// 잘못된 값이 입력되었는지 검사 다음중 하나라도 해당되면 잘못된 입력이라고 판단하여 아래 println문을 실행하고return 문으로 가서
		// 이 메소드를 호출한 곳으로 감
		// 호출은 manageProducts에서 case1에서 호출했으므로 break문-> while문 처음으로 가서 다시 메뉴 표시됨
		if (id == null || id.isEmpty() || name == null || name.isEmpty() || price <= 0 || stock < 0) {
			System.out.println("모든 필드는 필수이며, 가격과 재고는 양수여야 합니다.");
			return;
		}

		// 위에 return 이 실행되지 않았을 때만 이 부분을 실행
		Product product = new Product(id, name, price, stock);
		products.add(product);
		System.out.println("제품 추가 완료!");
	}

	// 상품을 삭제한 메소드(삭제할 상품의 ID를 매개변수로 받음)
	public void removeProduct(String id) {
		Product productToRemove = findProductById(id); // findProductById메소드를 호출해 해당 ID의 상품을 찾아서 찾은 상품 객체를
														// productToRemove 변수에 저장
		if (productToRemove != null) { // 상품을 찾았는지 확인(null이 아니면 찾음)
			products.remove(productToRemove); // 리스트에서 해당 객체를 삭제
			System.out.println("제품 삭제 완료!");
		} else {
			System.out.println("해당 제품을 찾을 수 없습니다.");
		}
	}

	// 모든 상품을 화면에 출력하는 메소드
	public void displayAllProducts() {
		if (products.isEmpty()) { // 리스트가 비어있는지 확인
			System.out.println("등록된 제품이 없습니다.");
		} else {
			// products 리스트의 모든 Product 객체를 하나씩 꺼내서 처리
			// Product: 꺼낼 객체의 타입, product: 꺼낸 객체를 담을 변수명, products: 꺼낼 대상 리스트
			// 즉, products 리스트에서 Product 객체를 하나씩 꺼내서 product 변수에 담아라
			for (Product product : products) {
				System.out.println("제품 ID: " + product.getId());
				System.out.println("제품명: " + product.getName());
				System.out.println("가격: " + product.getPrice() + "원");
				System.out.println("재고: " + product.getStock() + "개");
			}
		}
	}

	// ID로 상품을 찾는 메소드
	private Product findProductById(String id) { // 반환 타입이 Product 객체
		for (Product product : products) {
			if (product.getId().equals(id)) { // 현재 상품의 ID와 찾는 ID가 같은지 비교
				return product; // 일치하는 상품을 찾으면 즉시 그 상품 객체를 반환하고 메소드 종료
			}
		}
		return null;
	}

	// 고객을 등록하는 메소드
	public void registerCustomer(String id, String name) {
		if (id == null || id.isEmpty() || name == null || name.isEmpty()) {
			System.out.println("모든 필드는 필수입니다.");
			return;
		}

		Customer customer = new Customer(id, name);
		customers.add(customer);
		System.out.println("고객 등록 완료!");
	}

	// 고객 삭제 메소드
	public void removeCustomer(String id) {
		Customer customerToRemove = findCustomerById(id);
		if (customerToRemove != null) {
			customers.remove(customerToRemove);
			System.out.println("고객 삭제 완료!");
		} else {
			System.out.println("해당 고객을 찾을 수 없습니다.");
		}
	}

	// 모든 고객을 화면에 출력하는 메소드
	public void displayAllCustomers() {
		if (customers.isEmpty()) {
			System.out.println("등록된 고객이 없습니다.");
		} else {
			for (Customer customer : customers) {
				System.out.println("고객 ID: " + customer.getId());
				System.out.println("이름: " + customer.getName());
				System.out.println();
			}
		}
	}

	// ID로 고객을 찾는 메소드
	private Customer findCustomerById(String id) {
		for (Customer customer : customers) {
			if (customer.getId().equals(id)) {
				return customer;
			}
		}
		return null;
	}

	// 주문을 추가하는 메소드
	public void createOrder(String orderId, String customerId, String productId) {
		Customer customer = findCustomerById(customerId);
		Product product = findProductById(productId);

		if (customer != null && product != null && product.getStock() > 0) {
			Order order = new Order(orderId, customerId, productId);
			orders.add(order);
			product.setStock(product.getStock() - 1);
			System.out.println("주문 생성 완료!");
		} else if (customer == null) {
			System.out.println("해당 고객을 찾을 수 없습니다.");
		} else if (product == null) {
			System.out.println("해당 제품을 찾을 수 없습니다.");
		} else {
			System.out.println("제품 재고가 부족합니다.");
		}
	}

	// 모든 주문을 화면에 출력하는 메소드
	public void displayAllOrders() {
		if (orders.isEmpty()) {
			System.out.println("등록된 주문이 없습니다.");
		} else {
			for (Order order : orders) {
				System.out.println("주문번호: " + order.getId());
				System.out.println("고객 ID: " + order.getCustomerId());
				System.out.println("제품 ID: " + order.getProductId());
				System.out.println("주문 상태: " + order.getStatus());
				System.out.println();
			}
		}
	}

	// 특정 주문의 상태를 변경하는 메소드
	public void updateOrderStatus(String orderId, String newStatus) {
		Order order = findOrderById(orderId);
		if (order != null) {
			order.setStatus(newStatus); // 주문 객체의 상태를 새 상태로 변경
			System.out.println("주문 상태 업데이트됨: " + newStatus);
		} else {
			System.out.println("해당 주문번호를 찾을 수 없습니다.");
		}
	}

	// 주문 ID로 주문을 찾는 메소드
	private Order findOrderById(String id) {
		for (Order order : orders) {
			if (order.getId().equals(id)) {
				return order;
			}
		}
		return null;
	}

	// 결제를 추가하는 코드
	public void addPayment(String orderId, int amount) {
		Payment payment = new Payment(orderId, amount);
		payments.add(payment);
		System.out.println("결제 정보 추가 완료!");
	}

	// 주문번호를 받아서 그 주문의 결제 정보를 찾아 화면에 출력하는 메소드
	public void displayPaymentInfo(String orderId) {
		Payment payment = findPaymentByOrderId(orderId);
		if (payment != null) {
			System.out.println("주문번호: " + payment.getOrderId());
			System.out.println("결제 금액: " + payment.getAmount() + "원");
		} else {
			System.out.println("해당 주문번호와 결제 정보를 찾을 수 없습니다.");
		}
	}

	// 주문번호로 결제정보를 찾아서 반환하는 메소드
	private Payment findPaymentByOrderId(String orderId) {
		for (Payment payment : payments) {
			if (payment.getOrderId().equals(orderId)) {
				return payment;
			}
		}
		return null;
	}

	// 새로운 배송 정보를 생성해서 시스템에 추가하는 메소드
	public void addShippingInfo(String orderId) {
		Shipping shipping = new Shipping(orderId);
		shippings.add(shipping);
		System.out.println("배송 정보 추가 완료!");
	}

	// 특정 주문의 배송 정보를 화면에 출력하는 메소드
	public void displayShippingInfo(String orderID) {
		Shipping shipping = findShippingByOrderId(orderID);
		if (shipping != null) {
			System.out.println("주문번호: " + shipping.getOrderId());
			System.out.println("배송 상태: " + shipping.getStatus());
		} else {
			System.out.println("해당 주문번호와 배송 정보를 찾을 수 없습니다.");
		}
	}

	// 주문번호로 배송 정보를 찾아서 반환하는 메소드
	private Shipping findShippingByOrderId(String orderId) {
		for (Shipping shipping : shippings) {
			if (shipping.getOrderId().equals(orderId)) {
				return shipping;
			}
		}
		return null;
	}

	// 제품명으로 제품을 검색해서 모든 결과를 출력하는 메소드
	public void searchProductByName(String name) {
		boolean found = false;
		for (Product product : products) {
			if (product.getName().toLowerCase().contains(name.toLowerCase())) {
				System.out.println("제품 ID: " + product.getId());
				System.out.println("제품명: " + product.getName());
				System.out.println("가격: " + product.getPrice() + "원");
				System.out.println("재고: " + product.getStock() + "개");
				System.out.println();
				found = true;
			}
		}
		if (!found) {
			System.out.println("검색 결과가 없습니다.");
		}
	}

	// 고객명으로 고객을 검색해서 모든 결과를 출력하는 메소드
	public void searchCustomerByName(String name) {
		boolean found = false;
		for (Customer customer : customers) {
			if (customer.getName().toLowerCase().contains(name.toLowerCase())) {
				System.out.println("고객 ID: " + customer.getId());
				System.out.println("이름: " + customer.getName());
				System.out.println();
				found = true;
			}
		}
		if (!found) {
			System.out.println("검색 결과가 없습니다.");
		}
	}

	// 주문번호로 특정 주문을 검색해서 출력하는 메소드
	public void searchOrderById(String orderId) {
		Order order = findOrderById(orderId);
		if (order != null) {
			System.out.println("주문번호: " + order.getId());
			System.out.println("고객 ID: " + order.getCustomerId());
			System.out.println("재품 ID: " + order.getProductId());
			System.out.println("주문 상태: " + order.getStatus());
		} else {
			System.out.println("해당 주문번호를 찾을 수 없습니다.");
		}
	}

	public boolean login(String username, String password) {
		if ((username.equals("admin") && password.equals("password"))
				|| (username.equals(currentUser) && password.equals("12345"))) {
			isLoggedIn = true;
			return true;
		}
		return false;
	}

	public void logout() {
		isLoggedIn = false;
		currentUser = null;
	}

	// 관리자로 로그인되어있는지 확인하는 메소드
	public boolean isAdminLoggedIn() {
		return isLoggedIn && currentUser.equals("admin");
	}

	// 일반 사용자로 로그인되어있는지 확인하는 메소드
	public boolean isUserLoggedIn() {
		return isLoggedIn && !currentUser.equals("admin");
	}

	// 프로그램의 모든 데이터를 파일에 저장하는 메소드
	public void saveDataToFile() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("mall_data.dat"))) {
			oos.writeObject(products);
			oos.writeObject(customers);
			oos.writeObject(orders);
			oos.writeObject(payments);
			oos.writeObject(shippings);
			System.out.println("데이터 저장 완료");
		} catch (IOException e) {
			System.out.println("데이터 자장 중 오류 발생: " + e.getMessage());
		}
	}

	// 파일에서 저장된 데이터를 불러와서 리스트에 복원하는 메소드
	@SuppressWarnings("unchecked")
	public void loadDataFromFile() {
		File file = new File("mall_data.dat");
		if (file.exists()) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				products = (List<Product>) ois.readObject();
				customers = (List<Customer>) ois.readObject();
				orders = (List<Order>) ois.readObject();
				payments = (List<Payment>) ois.readObject();
				shippings = (List<Shipping>) ois.readObject();
				System.out.println("데이터 불러오기 완료");
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("데이터 불러오기 중 오류 발생: " + e.getMessage());
			}
		} else {
			System.out.println("저장된 데이터 파일이 없습니다.");
		}
	}

	// 프로그램의 메인 메뉴를 관리하는 메소드
	public void runMenu() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.println("\n온라인 쇼핑몰 주문 처리 시스템");
			System.out.println("------------------------");
			System.out.println("1. 로그인 (관리자)");
			System.out.println("2. 로그인 (일반 사용자)");
			System.out.println("3. 제품 관리");
			System.out.println("4. 고객 관리");
			System.out.println("5. 주문 관리");
			System.out.println("6. 결제 관리");
			System.out.println("7. 배송 관리");
			System.out.println("8. 검색");
			System.out.println("9. 내 주문 정보 보기 (로그인 후에만 사용 가능)");
			System.out.println("10. 프로그램 종료");

			System.out.print("선택: ");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				loginAdmin(scanner); // 이 입력도구를 사용해서 작업하하는 의미
				break;
			case 2:
				loginUser(scanner);
				break;
			case 3:
				manageProducts(scanner);
				break;
			case 4:
				manageCustomers(scanner);
				break;
			case 5:
				manageOrders(scanner);
				break;
			case 6:
				managePayments(scanner);
				break;
			case 7:
				manageShippings(scanner);
				break;
			case 8:
				search(scanner);
				break;
			case 9:
				displayUserOrders();
				break;
			case 10:
				saveDataToFile();
				System.exit(0);
				break;
			default:
				System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
			}
		}
	}

	// 관리자 로그인 전용 처리
	private void loginAdmin(Scanner scanner) {
		System.out.print("관리자 ID: ");
		String username = scanner.nextLine();
		System.out.print("비밀번호: ");
		String password = scanner.nextLine();

		if (login(username, password)) {
			currentUser = "admin"; // 현재 사용자를 admin으로 설정
			System.out.println("관리자 로그인 성공");
		} else {
			System.out.println("로그인 실패");
		}
	}

	// 일반 사용자 로그인 전용 처리
	private void loginUser(Scanner scanner) {
		System.out.print("사용자 이름: ");
		String username = scanner.nextLine();
		System.out.print("비밀번호: ");
		String password = scanner.nextLine();

		if (login(username, password)) {
			currentUser = username;
			System.out.println("사용자 로그인 성공");
		} else {
			System.out.println("로그인 실패");
		}
	}

	// 제품 관리 전용 메뉴 시스템
	private void manageProducts(Scanner scanner) {
		while (true) {
			System.out.println("\n제품 관리 메뉴");
			System.out.println("1. 제품 추가");
			System.out.println("2. 제품 삭제");
			System.out.println("3. 제품 목록 보기");
			System.out.println("4. 이전 메뉴로");

			System.out.print("선택: ");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				System.out.print("제품 ID: ");
				String id = scanner.nextLine();
				System.out.print("제품명: ");
				String name = scanner.nextLine();
				System.out.print("가격: ");
				int price = scanner.nextInt();
				System.out.print("재고: ");
				int stock = scanner.nextInt();
				scanner.nextLine();
				addProduct(id, name, price, stock);
				break;
			case 2:
				System.out.print("삭제할 제품의 ID: ");
				String idToDelete = scanner.nextLine();
				removeProduct(idToDelete);
				break;
			case 3:
				displayAllProducts();
				break;
			case 4:
				return;
			default:
				System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
			}
		}
	}

	// 고객 관리 전용 메뉴 시스템
	private void manageCustomers(Scanner scanner) {
		while (true) {
			System.out.println("\n고객 관리 메뉴");
			System.out.println("1. 고객 등록");
			System.out.println("2. 고객 삭제");
			System.out.println("3. 고객 목록 보기");
			System.out.println("4. 이전 메뉴로");

			System.out.print("선택: ");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				System.out.print("고객 ID: ");
				String id = scanner.nextLine();
				System.out.print("이름: ");
				String name = scanner.nextLine();
				registerCustomer(id, name);
				break;
			case 2:
				System.out.print("삭제할 고객의 ID: ");
				String idToDelete = scanner.nextLine();
				removeCustomer(idToDelete);
				break;
			case 3:
				displayAllCustomers();
				break;
			case 4:
				return;
			default:
				System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
			}
		}
	}

	// 주문 관리 전용 메뉴 시스템
	private void manageOrders(Scanner scanner) {
		while (true) {
			System.out.println("\n주문 관리 메뉴");
			System.out.println("1. 주문 생성");
			System.out.println("2. 주문 목록 보기");
			System.out.println("3. 주문 상태 변경");
			System.out.println("4. 이전 메뉴로");

			System.out.print("선택: ");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				System.out.print("주문번호: ");
				String orderId = scanner.nextLine();
				System.out.print("고객 ID: ");
				String customerId = scanner.nextLine();
				System.out.print("제품 ID: ");
				String productId = scanner.nextLine();
				createOrder(orderId, customerId, productId);
				break;
			case 2:
				displayAllOrders();
				break;
			case 3:
				System.out.print("주문번호: ");
				String orderIdToUpdate = scanner.nextLine();
				System.out.print("새로운 상태: ");
				String newStatus = scanner.nextLine();
				updateOrderStatus(orderIdToUpdate, newStatus);
				break;
			case 4:
				return;
			default:
				System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
			}
		}
	}

	// 결제 관리 전용 메뉴 시스템
	private void managePayments(Scanner scanner) {
		while (true) {
			System.out.println("\n결제 관리 메뉴");
			System.out.println("1. 결제 정보 추가");
			System.out.println("2. 결제 정보 보기");
			System.out.println("3. 이전 메뉴로");

			System.out.print("선택: ");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				System.out.print("주문번호: ");
				String orderId = scanner.nextLine();
				System.out.print("결제 금액: ");
				int amount = scanner.nextInt();
				scanner.nextLine();
				addPayment(orderId, amount);
				break;
			case 2:
				System.out.print("주문번호: ");
				String orderIdToDisplay = scanner.nextLine();
				displayPaymentInfo(orderIdToDisplay);
				break;
			case 3:
				return;
			default:
				System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
			}
		}
	}

	// 배송 관리 전용 메뉴 시스템
	private void manageShippings(Scanner scanner) {
		while (true) {
			System.out.println("\n배송 관리 메뉴");
			System.out.println("1. 배송 정보 추가");
			System.out.println("2. 배송 정보 보기");
			System.out.println("3. 이전 메뉴로");

			System.out.print("선택: ");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				System.out.print("주문번호: ");
				String orderId = scanner.nextLine();
				addShippingInfo(orderId);
				break;
			case 2:
				System.out.print("주문번호: ");
				String orderIdToDisplay = scanner.nextLine();
				displayShippingInfo(orderIdToDisplay);
				break;
			case 3:
				return;
			default:
				System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
			}
		}
	}

	// 검색 관리 전용 메뉴 시스템
	private void search(Scanner scanner) {
		while (true) {
			System.out.println("\n검색 메뉴");
			System.out.println("1. 제품명으로 검색");
			System.out.println("2. 고객명으로 검색");
			System.out.println("3. 주문번호로 검색");
			System.out.println("4. 이전 메뉴로");

			System.out.print("선택: ");
			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
			case 1:
				System.out.print("검색할 제품명: ");
				String productName = scanner.nextLine();
				searchProductByName(productName);
				break;
			case 2:
				System.out.print("검색할 고객명: ");
				String customerName = scanner.nextLine();
				searchCustomerByName(customerName);
				break;
			case 3:
				System.out.print("검색할 주문번호: ");
				String orderId = scanner.nextLine();
				searchOrderById(orderId);
				break;
			case 4:
				return;
			default:
				System.out.println("잘못된 선택입니다. 다시 시도해주세요.");
			}
		}
	}

	// 현재 로그인한 사용자의 주문 내역만 보여주는 메소드
	private void displayUserOrders() {
		if (!isUserLoggedIn()) { // 일반 사용자로 로그이되어 있지 않으면
			System.out.println("로그인 후에만 사용 가능합니다.");
			return;
		}

		int orderCount = 0; // 현재 사용자의 주문 개수를 세기 위한 변수
		for (Order order : orders) {
			if (order.getCustomerId().equals(currentUser)) { // 주문의 고객 ID와 현재 로그인한 사용자가 같은지 확인
				System.out.println("주문번호: " + order.getId());
				System.out.println("제품 ID: " + order.getProductId());
				System.out.println("주문 상태: " + order.getStatus());
				System.out.println();
				orderCount++;
			}
		}
		System.out.println(orderCount + "건의 주문이 있습니다.");
	}

	// 여러 종류의 리스트(Product, Customer, Order)에서 ID로 항목을 찾는 범용 메소드
	private <T> T findItemById(List<T> list, String id) {
		for (T item : list) {
			if (item instanceof Product && ((Product) item).getId().equals(id)) {
				return item;
			} else if (item instanceof Customer && ((Customer) item).getId().equals(id)) {
				return item;
			} else if (item instanceof Order && ((Order) item).getId().equals(id)) {
				return item;
			}
		}
		return null;
	}

	// 어떤 타입의 리스트든 받아서 그 안의 모든 항목들을 화면에 출력하는 메소드
	private void displayItem(List<?> list) { // 어떤 타입의 리스트든 받을 수 있다는 의미
		if (list.isEmpty()) {
			System.out.println("등록된 항목이 없습니다.");
		} else {
			for (Object item : list) {
				if (item instanceof Product) {
					Product product = (Product) item;
					System.out.println("제품 ID: " + product.getId());
					System.out.println("제품명: " + product.getName());
					System.out.println("가격: " + product.getPrice() + "원");
					System.out.println("재고: " + product.getStock() + "개");
				} else if (item instanceof Customer) {
					Customer customer = (Customer) item;
					System.out.println("고객 ID: " + customer.getId());
					System.out.println("이름: " + customer.getName());
				} else if (item instanceof Order) {
					Order order = (Order) item;
					System.out.println("주문번호: " + order.getId());
					System.out.println("고객 ID: " + order.getCustomerId());
					System.out.println("제품 ID: " + order.getProductId());
					System.out.println("주문 상태: " + order.getStatus());
				}
				System.out.println();
			}
		}
	}

	public static void main(String[] args) {
		ShoppingMallSystem system = new ShoppingMallSystem();
		system.loadDataFromFile();
		system.runMenu();
	}
}
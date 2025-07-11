package basicbank;

// 계좌 정보를 저장하는 클래스 (DB 테이블과 매핑)
public class Account {
	private String id;
	private String name;
	private double balance;
	
	public Account() {}
	
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

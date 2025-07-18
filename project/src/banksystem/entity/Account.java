package banksystem.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Account {
	private String accountId;       // 계좌번호
	private String accountName;     // 계좌명
	private String accountType;     // 계좌종류
	private String accountPassword; // 계좌 비밀번호
	private double balance;         // 계좌 잔액
	private String userId;          // 계좌 소유자 ID
	private Date createDate;        // 계좌 개설일
	private double interestRate;    // 연 이자율
	private Date lastInterestDate;  // 마지막 이자 지급일

	public Account() {}

	public Account(String accountId, String accountName, String accountType, 
			       String accountPassword, double balance, String userId) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.accountType = accountType;
		this.accountPassword = accountPassword;
		this.balance = balance;
		this.userId = userId;
		this.createDate = new Date();
		
		// 이자 계산의 시작점은 계좌 개설일자로
		// 즉, 이자는 마지막 이자 지급일부터 현재까지의 기간을 기준으로 계산
		this.lastInterestDate = new Date();
	}

	// 이자율 포함 생성자 
	public Account(String accountId, String accountName, String accountType, 
			       String accountPassword, double balance, String userId, double interestRate) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.accountType = accountType;
		this.accountPassword = accountPassword;
		this.balance = balance;
		this.userId = userId;
		this.createDate = new Date();
		this.interestRate = interestRate;
		this.lastInterestDate = new Date();
	}
}
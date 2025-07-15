package mondayproject;

import lombok.Data;
import java.util.Date;

@Data
public class Account {
    private String accountId;		// 계좌번호
    private String accountName;		// 계좌명
    private String accountType;		// 계좌종류
    private String accountPassword;	// 계좌 비밀번호
    private double balance;			// 계좌 잔액
    private String userId;			// 계좌 소유자 ID
    private Date createDate;		// 계좌 개설일

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
    }
}
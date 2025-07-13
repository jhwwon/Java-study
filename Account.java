package banksystem;

import lombok.Data;
import java.util.Date;

@Data
public class Account {
    private String accountId;
    private String accountName;
    private String accountType;
    private String accountPassword;  
    private double balance;
    private String userId; 
    private Date createDate;

    public Account() {}

    public Account(String accountId, String accountName, String accountType, String accountPassword, double balance, String userId) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.accountPassword = accountPassword;  
        this.balance = balance;
        this.userId = userId;
        this.createDate = new Date();
    }
}
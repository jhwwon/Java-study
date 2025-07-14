package mondayproject;

import lombok.Data;
import java.util.Date;

@Data
public class Transaction {
    private String transactionId;
    private String accountId;
    private String transactionType;
    private double amount;
    private double balanceAfter;
    private String counterpartAccount;
    private String counterpartName;        // 새로 추가: 상대방 이름
    private String depositorName;          // 새로 추가: 입금자명
    private String transactionMemo;        // 새로 추가: 거래 메모
    private Date transactionDate;

    public Transaction() {}

    // 입금/출금용 생성자
    public Transaction(String transactionId, String accountId, String transactionType, 
                       double amount, double balanceAfter) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.transactionDate = new Date();
    }

    // 이체용 생성자
    public Transaction(String transactionId, String accountId, String transactionType, 
                       double amount, double balanceAfter, String counterpartAccount) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.counterpartAccount = counterpartAccount;
        this.transactionDate = new Date();
    }
    
    // 완전한 거래내역용 생성자 (새로 추가)
    public Transaction(String transactionId, String accountId, String transactionType, 
                       double amount, double balanceAfter, String counterpartAccount,
                       String counterpartName, String depositorName, String transactionMemo) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.counterpartAccount = counterpartAccount;
        this.counterpartName = counterpartName;
        this.depositorName = depositorName;
        this.transactionMemo = transactionMemo;
        this.transactionDate = new Date();
    }
}

// 개선된 이체 정보를 담는 클래스
@Data
class TransferInfo {
    private String fromAccountId;
    private String toAccountId;
    private double amount;
    private String memo;               // 새로 추가: 이체 메모
    
    public TransferInfo(String fromAccountId, String toAccountId, double amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }
    
    // 메모 포함 생성자 (새로 추가)
    public TransferInfo(String fromAccountId, String toAccountId, double amount, String memo) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.memo = memo;
    }
}
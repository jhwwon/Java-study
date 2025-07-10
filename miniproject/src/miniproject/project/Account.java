package miniproject.project;

import java.util.Date;
import lombok.Data; // Getter, Setter, toString, equals, hashCode를 자동으로 생성
import lombok.NoArgsConstructor; // 인자 없는 기본 생성자를 자동으로 생성

// 계좌 클래스 (DB 연동용)
@Data 
@NoArgsConstructor // 인자 없는 기본 생성자를 자동으로 생성합니다.
public class Account {
    private String id;
    private String ownerId;
    private String accountHolderName;
    private String accountType;
    private String accountPassword;
    private double balance;
    private String accountAlias;
    private Date createdDate;
    private double interestRate;
    private Date lastInterestDate;

    public Account(String id, String ownerId, String accountHolderName, String accountType, 
                   String accountPassword, double initialBalance, String accountAlias, double interestRate) {
        this.id = id;
        this.ownerId = ownerId;
        this.accountHolderName = accountHolderName;
        this.accountType = accountType;
        this.accountPassword = accountPassword;
        this.balance = initialBalance;
        this.accountAlias = accountAlias;
        this.interestRate = interestRate;
        this.createdDate = new Date(); // 내부적으로 초기화
        this.lastInterestDate = new Date(); // 내부적으로 초기화
    }

    // 기능 메소드들:
    // 이 메소드들은 비즈니스 로직을 포함하므로 Lombok으로 대체할 수 없으며, 그대로 유지해야 합니다.
    public boolean checkPassword(String password) {
        return this.accountPassword.equals(password);
    }

    public double calculateMonthlyInterest() {
        double monthlyRate = interestRate / 100.0 / 12.0;
        return balance * monthlyRate;
    }
}
package finalbanksystem;

import java.util.Date;
import lombok.Data; // Getter, Setter, toString, equals, hashCode를 자동으로 생성
import lombok.NoArgsConstructor; // 인자 없는 기본 생성자를 자동으로 생성

@Data 
@NoArgsConstructor 
public class Account {
    private String id;                //계좌번호
    private String ownerId;           //계좌 소유자의 사용자 ID
    private String accountHolderName; //계좌주의 실제 이름
    private String accountType;       //계좌의 종류(현재 자유입출금통장, 적금통장, 정기예금)
    private String accountPassword;   //계좌 비밀번호
    private double balance;           //현재 계좌 잔액
    private String accountAlias;      //계좌 별명(사용자지정 또는 자동 생성)
    private Date createdDate;         //계좌 개설날짜와 시간
    private double interestRate;      //연간 이자율
    private Date lastInterestDate;    //마지막으로 이자를 지급받은 날짜

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
        this.createdDate = new Date();      // 내부적으로 초기화
        this.lastInterestDate = new Date(); // 내부적으로 초기화
    }

    //입력받은 비밀번호와 계좌 비밀번호 일지 여부 확인
    public boolean checkPassword(String password) {
        return this.accountPassword.equals(password);
    }

    //월 이자 금액 계산
    public double calculateMonthlyInterest() {
        double monthlyRate = interestRate / 100.0 / 12.0;
        return balance * monthlyRate;
    }
}
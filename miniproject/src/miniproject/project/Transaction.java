package miniproject.project;

import java.text.DecimalFormat;
import java.util.Date;
import lombok.Data; // Getter, Setter, toString, equals, hashCode를 자동으로 생성
import lombok.NoArgsConstructor; // 인자 없는 기본 생성자를 자동으로 생성
import lombok.AllArgsConstructor; // 모든 필드를 포함하는 생성자를 자동으로 생성

// 거래내역 클래스 (DB 연동용)
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode를 포함합니다.
@NoArgsConstructor // 인자 없는 기본 생성자를 자동으로 생성합니다.
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
public class Transaction {
    private int transactionId;
    private String accountId;
    private String type;
    private double amount;
    private String detail;
    private Date timestamp;

    public Transaction(String accountId, String type, double amount, String detail) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.detail = detail;
        this.timestamp = new Date(); // 내부적으로 초기화
    }

    @Override
    public String toString() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return "[" + formatter.format(timestamp) + "] " + type + " | " + formatter.format(amount) + "원 | " + detail;
    }
}
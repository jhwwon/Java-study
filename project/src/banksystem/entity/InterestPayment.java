package banksystem.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestPayment {
    private String paymentId;       // 이자 지급 고유 번호
    private String accountId;       // 이자를 받는 계좌
    private Date paymentDate;       // 이자 지급일
    private double interestAmount;  // 지급된 이자 금액
    private String adminId;         // 이자 지급을 실행한 관리자
    
    // 이자 지급용 생성자 (paymentDate는 자동 설정)
    public InterestPayment(String paymentId, String accountId, double interestAmount, String adminId) {
        this.paymentId = paymentId;
        this.accountId = accountId;
        this.interestAmount = interestAmount;
        this.adminId = adminId;
        this.paymentDate = new Date();
    }
}
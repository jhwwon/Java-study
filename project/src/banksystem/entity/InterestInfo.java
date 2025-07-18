package banksystem.entity;

import java.util.Date;
import lombok.Getter;

@Getter
public class InterestInfo {
    private String accountId;      // 이자를 계산할 계좌번호
    private double principal;	   // 원금
    private double interestRate;   // 연 이자율
    private Date lastInterestDate; // 마지막 이자 지급일(이자 계산 시작점)
    private Date currentDate;      // 현재 날짜
    private long days;			   // 경과 일수
    private double interestAmount; // 계산된 이자 금액
    private String accountType;    // 계좌 종류
    
    public InterestInfo(String accountId, double principal, double interestRate,
                       Date lastInterestDate, Date currentDate, long days,
                       double interestAmount, String accountType) {
        this.accountId = accountId;
        this.principal = principal;
        this.interestRate = interestRate;
        this.lastInterestDate = lastInterestDate;
        this.currentDate = currentDate;
        this.days = days;
        this.interestAmount = interestAmount;
        this.accountType = accountType;
    }

    // 이자 정보를 문자열로 포맷팅
    public String formatInfo() {
        return String.format(
            "계좌: %s | 원금: %,.0f원 | 이자율: %.1f%% | 경과일: %d일 | 이자: %,.0f원",
            accountId, principal, interestRate * 100, days, interestAmount
        );
    }
}

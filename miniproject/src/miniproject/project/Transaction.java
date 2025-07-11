package miniproject.project;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat; 
import java.util.Date;

public class Transaction {
    // 거래 정보를 저장하는 변수들
    private int transactionId;  // 거래의 고유 식별번호
    private String accountId;   // 거래가 발생한 계좌번호
    private String type;        // 거래유형 (입금, 출금, 이체 등)
    private double amount;      // 거래금액
    private String detail;      // 거래상세내용
    private Date timestamp;     // 거래발생날짜와 시간

    // 기본 생성자 (아무것도 받지 않는 생성자)
    public Transaction() {
    }

    // 모든 정보를 받는 생성자
    public Transaction(int transactionId, String accountId, String type, 
                      double amount, String detail, Date timestamp) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    // 거래내역을 만들 때 사용하는 생성자 (ID와 시간은 자동으로 설정)
    public Transaction(String accountId, String type, double amount, String detail) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.detail = detail;
        this.timestamp = new Date(); // 현재 시간으로 설정
    }

    // 각 변수에 대한 getter와 setter 메소드들
    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // 거래내역을 보기 좋게 출력하는 메소드
    @Override
    public String toString() {
        // 숫자에 천의 자리 콤마를 찍어주는 포맷터
        DecimalFormat amountFormatter = new DecimalFormat("#,###");
        // 날짜를 "2024-01-01 15:30:00" 형식으로 바꿔주는 포맷터
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        return "[" + dateFormatter.format(timestamp) + "] " + 
               type + " | " + 
               amountFormatter.format(amount) + "원 | " + 
               detail;
    }
}
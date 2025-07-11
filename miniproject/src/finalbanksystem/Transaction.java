package finalbanksystem;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat; 
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; 

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class Transaction {
    private int transactionId;  //거래의 교유 식별번호
    private String accountId;   //거래가 발생한 계좌번호
    private String type;        //거래유형
    private double amount;      //거래금액
    private String detail;      //거래상세내용
    private Date timestamp;     //거래발생날짜와 시간

    public Transaction(String accountId, String type, double amount, String detail) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.detail = detail;
        this.timestamp = new Date(); // 내부적으로 초기화
    }

    @Override
    public String toString() {
        DecimalFormat amountFormatter = new DecimalFormat("#,###");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        return "[" + dateFormatter.format(timestamp) + "] " + 
               type + " | " + 
               amountFormatter.format(amount) + "원 | " + 
               detail;
    }
}
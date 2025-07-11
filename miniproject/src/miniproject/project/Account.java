package miniproject.project;

import java.util.Date;

public class Account {
    // 계좌 정보를 저장하는 변수들
    private String id;                // 계좌번호
    private String ownerId;           // 계좌 소유자의 사용자 ID
    private String accountHolderName; // 계좌주의 실제 이름
    private String accountType;       // 계좌의 종류 (자유입출금통장, 적금통장, 정기예금)
    private String accountPassword;   // 계좌 비밀번호
    private double balance;           // 현재 계좌 잔액
    private String accountAlias;      // 계좌 별명
    private Date createdDate;         // 계좌 개설날짜와 시간
    private double interestRate;      // 연간 이자율
    private Date lastInterestDate;    // 마지막으로 이자를 지급받은 날짜

    // 기본 생성자 (아무것도 받지 않는 생성자)
    public Account() {
    }

    // 계좌를 만들 때 필요한 정보를 받는 생성자
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
        this.createdDate = new Date();      // 현재 시간으로 설정
        this.lastInterestDate = new Date(); // 현재 시간으로 설정
    }

    // 입력받은 비밀번호가 맞는지 확인하는 메소드
    public boolean checkPassword(String password) {
        return this.accountPassword.equals(password);
    }

    // 월 이자 금액을 계산하는 메소드
    public double calculateMonthlyInterest() {
        double monthlyRate = interestRate / 100.0 / 12.0;
        return balance * monthlyRate;
    }

    // ========== 추가된 유틸리티 메소드들 ==========
    
    /**
     * 계좌 비밀번호 유효성 검사 (4자리 숫자)
     */
    public static boolean isValidPassword(String password) {
        return password != null && 
               password.length() == 4 && 
               password.matches("\\d{4}");
    }
    
    /**
     * 출금 가능 여부 확인
     */
    public boolean canWithdraw(double amount) {
        return amount > 0 && balance >= amount;
    }
    
    /**
     * 계좌 타입별 이자율 반환
     */
    public static double getInterestRateByType(String accountType) {
        switch (accountType) {
            case "자유입출금통장": return 0.1;
            case "적금통장": return 2.5;
            case "정기예금": return 3.0;
            default: return 0.0;
        }
    }

    // 각 변수에 대한 getter와 setter 메소드들
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getAccountAlias() {
        return accountAlias;
    }

    public void setAccountAlias(String accountAlias) {
        this.accountAlias = accountAlias;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public Date getLastInterestDate() {
        return lastInterestDate;
    }

    public void setLastInterestDate(Date lastInterestDate) {
        this.lastInterestDate = lastInterestDate;
    }

    // 객체의 모든 정보를 문자열로 보여주는 메소드
    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", accountAlias='" + accountAlias + '\'' +
                ", createdDate=" + createdDate +
                ", interestRate=" + interestRate +
                '}';
    }
}
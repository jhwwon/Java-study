package banksystem.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import banksystem.entity.InterestInfo;

public class InterestCalculator {
    
    // 계좌 종류별 연이자율 상수
    public static final double SAVINGS_RATE = 0.001;     // 보통예금 0.1%
    public static final double FIXED_RATE = 0.015;       // 정기예금 1.5%
    public static final double INSTALLMENT_RATE = 0.020; // 적금 2.0%
    
    // 계좌 종류에 따른 이자율 반환
    public static double getInterestRateByType(String accountType) {
        return switch (accountType) {
            case "보통예금" -> SAVINGS_RATE;
            case "정기예금" -> FIXED_RATE;
            case "적금" -> INSTALLMENT_RATE;
            default -> 0.0;
        };
    }
    
    // 두 날짜 사이의 일수 계산
    public static long calculateDaysBetween(Date startDate, Date endDate) {
        LocalDate start = new java.sql.Date(startDate.getTime()).toLocalDate();
        LocalDate end = new java.sql.Date(endDate.getTime()).toLocalDate();
        return ChronoUnit.DAYS.between(start, end);
    }
    
    // 이자 계산 (일괄 계산)
    // 공식: (원금 × 연이자율 × 경과일수) ÷ 365
    public static double calculateInterest(double principal, double annualRate, long days) {
        if (principal <= 0 || annualRate <= 0 || days <= 0) {
            return 0.0;
        }
        
        double interest = (principal * annualRate * days) / 365.0;
        
        // 원 단위 이하 반올림
        return Math.round(interest);
    }
    
    // 특정 계좌의 이자 계산 (DB 조회 포함)
    public static InterestInfo calculateAccountInterest(Connection conn, String accountId) {
        String sql = "SELECT balance, interest_rate, last_interest_date, account_type " +
                    "FROM accounts WHERE account_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    double interestRate = rs.getDouble("interest_rate");
                    Date lastInterestDate = rs.getDate("last_interest_date");
                    String accountType = rs.getString("account_type");
                    
                    // 현재 날짜
                    Date currentDate = new Date();
                    
                    // 경과 일수 계산
                    long days = calculateDaysBetween(lastInterestDate, currentDate);
                    
                    // 이자 계산 (최소 1일 이상일 때만)
                    if (days >= 1) {
                        double interestAmount = calculateInterest(balance, interestRate, days);
                        
                        return new InterestInfo(
                            accountId,
                            balance,
                            interestRate,
                            lastInterestDate,
                            currentDate,
                            days,
                            interestAmount,
                            accountType
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("이자 계산 오류: " + e.getMessage());
        }
        
        return null; // 이자 지급 대상이 아님
    }
    
    // 이자율을 퍼센트 문자열로 반환
    public static String formatInterestRate(double rate) {
        return String.format("%.1f%%", rate * 100);
    }
    
    // 금액을 통화 형식으로 포맷팅
    public static String formatCurrency(double amount) {
        return String.format("%,.0f원", amount);
    }
}
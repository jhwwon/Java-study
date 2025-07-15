package banksystem.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class BankUtils {
    private static DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 금액을 통화 형식으로 포맷팅
    public static String formatCurrency(double amount) {
        return currencyFormat.format(amount) + "원";
    }

    // 날짜 포맷팅
    public static String formatDate(java.util.Date date) {
        return dateFormat.format(date);
    }

    // 새로운 계좌번호 생성 (기본 형식은 110-234-000000에서 1씩 증가)
    public static String generateAccountNumber(Connection conn) {
        String sql = "SELECT '110-234-' || LPAD(SEQ_ACCOUNT.NEXTVAL, 6, '0') FROM DUAL";
        try (PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next())
                return rs.getString(1);
        } catch (SQLException e) {
            System.out.println("❌ 계좌번호 생성 오류: " + e.getMessage());
        }
        return null;
    }

    // 새로운 거래번호 생성 (기본 형식은 T00000000에서 1씩 증가)
    public static String generateTransactionId(Connection conn) {
        String sql = "SELECT 'T' || LPAD(SEQ_TRANSACTION.NEXTVAL, 8, '0') FROM DUAL";
        try (PreparedStatement pstmt = conn.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next())
                return rs.getString(1);
        } catch (SQLException e) {
            System.out.println("❌ 거래번호 생성 오류: " + e.getMessage());
        }
        return null;
    }

    // 거래 유형에 따른 상대방 정보 표시 형식 결정
    public static String getCounterpartDisplay(String transactionType, String counterpartName, 
                                             String depositorName, String counterpartAccount) {
        switch (transactionType) {
            case "이체입금":
                return counterpartName != null ? "보낸사람: " + counterpartName
                        : counterpartAccount != null ? "보낸계좌: " + counterpartAccount : "-";
            case "이체출금":
                return counterpartName != null ? "받는사람: " + counterpartName
                        : counterpartAccount != null ? "받는계좌: " + counterpartAccount : "-";
            case "입금":
                return depositorName != null ? "입금자: " + depositorName : "-";
            default:
                return "-";
        }
    }
}
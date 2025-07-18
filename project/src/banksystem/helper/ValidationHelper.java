package banksystem.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ValidationHelper {
    private Connection conn;

    public ValidationHelper(Connection conn) {
        this.conn = conn;
    }

    // 사용자 ID 유효성 검사
    public boolean validateUserId(String userId) {
        if (userId.length() < 4 || userId.length() > 8) {
            System.out.println("아이디는 4~8자리여야 합니다.");
            return false;
        }
        // 대소문자 구분없는 영문자 최소 1개이상 포함, 숫자 최소 1개 이상 포함  ---> 영문자(대소문자)+숫자 조합 (비밀번호도 동일)
        if (!userId.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$")) {
            System.out.println("아이디는 영문과 숫자가 모두 포함되어야 합니다.");
            return false;
        }
        return true;
    }

    // 사용자 이름 유효성 검사
    public boolean validateUserName(String userName) {
        if (userName.length() > 20) {
            System.out.println("이름은 20자리까지만 가능합니다.");
            return false;
        }
        return true;
    }

    // 사용자 비밀번호 유효성 검사
    public boolean validateUserPassword(String password, String userId) {
        if (password.length() < 7 || password.length() > 12) {
            System.out.println("비밀번호는 7~12자리여야 합니다.");
            return false;
        }
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$")) {
            System.out.println("비밀번호는 영문과 숫자가 모두 포함되어야 합니다.");
            return false;
        }
        if (password.equals(userId)) {
            System.out.println("비밀번호는 아이디와 같을 수 없습니다.");
            return false;
        }
        return true;
    }

    // 이메일 유효성 검사
    public boolean validateEmail(String email) {
        if (email.length() > 100) {
            System.out.println("이메일은 100자리까지만 가능합니다.");
            return false;
        }
        //기본적인 이메일 검증
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            System.out.println("올바른 이메일 형식이 아닙니다. (예: test@example.com)");
            return false;
        }
        String[] commonDomains = { ".com", ".net", ".org", ".edu", ".gov", ".co.kr", ".kr" };
        for (String domain : commonDomains) {
            if (email.toLowerCase().endsWith(domain))
                return true;
        }
        System.out.println("일반적인 도메인을 사용해주세요. (.com, .net, .org, .kr 등)");
        return false;
    }

    // 전화번호 유효성 검사
    public boolean validatePhone(String phone) {
        if (!phone.matches("^010-\\d{4}-\\d{4}$")) {
            System.out.println("전화번호는 010-0000-0000 형식으로 입력해주세요.");
            return false;
        }
        String middlePart = phone.substring(4, 8);
        if (Integer.parseInt(middlePart) < 1000) {
            System.out.println("유효하지 않은 전화번호입니다. (010-1000-0000 이상이어야 합니다)");
            return false;
        }
        return true;
    }

    // 계좌 비밀번호 유효성 검사
    public boolean validateAccountPassword(String password) {
        if (password.length() != 4) {
            System.out.println("계좌 비밀번호는 4자리여야 합니다.");
            return false;
        }
        if (!password.matches("^[0-9]+$")) {
            System.out.println("계좌 비밀번호는 숫자만 입력 가능합니다.");
            return false;
        }
        return true;
    }

    // 사용자 ID 중복 확인
    public boolean checkUserIdDuplicate(String userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("이미 존재하는 아이디입니다.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("아이디 중복 확인 오류: " + e.getMessage());
            return false;
        }
        return true;
    }

    // 이메일 중복 확인 (수정 시 본인 제외)
    public boolean checkEmailDuplicate(String email, String currentUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_email = ? AND user_id != ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, currentUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("이미 사용 중인 이메일입니다.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("이메일 중복 확인 오류: " + e.getMessage());
            return false;
        }
        return true;
    }

    // 전화번호 중복 확인 (수정 시 본인 제외)
    public boolean checkPhoneDuplicate(String phone, String currentUserId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_phone = ? AND user_id != ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setString(2, currentUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("이미 사용 중인 전화번호입니다.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("전화번호 중복 확인 오류: " + e.getMessage());
            return false;
        }
        return true;
    }
}
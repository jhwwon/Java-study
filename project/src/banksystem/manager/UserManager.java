package banksystem.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;

import banksystem.entity.User;
import banksystem.helper.InputHelper;
import banksystem.helper.ValidationHelper;

public class UserManager {
    private Connection conn;
    private ValidationHelper validator;
    private InputHelper inputHelper;
    private Scanner scanner;

    public UserManager(Connection conn, ValidationHelper validator, InputHelper inputHelper, Scanner scanner) {
        this.conn = conn;
        this.validator = validator;
        this.inputHelper = inputHelper;
        this.scanner = scanner;
    }

    // 사용자 ID로 사용자 이름 조회
    public String getUserName(String userId) {
        String sql = "SELECT user_name FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("user_name");
            }
        } catch (SQLException e) {
            System.out.println("❌ 사용자 이름 조회 오류: " + e.getMessage());
        }
        return userId;
    }

    // 사용자 존재 여부 확인
    public boolean checkUserExists(String userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("❌ 사용자 조회 오류: " + e.getMessage());
            return false;
        }
    }

    // 사용자 비밀번호 확인
    public boolean checkUserPassword(String userId, String password) {
        String sql = "SELECT user_password FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return password.equals(rs.getString("user_password"));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 비밀번호 확인 오류: " + e.getMessage());
        }
        return false;
    }

    // User 객체를 DB에 저장
    public boolean saveUser(User user) {
        String sql = "INSERT INTO users (user_id, user_name, user_password, user_email, user_phone, join_date) "
                + "VALUES (?, ?, ?, ?, ?, SYSDATE)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUserName());
            pstmt.setString(3, user.getUserPassword());
            pstmt.setString(4, user.getUserEmail());
            pstmt.setString(5, user.getUserPhone());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ 회원가입 오류: " + e.getMessage());
            return false;
        }
    }

    // User ID로 User 객체 조회
    public User getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setUserName(rs.getString("user_name"));
                    user.setUserPassword(rs.getString("user_password"));
                    user.setUserEmail(rs.getString("user_email"));
                    user.setUserPhone(rs.getString("user_phone"));
                    user.setJoinDate(rs.getDate("join_date"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ 사용자 조회 오류: " + e.getMessage());
        }
        return null;
    }

    // User 객체 업데이트
    public boolean updateUser(User user) {
        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        boolean hasChanges = false;

        // 동적 SQL 생성
        if (user.getUserPassword() != null) {
            sql.append("user_password = ?");
            hasChanges = true;
        }

        if (user.getUserEmail() != null) {
            if (hasChanges)
                sql.append(", ");
            sql.append("user_email = ?");
            hasChanges = true;
        }

        if (user.getUserPhone() != null) {
            if (hasChanges)
                sql.append(", ");
            sql.append("user_phone = ?");
            hasChanges = true;
        }

        if (!hasChanges) {
            System.out.println("ℹ️ 변경된 항목이 없습니다.");
            return false;
        }

        sql.append(" WHERE user_id = ?");

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (user.getUserPassword() != null) {
                pstmt.setString(paramIndex++, user.getUserPassword());
            }
            if (user.getUserEmail() != null) {
                pstmt.setString(paramIndex++, user.getUserEmail());
            }
            if (user.getUserPhone() != null) {
                pstmt.setString(paramIndex++, user.getUserPhone());
            }
            pstmt.setString(paramIndex, user.getUserId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ 회원정보 수정 오류: " + e.getMessage());
            return false;
        }
    }

    // 현재 사용자 비밀번호 확인
    public boolean verifyCurrentPassword(String loginId) {
        System.out.print("현재 비밀번호를 입력하세요: ");
        String password = scanner.nextLine();

        User currentUser = getUserById(loginId);
        if (currentUser != null && password.equals(currentUser.getUserPassword())) {
            return true;
        } else {
            System.out.println("❌ 현재 비밀번호가 일치하지 않습니다.");
            return false;
        }
    }

    // 현재 회원정보 조회 및 출력
    public void displayCurrentUserInfo(String loginId) {
        User currentUser = getUserById(loginId);
        if (currentUser != null) {
            System.out.println("\n[현재 회원정보]");
            System.out.println("─────────────────────────────────────");
            System.out.println("아이디: " + currentUser.getUserId());
            System.out.println("이름: " + currentUser.getUserName());
            System.out.println("이메일: " + currentUser.getUserEmail());
            System.out.println("전화번호: " + currentUser.getUserPhone());
            System.out.println("가입일: " + currentUser.getJoinDate());
            System.out.println("─────────────────────────────────────");
        }
    }

    // 변경사항 미리보기
    public void displayChangePreview(String loginId, String newPassword, String newEmail, String newPhone) {
        // 현재 사용자 정보 조회
        User currentUser = getUserById(loginId);

        System.out.println("\n[변경사항 미리보기]");
        System.out.println("─────────────────────────────────────");

        if (newPassword != null) {
            System.out.println("비밀번호: 변경됨 (새로운 비밀번호로 설정)");
        } else {
            System.out.println("비밀번호: 기존 비밀번호 (기존 비밀번호 유지)");
        }

        if (newEmail != null) {
            System.out.println("이메일: " + newEmail + " (변경됨)");
        } else {
            System.out.println("이메일: " + (currentUser != null ? currentUser.getUserEmail() : "정보 없음") + " (기존 이메일 유지)");
        }

        if (newPhone != null) {
            System.out.println("전화번호: " + newPhone + " (변경됨)");
        } else {
            System.out.println("전화번호: " + (currentUser != null ? currentUser.getUserPhone() : "정보 없음") + " (기존 전화번호 유지)");
        }

        System.out.println("─────────────────────────────────────");
    }

    // 사용자 정보 일괄 업데이트
    public void updateUserInfo(String loginId, String newPassword, String newEmail, String newPhone) {
        // User 객체 생성하여 변경할 정보만 설정
        User updateUser = new User();
        updateUser.setUserId(loginId);
        updateUser.setUserPassword(newPassword);
        updateUser.setUserEmail(newEmail);
        updateUser.setUserPhone(newPhone);

        if (updateUser(updateUser)) {
            System.out.println("✅ 회원정보가 성공적으로 수정되었습니다!");

            // 변경된 항목들 안내
            if (newPassword != null)
                System.out.println("   - 비밀번호 변경 완료");
            if (newEmail != null)
                System.out.println("   - 이메일 변경 완료: " + newEmail);
            if (newPhone != null)
                System.out.println("   - 전화번호 변경 완료: " + newPhone);
        }
    }

    // 회원가입 처리
    public void join() {
        System.out.println("[회원가입]");
        String userId = inputHelper.inputUserId();
        String userName = inputHelper.inputUserName();
        String password = inputHelper.inputUserPassword(userId);
        String email = inputHelper.inputEmail();
        String phone = inputHelper.inputPhone();

        if (inputHelper.confirmAction()) {
            // User 객체 생성
            User user = new User();
            user.setUserId(userId);
            user.setUserName(userName);
            user.setUserPassword(password);
            user.setUserEmail(email);
            user.setUserPhone(phone);
            user.setJoinDate(new Date());

            if (saveUser(user)) {
                System.out.println("✅ 회원가입이 완료되었습니다!");
            }
        }
    }

    // 로그인 처리
    public String login() {
        System.out.println("[로그인]");

        // 아이디 검증
        String userId;
        do {
            userId = inputHelper.input("아이디: ");
            if (checkUserExists(userId)) {
                break;
            } else {
                System.out.println("❌ 존재하지 않는 아이디입니다. 다시 입력해주세요.");
            }
        } while (true);

        // 비밀번호 검증
        String password;
        do {
            password = inputHelper.input("비밀번호: ");
            if (checkUserPassword(userId, password)) {
                break;
            } else {
                System.out.println("❌ 비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
            }
        } while (true);

        // 최종 확인
        if (inputHelper.confirmAction()) {
            System.out.println("✅ 로그인 성공!");
            return userId;
        }
        return null;
    }

    // 통합 회원정보 수정 메소드
    public void modifyUserInfo(String loginId) {
        System.out.println("[회원정보 수정]");

        // 현재 비밀번호 확인
        if (!verifyCurrentPassword(loginId)) {
            return;
        }

        // 현재 정보 표시
        displayCurrentUserInfo(loginId);

        System.out.println("\n📝 변경하지 않을 항목은 '-'를 입력하세요. (기존 값 유지)");
        System.out.println("─────────────────────────────────────");

        // 새로운 정보 입력
        String newPassword = inputHelper.inputNewUserPassword(loginId);
        String newEmail = inputHelper.inputNewUserEmail(loginId);
        String newPhone = inputHelper.inputNewUserPhone(loginId);

        // 변경사항 미리보기
        displayChangePreview(loginId, newPassword, newEmail, newPhone);

        // 최종 확인 후 업데이트
        if (inputHelper.confirmAction()) {
            updateUserInfo(loginId, newPassword, newEmail, newPhone);
        }
    }
}
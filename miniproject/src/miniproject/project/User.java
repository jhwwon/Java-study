package miniproject.project;

import java.util.Date;

public class User {
    // 사용자 정보를 저장하는 변수들
    private String userId;    // 로그인 ID
    private String name;      // 이름
    private String password;  // 로그인 비밀번호
    private Date createdDate; // 회원가입 날짜

    // 기본 생성자 (아무것도 받지 않는 생성자)
    public User() {
    }

    // 모든 정보를 받는 생성자
    public User(String userId, String name, String password, Date createdDate) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.createdDate = createdDate;
    }

    // userId의 값을 가져오는 메소드
    public String getUserId() {
        return userId;
    }

    // userId의 값을 설정하는 메소드
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // name의 값을 가져오는 메소드
    public String getName() {
        return name;
    }

    // name의 값을 설정하는 메소드
    public void setName(String name) {
        this.name = name;
    }

    // password의 값을 가져오는 메소드
    public String getPassword() {
        return password;
    }

    // password의 값을 설정하는 메소드
    public void setPassword(String password) {
        this.password = password;
    }

    // createdDate의 값을 가져오는 메소드
    public Date getCreatedDate() {
        return createdDate;
    }

    // createdDate의 값을 설정하는 메소드
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // ========== 추가된 유틸리티 메소드들 ==========
    
    /**
     * 사용자 ID 유효성 검사 (3-20자)
     */
    public static boolean isValidUserId(String userId) {
        return userId != null && 
               userId.trim().length() >= 3 && 
               userId.trim().length() <= 20;
    }
    
    /**
     * 이름 유효성 검사
     */
    public static boolean isValidName(String name) {
        return name != null && 
               name.trim().length() > 0 && 
               name.trim().length() <= 20;
    }

    // 객체의 모든 정보를 문자열로 보여주는 메소드
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
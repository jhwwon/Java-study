package banksystem.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private String adminId;       // 관리자 아이디
    private String adminName;     // 관리자 이름
    private String adminPassword; // 관리자 비밀번호
    
    // 관리자 로그인용 생성자 (비밀번호 제외)
    public Admin(String adminId, String adminName) {
        this.adminId = adminId;
        this.adminName = adminName;
    }
}
package miniproject.project;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor; // 기본 생성자 자동 생성
import lombok.AllArgsConstructor; // 모든 필드를 포함하는 생성자 자동 생성

//사용자 클래스 (DB 연동용)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
 private String userId;
 private String name;
 private String password;
 private Date createdDate;
}
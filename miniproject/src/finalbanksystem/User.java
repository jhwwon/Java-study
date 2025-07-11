package finalbanksystem;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor; // 기본 생성자 자동 생성
import lombok.AllArgsConstructor; // 모든 필드를 포함하는 생성자 자동 생성

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
 private String userId;    //로그인 ID
 private String name;      //이름
 private String password;  //로그인 비밀번호
 private Date createdDate; //회원가입 날짜와 시간(계정 생성 시점을 기록)
}
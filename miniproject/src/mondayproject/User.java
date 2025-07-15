package mondayproject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class User {
    private String userId;       //아이디
    private String userName;	 //이름
    private String userPassword; //비밀번호
    private String userEmail;	 //이메일
    private String userPhone;	 //전화번호
    private Date joinDate;		 //회원가입일
}
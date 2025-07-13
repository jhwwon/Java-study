package banksystem;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private String userId;
    private String userName;
    private String userPassword;
    private String userEmail;  
    private String userPhone;
    private Date joinDate;

    public User() {}

    public User(String userId, String userName, String userPassword, String userEmail, String userPhone) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userEmail = userEmail;    
        this.userPhone = userPhone;
        this.joinDate = new Date();
    }
}
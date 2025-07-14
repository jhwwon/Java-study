package mondayproject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class User {
    private String userId;
    private String userName;
    private String userPassword;
    private String userEmail;
    private String userPhone;
    private Date joinDate;
}
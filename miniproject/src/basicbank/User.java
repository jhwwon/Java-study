package basicbank;

// 사용자 정보를 저장하는 클래스
public class User {
	private String userId;
	private String userName;
	private String userPassword;
	
	public User() {}
	
	public User(String userId, String userName, String userPassword) {
		this.userId = userId;
		this.userName = userName;
		this.userPassword = userPassword;
	}
	
	public String getUserId() {return userId;}
	public void setUserId(String userId) {this.userId = userId;}
	
	public String getUserName() {return userName;}
	public void setUserName(String userName) {this.userName = userName;}
	
	public String getUserPassword() {return userPassword;}
	public void setUserPassword(String userPassword) {this.userPassword = userPassword;}
}

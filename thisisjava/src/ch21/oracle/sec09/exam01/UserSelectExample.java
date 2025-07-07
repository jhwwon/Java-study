package ch21.oracle.sec09.exam01;

import java.sql.*;

public class UserSelectExample {
	public static void main(String[] args) {
		Connection conn = null;
		try {
			//JDBC Driver 등록
			Class.forName("oracle.jdbc.OracleDriver");
			//연결하기
			conn = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521/orcl", // oracle 접속정보 
				"jhw1",		// oracle 본인계정 이름 
				"1234"			// oracle 본인계정 암호
			);
						
			System.out.println("연결 성공");
			
			// SELECT DB에 있는 테이블 데이터 조회하기(1개의 행 가져오기)
			//매개변수화된 SQL문 작성
			String sql = "" +
				"SELECT userid, username, userpassword, userage, useremail " +
				"FROM users " +
				"WHERE userid = ?"
				;
			//PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "test2");
			
			// select문 실행
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) { // cursor 포인트가 1행 이동
				User user = new User();
				user.setUserId(rs.getString("userid"));
				user.setUserName(rs.getString("username"));
				user.setUserPassword(rs.getString("userpassword"));
//				user.setUserAge(rs.getInt("userage"));
//				user.setUserEmail(rs.getString("useremail"));
				user.setUserAge(rs.getInt(4));		// 컬럼 순번을 이용
				user.setUserEmail(rs.getString(5));	// 컬럼 순번을 이용
				
				System.out.println(user);
			} else {
				System.out.println("사용자 test1 아이디가 없습니다");
			}
			
			rs.close();
			pstmt.close();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(conn != null) {
				try { 
					//연결 끊기
					conn.close(); 
					System.out.println("연결 끊기");
				} catch (SQLException e) {}
			}
		}

	}
}

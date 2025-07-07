package ch21.oracle.sec06;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;

public class UserInsertExample {
	public static void main(String[] args) {
		Connection conn = null;
		try {
			//JDBC Driver 등록
			Class.forName("oracle.jdbc.OracleDriver");
			//연결하기
			conn = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521/orcl", // oracle 접속정보 
				"jhw1",		    // oracle 본인계정 이름 
				"1234"			// oracle 본인계정 암호
			);
			System.out.println("연결 성공");
			
			// 데이터 저장(INSERT SQL문)
			// 매개변수화된 SQL문 작성
			String sql = "" +
				"INSERT INTO users (userid, username, userpassword, userage, useremail) " +
				"VALUES (?, ?, ?, ?, ?)";
			
			//PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "winter2");
			pstmt.setString(2, "한겨울");
			pstmt.setString(3, "12345");
			pstmt.setInt(4, 25);
			pstmt.setString(5, "winter@mycompany.com");
			
			// sql문 실행
			int rows = pstmt.executeUpdate(); 
			System.out.println("저장된 행 수: " + rows);
			
			//PreparedStatement 닫기
			pstmt.close();
			
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(SQLException e) {
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

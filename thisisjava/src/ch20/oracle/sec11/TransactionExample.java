package ch20.oracle.sec11;

import java.sql.*;

public class TransactionExample {

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
			// 트랜잭션 활성화
			conn.setAutoCommit(false);  
			
			// 출금작업
			String sql1 = "UPDATE accounts SET balance = balance - ? WHERE ano = ?";
			PreparedStatement pstmt1 = conn.prepareStatement(sql1);
			pstmt1.setInt(1, 10000);
			pstmt1.setString(2, "111-111-1111");
			int rows1 = pstmt1.executeUpdate();
			if(rows1 == 0) throw new Exception("출금되지 않았음");  // row값이 0이면 update가 되지 않음
			pstmt1.close();
			
			// 입금작업
			String sql2 = "UPDATE accounts SET balance = balance + ? WHERE ano = ?";
			PreparedStatement pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setInt(1, 10000);
			pstmt2.setString(2, "333-333-3333");
			int rows2 = pstmt2.executeUpdate();
			if(rows2 == 0) throw new Exception("입금되지 않았음");  // row값이 0이면 update가 되지 않음
			pstmt2.close();
			
			conn.commit(); // 수동커밋 -> 모두 성공처리
			System.out.println("계좌 이체 성공");
		} catch(Exception e) {
			try {
				conn.rollback();
			} catch(SQLException se) {}
			
			System.out.println("계좌 이체 실패");
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

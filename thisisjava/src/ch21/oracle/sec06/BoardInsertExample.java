package ch21.oracle.sec06;

import java.sql.*;
import java.io.*;

public class BoardInsertExample {

	public static void main(String[] args) {
		Connection conn = null;
		try {
			//JDBC Driver 등록
			Class.forName("oracle.jdbc.OracleDriver");
			//연결하기
			conn = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521/orcl", // oracle 접속정보 
				"raymon1",		// oracle 본인계정 이름 
				"1234"			// oracle 본인계정 암호
			);
			System.out.println("연결 성공");
			
			conn.setAutoCommit(false);
			
			//매개변수화된 SQL문 작성
			String sql = "" +
				"INSERT INTO boards (bno, btitle, bcontent, bwriter, bdate, bfilename, bfiledata) " +
				"VALUES (SEQ_BNO.NEXTVAL, ?, ?, ?, SYSDATE, ?, ?)";
			//PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql, new String[] {"bno"});
			pstmt.setString(1, "눈오는 날");		 // varchar2
			pstmt.setString(2, "함박눈이 내려요.");  // clob
			pstmt.setString(3, "winter");		 // varchar2
			pstmt.setString(4, "snow.jpg");		 // varchar2
			pstmt.setBlob(5, new FileInputStream("src/ch21/oracle/sec06/snow.jpg"));
			
			//SQL문 실행
			int rows = pstmt.executeUpdate();
			//System.out.println("저장된 행 수: " + rows);
			
			// 앞에서 insert했던 sequence의 bno 값 얻기
			if(rows == 1) {	// insert가 1개 행이 되었을 때
				ResultSet rs = pstmt.getGeneratedKeys(); // seq_bno.nextval의 값을 조회
				if(rs.next()) { // 다음 행으로 이동
					int bno = rs.getInt(1);  // 첫번째 컬럼을 조회
					System.out.println("저장된 bno: " + bno);
				}
			}
			
			conn.commit();
			
			pstmt.close();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			
			try {
				conn.rollback();
			} catch(SQLException se) {
				
			}
		} catch(SQLException e) {
			e.printStackTrace();
			
			try {
				conn.rollback();
			} catch(SQLException se) {
				
			}
		} catch(Exception e) {
			e.printStackTrace();
			
			try {
				conn.rollback();
			} catch(SQLException se) {
				
			}
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

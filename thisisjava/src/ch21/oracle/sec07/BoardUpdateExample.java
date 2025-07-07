package ch21.oracle.sec07;

import java.sql.*;
import java.io.*;

public class BoardUpdateExample {

	public static void main(String[] args) {
		Connection conn = null;
		try {
			// JDBC Driver 등록
			Class.forName("oracle.jdbc.OracleDriver");
			// 연결하기
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/orcl", // oracle 접속정보
					"jhw1", // oracle 본인계정 이름
					"1234" // oracle 본인계정 암호
			);

			System.out.println("연결 성공");

			// Update문 실행
			// 매개변수화된 SQL문 작성
			String sql = new StringBuilder().append("UPDATE boards SET ").append("  btitle = ?, ") // 1
					.append("  bcontent = ?, ") // 2
					.append("  bfilename = ?, ") // 3
					.append("  bfiledata = ? ") // 4
					.append("WHERE bno = ?") // 5
					.toString();
			// PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "봄사람");
			pstmt.setString(2, "봄-사람");
			pstmt.setString(3, "spring.jpg");
			pstmt.setBlob(4, new FileInputStream("src/ch21/oracle/sec06/spring.jpg"));
			pstmt.setInt(5, 6); // boards 테이블에 있는 게시물 번호(bno) 지정

			// sql문 실행
			int rows = pstmt.executeUpdate();
			System.out.println("수정된 행 수: " + rows);
			
			pstmt.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					// 연결 끊기
					conn.close();
					System.out.println("연결 끊기");
				} catch (SQLException e) {
				}
			}
		}

	}

}

package ch20.oracle.sec09.exam02;

import java.sql.*;
import java.io.*;

public class BoardSelectExample {

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
			
			//매개변수화된 SQL문 작성
			String sql = "" +
				"SELECT bno, btitle, bcontent, bwriter, bdate, bfilename, bfiledata " +
				"FROM boards " +
				"WHERE bwriter = ?";
			//PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "winter");
			
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				Board board = new Board();
				board.setBno(rs.getInt("bno"));
				board.setBtitle(rs.getString("btitle"));
				board.setBcontent(rs.getString("bcontent"));
				board.setBwriter(rs.getString("bwriter"));
				board.setBdate(rs.getDate("bdate"));
				board.setBfilename(rs.getString("bfilename"));
				board.setBfiledata(rs.getBlob("bfiledata"));
				
				System.out.println(board); // 콘솔에 rs.next로 이동된 cursor행의 값을 출력
				
				// blob의 바이너리 파일을 다른 폴더에 저장
				Blob blob = board.getBfiledata();
				if(blob != null) {
					InputStream is = blob.getBinaryStream();
					OutputStream os = new FileOutputStream("C:/Temp/" + board.getBfilename());
					is.transferTo(os);
					os.flush();
					
					os.close();
					is.close();
				}
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

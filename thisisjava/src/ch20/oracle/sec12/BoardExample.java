package ch20.oracle.sec12;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BoardExample {
	// 사용자로부터 메뉴 혹인 게시물 정보들을 입력받기 위한 용도
	private Scanner scanner = new Scanner(System.in); 
	
	// DB 컨넥션 접속 객체 변수
	private Connection conn = null;
	
	// 기본 생성자
	public BoardExample() {
		try {
			//JDBC Driver 등록
			Class.forName("oracle.jdbc.OracleDriver");
			
			//연결하기
			conn = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521/orcl", // oracle 접속정보 
				"jhw1",		// oracle 본인계정 이름 
				"1234"			// oracle 본인계정 암호
			);
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
	}
	
	/**
	 * 게시판 리스트를 보여주는 기능
	 */
	private void list() {
		System.out.println();
		System.out.println("[게시물 목록]");
		System.out.println("-----------------------------------------------------------------------");
		System.out.printf("%-6s%-12s%-16s%-40s\n", 
							"no", 
							"writer", 
							"date", 
							"title");
		System.out.println("-----------------------------------------------------------------------");
		
		// boards 테이블에서 게시물 정보를 가져와서 출력하기
		
		try {
			//매개변수화된 SQL문 작성
			String sql = "" +
				"SELECT bno, btitle, bcontent, bwriter, bdate " +
				"FROM boards ORDER BY bno DESC";
			//PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				Board board = new Board();
				board.setBno(rs.getInt("bno"));
				board.setBtitle(rs.getString("btitle"));
				board.setBcontent(rs.getString("bcontent"));
				board.setBwriter(rs.getString("bwriter"));
				board.setBdate(rs.getDate("bdate"));

				System.out.printf("%-6s%-11s%-16s%-40s\n", 
									board.getBno(), 
									board.getBwriter(),
									board.getBdate(),
									board.getBtitle()
									);
			}
			
			rs.close();
			pstmt.close();
		} catch(SQLException e) {
			e.printStackTrace();
			exit();
		} catch(Exception e) {
			e.printStackTrace();
			exit();
		}
		
		mainMenu();		// 메인 메뉴 생성
	}
	
	/**
	 * 메인 메뉴를 생성하는 기능
	 */
	private void mainMenu() {
		System.out.println();
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("메인메뉴: 1.Create | 2.Read | 3.Clear | 4.Exit");
		System.out.print("메뉴선택: ");
		System.out.println();
		
		String menuNo = scanner.nextLine();
		switch(menuNo) {		// 메뉴 선택에 대한 메소드 실행
			case "1" -> create();
			case "2" -> read();
			case "3" -> clear();
			case "4" -> exit();
			default -> {
				System.out.println("***1번에서 4번의 숫자만 입력이 가능합니다. 다시 입력해 주세요***");
				list();
			}	
		}
	}
	
	/**
	 * 메뉴 1번의 게시판 생성 기능(create)
	 */
	private void create() {
		System.out.println("[새 게시물 입력]");
		System.out.print("제목: "); 	
		String boardTitle = scanner.nextLine();
		System.out.print("내용: ");
		String boardContent = scanner.nextLine();
		System.out.print("작성자: ");
		String boardWriter = scanner.nextLine();
		
		// 보조메뉴 출력
		if(printSubMenu().equals("1")) {	// 1. OK이면 실제 DB의 Boards테이블에 입력한 값을 등록
			try {		
				// 게시물 등록하는 sql
				String sql = "" +
						"INSERT INTO boards (bno, btitle, bcontent, bwriter, bdate) " +
						"VALUES (SEQ_BNO.NEXTVAL, ?, ?, ?, SYSDATE)";
				//PreparedStatement 얻기 및 값 지정
				PreparedStatement pstmt = conn.prepareStatement(sql, new String[] {"bno"});
				pstmt.setString(1, boardTitle);		 // varchar2
				pstmt.setString(2, boardContent);  // clob
				pstmt.setString(3, boardWriter);		 // varchar2
				//SQL문 실행
				int rows = pstmt.executeUpdate();
				
				pstmt.close();
			} catch(SQLException e) {
				e.printStackTrace();
				exit();
			} catch(Exception e) {
				e.printStackTrace();
				exit();
			}
		}
		
		// 게시물 목록 출력
		list();
	}
	/**
	 * 메뉴 2번의 게시판 상세조회 기능(read)
	 */
	private void read() {
		System.out.println("[게시물 읽기]");
		System.out.print("bno: "); 	
		int bno = Integer.parseInt(scanner.nextLine());
		
		// boards테이블에서 bno에 맞는 게시물을 가져와서 출력
		String sql = "" +
				"SELECT bno, btitle, bcontent, bwriter, bdate " +
				"FROM boards WHERE bno = ?";
		try {
			//PreparedStatement 얻기 및 값 지정
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bno);
			
			// select문 실행
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) { // cursor 포인트가 1행 이동
				System.out.println("##############");
				System.out.println("번호: " + rs.getInt("bno"));
				System.out.println("제목: " + rs.getString("btitle"));
				System.out.println("내용: " + rs.getString("bcontent"));
				System.out.println("작성자: " + rs.getString("bwriter"));
				System.out.println("날짜: " + rs.getDate("bdate"));
				System.out.println("----------------------------------");
				
				System.out.println("보조메뉴: 1.Update | 2.Delete | 3.List");
				System.out.print("메뉴선택: ");
				String menuNo = scanner.nextLine();
				
				if(menuNo.equals("1")) {
					update(bno);
				} else if(menuNo.equals("2")) {
					delete(bno);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			exit();
		}
		
		list();
	}
	
	/**
	 * 보조메뉴 출력
	 * @return
	 */
	private String printSubMenu() {
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("보조메뉴: 1.Ok | 2.Cancel");
		System.out.print("메뉴선택: ");
		return scanner.nextLine();
	}
	
	/**
	 * 특정 게시글 수정 기능
	 */
	private void update(int bno) {
		//수정 내용 입력 받기
		System.out.println("[수정 내용 입력]");
		System.out.print("제목: "); 	
		String bTitle = scanner.nextLine();
		System.out.print("내용: "); 	
		String bContent = scanner.nextLine();
		System.out.print("작성자: "); 	
		String bWriter = scanner.nextLine();
		
		// 보조메뉴 출력
		if(printSubMenu().equals("1")) {
			try {
				// boards 테이블에서 게시물 정보 수정
				String sql = new StringBuilder()
						.append("UPDATE           ")
						.append("  boards         ")
						.append("SET              ")		
						.append("  btitle = ?,    ")	// 1
						.append("  bcontent = ?,  ")	// 2
						.append("  bwriter = ?    ")	// 3
						.append("WHERE            ")	 
						.append("  bno = ?        ")	// 4
						.toString();
				//PreparedStatement 얻기 및 값 지정
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, bTitle);
				pstmt.setString(2, bContent);
				pstmt.setString(3, bWriter);
				pstmt.setInt(4, bno);
				
				//SQL문 실행
				int rows = pstmt.executeUpdate();
				pstmt.close();
			} catch(Exception e) {
				e.printStackTrace();
				exit();
			}
		}
	}
	/**
	 * 특정 게시글 삭제 기능
	 */
	private void delete(int bno) {
		//boards 테이블에 게시물 정보 삭제
		try {
			String sql = "DELETE FROM boards WHERE bno=?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bno);
			
			pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		
		//게시물 목록 출력		
		list();
	}
	/**
	 * 메뉴 3번의 게시판 삭제 기능(clear)
	 */
	private void clear() {
		if(printSubMenu().equals("1")) {
			//boards 테이블에 게시물 정보 전체 삭제
			try {
				String sql = "TRUNCATE TABLE boards";
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate();
				pstmt.close();
			} catch (Exception e) {
				e.printStackTrace();
				exit();
			}
		}
			
		//게시물 목록 출력
		list();
	}
	/**
	 * 메뉴 4번의 프로그램 종료(exit)
	 */
	private void exit() {
		System.out.println("게시판 프로그램이 정상적으로 종료되었습니다.");
		System.exit(0);	
	}
	
	public static void main(String[] args) {
		BoardExample boardExample = new BoardExample();
		boardExample.list();	  // 게시판 리스트 생성
		//boardExample.mainMenu();  // 메인 메뉴 생성
	}
}

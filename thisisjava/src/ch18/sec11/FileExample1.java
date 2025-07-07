package ch18.sec11;

import java.io.*;

public class FileExample1 {

	public static void main(String[] args) throws Exception {
		// C:/Temp/member-list.txt 파일을 삭제하는 코드
		
		//File 객체 생성
		File file1 = new File("C:/Temp/member-list.txt");
		// file1이 존재하는지 체크
		if(!file1.exists()) {
			System.out.println("파일이 존재하지 않습니다.");
		} else {
			boolean isDelete = file1.delete();
			if (isDelete) {
				System.out.println("member-list.txt파일이 삭제되었습니다");
			} else {
				System.out.println("member-list.txt파일이 삭제시도 중에 실패하였습니다.");
			}
		}
	}
}

package ch18.sec10;

import java.io.*;
import lombok.*;

/**
 * 객체(클래스 등)를 파일로 저장하는 예제(ObjectStream 사용)
 */
public class ObjectInputOutputStreamExample {

	public static void main(String[] args) throws Exception {
		//Member 객체 생성
		Member m1 = new Member("fall", "단풍이");
		Product p1 = new Product("노트북", 1500000);
				
		//Member 객체를 직렬화하기 위해서 FileOutputStream에 ObjectOutputStream 보조 스트림 연결
		FileOutputStream fos = new FileOutputStream("C:/Temp/object.dat"); //ObjectStream로 출력할 파일 이름 및 위치 저장
		ObjectOutputStream oos = new ObjectOutputStream(fos);	//object 관련 스트림으로 연결
//		FileOutputStream fos2 = new FileOutputStream("C:/Temp/member-list.txt"); //PrintStream로 출력할 파일 이름 및 위치 저장
//		PrintStream ps = new PrintStream(fos2);	//프린트관련 스트림 연결
		
		//Member 객체를 직렬화해서 파일에 저장
		oos.writeObject(m1);
		oos.writeObject(p1);
//		ps.println(m1);
//		ps.println(m1.getId() + "-" + m1.getName());
		
		//파일 쓰기 마무리를 하고 파일을 닫음
		oos.flush();
		oos.close();
		fos.close();
//		ps.close();
//		fos2.close();
		
		//--------------------------------------------------
		
		//Member 객체를 역직렬화하기 위해서 FileInputStream에 ObjectInputStream 보조 스트림 연결
		FileInputStream fis = new FileInputStream("C:/Temp/object.dat");
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		//Member 객체를 역직렬화해서 파일에 있는 내용 읽기
		Member m2 = (Member)ois.readObject();
		System.out.println(m2);
		System.out.println(m2.getId());
		System.out.println(m2.getName());
		System.out.println();
		
		//Product 객체를 역직렬화해서 파일에 있는 내용 읽기
		Product p2 = (Product) ois.readObject();
		System.out.println(p2);
		System.out.println(p2.getName());
		System.out.println(p2.getPrice());
	}

}
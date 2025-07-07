package ch18.sec10;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

//File 에 쓰려면(write) 직렬화가 필요
@AllArgsConstructor
@ToString
@Getter
public class Member implements Serializable {
	private static final long serialVersionUID = 5126116240738072853L; // 직렬화 혹은 역직렬화시 serialVersionUID에 유일한 값 필요
	
	private String id;
	private String name;
}

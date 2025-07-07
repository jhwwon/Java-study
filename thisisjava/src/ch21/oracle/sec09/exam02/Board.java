package ch21.oracle.sec09.exam02;

import java.util.Date;
import java.sql.Blob;
import lombok.Data;

@Data
public class Board {
	private int bno;
	private String btitle;
	private String bcontent;
	private String bwriter;
	private Date bdate;
	private String bfilename;
	private Blob bfiledata;
}

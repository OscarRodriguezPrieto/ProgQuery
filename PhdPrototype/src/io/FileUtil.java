package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

	
	public static void writeFile(String fileName,String text) throws IOException{
		BufferedWriter bw=new BufferedWriter(new FileWriter(fileName));
		bw.write(text);
		bw.close();
	}
}

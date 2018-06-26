package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

	public static void writeFile(String fileName, String text) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		bw.write(text);
		bw.close();
	}

	public static String getTextFromFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String text = "";
		while (br.ready())
			text += br.readLine() + "\n";
		br.close();
		return text;
	}
}

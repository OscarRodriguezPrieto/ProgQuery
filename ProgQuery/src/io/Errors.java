package io;

import java.io.IOException;

public class Errors {

	public static void addError(String msg) {
		try {
			FileUtil.appendFile("symbolErrors.log", msg);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Errors cannot be written in symbolErrors.log.");
		}
	}
}

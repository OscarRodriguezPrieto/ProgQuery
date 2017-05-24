package newTests;

import java.io.BufferedReader;
import java.io.Reader;


public class  OtherClass extends BufferedReader implements Interface{

	public OtherClass(Reader in) {
		super(in);
	}

	public int n;
	
	public static class InnerClass implements Interface{
		public static int n=0;
	}
}

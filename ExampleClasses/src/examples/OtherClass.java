package examples;

import java.io.BufferedReader;
import java.io.Reader;

import examples.classesDependencies.Interface;


public class  OtherClass /*extends BufferedReader*/ implements Interface{

	public OtherClass() {
	}

	public int n;
	
	public static class InnerClass implements Interface{
		public static int n=0;
	}
}

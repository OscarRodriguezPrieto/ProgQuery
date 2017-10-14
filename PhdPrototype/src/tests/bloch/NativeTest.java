package tests.bloch;

public class NativeTest {
	public native void printText ();
	
	static{
		System.loadLibrary ("happy");  
	}
	
}

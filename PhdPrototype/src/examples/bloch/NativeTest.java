package examples.bloch;

public class NativeTest {
	public native void printText ();
	
	static{
		System.loadLibrary ("happy");  
	}
	
}

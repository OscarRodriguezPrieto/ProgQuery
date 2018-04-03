package examples.bloch.experiments;

public class NativeTest {
	public native void printText ();
	
	static{
		System.loadLibrary ("happy");  
	}
	
}

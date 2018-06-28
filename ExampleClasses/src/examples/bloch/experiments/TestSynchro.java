package examples.bloch.experiments;

public class TestSynchro {

	
	
	public static synchronized void m() throws InterruptedException{
	new TestSynchro().wait();
	}
	public void m2() throws InterruptedException{
		synchronized(this){
			
		}
		wait();
	}
}

package tests.bloch;

public class MetodosHeredadosTest2 extends Thread{

	
	public void foo(){
		interrupt();
		super.interrupt();
		this.interrupt();
	}
}

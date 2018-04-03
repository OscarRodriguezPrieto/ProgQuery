package examples.bloch.experiments;

public class MetodosHeredadosTest2 extends Thread{

	
	public void foo(){
		interrupt();
		super.interrupt();
		this.interrupt();
	}
}

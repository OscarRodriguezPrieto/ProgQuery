package examples.bloch.experiments;

public class MetodosHeredados extends Thread{

	@Override
	public void interrupt(){
		System.out.println("naaaaaaa");
	}
	
	
	public void foo(){
		interrupt();
		super.interrupt();
		this.interrupt();
	}
}

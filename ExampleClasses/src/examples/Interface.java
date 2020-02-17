package examples;

public interface Interface {
	default void m () throws InterruptedException{
		this.wait();
	}
}

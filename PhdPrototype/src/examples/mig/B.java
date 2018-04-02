package examples.mig;

public class B extends A{

	public void m() {
		new A().am();
		new B();
		sm();
			
	}

	public static void sm() {
		A.asm();
		new B().m();
	}
}

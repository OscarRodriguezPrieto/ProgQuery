package examples.mig;

public class B extends A {
	@Override
	public B identity(A a) {
		return (B) a;
	}

	public void m() {
		new A().am();
		new B();
		sm();
		equals(null);
	}

	public static void sm() {
		A.asm();
		new B().m();
		new B().equals(null);
	}
}

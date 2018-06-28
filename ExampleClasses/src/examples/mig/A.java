package examples.mig;

public class A implements I {
	public A() {
		am();
		asm();
	}

	public A identity(A a) {
		return a;
	}

	public void am() {
		new B().m();
		new A();
		asm();
		identity(null);
	}

	public static void asm() {
		B.sm();
		new A().am();

	}

	// @Override
	// public boolean equals(Object a) {
	// return false;
	// }
}

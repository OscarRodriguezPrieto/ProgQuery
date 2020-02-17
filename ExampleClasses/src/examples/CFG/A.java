package examples.CFG;

import java.io.IOException;

public class A {

	public <T> A() throws IOException {


	}

	public void m() throws IOException {
		new B().mThrows();
		m();
	}

	int getI() {
		return 0;
	}

	public static void main(String[] args) {
		try {
			boolean i = false;
			i &= false;
			i |= true;
			if (args.length == 0)
				throw new RuntimeException();
		} finally {
			System.out.println("FINALLY");
		}
		System.out.println("AFTER FINALLy");
	}
}

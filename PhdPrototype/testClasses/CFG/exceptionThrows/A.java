package examples.CFG;

import java.io.IOException;

public class A {
	public A() throws IOException {

	}

	public void m() throws IOException {
		new B().mThrows();
		m();
	}
}

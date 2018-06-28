package examples.CFG;

import java.io.IOException;

public class B extends A {

	public B() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public void mThrows() throws IOException, AssertionError, NullPointerException {
		mThrows();
		new A().m();
	}
}

package examples.CFG;

import java.io.IOException;

public class B extends A {

	public B() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	boolean b() {
		return true;
	}

	public void mThrows() throws IOException, AssertionError, NullPointerException {
		if (b())
			mThrows();
		new A().m();
	}
}

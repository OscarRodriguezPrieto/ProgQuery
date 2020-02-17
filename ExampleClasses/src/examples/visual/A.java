package examples.visual;

import java.io.File;

public class A extends File {

	public A(String arg0) {
		super(arg0);
	}

	int a;

	public void m() {
		A aIns = new A("FOO");
		aIns.a = this.a;
		while (a < 10)
			this.a = this.a + 1;
	}
}

package examples.classesDependencies;

import examples.OtherClass;

public class ExtraClass implements Interface {

	static {
		ExtraClass e;
		(e = new ExtraClass()).getO();
		e.getO();
	}

	public ExtraClass() {
		ExtraClass e;
		(e = new ExtraClass()).getO();
		e.getO();
	}

	public static OtherClass getA() {
		return new ExtraClass().OTHER;
	}

	public OtherClass getO() {
		return getA();
	}

	private static class InnerClass {
		static int n = 0;
	}

	public OtherClass OTHER = new OtherClass();

}

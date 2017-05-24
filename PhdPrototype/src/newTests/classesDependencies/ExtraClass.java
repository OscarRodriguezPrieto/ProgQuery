package newTests.classesDependencies;

import newTests.OtherClass;

public class ExtraClass {

	public static OtherClass OTHER = new OtherClass(null);

	public OtherClass getO() {
		return OTHER;
	}
	
	
	private static class InnerClass {
		static int n=0;
	}
}

package examples.bloch.experiments;

import java.util.ArrayList;
import java.util.Collections;

public class TestCalls {

	public static void main(String[] args) throws Throwable {
		new HierarchyTest();
		new TestCalls();
		// System.out.println(o.toString());
		// NestedClassesTest n = (NestedClassesTest) o;

		// System.out.println(n.toString());
		// System.out.println( new HierarchyTest().toString());

		TestExceptions.m();
		
		
		Collections.emptyEnumeration();
		new TestCalls().classMethod();
		new HierarchyTest().toString();
		m2();
		new Exception();
		new ArrayList<>();
	}

	public static void m2() {

	}

	public void classMethod() {

	}
}

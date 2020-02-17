package examples.cmu.otherPackage;

import examples.cmu.A;
import examples.cmu.SubA;


public class B extends A {
	public int a = SubA.c;
	public char c = (char) 9.8;

	A getA() {
		return null;
	}
	@Override
	public Object clone() {
		Object clon;
		System.out.println((clon = a == c ? super.clone() : null).hashCode());
		test++;
		new B().test++;
		((B) getA()).test++;
		return clon;
	}

}

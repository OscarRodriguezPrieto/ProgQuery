package examples.cmu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.function.Function;


import examples.cmu.otherPackage.B;
import examples.util.EnumExample;

public class Z<X> implements Serializable, Cloneable {
	final int mm = 33 + /* SUMA */99;

	public static /* COMMENT */transient File fff;

	X id(X x) {

		new A().f = null;
		Map<String, X> m = new HashMap<String, X>();

		return m.put("key", x);
	}

	static int staticMethod(String s) {
		return A.staticMethod(s);
	}

	public static SubA getSUBA() {
		return new SubA();
	}

	public static int X() {
		return 0;
	}

	public void ordinal() {
		B b = new B();
		D d;
		EnumExample.FIRST.ordinal();
	}

	public void confusing(int i, String g) {
		EnumExample.SECOND.ordinal();
	}

	public int confusing(String g, int i) {
		return EnumExample.THIRD.ordinal();
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	MC getMC() {
		return null;
	}

	OMC getOMC() {
		return null;
	}

	public List<String> getC() {
		return null;
	}

	public List getRawC() {
		return null;
	}

	public int[] getArray() {
		if ("JOJ".contains("JK"))
			return null;
		else
			return "LMLM".length() == 3 ? null : new int[] {};

	}

	public void closeable() throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File("NJKNJK"))); // <-----
		BufferedReader		br2 = new BufferedReader(new FileReader(new File("NJKNJK")));
		br.close();
		if (8 == "HBUHB".length())
			"IHHHOIJ".toCharArray();
		Function<Object, Boolean> c = (br = null)::equals; // <------
		for (int i = 0; i < 6; i++)
			System.out.println(i);
		br2.close();
		br.close();

	}

	public void compliantCloseable() throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File("NJKNJK"))); // <----
		br.close();
		if (8 == "HBUHB".length())
			"IHHHOIJ".toCharArray();
		Function<Object, Boolean> c = (br = null)::equals;// <-----
		for (int i = 0; i < 6; i++)
			System.out.println(i);
		br.close();

	}

	public void compliantResourcesCloseable() throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(new File("NJKNJK")));
				BufferedReader br2 = new BufferedReader(new FileReader(new File("NJKNJK")))) {
			br.close();
			if (8 == "HBUHB".length())
				"IHHHOIJ".toCharArray();
			for (int i = 0; i < 6; i++)
				System.out.println(i);
			br2.close();
			br.close();
		}
	}

	public void ifTest(String p) {

		if (p == null) {
			for (;;) {//
				p = p + "OO";//
				if (a == 4) {//
					p = null;//
					System.out.println("");//
				} else
					System.out.println(p);
				System.out.println("KK");//
			}
		}
		System.out.println("LAST");//
	}

	public void difficultCompliantCloseable() throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File("NJKNJK")));
		br.close();
		BufferedReader br2 = new BufferedReader(new FileReader(new File("NJKNJK")));
		try {
			if (8 == "HBUHB".length()) {
				if (8 == "HBUHB".length()) {
					if (8 == "HBUHB".length())
						"IHHHOIJ".toCharArray();
					Function<Object, Boolean> c = (br = null)::equals;
					if (8 == "HBUHB".length())
						"IHHHOIJ".toCharArray();
					int b;
					for (int i = 0; i < 6; i++)
						System.out.println(i + "" + (br = null));
					assert (br.hashCode() == 9);
				} else {
					try {
						try {
							assert (br.hashCode() == 9);
							br2.close();
						} finally {

						}
						br2.close();
					} catch (AssertionError e) {
						try {
							throw new IllegalAccessError();
						} finally {

						}

					} finally {

					}
					br2.close();
				}
			}
		} finally {
			br2.close();
		}

	}

	public void helpingCG() {
		Object o = new Object(), o2 = new Object();
		int a = o.hashCode();
		o = null;
		System.out.println(o);
		if (89 > "NKNK".length())
			o = null;
		else
			System.out.println("ELSE");
		for (int i = 0; i < 10; i++) {
			System.out.println(o2);
			o2 = null;
		}
		try {
		} catch (Exception e) {
			o2 = null;
		} catch (Error r) {
			a++;
		}
	}

	public void vardecScope() {
		Object object = new Object();
		if ("JOJNO".contains("OJ")) {

			if (object.equals(null)) {
				object.toString();
			}
			object.toString();
		}
	}

	private int a, b, c, d, e, a2;
	private Z z, y;

	public void attrUse() {
		y.e = 3;
		System.out.println(z.e + y.e);
	}

	public void setA(int i) {
		a = i;
		new Z().d = 3;
		d = 4;
		System.out.println(new Z().d);
	}

	// public void attrAuxMethod() {
	// setA(4);
	// System.out.println(a);
	// }

	public void blocks() {
		if ("INIJO".contains(""))
			if ("JIJOJO".length() == 9)
				a = 5;
		{
			a = 3;
		}

		synchronized (z) {
			if ("".contains(""))
				System.out.println(a);
		}
	}

	public void blocksTwo(O o) {
		attrScope();
		if ("NK".length() == 8) {
			a = 3;

			if (a == 5)
				if (a == 3) {
					System.out.println(a);
				}
		}

	}

	void changes(Z z) {
		z.a = 2;
	}

	void changes(A a) {
		a.array[2] = 5;
	}

	public void apparentlyNoChanges() {
		changes(this);

	}

	public void apparentlyNoChangesBis() {
		changes(this.externallyMutableAttr);

	}

	public void attrScope() {
		this.a2 = 9;
		Z z = null;
		z.a = 0;
		this.b = 7;
		System.out.println(a2);
		System.out.println(z.a + b);
	}

	public void attrScopeBis() {
		a = 3;
		if ("NJHI".length() == 8)
			b = 4;
		System.out.println(a + b);
	}

	public void attrScopeBisBis() {
		if ("NJHI".length() == 8) {
			b = 4;
			a = 3;
			System.out.println(a + b);
		}
	}

	public void attrScopeLoop() {

		for (int i = 0; i < 50; i++) {
			a = 3;

			System.out.println(a + this.c);

			c = 4;
		}
	}

	private E eee;

	public E getE() {
		return eee;
	}

	private A externallyMutableAttr;

	public A getA() {
		return externallyMutableAttr;
	}

	public void untrustedClone(Z<String> z) throws CloneNotSupportedException {
		a2 = 9;
		Z aux = (Z) z.clone();
	}

	public static void main(String[] args) {
		try {
			throw new NullPointerException();
		} catch (NullPointerException n) {
			throw new IllegalStateException();
		} catch (IllegalStateException l) {

		}
	}
}

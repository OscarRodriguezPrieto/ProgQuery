package examples.cmu;

import java.io.File;
import java.io.Serializable;

import examples.classesDependencies.ClassUse;

public class A implements Serializable {
	protected int test;

	public static final int m = 3;
	public File f;
	int[] array;
	static int i;
	
	public void a(){
	i++;
	}
	
	public SubA s() {
		System.out.println(s);
		return s;
	}

	SubA s;

	public int[] getArray() {
		return array;
	}

	public void setArray(int index, int val) {
		array[index] = val;
	}

	static int staticMethod(String s) {
		return Z.staticMethod(s);
	}

	static int staticMethodBis(String s) {
		return A.staticMethodBis(s);
	}

	public static void main(String[] args) {
		System.out.println(new A().clone());
	}

	// @Override
	public void confusing(int i, String g) {
		synchronized (g) {

		}
		// VAR DEC
		// RETURN TYPE
		// new Class
		// Static Member access or ( mInv or Ident, static access implicit-
		// excluding the class itself)
		// Solo Z.m, o Z.A.m, nunca ().().a.m aunque m sea estático
		// Distinguir Z.a.m de Z.A.m symbol(Z.A) classSymbol vs symbol(Z.a)
		// attribute symbol
		// Class IDENT
		// System.out.println(new D().c().b + 2);
		if (this.f != null) {
			int[] array;
			array = null;
		}
		else{
			array[8]++;
			System.out.println("HELLLO");
		}
		this.f = null;
		f = null;
		int[] array;
		this.array[6] = 0;
		array = null;
		new ClassUse();
		// int a = ClassUse.getZ().fff.hashCode() + ClassUse.getZ().confusing(g,
		// i) + 2;

		switch ("MKOK".length()) {
		case 3:
			array = null;

		case 5:
			break;
		case 6:
			System.out.println(array);
		}
	}

	public Object clone() {
		return null;
	}

	public void m(A o) {

	}

	float ff = 7 / 2;

	public void convertToFloat() {
		float f = 9.0f;
		System.out.println(ff);
		float ff;
		System.out.println((ff = 7 / 5 / 8 / 2) * 0.9);
		f = f + 5 / 6;
		double d = f;
	}

private final void aaa() {
	
}
}

package examples.pdg;

public class PDG1 {
	public int i;
	private int[] array;
	static int si;

	public int[] getArray() {
		return array;
	}

	public void setI() {
	}

	public void setI(int i) {
		// method that changes the state of this
		PDG1.this.i = i;
		si = 2;
	}

	public void m(String param) {
		param = i + "";
		PDG2 pdg = new PDG2();
		// Aqui en i=.... además de que el atributo i es modifica , el estado de
		// this es modificado
		i = pdg.pdg.array[8] = pdg.pdg.i;
		System.out.println(this.i = array[pdg.pdg.i]);
		{
			int i = param.length();
			array[i] = array[this.i];
			i = this.i++;
			array[i] *= array[i]--;
		}
		pdg.pdg = this;
		while (i == 6) {
			int i = array[8] = (pdg.pdg.i * 8) + 5 ^ param.length();
			pdg.tensor[this.i = 5][2 + 3][5].i = (param = "" + (i = array[i])).length();
			array = new int[i];
		}
		pdg.m(this);
		pdg.m2(this);
		PDG1.this.i++;
		Exception[] d;
		d = new RuntimeException[5];
	}

	public static interface I {
		public default void m() {
			I.this.toString();
		}

		public void m2(PDG1 p);

	}

	// public void exx() {
	// Z a = new A();
	// a.confusing(0, "JON");
	// A aa = (A) a;
	// aa.confusing(00, "KOJO");
	// }
	// De comment to fail with Enums and A,B element treated as members of
	// classes or attributes uninitialized
	// private enum E {
	// A, B;
	// public void m() {
	// E.this.toString();
	// A.m();
	// }
	// }
}

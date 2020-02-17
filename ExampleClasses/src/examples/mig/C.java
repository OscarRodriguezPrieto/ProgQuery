package examples.mig;

public class C {
	private int i = getI();
	private int j = 2;
	private int k;

	@Override
	public String toString() {
		return "C [i=" + i + ", j=" + j + ", k=" + k + "]";
	}

	public C(int i) {
		super();
		SO();
		O();
	}

	static {
		SO();
		new C(2).O();
	}

	private static int si = getI();
	private static int sj = 2;
	private static int sk;

	private static int getI() {
		return 2;
	}

	private static void SO() {

	}

	private void O() {
	}

	private void OOOO(int nuevadec) {
	}

	public static void main(String[] a) {
	}
}

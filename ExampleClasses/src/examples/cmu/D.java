package examples.cmu;

public class D  {

	public final A a = null;
	public final int b = 2;
	public final C c = null;

	public C c() {
		new PT();
		System.out.println(new C().b + 2);
		c.confusing(2, "");
		C c = new C();
		c.d = null;
		a.array[6] = 9;
		return null;
	}
}

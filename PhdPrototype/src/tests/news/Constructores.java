package newTests;

public class Constructores {

	double b;

	public Constructores(int a) {
		b = a / 2.0;
	}

	public static Constructores newInstance(int a) {
		return new Constructores(a);
	}
}

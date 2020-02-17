package examples;

import java.io.BufferedReader;

import examples.classesDependencies.ClassUse;
import examples.classesDependencies.Interface;
import examples.cmu.SubA;
import examples.cmu.SubA2;

public class Constructores {
	SubA a;
	SubA2 a2;
	double b;

	public Constructores() {
	}

	public Constructores(Object a) {
		this.b = a.hashCode() / 2.0;
	}

	public Constructores(Integer a) {
		b = a / 2.0;
	}

	public Constructores(Interface a) {
		// b = a / 2.0;
	}

	public Constructores(BufferedReader a) {
		// b = a / 2.0;
	}

	public Constructores(int a) {
		b *= a / 2.0;

		new Interface() {

			public int a() {
				return 2;
			}
		};
	}

	public static Constructores newInstance(int a) {
		new Constructores();
		new Constructores(new BufferedReader(null));
		new Constructores();
		new Constructores("JMO");
		new Constructores(new ClassUse());

		return new Constructores(a);
	}

	public void m() {
		super.toString();
	}
}

package examples;

import java.io.BufferedReader;

import examples.classesDependencies.ClassUse;
import examples.classesDependencies.Interface;

public class Constructores {

	double b;
	
	public Constructores() {
		
	}

	public Constructores(Object a) {
		// b = a / 2.0;
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
		b = a / 2.0;

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

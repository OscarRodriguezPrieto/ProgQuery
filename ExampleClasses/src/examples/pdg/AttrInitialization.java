package examples.pdg;

public class AttrInitialization {

	private final int foo;

	public AttrInitialization() {
		// COMPILATION_ERROR
		// System.out.println(foo);
		foo = 2;
		System.out.println(foo);
	}

	public static void main(String[] args) {
		new AttrInitialization();
	}

}

package examples.CFG;

public class BreakTest {
	static final String F = "FFF";

	public static void main(final String[] args) {
		final int index = 0;
		// System.out.println("A");
		// // break;
		// System.out.println("B");
		// b: {
		// if ("B".length() == 1)
		// break b;
		// // break a sin label da error de compilación, sólo deja dentro de un
		// // loop o switch
		// System.out.println("UNREACHEABLE");
		// if (true)
		// ;
		// // ERROR COMPILACIÓN
		// // continue b;
		// }
		//
		// b: do {
		// if (true) {
		// // ERROR duplicate label b
		// // b:;
		// a: ;
		// // ERROR misssing label a
		// // break a;
		// }
		//
		// continue b;
		// } while (args[0].contains("a"));
		switch (args[0]) {
		}
		a: {
			System.out.println("BLOC K INI");
			if ("A".contains("A"))
				break a;
			System.out.println("BLOCK END");

		}

		a: if (true)
			b: {
				if ("A".contains("A"))
					break a;
				else
					break b;
			}
		s: synchronized (args) {

		}
		int i = 1;
		while (i < 20) {
			b: {

				if (i % 3 == 0)
					break b;
				System.out.println("BLOCK END" + i);

			}

			if (i % 5 == 0)
				break;
			System.out.println("LOOP END" + i);
			i++;
		}
		final String s2 = "J";
		final String s = "DDD";
		switch (s) {
		case s:
			System.out.println("A");
		case "B": {
		}
			System.out.println("B");
			// while (true)
			// if ("A".contains("A"))
			// break;
			// else
			// break sw;
		case "D":
			switch (2 + 5) {
			case 3:
				break;
			case 89:
			}
		case BreakTest.F: {
			System.out.println("C");
			break;
		}
		case (int) 2.9 + "":
		default:
			System.out.println("DEFAULT");

			// Aquí podemos dar fallo , no tiene sentido, es como añadir más
			// sentencias al default
		case "JH":
			System.out.println("JH");
		}
	}

	static String a = "B";

	private static String getC() {
		System.out.println("EVAL GETC");
		String ret = a;
		if (!a.contentEquals("X"))
			a = "C";
		return ret;
	}
}

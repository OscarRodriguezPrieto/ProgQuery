package examples.CFG;

import java.io.BufferedReader;
import java.io.IOException;

public class TryCatchFinally {
	boolean aux = false;

	public void m(int a) throws IOException {

		tr: try (BufferedReader r = new BufferedReader(null); BufferedReader r2 = null) {
			try {
				NullPointerException n = null;
				if (a < 2)
					throw n;
				try (BufferedReader rd = null) {
((Cloneable)n).clone();
				}
			} catch (IndexOutOfBoundsException e) {
				a = 5;
			} catch (RuntimeException f) {
				a += 7;
			} catch (Exception g) {
				fr: for (int i = 0; i < 6; i++)
					try {
						assert (i >= 9);
						if (i == a)
							continue;
						else if ("J".contains(i + ""))
							break;
						else if (aux)
							continue fr;
						else
							break tr;

					} finally {
						a++;
						NullPointerException n = null;
						if (a == 6)
							throw n;
						else if (a == 7)
							assert (a == 8);
						else
							throw new IllegalAccessError();
					}
			}
			// De momento debería entrar por NullPointer como si tuviera
			// polimorfismo
			throw new RuntimeException();
			// RelevntNode 12
		} catch (NullPointerException h) {
			break tr;
		} catch (AssertionError i) {
			fr: for (String st : "H".split("H"))
				while (true)
					try {
						if (a == 4)
							break;
						else if (a == 7)
							break tr;
						else
							continue fr;
					} finally {
						;
					}

		} finally {
			System.out.println(a);
			try {
				assert (a * 2 == 9);

				throw new IllegalMonitorStateException();
			} catch (Throwable t) {
				;
			} finally {
				try (BufferedReader br = null) {
					break tr;
				}
			}
		}
	}
}

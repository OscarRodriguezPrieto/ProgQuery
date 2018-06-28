package examples.CFG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;

public class TryCatchFinally {
	boolean aux = false;

	public void m(int a) throws IOException, ClassNotFoundException, AssertionError, CloneNotSupportedException,
			InstantiationException, IllegalAccessException {
		// Statement 0
		Method m = null;
		// Statement 1(label), 2(try), 3, 4 (resources)
		tr: try (BufferedReader r = new BufferedReader(getS()); BufferedReader r2 = null) {
			// Statement 5
			new TryCatchFinally().getR();
			// Statement 6
			try {
				// Statement 7, 8, 9
				m.getDeclaringClass().getFields()[7].set(m, 2);
				NullPointerException n = null;
				if (a < 2)
					// Statement 10
					throw n;
				// Statement 11, 12
				try (BufferedReader rd = null) {
					// Statement 13
					m.invoke(m, 1);
				}
				// Statement 14,15
			} catch (IndexOutOfBoundsException e) {
				// Statement 16
				a = 5;
				// Statement 17,18,19,20
			} catch (RuntimeException f) {
				a += 7;
				m.getClass().newInstance();
				// Satetement 21,22
			} catch (Exception g) {
				// Statement 23,24,25 (initialization), 26 (update)
				fr: for (int i = 0; i < 6; i++)
					// Statement 27
					try {
						// Statement 28,29,30 (continue), 31, 32 (break), 33, 34
						// (continue fr), 35 (break tr)
						assert (i >= 9);
						if (i == a)
							continue;
						else if ("J".contains(i + ""))
							break;
						else if (aux)
							continue fr;
						else
							break tr;
						// Statement 36
					} finally {
						// Statement 37, 38, 39, 40, 41, 42, 43
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
			// Statement 44
			throw new RuntimeException();
			// Statement 45,46,47
		} catch (NullPointerException h) {
			break tr;
			// Statement 48,49
		} catch (AssertionError i) {
			// Statement 50 (label) ,51,52 (declaration)
			fr: for (String st : "H".split("H"))
				// Statement 53
				while (true)
					// Statement 54
					try {
						// Statement 55,56,57,58,59
						if (a == 4)
							break;
						else if (a == 7)
							break tr;
						else
							continue fr;
						// Statement 60
					} finally {
					}
			// Statement 61
		} finally {
			// Statement 62
			System.out.println(a);
			// Statement 63
			try {
				// Statement 64,65
				assert (a * 2 == 9);
				m.getClass().newInstance();
				// Statement 66
				do
					// Statement 67 , 68
					if (m.toString().length() == 2)
						break;
				while (true);
				// Statement 69
				throw new IllegalMonitorStateException();
				// Statement 70,71,72
			} catch (Throwable t) {
				;
				// Statement 73
			} finally {
				// Statement 74,75,76,77
				try (BufferedReader br = null) {
					if (getR().ready())
						break tr;
				}
			}
		}
	}

	public Reader getS() throws IOException {
		return null;
	}

	public Reader getR() throws ClassNotFoundException, AssertionError {
		return null;
	}

	public TryCatchFinally() throws CloneNotSupportedException {

	}
}

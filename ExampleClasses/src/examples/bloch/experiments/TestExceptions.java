package examples.bloch.experiments;

import java.io.IOException;
import java.sql.SQLException;

public class TestExceptions {

	public static void m() throws Throwable {
		m1();
		try {
			m2();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ERROR", e);
		}
	}

	public static void m1() throws Exception {

	}

	public static void m2() throws IOException, SQLException {

	}
}

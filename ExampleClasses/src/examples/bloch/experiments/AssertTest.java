package examples.bloch.experiments;

public class AssertTest {

	public static void main(String[] args) {
		System.out.println("START");
		try {
			assert (args.length > 0);
		} catch (AssertionError  | Exception e) {
			System.out.println("CACHADO");
			e.printStackTrace();
		}
	}
}

package examples.bloch.experiments;

public class TryCatchFinallyTest {

	public static void main(String[] args) {
		System.out.println("START");
		try {
			System.out.println(1 / 0);
		} catch (Exception e) {
			System.out.println("CACHADO");
			e.printStackTrace();
		}
	}
}

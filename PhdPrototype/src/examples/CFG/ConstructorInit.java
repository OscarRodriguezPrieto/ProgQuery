package examples.CFG;

public class ConstructorInit {

	int a = getInt("A"), b = getInt("B");
	public ConstructorInit() {
		System.out.println("FIRST ST IN CONST");
	}

	int d = getInt("D");

	private int getInt(String string) {
		System.out.println(string);
		return 0;
	}

	int e = getInt("E"), f = getInt("F"), g = getInt("G");
	public static void main(String[] args) {
		new ConstructorInit();
	}

	int c = getInt("C");

}

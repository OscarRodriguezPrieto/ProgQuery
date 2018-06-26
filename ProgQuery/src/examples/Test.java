package examples;

public class Test {

	public static void main(String[] args) {
		int a = 2;
	}

	@Override
	public boolean equals(Object o) {
		return true;
	}
	public boolean equals(Object o,Object o2) {
		return true;
	}
	@Override
	public int hashCode() {
		return 2;
	}
}

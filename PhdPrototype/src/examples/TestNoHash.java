package examples;

public class TestNoHash {
	@Override
	public boolean equals(Object o) {
		return true;
	}

	public int hashCode(int arg) {
		return arg;
	}
}

package examples;

public class TestNoHash {
	@Override
	public boolean equals(Object o) {
		return true;
	}

	public <T extends Number & Comparable> int hashCode(int arg) {
//	Class<? extends  ? extends Object> c;
		return arg
//				+2.5
				;
	}
}

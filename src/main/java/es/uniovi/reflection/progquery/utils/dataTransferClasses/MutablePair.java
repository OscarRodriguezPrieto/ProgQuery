package es.uniovi.reflection.progquery.utils.dataTransferClasses;


public class MutablePair<X, Y> {

	private X x;
	private Y y;

	private MutablePair(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public X getFirst() {
		return x;
	}

	public Y getSecond() {
		return y;
	}

	public void setFirst(X x) {
		this.x = x;
	}

	public void setSecond(Y y) {
		this.y = y;
	}

	public static <X, Y> MutablePair<X, Y> create(X x, Y y) {
		return new MutablePair<>(x, y);
	}

}

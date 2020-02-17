package examples.cmu;

public class PT extends Z<PT> implements Cloneable {

	public PT getMyClon() throws CloneNotSupportedException {
		return (PT) clone();
	}

}

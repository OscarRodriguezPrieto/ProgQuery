package examples.mig;

public interface I extends J {
	@Override
	default void i() {
		this.toString();
//		super.toString();
	}
}

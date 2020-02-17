package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import examples.mig.C;

public abstract class MyAnn<T extends Exception & Cloneable> extends C{
	public MyAnn(int i) {
		super(i);
	}

	// public <TT extends Exception & Cloneable> void f() throws T{
	// Class<? super Cloneable> c;
	// Class<? extends D> d;
	//
	// }
	private List<String> l = new ArrayList<String>();
	private List<String> l2 = getL();

	ArrayList<String> getL() {
		return null;
	}

	int getI() {
		return 0;
	}

	public String toString() {
		return "";
	}
	public String getS(){
		return null;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface JsonElement {
		public static final String o = "GGGG";

		public String key() default JsonElement.o;

		public int g() default (int) 2.9;
	}

	@JsonElement
	private int a;
}

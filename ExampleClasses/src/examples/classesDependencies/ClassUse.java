package examples.classesDependencies;

import java.util.ArrayList;
import java.util.Collections;

import examples.Constructores;
import examples.OtherClass;

public class ClassUse implements Interface {
	
	
	Constructores getCOnstructores() {
		return new Constructores();
	}
	
	
	
	private ArrayList<String> l;

	// public static Z getZ() {
	// return new Z();
	// }
	public int m() {
		return Collections.emptyMap().size();
	}

	public int m(Integer i) {
		return Collections.emptyMap().size();
	}

	public int m(String s) {
		return Collections.emptyMap().size();
	}

	public int m(Object o) {
		return Collections.emptyMap().size();
	}

	public int mm() {
		m("JBOÑL");
		m(1);
		m("MLL".getClass());
		return new ExtraClass().OTHER.n;
	}

	public int mmm() {

		return getE().getO().n;
	}

	public ExtraClass getE() {
		return new ExtraClass();
	}

	public Object getI() {
		return OtherClass.InnerClass.n;
	}
}

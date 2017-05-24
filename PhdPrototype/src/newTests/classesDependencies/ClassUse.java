package newTests.classesDependencies;

import java.util.ArrayList;
import java.util.Collections;

import newTests.OtherClass;

public class ClassUse {

	private ArrayList<String> l;

	public int m() {
		return Collections.emptyMap().size();
	}

	public int mm() {
		return ExtraClass.OTHER.n;
	}

	public int mmm() {
		return getE().getO().n;
	}

	public ExtraClass getE() {
		return new ExtraClass();
	}
	
	public Object getI(){
		return  OtherClass.InnerClass.n;
	}
}

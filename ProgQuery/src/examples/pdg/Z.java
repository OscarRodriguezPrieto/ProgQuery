package examples.pdg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.function.Function;

import com.sun.javafx.UnmodifiableArrayList;

import database.relations.RelationTypes;

public class Z implements Serializable, Cloneable {
	final int m = 33;

	public static transient File ff;

	public static int X() {
		return 0;
	}

	public void ordinal() {
		RelationTypes.ARRAYACCESS_EXPR.ordinal();
	}

	public void confusing(int i, String g) {
		RelationTypes.ARRAYACCESS_EXPR.ordinal();
	}

	public int confusing(String g, int i) {
		return RelationTypes.ARRAYACCESS_EXPR.ordinal();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// return super.clone();
		return null;
	}

	public UnmodifiableArrayList getC() {
		return null;
	}

	public int[] getArray() {
		if ("JOJ".contains("JK"))
			return null;
		else
			return "LMLM".length() == 3 ? null : new int[] {};

	}

	public void closeable() throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File("NJKNJK"))),
				br2 = new BufferedReader(new FileReader(new File("NJKNJK")));
		br.close();
		Function<Object, Boolean> c = (br = null)::equals;
		for (int i = 0; i < 6; i++)
			System.out.println(i);
		br2.close();
		br.close();

	}

}

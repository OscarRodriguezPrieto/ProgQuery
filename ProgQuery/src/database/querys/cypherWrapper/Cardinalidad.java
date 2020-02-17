package database.querys.cypherWrapper;

import java.util.function.BiFunction;

public class Cardinalidad {
	public static final Cardinalidad JUST_ONE = new Cardinalidad((min, max) -> ""),
			ONE_TO_INF = new Cardinalidad((min, max) -> "*");


	private BiFunction<Integer, Integer, String> toString;
	private int min, max;

	private Cardinalidad(BiFunction<Integer, Integer, String> toString, int min, int max) {
		this.toString = toString;
		this.min = min;
		this.max = max;
	}

	private Cardinalidad(BiFunction<Integer, Integer, String> toString, int min) {
		this.toString = toString;
		this.min = min;
		this.max = -1;
	}

	private Cardinalidad(BiFunction<Integer, Integer, String> toString) {
		this.toString = toString;
	}

	public static Cardinalidad MIN_TO_INF(int minimum) {
		return new Cardinalidad((min, max) -> "*" + min + "..", minimum);
	}

	public static Cardinalidad MIN_TO_MAX(int minimum, int maximum) {
		return new Cardinalidad((min, max) -> "*" + min + ".." + max, minimum, maximum);
	}

	public static Cardinalidad ONE_TO_MAX(int maximum) {
		return new Cardinalidad((min, max) -> "*.." + max, -1, maximum);
	}

	public String toString() {
		return toString.apply(min, max);
	}

}

package database.querys.cypherWrapper;

public class Reduce extends Extract {

	private String seed;

	public Reduce(String list, String map, String var, String seed) {
		super(list, map, var);
		this.seed = seed;
	}

	public Reduce(String list, String map, String seed) {
		super(list, map);
		this.seed = seed;
	}

	public Reduce(String list, Expression map, String seed) {
		super(list, map);
		this.seed = seed;
	}

	public Reduce(Expression list, Expression map, String var, String seed) {
		super(list, map, var);
		this.seed = seed;
	}

	public Reduce(String list, Expression map, String var, String seed) {
		super(new ExprImpl(list), map, var);
		this.seed = seed;
	}

	@Override
	public String expToString() {
		return "REDUCE(" + seed + "," + var + " IN " + list.expToString() + " | " + map.expToString() + " )";
	}

}

package database.querys.cypherWrapper;

public class All extends Extract {

	public All(String list, String map, String var) {
		super(list, map, var);
		// TODO Auto-generated constructor stub
	}

	public All(Expression list, Expression map, String var) {
		super(list, map, var);
		// TODO Auto-generated constructor stub
	}
	public All(String list, String map) {
		super(list, map);
		// TODO Auto-generated constructor stub
	}

	public All(String list, Expression map) {
		super(list, map);
		// TODO Auto-generated constructor stub
	}

	public All(Expression list, Expression map) {
		super(list, map, "");
		// TODO Auto-generated constructor stub
	}

	@Override
	public String expToString() {
		return "ALL" + "(" + var + " IN " + list.expToString() + " WHERE " + map.expToString() + " )";
	}

}

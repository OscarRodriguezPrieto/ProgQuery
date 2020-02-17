package database.querys.cypherWrapper;

public class Filter extends Extract {

	public Filter(String list, String map, String var) {
		super(list, map, var);
		// TODO Auto-generated constructor stub
	}

	public Filter(Expression list, Expression map, String var) {
		super(list, map, var);
		// TODO Auto-generated constructor stub
	}
	public Filter(String list, String map) {
		super(list, map);
		// TODO Auto-generated constructor stub
	}

	public Filter(String list, Expression map) {
		super(list, map);
		// TODO Auto-generated constructor stub
	}

	public Filter(Expression list, Expression map) {
		super(list, map, "");
		// TODO Auto-generated constructor stub
	}

	@Override
	public String expToString() {
		return "FILTER" + "(" + var + " IN " + list.expToString() + " WHERE " + map.expToString() + " )";
	}

}

package database.querys.cypherWrapper;

public class Extract implements Expression {

	protected Expression list;
	protected Expression map;
	protected String var = "x";

	public Extract(Expression list, Expression map, String var) {
		super();
		this.list = list;
		this.map = map;
		this.var = var;
	}

	public Extract(String list, String map) {
		super();
		this.list = new ExprImpl(list);
		this.map = new ExprImpl(map);
	}

	public Extract(Expression list, String map) {
		super();
		this.list = list;
		this.map = new ExprImpl(map);
	}
	public Extract(String list, String map, String var) {
		super();
		this.list = new ExprImpl(list);
		this.map = new ExprImpl(map);
		this.var = var;
	}

	public Extract(String list, Expression map) {
		super();
		this.list = new ExprImpl(list);
		this.map = map;
	}

	public Extract(Filter list, String map, String var) {
		this.map = new ExprImpl(map);
		this.list = list;
		this.var = var;
	}

	@Override
	public String expToString() {
		return "EXTRACT(" + var + " IN " + list.expToString() + " | " + map.expToString() + " )";
	}

}

package database.querys.cypherWrapper;

public class Function implements Expression {
	private String name;

	private Expression[] exprs;

	public Function(String name, Expression... exprs) {
		super();
		this.name = name;
		this.exprs = exprs;
	}

	@Override
	public String expToString() {
		String res = name + "(";
		for (Expression expr : exprs)
			res += expr.expToString() + ",";
		return res.substring(0, res.length() - 1) + ")";

	}

}

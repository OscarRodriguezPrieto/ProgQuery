package database.querys.cypherWrapper;

public class OrderByClause implements Clause {
	private Expression[] exprs;

	public OrderByClause(Expression... exprs) {
		super();
		this.exprs = exprs;
	}

	public OrderByClause(String... exprTexts) {
		super();
		exprs = new Expression[exprTexts.length];
		for (int i = 0; i < exprs.length; i++)
			exprs[i] = new ExprImpl(exprTexts[i]);
	}

	@Override
	public String clauseToString() {

		String res = "ORDER BY ";
		for (Expression exp : exprs)
			res += exp.expToString() + ", ";
		return res.substring(0, res.length() - 2);
	}

}

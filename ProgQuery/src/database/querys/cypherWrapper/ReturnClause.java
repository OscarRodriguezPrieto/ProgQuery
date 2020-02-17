package database.querys.cypherWrapper;

public class ReturnClause implements Clause {

	private Expression[] exprs;
	private String[] renames;

	public ReturnClause(String... exprs) {
		super();
		this.exprs = new Expression[exprs.length];
		int i = 0;
		for (String s : exprs)
			this.exprs[i++] = new ExprImpl(s);
	}

	public ReturnClause(Expression... exprs) {
		super();
		this.exprs = exprs;
	}

	public static ReturnClause fromStringsToReturnWithExprs(String... exprs) {
		Expression[] exps = new Expression[exprs.length];
		for (int i = 0; i < exprs.length; i++)
			exps[i] = new ExprImpl(exprs[i]);
		return new ReturnClause(exps);
	}

	public ReturnClause(Expression[] exprs, String... renames) {
		super();
		this.exprs = exprs;
		this.renames = renames;
	}

	@Override
	public String clauseToString() {
		String res = "RETURN ";
		for (int i = 0; i < exprs.length; i++)
			res += exprs[i].expToString() + (renames != null ? " as " + renames[i] : "") + ", ";
		return res.substring(0, res.length() - 2);
	}
}

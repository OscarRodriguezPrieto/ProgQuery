package database.querys.cypherWrapper;

public class SetClause implements Clause {
	private Expression[] exps;

	public SetClause(Expression... exps) {
		super();
		this.exps = exps;
	}

	public SetClause(String... exps) {
		this.exps = new Expression[exps.length];
		for (int i = 0; i < exps.length; i++)
			this.exps[i] = new ExprImpl(exps[i]);

	}

	@Override
	public String clauseToString() {
		String res = "SET ";
		for (int i = 0; i < exps.length; i += 2)
			res += exps[i].expToString() + " = " + exps[i + 1].expToString() + ", ";
		return res.substring(0, res.length() - 2);
	}

}

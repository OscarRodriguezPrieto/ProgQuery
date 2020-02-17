package database.querys.cypherWrapper;

import utils.dataTransferClasses.Pair;

public class WithClause extends SimpleWithClause {

	Expression[] exprs;

	public WithClause(String[] renames, Expression... exprs) {
		super(renames);
		this.exprs = exprs;
	}

	public WithClause(String[] renames, Pair<String, Expression>... exprs) {
		super(getArray(renames, exprs));
		initExprArray(renames, exprs);
	}

	private static String[] getArray(String[] renames, Pair<String, Expression>... exprs) {
		String[] res = new String[renames.length + exprs.length];
		for (int i = 0; i < renames.length; i++)
			res[i] = renames[i];
		for (int i = 0; i < exprs.length; i++)
			res[renames.length + i] = exprs[i].getFirst();
		return res;
	}

	private void initExprArray(String[] renames, Pair<String, Expression>... exprs) {
		this.exprs = new Expression[renames.length + exprs.length];
		for (int i = 0; i < renames.length; i++)
			this.exprs[i] = null;
		for (int i = 0; i < exprs.length; i++)
			this.exprs[renames.length + i] = exprs[i].getSecond();
	}
	@Override
	public String clauseToString() {
		String res = "WITH ";
		for (int i = 0; i < exprs.length; i++)
			res += (exprs[i] != null ? exprs[i].expToString() + " as " : "") + renames[i] + ", ";
		return res.substring(0, res.length() - 2);
	}

}

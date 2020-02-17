package database.querys.cypherWrapper;

public class Binop implements Expression {
	private String operator;

	private Expression left, right;

	public Binop(String operator, Expression left, Expression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	@Override
	public String expToString() {
		return left.expToString() + " " + operator + " " + right.expToString();
	}

	static Expression andExpression(Expression... exprs) {
		return consecutiveExprs("AND", exprs);
	}

	static Expression orExpression(Expression... exprs) {
		return consecutiveExprs(" OR ", exprs);
	}

	private static Expression consecutiveExprs(String op, Expression... exprs) {
		Expression exp = exprs[0];
		for (int i = 1; i < exprs.length; i++)
			exp = new Binop(op, exp, exprs[i]);
		return exp;
	}

}

package database.querys.cypherWrapper;

public class Case implements Expression {

	private Expression condition, ifExpr, elseExpr;

	public Case(Expression condition, Expression ifExpr, Expression elseExpr) {
		this.condition = condition;
		this.ifExpr = ifExpr;
		this.elseExpr = elseExpr;
	}

	public Case(String condition, String ifExpr, String elseExpr) {
		this.condition = new ExprImpl(condition);
		this.ifExpr = new ExprImpl(ifExpr);
		this.elseExpr = new ExprImpl(elseExpr);
	}

	public Case(String condition, Expression ifExpr, String elseExpr) {
		this(new ExprImpl(condition), ifExpr, new ExprImpl(elseExpr));
	}

	@Override
	public String expToString() {

		return "(CASE WHEN " + condition.expToString() + " THEN " + ifExpr.expToString() + " ELSE "
				+ elseExpr.expToString() + " END)";
	}

}

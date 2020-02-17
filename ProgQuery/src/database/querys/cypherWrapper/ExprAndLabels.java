package database.querys.cypherWrapper;

public class ExprAndLabels implements Expression {
	private Expression exp;

	public ExprAndLabels(Expression exp) {
		super();
		this.exp = exp;
	}

	public ExprAndLabels(String exp) {
		super();
		this.exp = new ExprImpl(exp);
	}

	@Override
	public String expToString() {
		return exp.expToString() + ", LABELS(" + exp.expToString() + ")";
	}

}

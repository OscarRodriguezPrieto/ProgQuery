package database.querys.cypherWrapper;

public class Any implements Expression {

	private Expression list;
	private Expression condition;

	public Any(Expression list, Expression condition) {
		super();
		this.list = list;
		this.condition = condition;
	}

	public Any(String list, String condition) {
		super();
		this.list = new ExprImpl(list);
		this.condition = new ExprImpl(condition);
	}

	@Override
	public String expToString() {
		return "ANY( x IN " + list.expToString() + " WHERE " + condition.expToString() + " )";
	}

	public static Any collectAndAny(String rowsToCollect, String condition) {
		return new Any(new ExprImpl("COLLECT(" + rowsToCollect + ")"), new ExprImpl(condition));
	}
}

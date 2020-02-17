package database.querys.cypherWrapper;

public class UnwindClause implements Clause {
	private Expression list;
	private String name;

	public UnwindClause(Expression list, String name) {
		super();
		this.list = list;
		this.name = name;
	}

	public UnwindClause(String list, String name) {
		super();
		this.list = new ExprImpl(list);
		this.name = name;
	}

	@Override
	public String clauseToString() {
		return "UNWIND " + list.expToString() + " as " + name;
	}

}

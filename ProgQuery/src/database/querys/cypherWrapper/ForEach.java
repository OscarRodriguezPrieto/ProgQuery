package database.querys.cypherWrapper;

public class ForEach implements Clause {

	private String name;
	private Expression list;
	private Clause clause;

	public ForEach(String name, Expression list, Clause clause) {
		super();
		this.name = name;
		this.list = list;
		this.clause = clause;
	}

	public ForEach(String name, String list, Clause clause) {
		super();
		this.name = name;
		this.list = new ExprImpl(list);
		this.clause = clause;
	}

	@Override
	public String clauseToString() {
		return "FOREACH (" + name + " IN " + list.expToString() + " | " + clause.clauseToString() + ") ";
	}

}

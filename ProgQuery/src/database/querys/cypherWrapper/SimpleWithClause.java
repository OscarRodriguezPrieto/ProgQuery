package database.querys.cypherWrapper;

public class SimpleWithClause implements Clause {
	String[] renames;
	boolean distinct;

	public SimpleWithClause(String... renames) {
		this.renames = renames;
	}

	public SimpleWithClause(boolean distinct,String... renames) {
		super();
		this.renames = renames;
		this.distinct = distinct;
	}

	@Override
	public String clauseToString() {
		String res = "WITH " + (distinct ? " DISTINCT " : "");
		for (int i = 0; i < renames.length; i++)
			res += renames[i] + ", ";
		return res.substring(0, res.length() - 2);
	}
}

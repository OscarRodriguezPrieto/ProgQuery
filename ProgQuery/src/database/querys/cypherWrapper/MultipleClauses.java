package database.querys.cypherWrapper;

public class MultipleClauses implements Clause {
	private Clause[] clauses;

	@Override
	public String clauseToString() {
		String res = "";
		for (Clause clause : clauses)
			res += clause.clauseToString() + "\n";
		return res;
	}

	public MultipleClauses(Clause... clauses) {

		this.clauses = clauses;
	}

}

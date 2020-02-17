package database.querys.cypherWrapper;

public class ClauseImpl implements Clause {
	private String clause;

	public ClauseImpl(String clause) {
		super();
		this.clause = clause;
	}

	@Override
	public String clauseToString() {
		return clause;
	}

}

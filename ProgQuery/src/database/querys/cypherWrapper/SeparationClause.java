package database.querys.cypherWrapper;

public class SeparationClause implements Clause {

	@Override
	public String clauseToString() {
		return ";";
	}

}

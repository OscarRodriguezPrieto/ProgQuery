package database.querys.cypherWrapper;

public class PathEnd implements MatchElement {
	private Clause[] endClause;

	private MatchElement element;

	public PathEnd(MatchElement element, Clause... endClause) {
		this.endClause = endClause;
		this.element = element;
	}


	public String matchToString() {

		String res = element.matchToString();
		for (Clause clause : endClause)
			res += "\n" + clause.clauseToString();
		return res;
	}

	@Override
	public Node getLastNode() {
		return element.getLastNode();
	}

	public void setEndClause(Clause c) {
		endClause[endClause.length - 1] = c;
	}
}

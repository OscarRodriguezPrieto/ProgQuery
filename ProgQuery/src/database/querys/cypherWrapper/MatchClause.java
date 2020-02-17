package database.querys.cypherWrapper;

public class MatchClause extends CreateClause {

	boolean isOptional;

	public MatchClause(MatchElement... elements) {
		super(elements);
	}

	public MatchClause(boolean isOptional, MatchElement... elements) {
		super(elements);
		this.isOptional = isOptional;
	}

	public MatchClause(boolean isOptional, String content) {
		super(new MatchImpl(content));
		this.isOptional = isOptional;
	}

	@Override
	public String clauseToString() {
		String res = (isOptional ? "OPTIONAL " : "") + "MATCH ";
		for (MatchElement match : elements)
			// if (match instanceof EmptyElement)
			// res = res.substring(0, res.length() - 2);
			res += match.matchToString() + ",\n";

		return res.substring(0, res.length() - 2);
	}

	@Override
	public Clause append(Element e) {
		return e.beAppendedTo(this);
	}
}

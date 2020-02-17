package database.querys.cypherWrapper;

public interface Clause extends Element {

	String clauseToString();

	default Clause append(Element e) {
		return e.beAppendedTo(this);
	}

	default Clause beAppendedTo(MatchClause clause) {

		return beAppendedTo((Clause) clause);
	}

	@Override
	default Clause beAppendedTo(Clause e) {
		return new MultipleClauses(e, this);
	}

	default MatchElement beAppendedTo(MatchElement matchElement){
		return new PathEnd(matchElement, this);
	}
}

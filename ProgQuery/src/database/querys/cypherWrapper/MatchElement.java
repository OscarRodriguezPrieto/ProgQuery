package database.querys.cypherWrapper;

import utils.dataTransferClasses.Pair;

public interface MatchElement extends Element {

	String matchToString();

	default Path append(Pair<Edge, MatchElement>... subPath) {
		return new Path(this, subPath);
	}

	default MatchElements appendNewElement(MatchElement m) {
		return new MatchElements(this, m);
	}

	Node getLastNode();

	default Clause beAppendedTo(Clause e) {
		return new MultipleClauses(e, new MatchClause(this));
	}

	default Clause beAppendedTo(MatchClause clause) {

		return new MatchClause(clause.isOptional, new MatchElements(clause.elements), this);
	}

	default MatchElement beAppendedTo(MatchElement matchElement) {

		// también podría ser PAth
		return new MatchElements(matchElement, this);
	}
}

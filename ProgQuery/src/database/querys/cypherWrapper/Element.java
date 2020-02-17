package database.querys.cypherWrapper;

public interface Element {

	Clause beAppendedTo(Clause clause);

	Clause beAppendedTo(MatchClause clause);

	MatchElement beAppendedTo(MatchElement matchElement);

}

package database.querys.cypherWrapper;

public interface Relationship extends MatchElement {

	String relToString();

	default String matchToString() {
		return relToString();
	}
}

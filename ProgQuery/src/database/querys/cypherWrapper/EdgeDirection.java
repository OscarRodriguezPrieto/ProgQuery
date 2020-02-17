package database.querys.cypherWrapper;

public enum EdgeDirection {

	UNDIRECTED("-[", "]-"), INCOMING("-[", "]->"), OUTGOING("<-[", "]-");

	private String directionToStringPart1, directionToStringPart2;

	private EdgeDirection(String directionToStringPart1, String directionToStringPart2) {
		this.directionToStringPart1 = directionToStringPart1;
		this.directionToStringPart2 = directionToStringPart2;
	}

	public String getDirectionToStringPart1() {
		return directionToStringPart1;
	}

	public String getDirectionToStringPart2() {
		return directionToStringPart2;
	}

}

package database.querys.cypherWrapper;

public class AnonymousNode implements Node {

	@Override
	public String matchToString() {
		return "()";
	}

	@Override
	public Node getLastNode() {
		return this;
	}

	@Override
	public String getName() {
		throw new IllegalStateException();
	}

}

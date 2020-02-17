package database.querys.cypherWrapper;

public class NodeVar extends AnonymousNode {

	protected String name;

	public NodeVar(String name) {
		super();
		this.name = name;
	}

	@Override
	public String matchToString() {
		return "(" + name + ")";
	}

	@Override
	public String getName() {
		return name;
	}
}

package database.querys.cypherWrapper;

public class MatchImpl implements MatchElement {
	private String string;
	private String lastNode;
	public MatchImpl(String string) {
		super();
		this.string = string;
	}

	public MatchImpl(String string, String lastNode) {
		super();
		this.string = string;
		this.lastNode = lastNode;
	}

	@Override
	public String matchToString() {
		// TODO Auto-generated method stub
		return string;
	}

	@Override
	public Node getLastNode() {
		return new NodeVar(lastNode);
	}

}

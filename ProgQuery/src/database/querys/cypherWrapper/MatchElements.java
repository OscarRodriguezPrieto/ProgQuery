package database.querys.cypherWrapper;

public class MatchElements implements MatchElement {
	private MatchElement[] elements;

	public MatchElements(MatchElement... elements) {
		this.elements = elements;
	}

	@Override
	public String matchToString() {
		String res = "";
		if (elements.length == 0)
			return res;
		for (MatchElement element : elements)
			res += element.matchToString() + ",";
		return res.substring(0, res.length() - 1);
	}

	@Override
	public Node getLastNode() {
		return elements[elements.length - 1].getLastNode();
	}


}

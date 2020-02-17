package database.querys.cypherWrapper;

import utils.dataTransferClasses.Pair;

public class Path implements MatchElement {
	MatchElement initialNode;
	Pair<Edge, MatchElement>[] path;
	String name;

	public Path(MatchElement initialNode, Pair<Edge, MatchElement>... path) {
		this.initialNode = initialNode;
		this.path = path;
		name = "";
	}

	public Path(MatchElement initialNode, String name, Pair<Edge, MatchElement>... path) {
		this.initialNode = initialNode;
		this.path = path;
		this.name = name + "=";
	}

	public String matchToString() {

		String res = name + initialNode.matchToString();
		for (Pair<Edge, MatchElement> pair : path)
			res += pair.getFirst().edgeToString() + pair.getSecond().matchToString();
		return res;
	}


	@Override
	public Node getLastNode() {
		return path[path.length - 1].getSecond().getLastNode();
	}
}

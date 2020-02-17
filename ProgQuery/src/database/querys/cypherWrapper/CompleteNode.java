package database.querys.cypherWrapper;

import database.nodes.NodeTypes;
import utils.dataTransferClasses.Pair;

public class CompleteNode extends NodeVar {

	private NodeTypes label;

	private Pair<String, Object>[] property;

	public CompleteNode(String name, NodeTypes label, Pair<String, Object>... property) {
		super(name);
		this.label = label;
		this.property = property;
	}

	public CompleteNode(NodeTypes label, Pair<String, Object>... property) {
		super("");
		this.label = label;
		this.property = property;
	}

	public CompleteNode(String name, Pair<String, Object>... property) {
		this(name, null, property);
	}

	public CompleteNode(String name, NodeTypes label) {
		this(name, label, new Pair[0]);
	}

	public CompleteNode(Pair<String, Object>... property) {
		this("", null, property);
	}

	public CompleteNode(NodeTypes label) {
		this("", label, new Pair[0]);
	}

	@Override
	public String matchToString() {
		return "(" + name + (label == null ? "" : ":" + label) + propertiesToString() + ")";
	}

	private String propertiesToString() {
		if (property.length == 0)
			return "";

		String res = "{";
		for (Pair p : property)
			
			res += p.getFirst().toString() + ":" + (p.getSecond() instanceof String
					? "'" + p.getSecond().toString() + "'" : p.getSecond().toString()) + ",";

		return res.substring(0, res.length() - 1) + "}";
	}
}

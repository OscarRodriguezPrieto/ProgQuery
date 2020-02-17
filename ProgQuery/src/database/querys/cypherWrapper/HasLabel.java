package database.querys.cypherWrapper;

import database.nodes.NodeTypes;

public class HasLabel implements Expression {

	private Expression exp;
	private NodeTypes nodeType;

	public HasLabel(Expression exp, NodeTypes nodeType) {
		this.exp = exp;
		this.nodeType = nodeType;
	}

	public HasLabel(String exp, NodeTypes nodeType) {
		this.exp = new ExprImpl(exp);
		this.nodeType = nodeType;
	}

	@Override
	public String expToString() {
		return exp.toString() + ":" + nodeType.name();
	}

}

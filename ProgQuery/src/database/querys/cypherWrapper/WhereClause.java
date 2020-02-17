package database.querys.cypherWrapper;

import database.nodes.NodeTypes;

public class WhereClause implements Clause {
	private Expression expression;

	public WhereClause(Expression expression) {
		this.expression = expression;
	}

	public WhereClause(String expression) {
		this.expression = new ExprImpl(expression);
	}

	@Override
	public String clauseToString() {
		return " WHERE " + expression.expToString();
	}

	public Clause[] addToClauses(Clause... clauses) {
		Clause[] res = new Clause[clauses.length + 1];
		for (int i = 0; i < clauses.length; i++)
			res[i] = clauses[i];
		res[clauses.length] = this;
		return res;
	}

	public static WhereClause getFilterOnNodeLabels(String nodeId, NodeTypes... labels) {
		String exp = "";
		for (NodeTypes label : labels)
			exp += nodeId + ":" + label.name() + " OR ";
		return new WhereClause(new ExprImpl(exp.substring(0, exp.length() - 4)));
	}

	public static Expression isElementOfWiggleType(String element, NodeTypes... types) {
		String res = "";
		for (NodeTypes t : types)
			res += element + ".nodeType='" + Node.PQNodeTypeToWiggle.get(t) + "' OR ";
		return new ExprImpl(res.substring(0, res.length() - 4));
	}
}

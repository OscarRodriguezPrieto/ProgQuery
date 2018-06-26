package ast;

import java.util.Map;

import org.neo4j.graphdb.Node;

import com.sun.source.tree.MethodTree;

public class MethodInfo {

	// SimpleTreeNodeCache<Tree> cfgCache;
	// Map<TryTree, List<Pair<Node, List<Symbol>>>> invocationsInStatements;
	MethodTree tree;
	// List<StatementTree> originalStatements;
	Node methodNode;
	Map<Node, Node> identificationForLeftAssignExprs;

	public MethodInfo(MethodTree tree, Node methodNode,

			Map<Node, Node> identificationForLeftAssignExprs) {
		// this.cfgCache = cfgCache;
		// this.invocationsInStatements = invocationsInStatements;
		this.tree = tree;
		this.methodNode = methodNode;
		this.identificationForLeftAssignExprs = identificationForLeftAssignExprs;
		// originalStatements = tree.getBody() != null ? new
		// ArrayList<StatementTree>(tree.getBody().getStatements())
		// : new ArrayList<StatementTree>();
	}

}

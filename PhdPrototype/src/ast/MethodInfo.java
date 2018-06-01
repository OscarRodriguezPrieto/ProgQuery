package ast;

import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.tools.javac.code.Symbol;

import cache.SimpleTreeNodeCache;
import utils.Pair;

public class MethodInfo {

	SimpleTreeNodeCache<Tree> cfgCache;
	Map<TryTree, List<Pair<Node, List<Symbol>>>> invocationsInStatements;
	MethodTree tree;
	// List<StatementTree> originalStatements;
	Node methodNode;
	Map<Node, Node> identificationForLeftAssignExprs;

	public MethodInfo(SimpleTreeNodeCache<Tree> cfgCache, MethodTree tree, Node methodNode,
			Map<TryTree, List<Pair<Node, List<Symbol>>>> invocationsInStatements,
			Map<Node, Node> identificationForLeftAssignExprs) {
		this.cfgCache = cfgCache;
		this.invocationsInStatements = invocationsInStatements;
		this.tree = tree;
		this.methodNode = methodNode;
		this.identificationForLeftAssignExprs = identificationForLeftAssignExprs;
		// originalStatements = tree.getBody() != null ? new
		// ArrayList<StatementTree>(tree.getBody().getStatements())
		// : new ArrayList<StatementTree>();
	}

}

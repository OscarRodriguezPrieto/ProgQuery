package database.querys.cypherWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.nodes.NodeTypes;
import utils.dataTransferClasses.Pair;

public interface Node extends MatchElement {
	static final Map<NodeTypes, String> PQNodeTypeToWiggle = getPQNodeTypeToWiggle();

	public static Node nodeForWiggle(String name, NodeTypes label, Pair<String, Object>... property) {
		List<Pair> props = new ArrayList<Pair>(Arrays.asList(property));
		props.add(Pair.create("nodeType", PQNodeTypeToWiggle.get(label)));
		return new CompleteNode(name, props.toArray(new Pair[] {}));
	}

	public static Node nodeForWiggle(NodeTypes label, Pair<String, Object>... property) {
		return nodeForWiggle("", label, property);
	}

	String getName();
	public static Node nodeForWiggle(String name, NodeTypes label) {
		return nodeForWiggle(name, label, new Pair[] {});
	}
	static Map<NodeTypes, String> getPQNodeTypeToWiggle() {
		Map<NodeTypes, String> res = new HashMap<NodeTypes, String>();
		res.put(NodeTypes.IDENTIFIER, "JCIdent");
		res.put(NodeTypes.ASSIGNMENT, "JCAssign");
		res.put(NodeTypes.LITERAL, "JCLiteral");
		res.put(NodeTypes.LOCAL_VAR_DEF, "JCVariableDecl");
		res.put(NodeTypes.ATTR_DEF, "JCVariableDecl");

		res.put(NodeTypes.PARAMETER_DEF, "JCVariableDecl");
		res.put(NodeTypes.METHOD_DEF, "JCMethodDecl");
		res.put(NodeTypes.BREAK_STATEMENT, "JCBreak");

		res.put(NodeTypes.FOR_LOOP, "JCForLoop");
		res.put(NodeTypes.DO_WHILE_LOOP, "JCDoWhileLoop");
		res.put(NodeTypes.WHILE_LOOP, "JCWhileLoop");
		res.put(NodeTypes.FOR_EACH_LOOP, "JCEnhancedForLoop");
		res.put(NodeTypes.MEMBER_SELECTION, "JCFieldAccess");

		return res;
	}
}

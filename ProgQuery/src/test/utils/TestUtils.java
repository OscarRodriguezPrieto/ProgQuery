package test.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;

import database.nodes.NodeTypes;
import database.nodes.NodeUtils;
import database.relations.RelationTypes;
import database.relations.RelationTypesInterface;

public class TestUtils {

	public static <T> int getCount(Iterable<T> it) {
		int i = 0;
		for (T t : it)
			i++;
		return i;
	}

	public static void assertExpressionStatement(Node statement, NodeTypes type) {

		assertEquals(statement.getProperty("nodeType"), NodeTypes.EXPRESSION_STATEMENT.toString());
		assertEquals(statement.getSingleRelationship(RelationTypes.ENCLOSES_EXPR, Direction.OUTGOING).getEndNode()
				.getProperty("nodeType"), type.toString());
	}

	public static List<Node> listFromResult(Result res) {
		List<Node> ret = new ArrayList<Node>();
		String uniqueColumnName = res.columns().get(0);
		while (res.hasNext())
			ret.add((Node) res.next().get(uniqueColumnName));
		return ret;
	}

	public static List<List<Node>> bidimensionalListFromResult(Result res) {
		List<List<Node>> ret = new ArrayList<List<Node>>();
		while (res.hasNext()) {
			ret.add(new ArrayList<Node>());
			int i = 0;
			Map<String, Object> row = res.next();
			for (String columnName : res.columns())
				ret.get(i).add((Node) row.get(columnName));
			i++;
		}
		return ret;
	}

	public static void assertBinopCondition(Node cond, String operator) {

		assertEquals(cond.getProperty("nodeType"), NodeTypes.BINARY_OPERATION.toString());
		assertEquals(cond.getProperty("operator"), operator);
	}

	public static Node getNextNode(Node previous, RelationTypesInterface r) {
		return getNextNode(previous, r, Direction.OUTGOING);
	}

	public static Node getNextNode(Node previous, RelationTypesInterface r, Direction d) {
		Relationship rel = previous.getSingleRelationship(r, d);
		return d == Direction.OUTGOING ? rel.getEndNode() : rel.getStartNode();
	}

	public static Node assertHasNextWith(Node previous, RelationTypesInterface r, Predicate<Node> p) {
		return assertHasNextWith(previous, r, Direction.OUTGOING, p);
	}

	public static Node assertHasNextWith(Node previous, RelationTypesInterface r, NodeTypes nodeType) {
		return assertHasNextWith(previous, r, Direction.OUTGOING, nodeType);
	}

	public static Node assertHasNextWith(Node previous, RelationTypesInterface r, Direction d, Predicate<Node> p) {
		Node n = getNextNode(previous, r, d);
		assertEquals(true, p.test(n));
		return n;
	}

	public static Node assertHasNextWith(Node previous, RelationTypesInterface r, Direction d, NodeTypes nodeType) {
		return assertHasNextWith(previous, r, d, n -> n.hasLabel(nodeType));
	}

	public static int relationshipsWith(Node b, Iterable<Relationship> rels) {
		int total = 0;
		for (Relationship r : rels)
			if (r.getStartNode().equals(b) || r.getEndNode().equals(b))
				total++;
		return total;

	}

	public static void justOneRelationshipWith(Node st, Node end, RelationTypesInterface rt,
			Predicate<Relationship> p) {
		justNRelationshipsWith(st, end, rt, p, 1);
	}

	public static void justNRelationshipsWith(Node st, Node end, RelationTypesInterface rt, Predicate<Relationship> p,
			int n) {
		int count = 0;
		for (Relationship r : st.getRelationships(rt))
			if (r.getStartNode().equals(st) && r.getEndNode().equals(end) && p.test(r))
				count++;
		assertEquals(n, count);
	}

	public static boolean hasAnyRelationshipsWith(Node a, Node b) {
		return relationshipsWith(b, a.getRelationships()) > 0;
	}

	public static void assertNoRels(Node a, Node b) {
		assert (!hasAnyRelationshipsWith(a, b));
	}

	public static void assertHasSingleRel(Node a, Node b, RelationTypesInterface r, Direction d) {
		assertEquals(1, relationshipsWith(b, a.getRelationships(r, d)));
	}

	public static Node getNodePredicate(Node previous, RelationTypesInterface r, Predicate<Node> pred) {
		for (Relationship rel : previous.getRelationships(r, Direction.OUTGOING))
			if (pred.test(rel.getEndNode()))
				return rel.getEndNode();
		throw new IllegalArgumentException(String.format("No nodes for Rel:s%, with this previous and predicate", r));
	}

	public static void printNodeInfo(Node... nodes) {
		for (Node n : nodes)
			System.out.println(NodeUtils.nodeToString(n));
	}

	public static void printNodeInfo(Iterable<Node> nodes) {
		for (Node n : nodes)
			System.out.println(NodeUtils.nodeToString(n));
	}
}

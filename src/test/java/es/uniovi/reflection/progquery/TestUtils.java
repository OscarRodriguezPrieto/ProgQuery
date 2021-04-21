package es.uniovi.reflection.progquery;

import static org.junit.Assert.assertEquals;

public class TestUtils {
/* TEST DESIGNED TO USE EMBEEDDED, MUST BE RE-IMPLEMENTED
	public static <T> int getCount(Iterable<T> it) {
		int i = 0;
		for (T t : it)
			i++;
		return i;
	}

	public static void assertExpressionStatement(NodeWrapper statement, NodeTypes type) {

		assertEquals(statement.getProperty("nodeType"), NodeTypes.EXPRESSION_STATEMENT.toString());
		assertEquals(statement.getSingleRelationship(RelationTypes.ENCLOSES_EXPR, Direction.OUTGOING).getEndNode()
				.getProperty("nodeType"), type.toString());
	}

	public static List<NodeWrapper> listFromResult(Result res) {
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

	public static void assertBinopCondition(NodeWrapper cond, String operator) {

		assertEquals(cond.getProperty("nodeType"), NodeTypes.BINARY_OPERATION.toString());
		assertEquals(cond.getProperty("operator"), operator);
	}

	public static NodeWrapper getNextNode(NodeWrapper previous, RelationTypesInterface r) {
		return getNextNode(previous, r, Direction.OUTGOING);
	}

	public static NodeWrapper getNextNode(NodeWrapper previous, RelationTypesInterface r, Direction d) {
		Relationship rel = previous.getSingleRelationship(r, d);
		return d == Direction.OUTGOING ? rel.getEndNode() : rel.getStartNode();
	}

	public static NodeWrapper assertHasNextWith(NodeWrapper previous, RelationTypesInterface r, Predicate<Node> p) {
		return assertHasNextWith(previous, r, Direction.OUTGOING, p);
	}

	public static NodeWrapper assertHasNextWith(NodeWrapper previous, RelationTypesInterface r, NodeTypes nodeType) {
		return assertHasNextWith(previous, r, Direction.OUTGOING, nodeType);
	}

	public static NodeWrapper assertHasNextWith(NodeWrapper previous, RelationTypesInterface r, Direction d,
			Predicate<Node> p) {
		NodeWrapper n = getNextNode(previous, r, d);
		assertEquals(true, p.test(n));
		return n;
	}

	public static NodeWrapper assertHasNextWith(NodeWrapper previous, RelationTypesInterface r, Direction d,
			NodeTypes nodeType) {
		return assertHasNextWith(previous, r, d, n -> n.hasLabel(nodeType));
	}

	public static int relationshipsWith(NodeWrapper b, Iterable<Relationship> rels) {
		int total = 0;
		for (RelationshipWrapper r : rels)
			if (r.getStartNode().equals(b) || r.getEndNode().equals(b))
				total++;
		return total;

	}

	public static void justOneRelationshipWith(NodeWrapper st, NodeWrapper end, RelationTypesInterface rt,
			Predicate<Relationship> p) {
		justNRelationshipsWith(st, end, rt, p, 1);
	}

	public static void justNRelationshipsWith(NodeWrapper st, NodeWrapper end, RelationTypesInterface rt,
			Predicate<Relationship> p, int n) {
		int count = 0;
		for (Relationship r : st.getRelationships(rt))
			if (r.getStartNode().equals(st) && r.getEndNode().equals(end) && p.test(r))
				count++;
		assertEquals(n, count);
	}

	public static boolean hasAnyRelationshipsWith(NodeWrapper a, NodeWrapper b) {
		return relationshipsWith(b, a.getRelationships()) > 0;
	}

	public static void assertNoRels(NodeWrapper a, NodeWrapper b) {
		assert (!hasAnyRelationshipsWith(a, b));
	}

	public static void assertHasSingleRel(NodeWrapper a, NodeWrapper b, RelationTypesInterface r, Direction d) {
		assertEquals(1, relationshipsWith(b, a.getRelationships(r, d)));
	}

	public static NodeWrapper getNodePredicate(NodeWrapper previous, RelationTypesInterface r,
			Predicate<NodeWrapper> pred) {
		for (RelationshipWrapper rel : previous.getRelationships(EdgeDirection.OUTGOING, r))
			if (pred.test(rel.getEndNode()))
				return rel.getEndNode();
		throw new IllegalArgumentException(String.format("No nodes for Rel:s%, with this previous and predicate", r));
	}

	public static void printNodeInfo(NodeWrapper... nodes) {
		for (NodeWrapper n : nodes)
			System.out.println(NodeUtils.nodeToString(n));
	}

	public static void printNodeInfo(Iterable<NodeWrapper> nodes) {
		for (NodeWrapper n : nodes)
			System.out.println(NodeUtils.nodeToString(n));
	}*/
}

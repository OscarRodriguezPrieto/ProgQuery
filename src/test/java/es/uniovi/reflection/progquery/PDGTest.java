package es.uniovi.reflection.progquery;

public class PDGTest extends OldPluginBasedTest {
	/* TEST DESIGNED TO USE EMBEEDDED, MUST BE RE-IMPLEMENTED
	private static List<Node> getIdsAndMemberSelections(Node assign, GraphDatabaseService graphDB) {
		TraversalDescription td = graphDB.traversalDescription().expand(new PathExpander() {

			@Override
			public Iterable<Relationship> expand(Path arg0, BranchState arg1) {
				Node endNode = arg0.endNode();
				if (endNode.hasLabel(NodeTypes.NEW_INSTANCE))
					return endNode.getRelationships(RelationTypes.NEW_CLASS_ARGUMENTS, RelationTypes.NEW_CLASS_BODY,
							RelationTypes.NEW_CLASS_TYPE_ARGUMENTS);
				else
					return endNode.getRelationships(Direction.OUTGOING);
			}

			@Override
			public PathExpander reverse() {
				return this;
			}
		}).evaluator(new Evaluator() {
			@Override
			public Evaluation evaluate(Path path) {
				Node currentNode = path.endNode();
				// System.out.println("TRAVERSING----");
				// System.out.println(NodeUtils.nodeToString(currentNode));
				return Evaluation.of(
						currentNode.hasLabel(NodeTypes.IDENTIFIER) || currentNode.hasLabel(NodeTypes.MEMBER_SELECTION),
						true);
			}
		});
		List<Node> res = new ArrayList<Node>();
		td.traverse(assign).nodes().forEach(res::add);
		return res;
	}

	@Test
	public void test() {
		try (Result result = graphDb.execute(
				"MATCH (left)-[:ASSIGNMENT_LHS | :HAS_VARIABLEDECL_INIT]-(assign)-[:ASSIGNMENT_RHS | :INITIALIZATION_EXPR]->(right) RETURN  assign,  left, right ORDER BY assign.lineNumber, assign.position");
				Result attrs = graphDb.execute(
						"MATCH (classDec)-[:DECLARES_FIELD]->(attrDec) RETURN attrDec ORDER BY classDec.simpleName,attrDec.lineNumber")) {

			List<Node> assignList = TestUtils.listFromResult(result), attrList = TestUtils.listFromResult(attrs);
			Node methodParam = TestUtils
					.getNodePredicate(
							attrList.get(0).getSingleRelationship(RelationTypes.DECLARES_FIELD, Direction.INCOMING)
									.getStartNode(),
							RelationTypes.DECLARES_METHOD, n -> n.getProperty("name").equals("m"))
					.getSingleRelationship(RelationTypes.HAS_METHODDECL_PARAMETERS, Direction.OUTGOING).getEndNode();
			List<List<Node>> idsAndMembers = assignList.stream().map(n -> getIdsAndMemberSelections(n, graphDb))
					.collect(Collectors.toList());
			// Orden, primero izquierda, luego derecha, pero de derecha a
			// izquierda en memberAccess
			for (List<Node> idsAndMemberSelections : idsAndMembers)
				idsAndMemberSelections.sort((n1, n2) -> {
					int lineDiff = Integer.parseInt(n1.getProperty("lineNumber").toString())
							- Integer.parseInt(n2.getProperty("lineNumber").toString());
					return lineDiff == 0 ? Integer.parseInt(n1.getProperty("position").toString())
							- Integer.parseInt(n2.getProperty("position").toString()) : lineDiff;
				});
			// System.out.println(NodeUtils.nodeToString(assignList.get(2)));
			// System.out.println(NodeUtils.nodeToString(assignList.get(3)));
			Node varLocal = assignList.get(3)
					.getSingleRelationship(RelationTypes.HAS_VARIABLEDECL_INIT, Direction.INCOMING).getStartNode();

			// System.out.println(NodeUtils.nodeToString(assignList.get(1)));
			// System.out.println(NodeUtils.nodeToString(methodParam));

			// OJO EL TRAVERSAL CON IDS COMO PDG2 o new PDG2 l�nea 16
			// Line 15
			TestUtils.assertHasSingleRel(methodParam, assignList.get(2), PDGRelationTypes.MODIFIED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(0), idsAndMembers.get(2).get(1), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);

			// Line 16
			TestUtils.assertHasSingleRel(varLocal, assignList.get(3), PDGRelationTypes.MODIFIED_BY, Direction.OUTGOING);
			// Line 18
			TestUtils.assertHasSingleRel(varLocal, assignList.get(5), PDGRelationTypes.STATE_MODIFIED_BY,
					Direction.OUTGOING);

			TestUtils.assertHasSingleRel(attrList.get(2), assignList.get(1), PDGRelationTypes.MODIFIED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(3), assignList.get(5), PDGRelationTypes.STATE_MODIFIED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(1), assignList.get(5), PDGRelationTypes.STATE_MODIFIED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(0), assignList.get(6), PDGRelationTypes.MODIFIED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(varLocal, idsAndMembers.get(5).get(2), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(3), idsAndMembers.get(5).get(1), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(0), idsAndMembers.get(5).get(3), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);

			TestUtils.assertHasSingleRel(varLocal, idsAndMembers.get(5).get(5), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(1), idsAndMembers.get(5).get(0), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(3), idsAndMembers.get(5).get(4), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);
			// Line 19

			TestUtils.assertHasSingleRel(attrList.get(0), assignList.get(6), PDGRelationTypes.MODIFIED_BY,
					Direction.OUTGOING);
			// When f(assig) both left and right side of assig are used, the
			// same as a=assig
			TestUtils.assertHasSingleRel(attrList.get(0), idsAndMembers.get(6).get(0), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(0), idsAndMembers.get(5).get(3), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);

			TestUtils.assertHasSingleRel(attrList.get(3), idsAndMembers.get(6).get(4), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(varLocal, idsAndMembers.get(6).get(5), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);
			TestUtils.assertHasSingleRel(attrList.get(1), idsAndMembers.get(6).get(2), PDGRelationTypes.USED_BY,
					Direction.OUTGOING);

		}
	}
*/
}

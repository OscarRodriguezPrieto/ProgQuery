package test;

public abstract class PluginBasedTestWithCommands {

	private String commandName, initialQuery;
	private int numberNodes;

	public PluginBasedTestWithCommands(String commandName, String initialQuery, int numberNodes) {
		super();
		this.commandName = commandName;
		this.initialQuery = initialQuery;
		this.numberNodes = numberNodes;
	}
/* TEST DESIGNED TO USE EMBEEDDED, MUST BE RE-IMPLEMENTED
	protected GraphDatabaseService graphDb;

	private Transaction transaction;
	protected List<Node> nodesToTest;

	private void runCommand(String command) throws IOException {
		Process process = Runtime.getRuntime().exec(command);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String s;
		int i = 0;
		while ((s = stdInput.readLine()) != null) {
			i += s.length();
		}
	}




	@Before
	public void prepareTestDatabase() throws Exception {
		runCommand(commandName);
		graphDb = DatabaseFachade.getDB();
		transaction = graphDb.beginTx();
		// System.out.println(graphDb.execute(initialQuery).resultAsString());
		nodesToTest = TestUtils.listFromResult(graphDb.execute(initialQuery));
		assertEquals(numberNodes, nodesToTest.size());
	}

	@After
	public void destroyTestDatabase() {
		// Result result = graphDb.execute(
		// "start n=node(*) MATCH m-[r:CFG_ENTRY | CFG_END_OF |
		// CFG_NEXT_CONDITION | CFG_NEXT_STATEMENT | CFG_NEXT_COND_IF_TRUE |
		// CFG_NEXT_STATEMENT_IF_TRUE | CFG_NEXT_COND_IF_FALSE |
		// CFG_NEXT_STATEMENT_IF_FALSE]->n RETURN m,r,n");
		// Result result = graphDb.execute(MainQuery.ALL_NODES);
		// System.out.println(result.resultAsString());

		graphDb.execute(MainQuery.DELETE_ALL);
		try {
			transaction.success();
		} finally {
			transaction.close();
		}
		graphDb.shutdown();
	}

	protected void hasSingleRel(int starting, int end, RelationTypesInterface r) {
		hasSingleRel(starting, end, r, Direction.OUTGOING);
	}

	protected void hasSingleRel(int starting, int end, RelationTypesInterface r, Direction d) {
		TestUtils.assertHasSingleRel(nodesToTest.get(starting), nodesToTest.get(end), r, d);
	}

	protected void assertNext(int starting, int end, RelationTypesInterface r, NodeTypes typeNext) {
		assertNext(starting, r, Direction.OUTGOING, n -> n.equals(nodesToTest.get(end)) && n.hasLabel(typeNext));
	}

	protected void assertNext(Node starting, int end, RelationTypesInterface r, NodeTypes typeNext) {
		assertNext(starting, r, Direction.OUTGOING, n -> n.equals(nodesToTest.get(end)) && n.hasLabel(typeNext));
	}

	protected void assertNext(Node starting, Node end, RelationTypesInterface r, NodeTypes typeNext) {
		assertNext(starting, r, Direction.OUTGOING, n -> n.equals(end) && n.hasLabel(typeNext));
	}

	protected void assertNext(int starting, RelationTypesInterface r, Direction d, Predicate<Node> p) {
		TestUtils.assertHasNextWith(nodesToTest.get(starting), r, d, p);
	}

	protected void assertNext(Node starting, RelationTypesInterface r, Direction d, Predicate<Node> p) {
		TestUtils.assertHasNextWith(starting, r, d, p);
	}

	protected void assertOneRelBetweenMany(int starting, int end, RelationTypesInterface r, Predicate<Relationship> p) {
		TestUtils.justOneRelationshipWith(nodesToTest.get(starting), nodesToTest.get(end), r, p);
	}

	protected void assertNRelsBetweenMany(Node starting, Node end, RelationTypesInterface r, Predicate<Relationship> p,
			int n) {
		TestUtils.justNRelationshipsWith(starting, end, r, p, n);
	}

	protected void assertOneRelBetweenMany(Node starting, int end, RelationTypesInterface rel, NodeTypes nodeType,
			Predicate<Relationship> p) {
		assertOneRelBetweenMany(starting, nodesToTest.get(end), rel, nodeType, p);
	}

	protected void assertOneRelBetweenMany(Node starting, Node end, RelationTypesInterface rel, NodeTypes nodeType,
			Predicate<Relationship> p) {
		TestUtils.justOneRelationshipWith(starting, end, rel, r -> r.getEndNode().hasLabel(nodeType) && p.test(r));
	}

	protected void assertOneThrowsRel(int starting, int end, RelationTypesInterface rt, NodeTypes nodeType,
			String exType) {
		assertOneRelBetweenMany(starting, end, rt, r -> r.getEndNode().hasLabel(nodeType)
				&& r.getProperty("exceptionType").toString().contentEquals(exType));
	}

	protected void assertOneThrowsRel(int starting, Node end, RelationTypesInterface rt, NodeTypes nodeType,
			String exType) {
		assertOneRelBetweenMany(nodesToTest.get(starting), end, rt, nodeType,
				r -> r.getProperty("exceptionType").toString().contentEquals(exType));
	}

	protected void printNode(int index) {
		System.out.println(NodeUtils.nodeToString(nodesToTest.get(index)));

	}

	protected void printNode(Node n) {
		System.out.println(NodeUtils.nodeToString(n));

	}*/
}

package test;

public abstract class OldPluginBasedTest {
	/* TEST DESIGNED TO USE EMBEEDDED, MUST BE RE-IMPLEMENTED
	protected GraphDatabaseService graphDb;
	private static final String[] TEST_COMMANDS = { "pdgTest.bat" };

	private int testCounter = 0;
	private Transaction transaction;

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
		runCommand(TEST_COMMANDS[0]);
		graphDb = DatabaseFachade.getDB();
		transaction = graphDb.beginTx();
		// System.out.println(graphDb.execute(MainQuery.ALL_NODES).resultAsString());
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
*/
}

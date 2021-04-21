package es.uniovi.reflection.progquery;

public abstract class GDBAPIBasedTest {

/* TEST DESIGNED TO USE EMBEEDDED, MUST BE RE-IMPLEMENTED
	protected GraphDatabaseService graphDb;

	private Transaction transaction;

	public abstract String getFileName();

	@Before
	public void prepareTestDatabase() throws Exception {
		graphDb = DatabaseFachade.getDB();
		JavacTaskImpl task = CompilerUtils.getTask(getFileName());

		List<? extends CompilationUnitTree> parse = (List<? extends CompilationUnitTree>) task.parse();
		task.analyze();

		CompilationUnitTree u = parse.get(0);
		Tree t = u.getTypeDecls().get(0);
		JavacInfo.setJavacInfo(new JavacInfo(u, task));
		DatabaseFachade.setDB(graphDb);
		transaction = graphDb.beginTx();
		graphDb.execute(MainQuery.DELETE_ALL);
		ASTAuxiliarStorage es.uniovi.reflection.progquery.ast = new ASTAuxiliarStorage();
		Node cuNode = DatabaseFachade.createSkeletonNode(u, NodeTypes.COMPILATION_UNIT);
		PDGVisitor pdgUtils;
		new ASTTypesVisitor((ClassTree) t, true, pdgUtils = new PDGVisitor(), es.uniovi.reflection.progquery.ast, cuNode).scan(u,
				Pair.createPair(cuNode, null));
		es.uniovi.reflection.progquery.ast.doDynamicMethodCallAnalysis();
		es.uniovi.reflection.progquery.ast.doInterproceduralPDGAnalysis();
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
		try {
			transaction.success();
		} finally {
			transaction.close();
		}
		graphDb.shutdown();
	}
*/ 
}

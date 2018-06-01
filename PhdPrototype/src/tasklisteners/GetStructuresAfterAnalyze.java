package tasklisteners;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;

import ast.ASTAuxiliarStorage;
import database.DatabaseFachade;
import database.nodes.NodeTypes;
import database.relations.PartialRelation;
import database.relations.RelationTypes;
import utils.JavacInfo;
import utils.Pair;
import visitors.ASTTypesVisitor;
import visitors.PDGVisitor;

public class GetStructuresAfterAnalyze implements TaskListener {
	private static final boolean DEBUG = false;
	private final JavacTask task;
	private final GraphDatabaseService graphDb;
	private Map<CompilationUnitTree, Integer> classCounter = new HashMap<CompilationUnitTree, Integer>();
	// private Set<CompilationUnitTree> unitsInTheSameFile = new
	// HashSet<CompilationUnitTree>();
	private boolean started = false;
	private boolean firstClass = true;

	private int counter = 0;

	private Transaction transaction;
	private Pair<PartialRelation<RelationTypes>, Object> argument;
	private CompilationUnitTree cu;
	private PDGVisitor pdgUtils = new PDGVisitor();
	private ASTAuxiliarStorage ast = new ASTAuxiliarStorage();

	public GetStructuresAfterAnalyze(JavacTask task, GraphDatabaseService graphDb) {
		this.task = task;
		this.graphDb = graphDb;
	}

	@Override
	public void finished(TaskEvent arg0) {
		if (DEBUG)
			System.out.println("FINISHING " + arg0.getKind());
		CompilationUnitTree u = arg0.getCompilationUnit();
		if (arg0.getKind() == Kind.PARSE)
			classCounter.put(u, u.getTypeDecls().size());
		else if (arg0.getKind() == Kind.ANALYZE) {

			started = true;
			classCounter.put(u, classCounter.get(u) - 1);

			if (firstClass) {
				firstClass = false;
				firstScan(u, u.getTypeDecls().get(counter++));
			} else
				scan(u.getTypeDecls().get(counter++), false);

			if (classCounter.get(u) == 0) {
				classCounter.remove(u);
				firstClass = true;
				counter = 0;
				// System.out.println("AFTER ANALYZE");
				// System.out.println(ast.mm + "\n" + ast.b + "\n" + ast.s1);
				transaction.success();
				transaction.close();

			}

		}
		if (DEBUG)
			System.out.println("FINISHED " + arg0.getKind());
	}

	private void firstScan(CompilationUnitTree u, Tree typeDeclaration) {
		JavacInfo.setJavacInfo(new JavacInfo(u, task));
		DatabaseFachade.setDB(graphDb);

		String fileName = u.getSourceFile().toUri().toString();
		transaction = DatabaseFachade.beginTx();
		Node compilationUnitNode = DatabaseFachade.createSkeletonNode(u, NodeTypes.COMPILATION_UNIT);
		compilationUnitNode.setProperty("fileName", fileName);

		argument = Pair.createPair(compilationUnitNode, RelationTypes.CU_PACKAGE_DEC);
		cu = u;
		scan(typeDeclaration, true);

	}

	private void scan(Tree typeDeclaration, boolean first) {
		if (DEBUG) {
			System.err.println("-*-*-*-*-*-*-* NEW TYPE DECLARATION AND VISITOR-*-*-*-*-*-*-*");
			System.err.println(cu.getSourceFile().getName());
			System.out.println("Final State:\n");

			System.out.println(typeDeclaration);
		}
		new ASTTypesVisitor(typeDeclaration, first, pdgUtils, ast).scan(cu, argument);
	}

	@Override
	public void started(TaskEvent arg0) {
		if (DEBUG)
			System.out.println("STARTING " + arg0.getKind());
		if (arg0.getKind() == Kind.GENERATE && started) {

			if (classCounter.size() == 0) {
				// System.out.println("BEFORE CFG");
				// System.out.println(ast.mm + "\n" + ast.b + "\n" + ast.s1);
				cfgAnalysis();
				dynamicMethodCallAnalysis();
				interproceduralPDGAnalysis();
				shutdownDatabase();
				started = false;
			}
		}
		if (DEBUG)
			System.out.println("STARTED " + arg0.getKind());

	}

	private void interproceduralPDGAnalysis() {

		Transaction transaction = DatabaseFachade.beginTx();
		ast.doInterproceduralPDGAnalysis(pdgUtils.getMethodsMutateThisAndParams(), pdgUtils.getParamsMutatedInMethods(),
				pdgUtils.getParamsMayMutateInMethods(), pdgUtils.getThisRefsOfMethods());
		transaction.success();
		transaction.close();
	}

	private void cfgAnalysis() {
		Transaction transaction = DatabaseFachade.beginTx();
		ast.doCfgAnalysis();
		transaction.success();
		transaction.close();

	}

	private void dynamicMethodCallAnalysis()
	{
		Transaction transaction = DatabaseFachade.beginTx();
		ast.doDynamicMethodCallAnalysis();
		transaction.success();
		transaction.close();
	}

	public void shutdownDatabase() {
		if (DEBUG)
			System.out.println("SHUTDOWN THE DATABASE");
		graphDb.shutdown();
		if (DEBUG)
			System.out.println("SHUTDOWN THE DATABASE ENDED");

	}

}

package tasklisteners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

public class GetAllStructuresAfterAnalyze implements TaskListener {
	private static final boolean DEBUG = true;
	private final JavacTask task;
	private final GraphDatabaseService graphDb;
	private Map<CompilationUnitTree, Integer> classCounter = new HashMap<CompilationUnitTree, Integer>();
	private Set<CompilationUnitTree> cuLists = new HashSet<CompilationUnitTree>();
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

	public GetAllStructuresAfterAnalyze(JavacTask task, GraphDatabaseService graphDb) {
		this.task = task;
		this.graphDb = graphDb;
		System.out.println(task.getClass());
	}

	@Override
	public void finished(TaskEvent arg0) {
		if (DEBUG)
			System.out.println("FINISHING " + arg0.getKind());
		CompilationUnitTree u = arg0.getCompilationUnit();
		if (arg0.getKind() == Kind.PARSE) {
			classCounter.put(u, u.getTypeDecls().size());
			cuLists.add(u);
		} else if (arg0.getKind() == Kind.ANALYZE) {

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

				// Tomamos como transacción cada archivo
				try {
					transaction.success();
				} finally {
					transaction.close();
				}
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
		// pdgVisitor.scan(typeDeclaration, null);
	}

	@Override
	public void started(TaskEvent arg0) {
		if (DEBUG)
			System.out.println("STARTING " + arg0.getKind());
		if (arg0.getKind() == Kind.GENERATE && started) {

			if (classCounter.size() == 0) {
				shutdownDatabase();
				started = false;
				System.out.println("CULIST SIZE:" + cuLists.size());
				for (CompilationUnitTree cu : cuLists) {
					for (Tree t : cu.getTypeDecls())
						new ASTTypesVisitor(t, firstClass, pdgUtils, ast).scan(t, null);
					System.out.println(cu);
				}
			}
		}
		if (DEBUG)
			System.out.println("STARTED " + arg0.getKind());

	}

	public void shutdownDatabase() {
		if (DEBUG)
			System.out.println("SHUTDOWN THE DATABASE");
		graphDb.shutdown();
		if (DEBUG)
			System.out.println("SHUTDOWN THE DATABASE ENDED");

	}

}

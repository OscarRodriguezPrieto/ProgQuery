package tasklisteners;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import visitors.ASTTypesVisitor;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TaskEvent.Kind;
import database.DatabaseFachade;
import relations.NodeTypes;
import relations.RelationTypes;
import utils.GraphUtils;
import utils.JavacInfo;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> argument;

	public GetStructuresAfterAnalyze(JavacTask task, GraphDatabaseService graphDb) {
		this.task = task;
		this.graphDb = graphDb;
	}

	@Override
	public void finished(TaskEvent arg0) {
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

				// Tomamos como transacción cada archivo
				try {
					transaction.success();
				} finally {
					transaction.close();
				}
			}

		}
	}

	private void firstScan(CompilationUnitTree u, Tree typeDeclaration) {

		JavacInfo.setJavacInfo(new JavacInfo(u, task));
		DatabaseFachade.setDB(graphDb);

		String fileName = u.getSourceFile().toUri().toString();

		transaction = DatabaseFachade.beginTx();
		Node compilationUnitNode = DatabaseFachade.createSkeletonNode(u, NodeTypes.COMPILATION_UNIT);
		compilationUnitNode.setProperty("fileName", fileName);

		argument = Pair.createPair(u, compilationUnitNode, RelationTypes.CU_PACKAGE_DEC);
		scan(typeDeclaration, true);

	}

	private void scan(Tree typeDeclaration, boolean first) {
		// if (DEBUG) {
		CompilationUnitTree u = (CompilationUnitTree) argument.getFirst().getFirst().getFirst();

		if (DEBUG) {
			System.err.println("-*-*-*-*-*-*-* NEW TYPE DECLARATION AND VISITOR-*-*-*-*-*-*-*");
			System.err.println(u.getSourceFile().getName());
			// }
			// new ASTVisitor().scan(u, null);
			// new TypeHierarchyVisitor().scan(u, null);

			System.out.println("Final State:\n");

			System.out.println(typeDeclaration);
		}
		new ASTTypesVisitor(typeDeclaration, first).scan(u, argument);
		// new TotalVisitor(task, graphDb, new HashMap<String,
		// String>()).scan(u, null);
	}

	@Override
	public void started(TaskEvent arg0) {
		if (arg0.getKind() == Kind.GENERATE && started) {

			if (classCounter.size() == 0) {
				shutdownDatabase();
				started = false;
			}
		}
	}

	public void shutdownDatabase() {
		graphDb.shutdown();
	}

}

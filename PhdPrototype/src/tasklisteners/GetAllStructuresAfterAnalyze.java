package tasklisteners;

import org.neo4j.graphdb.GraphDatabaseService;

import org.neo4j.graphdb.factory.GraphDatabaseBuilder;

import visitors.ASTTypesVisitor;
import visitors.OldASTTypesVisitor;
import visitors.TotalVisitor;
import visitors.TypeHierarchyVisitor;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.Trees;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.tools.javac.tree.TreeInfo;

import cache.nodes.DefinitionCache;
import database.DatabaseFachade;
import utils.JavacInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GetAllStructuresAfterAnalyze implements TaskListener {
	private static final boolean DEBUG = false;
	private final JavacTask task;
	private final GraphDatabaseService graphDb;
	private Map<CompilationUnitTree, Integer> classCounter = new HashMap<CompilationUnitTree, Integer>();
	// private Set<CompilationUnitTree> unitsInTheSameFile = new
	// HashSet<CompilationUnitTree>();

	private boolean codeGeneration = false;

	public GetAllStructuresAfterAnalyze(JavacTask task, GraphDatabaseService graphDb) {
		this.task = task;
		this.graphDb = graphDb;
	}

	@Override
	public void finished(TaskEvent arg0) {

		CompilationUnitTree u = arg0.getCompilationUnit();
		if (arg0.getKind() == Kind.PARSE)
			classCounter.put(u, u.getTypeDecls().size());
		else if (arg0.getKind() == Kind.ANALYZE) {
			codeGeneration = true;
			int count;
			classCounter.put(u, count = classCounter.get(u) - 1);

			if (count == 0) {
				// if (DEBUG) {
				System.err.println("-*-*-*-*-*-*-* NEW COMPILATION UNIT AND VISITOR-*-*-*-*-*-*-*");
				System.err.println(u.getSourceFile().getName());
				// }
				JavacInfo.setJavacInfo(new JavacInfo(u, task));
				DatabaseFachade.setDB(graphDb);
				// new ASTVisitor().scan(u, null);
				// new TypeHierarchyVisitor().scan(u, null);
//				new ASTTypesVisitor(new ArrayList<Tree>()).scan(u, null);

				System.out.println("Removing Compilation Unit" + arg0.getCompilationUnit().hashCode());
				classCounter.remove(arg0.getCompilationUnit());
				// new TotalVisitor(task, graphDb, new HashMap<String,
				// String>()).scan(u, null);
			}
		}
	}

	@Override
	public void started(TaskEvent arg0) {
		if (arg0.getKind() == Kind.GENERATE && codeGeneration) {

			if (classCounter.size() == 0) {
				shutdownDatabase();
			}
		}
	}

	public void shutdownDatabase() {
		graphDb.shutdown();
	}

}

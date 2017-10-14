package tasklisteners;

import org.neo4j.graphdb.GraphDatabaseService;

import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import visitors.TotalVisitor;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

import cache.nodes.DefinitionCache;

import java.util.Map;

public class AfterAnalyze implements TaskListener {

	private final JavacTask task;
	private final GraphDatabaseBuilder graphDbBuilder;
	private final Map<String, String> cuProps;
	private final GraphDatabaseService graphDb;

	private boolean analizeFinished = false;

	public AfterAnalyze(JavacTask task, GraphDatabaseBuilder graphDbBuilder, Map<String, String> cuProps) {
		this.graphDbBuilder = graphDbBuilder;
		this.task = task;
		this.cuProps = cuProps;
		graphDb = graphDbBuilder.newGraphDatabase();
	}

	@Override
	public void finished(TaskEvent arg0) {
		if (arg0.getKind().toString().equals("ANALYZE")) {
			System.out.println("-*-*-*-*-*-*-* NEW COMPILATION UNIT AND VISITOR-*-*-*-*-*-*-*");
			CompilationUnitTree u = arg0.getCompilationUnit();
			new TotalVisitor(task, graphDb, cuProps).scan(u, null);
			analizeFinished = false;
		}
	}

	@Override
	public void started(TaskEvent arg0) {
		
		//SOLO SALE BIEN CON CLASSDEP/*.java y no con newTest/*.java
		if (arg0.getKind().equals(TaskEvent.Kind.GENERATE))
			if (analizeFinished) {
				System.out.println("GENERATE TASK WITHOUT PREVIOUS ANALIZE----->CLOSING THE DB");
				graphDb.shutdown();
			} else
				analizeFinished = true;

	}

	public void shutdownDatabase() {
		graphDb.shutdown();
	}

}

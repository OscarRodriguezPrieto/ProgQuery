package tasklisteners;

import org.neo4j.graphdb.GraphDatabaseService;

import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
<<<<<<< HEAD
import visitors.TotalVisitor;
=======
import visitors.WiggleVisitor;
>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

<<<<<<< HEAD
import cache.nodes.DefinitionCache;

=======
>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f
import java.util.Map;

public class AfterAnalyze implements TaskListener {

	private final JavacTask task;
	private final GraphDatabaseBuilder graphDbBuilder;
	private final Map<String, String> cuProps;
<<<<<<< HEAD
	private final GraphDatabaseService graphDb;

	private boolean analizeFinished = false;
=======
>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f

	public AfterAnalyze(JavacTask task, GraphDatabaseBuilder graphDbBuilder, Map<String, String> cuProps) {
		this.graphDbBuilder = graphDbBuilder;
		this.task = task;
		this.cuProps = cuProps;
<<<<<<< HEAD
		graphDb = graphDbBuilder.newGraphDatabase();
=======
>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f
	}

	@Override
	public void finished(TaskEvent arg0) {
<<<<<<< HEAD
		if (arg0.getKind().toString().equals("ANALYZE")) {
			System.out.println("-*-*-*-*-*-*-* NEW COMPILATION UNIT AND VISITOR-*-*-*-*-*-*-*");
			CompilationUnitTree u = arg0.getCompilationUnit();
			new TotalVisitor(task, graphDb, cuProps).scan(u, null);
			analizeFinished = false;
=======

		if (arg0.getKind().toString().equals("ANALYZE")) {
			CompilationUnitTree u = arg0.getCompilationUnit();
			GraphDatabaseService graphDb = graphDbBuilder.newGraphDatabase();
			new WiggleVisitor(task, graphDb, cuProps).scan(u, null);
			graphDb.shutdown();
>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f
		}
	}

	@Override
	public void started(TaskEvent arg0) {
<<<<<<< HEAD
		
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
=======

>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f
	}

}

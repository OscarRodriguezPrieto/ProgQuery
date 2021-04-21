package es.uniovi.reflection.progquery.tasklisteners;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;

public class ShutdownDBAfterAnalize implements TaskListener {

	private final JavacTask task;
	// private final GraphDatabaseBuilder graphDbBuilder;
	private int classCounter = 0;

	public ShutdownDBAfterAnalize(JavacTask task
	// , GraphDatabaseBuilder graphDbBuilder
	) {
		// this.graphDbBuilder = graphDbBuilder;
		this.task = task;
	}

	@Override
	public void finished(TaskEvent arg0) {
		System.out.println("FINISHING ONE TASK " + arg0.getKind());
		System.out.println(
				arg0.getCompilationUnit() == null ? null : arg0.getCompilationUnit().getSourceFile().getName());
		if (arg0.getKind().toString().equals("ANALYZE")) {
			System.out.println("-*-*-*-*-*-*-*  AFTER ANALIZE TASK -*-*-*-*-*-*-*");

		}
		if (arg0.getKind() == Kind.PARSE) {
			System.out.println("N�mero de clases (ClassTypes): " + arg0.getCompilationUnit().getTypeDecls().size());
			classCounter += arg0.getCompilationUnit().getTypeDecls().size();
		}
	}

	@Override
	public void started(TaskEvent arg0) {
		System.out.println("STARTING ONE TASK " + arg0.getKind());
		if (arg0.getKind() == Kind.GENERATE) {
			if (--classCounter == 0)
				System.out.println("APAGANDO.....");
		}
		System.out.println(
				arg0.getCompilationUnit() == null ? null : arg0.getCompilationUnit().getSourceFile().getName());
	}

}

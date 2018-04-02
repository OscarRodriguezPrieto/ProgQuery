
import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
//import org.neo4j.kernel.impl.util.FileUtils;

import com.sun.source.util.JavacTask;

import database.DatabaseFachade;
import tasklisteners.GetStructuresAfterAnalyze;

public class WiggleIndexerPlugin implements com.sun.source.util.Plugin {

	private static final String PLUGIN_NAME = "WiggleIndexerPlugin";

	@Override
	public void init(JavacTask task, String[] args) {
		task.addTaskListener(new GetStructuresAfterAnalyze(task, DatabaseFachade.getDB()));

	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	@Override
	public String getName() {
		return PLUGIN_NAME;
	}

}

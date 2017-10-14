import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
//import org.neo4j.kernel.impl.util.FileUtils;

import tasklisteners.AfterAnalyze;
import tasklisteners.GetAllStructuresAfterAnalyze;
import tasklisteners.GetStructuresAfterAnalyze;
import tasklisteners.ShutdownDBAfterAnalize;

import com.sun.source.util.JavacTask;

public class WiggleIndexerPlugin implements com.sun.source.util.Plugin {

	private static final String PLUGIN_NAME = "WiggleIndexerPlugin";
	private GraphDatabaseBuilder graphDbBuilder;

	private String wiggleDbPath;
	private String wiggleClearDb;

	@Override
	public void init(JavacTask task, String[] args) {
		createDb();
		task.addTaskListener(new GetStructuresAfterAnalyze(task, graphDbBuilder.newGraphDatabase()));

	}

	private String getDBPath() {
		String dbPath = System.getenv("WIGGLE_DB_PATH");
		if (dbPath == null)
			dbPath = "./neo4j/data/wiggle.db";
		return dbPath;
	}

	public void createDb() {

		this.wiggleDbPath = getDBPath();

		graphDbBuilder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(wiggleDbPath)
				.setConfig(GraphDatabaseSettings.node_keys_indexable, "nodeType")
				.setConfig(GraphDatabaseSettings.relationship_keys_indexable, "typeKind")
				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
				.setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true");
		// registerShutdownHook( graphDb );

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

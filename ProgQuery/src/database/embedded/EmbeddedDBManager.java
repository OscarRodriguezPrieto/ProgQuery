package database.embedded;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;


public class EmbeddedDBManager {
	private static final String DEFAULT_DB_PATH = "./neo4j/data/ProgQuery.db";

	public static GraphDatabaseService getNewEmbeddedDBService() {
		return new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(DEFAULT_DB_PATH))
				.setConfig(GraphDatabaseSettings.node_keys_indexable, "nodeType")
				.setConfig(GraphDatabaseSettings.relationship_keys_indexable, "typeKind")
				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
				.setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true").newGraphDatabase();
	}

	public static GraphDatabaseService getNewEmbeddedDBService(String dbPath) {
		return new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(dbPath))
				.setConfig(GraphDatabaseSettings.node_keys_indexable, "nodeType")
				.setConfig(GraphDatabaseSettings.relationship_keys_indexable, "typeKind")
				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
				.setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true").newGraphDatabase();
	}

	public static void setDB(GraphDatabaseService db) {
		graphDb = db;
	}

	private static GraphDatabaseService graphDb;

}

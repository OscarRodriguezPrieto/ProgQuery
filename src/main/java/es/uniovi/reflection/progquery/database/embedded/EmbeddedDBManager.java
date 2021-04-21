package es.uniovi.reflection.progquery.database.embedded;

import java.nio.file.Paths;

import org.neo4j.graphdb.GraphDatabaseService;

import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.EmbeddedGGDBServiceInsertion;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;

public class EmbeddedDBManager {
	public static void main(String[] args) {
		   DatabaseFachade
			.init( new EmbeddedGGDBServiceInsertion());
		   DatabaseFachade.CURRENT_INSERTION_STRATEGY.endAnalysis();
	   }
	private static final String DEFAULT_DB_PATH = "neo4j/data/ProgQuery.db";
	public static DatabaseManagementService manager;
	public static GraphDatabaseService getNewEmbeddedDBService() {
		return getNewEmbeddedDBService(DEFAULT_DB_PATH);
	}

	public static GraphDatabaseService getNewEmbeddedDBService(String dbPath) {
		String[] dirsInPath = dbPath.split("/");
		String dbName = dirsInPath[dirsInPath.length - 1];
		manager = new DatabaseManagementServiceBuilder(
				Paths.get(dbPath.substring(0, dbPath.length() - dbName.length()))).build();
		GraphDatabaseService graphDb = manager.database(dbName);
		registerShutdownHook(manager);
		return graphDb;
//				.setConfig(GraphDatabaseSettings.node_keys_indexable, "nodeType")
//				.setConfig(GraphDatabaseSettings.relationship_keys_indexable, "typeKind")
//				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
//				.setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true").newGraphDatabase();
	}

	public static void setDB(GraphDatabaseService db) {
		graphDb = db;
	}

	private static GraphDatabaseService graphDb;

	private static void registerShutdownHook(final DatabaseManagementService managementService) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				managementService.shutdown();
			}
		});
	}
}

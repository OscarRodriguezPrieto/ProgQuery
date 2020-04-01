package database.embedded;

import java.io.File;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;


public class EmbeddedDBManager {
	private static final String DEFAULT_DB_PATH = "C:\\Users\\Oskar\\Desktop\\Thesis\\ProgQuery\\ProgQuery\\ProgQuery";
	private static final String DEFAULT_DB_NAME= "neo4j";
	 static DatabaseManagementService managementService;
	   

	public static GraphDatabaseService getNewEmbeddedDBService() {
 
        managementService = new DatabaseManagementServiceBuilder(new File( DEFAULT_DB_PATH )).build();
	        return managementService.database( DEFAULT_DB_NAME );
		
		
		}

	public static GraphDatabaseService getNewEmbeddedDBService(String dbPath) {
        managementService = new DatabaseManagementServiceBuilder(new File( dbPath )).build();
        return managementService.database( DEFAULT_DB_NAME );
	
			}

	public static void setDB(GraphDatabaseService db) {
		graphDb = db;
	}

	private static GraphDatabaseService graphDb;
public static void shutdownCurrent() {
	managementService.shutdown();
}
}

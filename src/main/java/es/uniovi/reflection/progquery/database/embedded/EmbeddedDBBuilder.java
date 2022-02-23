package es.uniovi.reflection.progquery.database.embedded;

import java.nio.file.Paths;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.graphdb.GraphDatabaseService;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;

public class EmbeddedDBBuilder {

    private static final String DEFAULT_DB_DIR = "", DEFAULT_DB_NAME = "neo4j";

    private DatabaseManagementService manager;
    private String dbName;

    public EmbeddedDBBuilder(String dbDir, String dbName) {
        this.dbName = dbName;
        manager = configuration(new DatabaseManagementServiceBuilder(
                Paths.get(dbDir)), dbName);
        registerShutdownHook(manager);
    }

    public EmbeddedDBBuilder() {
        this(DEFAULT_DB_DIR, DEFAULT_DB_NAME);
    }


    public GraphDatabaseService getNewEmbeddedDBService() {

       return manager.database(dbName);
    }


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

    private boolean existsDatabase(String dbName) {
        for (String existingName : manager.listDatabases())
            if (existingName.contentEquals(dbName))
                return true;
        return false;
    }

    public static DatabaseManagementService configuration(DatabaseManagementServiceBuilder dbmsBuilder, String dbName) {

        return dbmsBuilder
                .setConfig(GraphDatabaseSettings.default_database, dbName)
                .build();
    }

    public void shutdownManager() {


        manager.shutdown();
    }
}

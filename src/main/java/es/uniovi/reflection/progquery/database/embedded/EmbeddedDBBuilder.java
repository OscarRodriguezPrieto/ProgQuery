package es.uniovi.reflection.progquery.database.embedded;

import java.nio.file.Paths;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.graphdb.GraphDatabaseService;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;

public class EmbeddedDBBuilder {

    private static final String DEFAULT_DB_DIR = "", DEFAULT_DB_NAME = "neo4j";

    private DatabaseManagementService manager;
    private String database_name;

    public EmbeddedDBBuilder(String database_directory, String database_name) {
        this.database_name = database_name;
        manager = configuration(new DatabaseManagementServiceBuilder(Paths.get(database_directory)), database_name);
        registerShutdownHook(manager);
    }

    public EmbeddedDBBuilder() {
        this(DEFAULT_DB_DIR, DEFAULT_DB_NAME);
    }


    public GraphDatabaseService getNewEmbeddedDBService() { return manager.database(database_name); }


    private static void registerShutdownHook(final DatabaseManagementService managementService) {
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

    public static DatabaseManagementService configuration(DatabaseManagementServiceBuilder dbmsBuilder, String database_name) {
        return dbmsBuilder
                .setConfig(GraphDatabaseSettings.default_database, database_name)
                .build();
    }

    public void shutdownManager() { manager.shutdown(); }
}

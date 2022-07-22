package es.uniovi.reflection.progquery.database;

import es.uniovi.reflection.progquery.database.insertion.lazy.DriverLazyInsertionService;
import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.database.manager.NEO4JServerManager;

public class Neo4jDriverLazyInsertion extends NotPersistentLazyInsertion {
    private final int MAX_OPERATIONS_PER_TRANSACTION;
    private final String ADDRESS, USER, PASS, DB_NAME;

    public static final String NEO4J_DEFAULT_DB = "neo4j";
    private static final int DEFAULT_MAX = 80_000;
    private NEO4JServerManager managerForRetrievedNodes;
    private boolean isMultiModule = false;

    public Neo4jDriverLazyInsertion(String connectionString) {
        this(DEFAULT_MAX, connectionString);
    }

    public Neo4jDriverLazyInsertion(int maxNodes, String connectionString) {
        super();
        MAX_OPERATIONS_PER_TRANSACTION = maxNodes;
        String[] connectionData = connectionString.split(";");
        ADDRESS = connectionData[2];
        USER = connectionData[0];
        PASS = connectionData[1];
        DB_NAME = connectionData.length == 4 ? connectionData[3] : NEO4J_DEFAULT_DB;
    }

    public Neo4jDriverLazyInsertion(String host, String port, String user, String password, String database,
                                    String max_operations_transaction) {

        super();
        MAX_OPERATIONS_PER_TRANSACTION = Integer.parseInt(max_operations_transaction);
        ADDRESS = host + ":" + port;
        USER = user;
        PASS = password;
        DB_NAME = database;
    }

    @Override
    public void startAnalysis() {
        NEO4JServerManager.startDriver(ADDRESS, USER, PASS);
    }

    @Override
    public void endAnalysis() {
        DriverLazyInsertionService
                .updateRetrievedNodesAndRels(InfoToInsert.INFO_TO_INSERT, MAX_OPERATIONS_PER_TRANSACTION, DB_NAME);

        DriverLazyInsertionService
                .insertToSpecificDB(InfoToInsert.INFO_TO_INSERT, MAX_OPERATIONS_PER_TRANSACTION, DB_NAME);
        if (isMultiModule)
            managerForRetrievedNodes.close();
        NEO4JServerManager.closeDriver();
    }

    @Override
    public NEO4JManager getNewManager() {
        return new NEO4JServerManager(DB_NAME);
    }

    @Override
    public void newMultiModuleProject() {
        isMultiModule = true;
        managerForRetrievedNodes = new NEO4JServerManager(DB_NAME);
    }

    public NEO4JServerManager getManagerForRetrievedNodes() {
        return managerForRetrievedNodes;
    }
}

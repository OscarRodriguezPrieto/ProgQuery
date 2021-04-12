package database.manager;

import node_wrappers.Neo4jLazyServerDriverNode;
import node_wrappers.NodeWrapper;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.List;

public class NEO4JServerManager implements NEO4JManager {
    public static final String NEO4J_PROTOCOL = "neo4j://";
    public static final String NEO4J_DEFAULT_DB = "neo4j";

    private final Driver driver;
    private Session session;
@Override
    public NodeWrapper getProgramFromDB(String programId, String userId) {
        List<Record> programsIfAny = executeQuery(String.format("MATCH (p:PROGRAM) WHERE p.ID='%s' AND p.USER_ID='%s' RETURN ID(p)", programId, userId));
        if (programsIfAny.size() == 0)
            return null;
        return new Neo4jLazyServerDriverNode(programsIfAny.get(0).get(0).asLong());
    }

    public NEO4JServerManager(String address, String user, String password) {
        this(address,user,password, NEO4JServerManager.NEO4J_DEFAULT_DB);
    }

    public NEO4JServerManager(String address, String user, String password, String db_name) {
        Config config = Config.builder().withLogging(Logging.none()).build();
        driver = GraphDatabase.driver(NEO4J_PROTOCOL + address,AuthTokens.basic(user, password),config);
        session = driver.session(SessionConfig.forDatabase(db_name));
    }

    private List<Record> executeQuery(String query) {
        return session.writeTransaction(tx -> tx.run(query).list());
    }

    /*
    public List<Record> executeQuery(String query, String[] userIds, String[] programIds) {
        return session.writeTransaction(tx -> {
            Result result = tx.run(CypherAdapter.limitQuery(query, userIds, programIds));
            return result.list();
        });
    }*/

    @Override
    public void close() throws Exception {
        session.close();
        driver.close();
    }
}

package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.types.ExternalTypeDefKey;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.List;
import java.util.stream.Stream;

public class NEO4JServerManager implements NEO4JManager {
    public static final String NEO4J_PROTOCOL = "neo4j://";
    public static final String NEO4J_DEFAULT_DB = "neo4j";

    private final Driver driver;
    private Session session;


    @Override
    public NodeWrapper getProgramFromDB(String programId, String userId) {
        List<Record> programsIfAny =
                executeQuery(String.format("MATCH (p:PROGRAM{ID:'%s',USER_ID:'%s'}) RETURN ID(p)", programId, userId));
        if (programsIfAny.size() == 0)
            return null;
        return new Neo4jLazyNode(programsIfAny.get(0).get(0).asLong());
    }

    @Override
    public Stream<Pair<NodeWrapper, ExternalTypeDefKey>> getDeclaredMethodsFrom(String programID, String userID) {
        return null;
    }
  public static  final String QUERY =
            "MATCH (p:PROGRAM{ID:\"%s\", USER_ID:\"%s\"}) -[:PROGRAM_DECLARES_PACKAGE]->(:PACKAGE)" +
                    "-[:PACKAGE_HAS_COMPILATION_UNIT]-> (cu:COMPILATION_UNIT)" +
                    "-[:HAS_TYPE_DEF|HAS_INNER_TYPE_DEF]->" +
                    "(type{isDeclared:true}) WHERE type.accessLevel='public' or type.accessLevel='protected' and " +
                    "not type" + ".isFinal  RETURN %s, type.fullyQualifiedName, cu.fileName";
    @Override
    public Stream<Pair<NodeWrapper, ExternalTypeDefKey>> getDeclaredTypeDefsFrom(String programID, String userID) {

        final int TYPE_ID = 0, TYPE_NAME = 1, FILE_NAME = 2;
        final String TYPE_ID_STR="ID(type)";
        return executeQuery(String.format(QUERY,programID,userID, TYPE_ID_STR)).stream().map(record -> Pair
                .create(new Neo4jLazyNode(record.get(TYPE_ID).asLong()),
                        new ExternalTypeDefKey(record.get(FILE_NAME).asString(), record.get(TYPE_NAME).asString())));

    }

    public NEO4JServerManager(String address, String user, String password) {
        this(address, user, password, NEO4JServerManager.NEO4J_DEFAULT_DB);
    }

    public NEO4JServerManager(String address, String user, String password, String db_name) {
        Config config = Config.builder().withLogging(Logging.none()).build();
        driver = GraphDatabase.driver(NEO4J_PROTOCOL + address, AuthTokens.basic(user, password), config);
        session = driver.session(SessionConfig.forDatabase(db_name));
    }

    private List<Record> executeQuery(String query) {
        return session.writeTransaction(tx -> tx.run(query).list());
    }


    @Override
    public void close() {
        session.close();
        driver.close();
    }
}

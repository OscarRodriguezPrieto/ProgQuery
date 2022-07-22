package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.types.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.types.ExternalTypeDefKey;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class NEO4JServerManager implements NEO4JManager {
    public static final String NEO4J_PROTOCOL = "neo4j://";

    private static Driver driver;
    private Session session;

    public static void startDriver(String address, String user, String password){
        Config config = Config.builder().withLogging(Logging.none()).build();
        driver = GraphDatabase.driver(NEO4J_PROTOCOL + address, AuthTokens.basic(user, password), config);
    }
    public static void closeDriver(){
        driver.close();
    }


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

    public static final String DEFINED_TYPES_QUERY =
            "MATCH (p:PROGRAM{ID:\"%s\", USER_ID:\"%s\"}) -[:PROGRAM_DECLARES_PACKAGE]->(:PACKAGE)" +
                    "-[:PACKAGE_HAS_COMPILATION_UNIT]-> (cu:COMPILATION_UNIT)" +
                    "-[:HAS_TYPE_DEF|HAS_INNER_TYPE_DEF]->" +
                    "(type:TYPE_DEFINITION{isDeclared:true}) WHERE type.accessLevel='public' or type" +
                    ".accessLevel='protected' and " +
                    "not type.isFinal  RETURN %s, type.fullyQualifiedName, cu.fileName";

    public static final String NON_DEFINED_TYPES_QUERY =
            "MATCH (p:PROGRAM{ID:\"%s\", USER_ID:\"%s\"}) CALL apoc.path.subgraphNodes(p, " +
                    "{labelFilter:\">TYPE_DEFINITION\", minLevel:0}) YIELD node WITH node as type WHERE NOT type" +
                    ".isDeclared RETURN  %s, type.fullyQualifiedName";

    @Override
    public Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getDeclaredTypeDefsFrom(String programID, String userID) {

        final int TYPE_ID = 0, TYPE_NAME = 1, FILE_NAME = 2;
        final String TYPE_ID_STR = "ID(type)";
          return      executeQuery(String.format(DEFINED_TYPES_QUERY, programID, userID, TYPE_ID_STR)).stream()
                        .map(record -> Pair.create(new Neo4jLazyNode(record.get(TYPE_ID).asLong()),
                                new ExternalTypeDefKey(record.get(FILE_NAME).asString(),
                                        record.get(TYPE_NAME).asString())));
    }
    @Override
    public Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getNotDeclaredTypesFrom(String programID, String userID) {

        final int TYPE_ID = 0, TYPE_NAME = 1;
        final String TYPE_ID_STR = "ID(type)";

        return  executeQuery(String.format(NON_DEFINED_TYPES_QUERY, programID, userID, TYPE_ID_STR)).stream().map(record -> Pair
                .create(new Neo4jLazyNode(record.get(TYPE_ID).asLong()),
                        new ExternalNotDefinedTypeKey(record.get(TYPE_NAME).asString())));
    }

    public NEO4JServerManager(String db_name) {
        session = driver.session(SessionConfig.forDatabase(db_name));
    }

    public List<Record> executeQuery(String query) {
        return session.writeTransaction(tx -> tx.run(query).list());
    }
    public List<Record> executeQuery(String query, Map<String, Object> params) {
        return session.run(query, params).list();
    }
    public Session getSession() {
        return session;
    }

    @Override
    public void close() {
        session.close();
    }
}

package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.database.nodes.NodeCategory;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalTypeDefKey;
import org.neo4j.driver.Record;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class NEO4JServerManager implements NEO4JManager {
    public static final String NEO4J_PROTOCOL = "neo4j://";

    private static Driver driver;
    private Session session;

    public static void startDriver(String address, String user, String password) {
        Config config = Config.builder().withLogging(Logging.none()).build();
        driver = GraphDatabase.driver(NEO4J_PROTOCOL + address, AuthTokens.basic(user, password), config);
    }

    public static void closeDriver() {
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


    static final String DEFINED_TYPES_QUERY =
            "MATCH (type:TYPE_DEFINITION{isDeclared:true}) WHERE type.accessLevel='public' or type \n" +
                    "                    .accessLevel='protected' and  \n" + " not type.isFinal  \n" +
                    " OPTIONAL MATCH (p:PROGRAM) -[:PROGRAM_DECLARES_PACKAGE]->(:PACKAGE) \n" +
                    "                    -[:PACKAGE_HAS_COMPILATION_UNIT]-> (cu:COMPILATION_UNIT) \n" +
                    "                    -[:HAS_TYPE_DEF|HAS_INNER_TYPE_DEF]-> (type)\n" + " WITH cu, type\n" +
                    " RETURN  cu IS NULL, cu.fileName, %s, type.fullyQualifiedName, type.simpleName";

    static final String NON_DEFINED_TYPES_QUERY =
            "MATCH (p:PROGRAM{ID:\"%s\", USER_ID:\"%s\"}) CALL apoc.path.subgraphNodes(p, " +
                    "{labelFilter:\">TYPE_NODE\", minLevel:0}) YIELD node WITH node as type WHERE NOT(EXISTS(type" +
                    ".isDeclared)) OR NOT type" + ".isDeclared RETURN type";

    @Override
    public Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getDeclaredTypeDefsFrom(String programID,
                                                                                        String userID) {

        final int TYPE_ID = 2, TYPE_FULL_NAME = 3, TYPE_SIMPLE_NAME = 4, FILE_NAME = 1, NOT_CU = 0;
        final String TYPE_ID_STR = "ID(type)";
        return executeQuery(String.format(DEFINED_TYPES_QUERY, programID, userID, TYPE_ID_STR)).stream()
                .map(record -> Pair.create(new Neo4jLazyNode(record.get(TYPE_ID).asLong()),
                        record.get(NOT_CU).asBoolean() ?
                                new ExternalNotDefinedTypeKey(record.get(TYPE_FULL_NAME).asString(),
                                        NodeCategory.TYPE_DEFINITION.toString()) :
                                new ExternalTypeDefKey(record.get(FILE_NAME).asString(),
                                        record.get(TYPE_SIMPLE_NAME).asString())));
    }

    final static Set<String> INVALID_NODE_TYPES = new HashSet<>();

    static {
        INVALID_NODE_TYPES.add(NodeCategory.TYPE_NODE.toString());
        INVALID_NODE_TYPES.add(NodeCategory.PQ_NODE.toString());
        INVALID_NODE_TYPES.add(NodeTypes.CLASS_DEF.toString());
        INVALID_NODE_TYPES.add(NodeTypes.ENUM_DEF.toString());
        INVALID_NODE_TYPES.add(NodeTypes.INTERFACE_DEF.toString());
        INVALID_NODE_TYPES.add(NodeCategory.DEFINITION.toString());
        INVALID_NODE_TYPES.add(NodeCategory.AST_NODE.toString());
    }

    private static String getTypeLabelFromNode(Node node) {
        for (String label : node.labels())
            if (!INVALID_NODE_TYPES.contains(label))
                return label;
        throw new IllegalArgumentException("node " + node + " does not have any type label");
    }

    @Override
    public Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getNotDeclaredTypesFrom(String programID,
                                                                                        String userID) {

        final int TYPE_NODE = 0;
        return executeQuery(String.format(NON_DEFINED_TYPES_QUERY, programID, userID)).stream()
                .map(record -> {
                    Node node = record.get(TYPE_NODE).asNode();
                    return Pair.create(new Neo4jLazyNode(node.id()),
                            new ExternalNotDefinedTypeKey(node.get("fullyQualifiedName").asString(),
                                    getTypeLabelFromNode(node)));
                });
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

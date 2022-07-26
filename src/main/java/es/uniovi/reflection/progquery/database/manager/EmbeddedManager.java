package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.database.nodes.NodeCategory;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jEmbeddedWrapperNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalTypeDefKey;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.stream.Stream;

public class EmbeddedManager implements NEO4JManager {

    private final Transaction currentTransaction;

    public EmbeddedManager(Transaction currentTransaction) {

        this.currentTransaction = currentTransaction;
    }

    @Override
    public NodeWrapper getProgramFromDB(String programId, String userId) {

        ResourceIterator<Node> programsIfAny =
                currentTransaction.findNodes(NodeTypes.PROGRAM, "ID", programId, "USER_ID", userId);
        if (!programsIfAny.hasNext())
            return null;
        return new Neo4jEmbeddedWrapperNode(programsIfAny.next());
    }

    @Override
    public Stream<Pair<NodeWrapper, ExternalTypeDefKey>> getDeclaredMethodsFrom(String programID, String userID) {
        return null;
    }

    @Override
    public Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getDeclaredTypeDefsFrom(String programID,
                                                                                        String userID) {
        final String TYPE_NODE = "type", TYPE_FULL_NAME = "type.fullyQualifiedName", TYPE_SIMPLE_NAME =
                "type.simpleName", FILE_NAME = "cu.fileName", NOT_CU = "cu IS NULL";
        return currentTransaction
                .execute(String.format(NEO4JServerManager.DEFINED_TYPES_QUERY, programID, userID)).stream()
                .map(record -> Pair.create(new Neo4jEmbeddedWrapperNode((Node) record.get(TYPE_NODE)),
                        (boolean) record.get(NOT_CU) ?
                                new ExternalNotDefinedTypeKey((String) record.get(TYPE_FULL_NAME),
                                        NodeCategory.TYPE_DEFINITION.toString()) :
                                new ExternalTypeDefKey((String) record.get(FILE_NAME),
                                        (String) record.get(TYPE_SIMPLE_NAME))));
    }
    private static String getTypeLabelFromNode(Node node) {
        for (Label label : node.getLabels())
            if (!NEO4JServerManager.INVALID_NODE_TYPES.contains(label.toString()))
                return label.toString();
        throw new IllegalArgumentException("node " + node + " does not have any type label");
    }

    @Override
    public Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> getNotDeclaredTypesFrom(String programID,
                                                                                        String userID) {
        final String TYPE_NODE = "type";

        return currentTransaction
                .execute(String.format(NEO4JServerManager.NON_DEFINED_TYPES_QUERY, programID, userID))
                .stream().map(record ->{
                   Node node= (Node) record.get(TYPE_NODE);
                  return  Pair.create(new Neo4jEmbeddedWrapperNode(node),
                        new ExternalNotDefinedTypeKey((String)node.getProperty("fullyQualifiedName"), getTypeLabelFromNode(node)));
                });
    }

    @Override
    public void close() {
    }

}

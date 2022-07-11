package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jEmbeddedWrapperNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.types.ExternalTypeDefKey;
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
    public Stream<Pair<NodeWrapper, ExternalTypeDefKey>> getDeclaredTypeDefsFrom(String programID, String userID) {
        final String TYPE_NODE = "type", TYPE_NAME = "type.fullyQualifiedName", FILE_NAME = "cu.fileName";
        return currentTransaction.execute(String.format(NEO4JServerManager.QUERY, programID, userID, TYPE_NODE))
                .stream().map(record -> Pair.create(new Neo4jEmbeddedWrapperNode((Node) record.get(TYPE_NODE)),
                        new ExternalTypeDefKey((String) record.get(FILE_NAME), (String) record.get(TYPE_NAME))));
    }

    @Override
    public void close() {
    }

}

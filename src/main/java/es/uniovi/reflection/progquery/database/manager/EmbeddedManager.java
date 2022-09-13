package es.uniovi.reflection.progquery.database.manager;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jEmbeddedWrapperNode;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import org.neo4j.driver.Record;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.List;

public class EmbeddedManager implements NEO4JManager{

    private Transaction currentTransaction;

    public EmbeddedManager(Transaction currentTransaction) {

        this.currentTransaction = currentTransaction;
    }

    @Override
    public NodeWrapper getProgramFromDB(String programId, String userId) {

        ResourceIterator<Node> programsIfAny =  currentTransaction.findNodes(NodeTypes.PROGRAM, "ID", programId, "USER_ID", userId);
        if (!programsIfAny.hasNext())
            return null;
        return new Neo4jEmbeddedWrapperNode(programsIfAny.next());
    }

    @Override
    public void close() {
    }

}

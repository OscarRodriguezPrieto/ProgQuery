package database;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import database.embedded.EmbeddedDBManager;
import database.nodes.NodeTypes;
import node_wrappers.Neo4jEmbeddedWrapperNode;
 
public class EmbeddedGGDBServiceInsertion implements InsertionStrategy {
	private GraphDatabaseService gDBService;
	private Transaction t;

	public EmbeddedGGDBServiceInsertion() {
//		System.out.println("EMBEDDED NO PATH");
		gDBService = EmbeddedDBManager.getNewEmbeddedDBService();
	}

	public EmbeddedGGDBServiceInsertion(String dbPath) {
//		System.out.println("EMBEDDED " + dbPath);
		gDBService = EmbeddedDBManager.getNewEmbeddedDBService(dbPath);
	}
	@Override
	public Neo4jEmbeddedWrapperNode createNode() {
		return new Neo4jEmbeddedWrapperNode(gDBService.createNode());
	}

	@Override
	public Neo4jEmbeddedWrapperNode createNode(NodeTypes label) {
		Node n = gDBService.createNode();
		n.addLabel(label);
		return new Neo4jEmbeddedWrapperNode(n);

	}

	@Override
	public Neo4jEmbeddedWrapperNode createNode(NodeTypes label, Object[] props) {
		Node n = gDBService.createNode();
		n.addLabel(label);
		for (int i = 0; i < props.length; i = i + 2)
			n.setProperty(props[i].toString(), props[i + 1]);

		return new Neo4jEmbeddedWrapperNode(n);
	}


	@Override
	public Neo4jEmbeddedWrapperNode createNode(Object[] props) {
		Node n = gDBService.createNode();
		for (int i = 0; i < props.length; i = i + 2)
			n.setProperty(props[i].toString(), props[i + 1]);

		return new Neo4jEmbeddedWrapperNode(n);
	}

	@Override
	public void startAnalysis() {
		// System.out.println("S");
		t = gDBService.beginTx();
	}

	@Override
	public void endAnalysis() {
		// System.out.println("END");
//		System.out.println("BEFORE SHUTDOWN");
		t.success();
		t.close();
		gDBService.shutdown();
	}

}

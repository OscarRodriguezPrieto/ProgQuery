package es.uniovi.reflection.progquery.database;

import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import es.uniovi.reflection.progquery.database.embedded.EmbeddedDBManager;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jEmbeddedWrapperNode;

public class EmbeddedGGDBServiceInsertion implements InsertionStrategy {
	private GraphDatabaseService gDBService;
	private Transaction currentTransaction;

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

		return new Neo4jEmbeddedWrapperNode(currentTransaction.createNode());
	}

	@Override
	public Neo4jEmbeddedWrapperNode createNode(NodeTypes label) {
		Neo4jEmbeddedWrapperNode wrapper = createNode();
		wrapper.getNode().addLabel(label);
		return wrapper;

	}

	@Override
	public Neo4jEmbeddedWrapperNode createNode(NodeTypes label, Object[] props) {
		Neo4jEmbeddedWrapperNode wrapper = createNode(label);
		addProps(wrapper, props);
		return wrapper;
	}


	@Override
	public Neo4jEmbeddedWrapperNode createNode(Object[] props) {
		Neo4jEmbeddedWrapperNode wrapper = createNode();
		addProps(wrapper, props);
		return wrapper;
	}

	private void addProps(Neo4jEmbeddedWrapperNode wrapper, Object[] props) {

		Node n = wrapper.getNode();
		for (int i = 0; i < props.length; i = i + 2)
			n.setProperty(props[i].toString(), props[i + 1]);
	}
	@Override
	public void startAnalysis() {
		// System.out.println("S");
		currentTransaction = gDBService.beginTx();
	}

	@Override
	public void endAnalysis() {
		// System.out.println("END");
//		System.out.println("JUST BEFORE SHUTDOWN");
//		System.out.println("TOTAL OF NODES TO INSERT " + Neo4jEmbeddedWrapperNode.counter);
//		System.out.println(t.00);
		currentTransaction.commit();
		currentTransaction.close();
		EmbeddedDBManager.manager.shutdown();
	}

	@Override
	public NEO4JManager getManager() {
		//TODO
		throw new IllegalStateException("NOT IMPLEMENTEd YET");
	}

}

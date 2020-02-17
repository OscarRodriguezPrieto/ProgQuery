package database;

import database.insertion.lazy.InfoToInsert;
import database.insertion.lazy.LazyInsertionManagerMultipleTrans;
import database.nodes.NodeTypes;
import node_wrappers.Neo4jLazyServerDriverNode;
import node_wrappers.NodeWrapper;

public class Neo4jDriverLazyWrapperInsertion implements InsertionStrategy {
	private final int MAX_NODES_PER_TRANSACTION;
	private final String ADDRESS, USER, PASS;

	private static final int DEFAULT_MAX = 80_000;

	@Override
	public NodeWrapper createNode() {
		return new Neo4jLazyServerDriverNode();
	}

	public Neo4jDriverLazyWrapperInsertion(String connectionString) {
		this(DEFAULT_MAX, connectionString);
	}

	public Neo4jDriverLazyWrapperInsertion(int maxNodes, String connectionString) {
		super();
		MAX_NODES_PER_TRANSACTION = maxNodes;
		String[] connectionData = connectionString.split(";");
		ADDRESS = connectionData[2];
		USER = connectionData[0];
		PASS = connectionData[1];
		// System.out.println("SERVER " + maxNodes + " " + address);
	}

	@Override
	public NodeWrapper createNode(NodeTypes label) {
		return new Neo4jLazyServerDriverNode(label);
	}

	@Override
	public NodeWrapper createNode(NodeTypes label, Object[] props) {
		return new Neo4jLazyServerDriverNode(label, props);
	}

	@Override
	public NodeWrapper createNode(Object[] props) {
		return new Neo4jLazyServerDriverNode(props);
	}

	@Override
	public void startAnalysis() {

	}

	@Override
	public void endAnalysis() {
		LazyInsertionManagerMultipleTrans.insertIntoNeo4jServerByDriver(InfoToInsert.INFO_TO_INSERT, ADDRESS, USER,
				PASS, MAX_NODES_PER_TRANSACTION);

	}

}

package es.uniovi.reflection.progquery.database;

import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.insertion.lazy.DriverLazyInsertionManagerWithIter;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.database.manager.NEO4JServerManager;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class Neo4jDriverLazyInsertion extends NotPersistentLazyInsertion{
	private final int MAX_OPERATIONS_PER_TRANSACTION;
	private final String ADDRESS, USER, PASS, DB_NAME;

	private static final int DEFAULT_MAX = 80_000;


	public Neo4jDriverLazyInsertion(String connectionString) {
		this(DEFAULT_MAX, connectionString);
	}

	public Neo4jDriverLazyInsertion(int maxNodes, String connectionString) {

		super();
		MAX_OPERATIONS_PER_TRANSACTION = maxNodes;
		String[] connectionData = connectionString.split(";");
		ADDRESS = connectionData[2];
		USER = connectionData[0];
		PASS = connectionData[1];
		DB_NAME = connectionData.length == 4 ? connectionData[3] : null;
		// System.out.println("SERVER " + maxNodes + " " + address);
	}
	
	public Neo4jDriverLazyInsertion(String host, String port, String user, String password, String database, String max_operations_transaction) {

		super();
		MAX_OPERATIONS_PER_TRANSACTION = Integer.parseInt(max_operations_transaction);		
		ADDRESS = host + ":" + port;
		USER = user;
		PASS = password;
		DB_NAME = database;
	}

	@Override
	public void endAnalysis() {
		if (DB_NAME == null)
			DriverLazyInsertionManagerWithIter.defaultDBInsertion(InfoToInsert.INFO_TO_INSERT, ADDRESS, USER, PASS,
					MAX_OPERATIONS_PER_TRANSACTION);
		else

			DriverLazyInsertionManagerWithIter.insertToSpecificDB(InfoToInsert.INFO_TO_INSERT, ADDRESS, USER, PASS,
					MAX_OPERATIONS_PER_TRANSACTION, DB_NAME);

	}

	@Override
	public NEO4JManager getManager() {
		return new NEO4JServerManager(ADDRESS,USER,PASS,DB_NAME);
	}


}

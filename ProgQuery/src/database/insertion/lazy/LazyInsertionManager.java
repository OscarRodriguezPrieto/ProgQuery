package database.insertion.lazy;

import java.util.Map.Entry;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.graphdb.Label;

import node_wrappers.NodeWrapper;

public class LazyInsertionManager {

	public static void insertIntoNeo4jServerByDriver(InfoToInsert info, final String SERVER_ADDRESS) {
		final Driver driver = GraphDatabase.driver(SERVER_ADDRESS, AuthTokens.basic("neo4j", "s3cr3t0."));
		Session session = driver.session();

		// Varias opciones transacción a transacción con writeTrans, o con una
		// sola transaccion beginTrans y success
		Transaction t = session.beginTransaction();

		storeNodes(t, info);
		t.success();
		t.commitAsync(); 
		session.close();
	}

	private static void storeNodes(Transaction t, InfoToInsert info) {
		for (NodeWrapper n : info.nodeSet)
			t.run(createQueryFor(n));
	}

	private static String createQueryFor(NodeWrapper n) {
		final String queryEnd = ") RETURN ID(n)";
		String query = "CREATE (n";
		for (Label label : n.getLabels())
			query += ":" + label;
		if (n.getAllProperties().size() == 0)
			return query + queryEnd;
		query += "{";
		for (Entry<String, Object> prop : n.getAllProperties())
			query += prop.getKey() + ":" + prop.getValue() + ",";

		return query.substring(0, query.length() - 1) + "}" + queryEnd;

	}
}

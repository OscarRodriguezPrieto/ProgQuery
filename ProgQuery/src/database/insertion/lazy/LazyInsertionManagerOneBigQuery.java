package database.insertion.lazy;

import java.util.Map.Entry;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.graphdb.Label;

import node_wrappers.NodeWrapper;

public class LazyInsertionManagerOneBigQuery {

	public static void insertIntoNeo4jServerByDriver(InfoToInsert info, final String SERVER_ADDRESS) {
		final Driver driver = GraphDatabase.driver(SERVER_ADDRESS,
				AuthTokens.basic("neo4j", "s3cr3t0."));
		// Session session = ;

		// Varias opciones transacción a transacción con writeTrans, o con una
		// sola transaccion beginTrans y success
		storeNodes(driver.session(), info);
		System.out.println("SUCCES COM");
	}

	private static void storeNodes(Session session, InfoToInsert info) {
		for (NodeWrapper n : info.nodeSet)
			session.writeTransaction(new TransactionWork<Long>() {

				@Override
				public Long execute(Transaction tx) {
					// tx.

					StatementResult result = tx.run(createQueryFor(n));
					// result.list().get(0).asMap().entrySet()
					// .forEach(e -> System.out.println(e.getKey() + "," +
					// e.getValue()));
					return result.list().get(0).values().get(0).asLong();
				}

			});

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

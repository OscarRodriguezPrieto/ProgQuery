package database.insertion.lazy;


import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import org.neo4j.driver.*;

import node_wrappers.NodeWrapper;
import utils.dataTransferClasses.Pair;

public class LazyInsertionManagerMultipleTrans {
	private static final int REPETITIONS = 1;

	public static void insertIntoNeo4jServerByDriver(InfoToInsert info, final String SERVER_ADDRESS,final String DB_NAME, final String USER,
			final String PASS, final int MAX_NODES_PER_TRANSACTION) {

		try (final Driver driver = GraphDatabase.driver(SERVER_ADDRESS, AuthTokens.basic(USER, PASS));
				Session session = driver.session( SessionConfig.builder().withDatabase(DB_NAME).build() ) ) {

			final List<Pair<String, Map<String,Object>>> nodeInfo = info.getNodeQueriesInfo();
			// System.out.println("AFTER ANALYSIS " + nodeInfo.size() + " nodes
			// ");
//			int totalEdges = 0;
			for (int i = 0; i < REPETITIONS; i++) {
				// System.out.println("ITER " + i);
				actionByParts(info.nodeSet.size(), MAX_NODES_PER_TRANSACTION, (s, e) -> executeNodesQuery(session,
						info.nodeSet, nodeInfo, r -> r.list().get(0).values().get(0).asLong(), s, e));

				final List<Pair<String,Map<String,Object>>> relInfo = info.getRelQueriesInfo();
				actionByParts( info.relSet.size(), MAX_NODES_PER_TRANSACTION,
						(s, e) -> executeRelsQuery(session, relInfo, s, e));
			}
			// System.out.println("AFTER ANALYSIS " + totalEdges + " edges");
			
		}
	}

	// private interface ActionByParts<T> {
	// void accept(T t, int start, int end);
	// }

	public static <T> void actionByParts(int listSize, int numberPerPart, BiConsumer<Integer, Integer> action) {

		int i = 0;
		while ((i + 1) * numberPerPart < listSize)
			// {
			// System.out.println("ITER " + i);
			// System.out.println("FROM " + i * numberPerPart + " TO " + (i + 1)
			// * numberPerPart);
			action.accept(i++ * numberPerPart, i * numberPerPart);
		// }
		// System.out.println("LAST ITER " + i);
		// System.out.println("FROM " + i * numberPerPart + " TO " + listSize);

		action.accept(i * numberPerPart, listSize);

	}

	/*
	 * private static <T> T executeQuery(String query, Session session,
	 * Function<StatementResult, T> resultF) { return
	 * session.writeTransaction(new TransactionWork<T>() {
	 * 
	 * @Override public T execute(Transaction tx) { // tx.
	 * 
	 * StatementResult result = tx.run(query); //
	 * result.list().get(0).asMap().entrySet() // .forEach(e ->
	 * System.out.println(e.getKey() + "," + // e.getValue())); return
	 * resultF.apply(result); }
	 * 
	 * }); }
	 */

	private static <T> T executeQuery(String query, Session session, Function<Result, T> resultF) {
		return session.writeTransaction(new TransactionWork<T>() {

			@Override
			public T execute(Transaction tx) {
				// tx.

				Result result = tx.run(query);
				// result.list().get(0).asMap().entrySet()
				// .forEach(e -> System.out.println(e.getKey() + "," +
				// e.getValue()));
				return resultF.apply(result);
			}

		});
	}
	//
	// private static <T> List<T> executeNodesQuery(Session session,
	// List<NodeWrapper> nodes,
	// Function<StatementResult, T> resultF) {
	// return session.writeTransaction(new TransactionWork<List<T>>() {
	//
	// @Override
	// public List<T> execute(Transaction tx) {
	// List<T> resultIds = new ArrayList<T>();
	//
	// // tx.
	// for (NodeWrapper n : nodes)
	// resultIds.add(resultF.apply(tx.run(createQueryFor(n))));
	// // result.list().get(0).asMap().entrySet()
	// // .forEach(e -> System.out.println(e.getKey() + "," +
	// // e.getValue()));
	// return resultIds;
	// }
	//
	// });
	// }

	private static Void executeNodesQuery(Session session, List<NodeWrapper> nodes,
			List<Pair<String, Map<String,Object>>> nodeQueries, Function<Result, Long> resultF, int start, int end) {
		return session.writeTransaction(new TransactionWork<Void>() {

			@Override
			public Void execute(Transaction tx) {

				// tx.
				for (int i = start; i < end; i++) {
					NodeWrapper n = nodes.get(i);
					Pair<String, Map<String,Object>> queryAndParams = nodeQueries.get(i);
					n.setId(resultF.apply(tx.run(queryAndParams.getFirst(), queryAndParams.getSecond())));
				}
				// result.list().get(0).asMap().entrySet()
				// .forEach(e -> System.out.println(e.getKey() + "," +
				// e.getValue()));
				return null;
			}

		});
	}

	private static Void executeRelsQuery(Session session, List<Pair<String, Map<String,Object>>> relsQueries, int start,
			int end) {
		return session.writeTransaction(new TransactionWork<Void>() {

			@Override
			public Void execute(Transaction tx) {
				for (int i = start; i < end; i++) {
					Pair<String,Map<String,Object>> pair = relsQueries.get(i);
					tx.run(pair.getFirst(), pair.getSecond());
				}
				return null;
			}

		});
	}

	// private static void storeNodes(Session session, InfoToInsert info) {
	//
	// for (NodeWrapper n : info.nodeSet)
	//
	// n.setId(executeQuery(createQueryFor(n), session, r ->
	// r.list().get(0).values().get(0).asLong()));
	//
	// }
	//
	// private static void storeRels(Session session, InfoToInsert info) {
	// for (RelationshipWrapper r : info.relSet)
	// executeQuery(createQueryFor(r), session, result -> null);
	//
	// }

}

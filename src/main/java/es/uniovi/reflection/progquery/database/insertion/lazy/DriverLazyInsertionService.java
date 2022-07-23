package es.uniovi.reflection.progquery.database.insertion.lazy;

import es.uniovi.reflection.progquery.database.manager.NEO4JServerManager;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class DriverLazyInsertionService {
    private static final int REPETITIONS = 1;

    public static void insertToSpecificDB(InfoToInsert info, final int MAX_OPERATIONS_PER_TRANSACTION, String DB_NAME) {
        try (NEO4JServerManager manager = new NEO4JServerManager(DB_NAME)) {

            final List<Pair<String, Object[]>> nodeInfo = info.getNodeQueriesInfo();
            for (int i = 0; i < REPETITIONS; i++) {
                actionByParts(info.nodeSet.size(), MAX_OPERATIONS_PER_TRANSACTION,
                        (start, end) -> executeNodesQuery(manager.getSession(), info.nodeSet, nodeInfo,
                                r -> r.list().get(0).values().get(0).asLong(), start, end));

                final List<Pair<String, Object[]>> relInfo = info.getRelQueriesInfo();
                actionByParts(info.relSet.size(), MAX_OPERATIONS_PER_TRANSACTION,
                        (start, end) -> executeRelsQuery(manager.getSession(), relInfo, start, end));
            }

        }
    }

    public static <T> void actionByParts(int listSize, int numberPerPart, BiConsumer<Integer, Integer> action) {

        int i = 0;
        while ((i + 1) * numberPerPart < listSize)
            action.accept(i++ * numberPerPart, i * numberPerPart);

        action.accept(i * numberPerPart, listSize);

    }


    private static Void executeNodesQuery(Session session, List<Neo4jLazyNode> nodes,
                                          List<Pair<String, Object[]>> nodeQueries, Function<Result, Long> resultF,
                                          int start, int end) {
        return session.writeTransaction(tx -> {

            for (int i = start; i < end; i++) {
                NodeWrapper n = nodes.get(i);
                Pair<String, Object[]> queryAndParams = nodeQueries.get(i);
                n.setId(resultF.apply(tx.run(queryAndParams.getFirst(), parameters(queryAndParams.getSecond()))));
            }
            return null;
        });
    }

    private static Void executeRelsQuery(Session session, List<Pair<String, Object[]>> relsQueries, int start,
                                         int end) {
        return executeElementQuery(session, relsQueries, start, end);
    }

    private static Void executeNodesUpdateQuery(Session session, List<Pair<String, Object[]>> nodeUpdates, int start,
                                                int end) {
        return executeElementQuery(session, nodeUpdates, start, end);
    }

    private static Void executeRelsUpdateQuery(Session session, List<Pair<String, Object[]>> relsUpdates, int start,
                                               int end) {
        return executeElementQuery(session, relsUpdates, start, end);
    }

    private static <T> Void executeElementQuery(Session session, List<Pair<String, Object[]>> queries, int start,
                                                int end) {
        return session.writeTransaction(tx -> {
            for (int i = start; i < end; i++) {
                Pair<String, Object[]> pair = queries.get(i);
                tx.run(pair.getFirst(), parameters(pair.getSecond()));
            }
            return null;
        });
    }

    public static void updateRetrievedNodesAndRels(InfoToInsert infoToInsert, final int MAX_OPERATIONS_PER_TRANSACTION,
                                                   final String DB_NAME) {

        final List<Pair<String, Object[]>> updateNodeInfo = infoToInsert.getNodeQueriesUpdateInfo().stream().filter(q->q!=null).collect(
                Collectors.toList());
        try (NEO4JServerManager manager = new NEO4JServerManager(DB_NAME)) {
            actionByParts(infoToInsert.retrievedNodeCache.size(), MAX_OPERATIONS_PER_TRANSACTION,
                    (start, end) -> executeNodesUpdateQuery(manager.getSession(), updateNodeInfo, start, end));
            final List<Pair<String, Object[]>> updateRelInfo = infoToInsert.getRelQueriesUpdateInfo().stream().filter(q->q!=null).collect(
                    Collectors.toList());;
            actionByParts(infoToInsert.retrievedRelSet.size(), MAX_OPERATIONS_PER_TRANSACTION,
                    (start, end) -> executeRelsUpdateQuery(manager.getSession(), updateRelInfo, start, end));
        }
    }
}

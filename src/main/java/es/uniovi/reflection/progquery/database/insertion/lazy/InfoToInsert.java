package es.uniovi.reflection.progquery.database.insertion.lazy;

import es.uniovi.reflection.progquery.node_wrappers.*;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import org.neo4j.driver.types.Node;
import org.neo4j.graphdb.Label;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class InfoToInsert {

    final Map<Long, Neo4jServerRetrievedNode> retrievedNodeCache = new HashMap<>();

    final List<Neo4jLazyNode> nodeSet = new ArrayList<>();

    final List<RelationshipWrapper> relSet = new ArrayList<>();
    final List<Neo4jLazyRetrievedRel> retrievedRelSet = new ArrayList<>();

    public List<Neo4jLazyNode> getNodeSet() {
        return Collections.unmodifiableList(nodeSet);
    }

    public List<RelationshipWrapper> getRelSet() {

        return Collections.unmodifiableList(relSet);
    }

    public void addRetrievedRel(Neo4jLazyRetrievedRel retrievedRel) {
        retrievedRelSet.add(retrievedRel);
    }

    public void removeRetrievedRel(Neo4jLazyRetrievedRel retrievedRel) {
        retrievedRelSet.remove(retrievedRel);
    }

    public void putIntoRetrievedCache(Neo4jServerRetrievedNode retrievedNode) {
        retrievedNodeCache.put(retrievedNode.getId(), retrievedNode);
    }

    public Neo4jServerRetrievedNode getOrCreateNode(Node node) {
        Neo4jServerRetrievedNode nodeWrapper = retrievedNodeCache.get(node.id());
        return nodeWrapper == null ? new Neo4jServerRetrievedNode(node) : nodeWrapper;

    }

    public static final InfoToInsert INFO_TO_INSERT = new InfoToInsert();

    public void addNewNode(Neo4jLazyNode newNode) {
        nodeSet.add(newNode);
    }

    public void deleteNode(Neo4jLazyNode node) {
        nodeSet.remove(node);
    }

    public void addNewRel(RelationshipWrapper newRel) {
        relSet.add(newRel);

    }

    public void deleteRel(Neo4jLazyRelationship rel) {
        relSet.remove(rel);
    }

    public List<Pair<String, Object[]>> getNodeQueriesInfo() {
        return generateQueriesFor(nodeSet, InfoToInsert::createParameterizedQueryFor);
    }

    public List<Pair<String, Object[]>> getNodeQueriesUpdateInfo() {
        return generateQueriesFor(nodeSet, InfoToInsert::createParamQueryForNodeUpdate);
    }

    public List<Pair<String, Object[]>> getRelQueriesUpdateInfo() {
        return generateQueriesFor(retrievedRelSet, InfoToInsert::createParamQueryForRelUpdate);
    }

    private static Pair<String, Object[]> createParamQueryForRelUpdate(Neo4jLazyRetrievedRel rel) {

        String query = "MATCH ()-[e]-() WHERE ID(e)=$id SET ";
        Pair<String, Object[]> pair = getParamPropsForUpdate(rel.getAllProperties(), rel.getId());
        if(pair.getSecond().length==0)
            return null;
        return Pair.create(query + "," + pair.getFirst(), pair.getSecond());
    }

    private static Pair<String, Object[]> createParamQueryForNodeUpdate(NodeWrapper node) {

        String query = "MATCH (e) WHERE ID(e)=$id SET e";
        for (Label label : node.getLabels())
            query += ":" + label;
        Pair<String, Object[]> pair = getParamPropsForUpdate(node.getAllProperties(), node.getId());
        if(query.length()==0 && pair.getSecond().length==0)
            return null;
        return Pair.create(query + "," + pair.getFirst(), pair.getSecond());
    }

    public List<Pair<String, Object[]>> getRelQueriesInfo() {

        return generateQueriesFor(relSet, InfoToInsert::createParameterizedQueryFor);
    }

    private <T> List<Pair<String, Object[]>> generateQueriesFor(Iterable<T> elementsSet,
                                                                Function<T, Pair<String, Object[]>> fromElemToQuery) {

        final List<Pair<String, Object[]>> queries = new ArrayList<>();
        for (T element : elementsSet)
            queries.add(fromElemToQuery.apply(element));
        return queries;
    }

    private static Pair<String, Object[]> createParameterizedQueryFor(RelationshipWrapper r) {
        Pair<String, Object[]> props = getParameterizedProps(r.getAllProperties());
        Object[] propArray = new Object[4 + props.getSecond().length];
        propArray[0] = "startId";
        propArray[1] = r.getStartNode().getId();
        propArray[2] = "endId";
        propArray[3] = r.getEndNode().getId();
        int i = 4;
        for (Object o : props.getSecond())
            propArray[i++] = o;

        return Pair.create("MATCH (n),(m) WHERE ID(n)=$startId AND ID(m)=$endId CREATE (n)-[r:" + r.getTypeString() +
                props.getFirst() + "]->(m)", propArray);

    }

    private static Pair<String, Object[]> getOnlyProps(String prefix, Set<Entry<String, Object>> props,
                                                       Object[] parameters) {
        String queryPart = "";
        int i = 0;
        for (Entry<String, Object> prop : props) {
            queryPart += prefix + prop.getKey() + ":$p" + i + ",";
            parameters[i * 2] = "p" + i;
            parameters[i++ * 2 + 1] = prop.getValue();
        }
        return Pair.create(queryPart.substring(0, queryPart.length() - 1), parameters);

    }

    private static Pair<String, Object[]> getParamPropsForUpdate(Set<Entry<String, Object>> props, long id) {
        if (props.size() == 0)
            return Pair.create("", new Object[]{});
        Object[] parameters = new Object[props.size() * 2 + 2];
        parameters[parameters.length - 2] = "id";
        parameters[parameters.length - 1] = id;
        return getOnlyProps("e.", props, parameters);
    }

    private static Pair<String, Object[]> getParameterizedProps(Set<Entry<String, Object>> props) {
        if (props.size() == 0)
            return Pair.create("", new Object[]{});
        Object[] parameters = new Object[props.size() * 2];
        Pair<String, Object[]> paramQuery = getOnlyProps("", props, parameters);
        return Pair.create("{" + paramQuery.getFirst() + "}", paramQuery.getSecond());
    }

    private static Pair<String, Object[]> createParameterizedQueryFor(NodeWrapper n) {
        final String queryEnd = ") RETURN ID(n)";
        String query = "CREATE (n";
        for (Label label : n.getLabels())
            query += ":" + label;
        Pair<String, Object[]> pair = getParameterizedProps(n.getAllProperties());
        return Pair.create(query + pair.getFirst() + queryEnd, pair.getSecond());

    }

    public void removeFromRetrievedCache(Long id) {
        retrievedNodeCache.remove(id);
    }
}

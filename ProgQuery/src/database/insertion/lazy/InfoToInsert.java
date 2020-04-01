package database.insertion.lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Label;

import node_wrappers.Neo4jLazyServerDriverNode;
import node_wrappers.Neo4jLazyServerDriverRelationship;
import node_wrappers.NodeWrapper;
import node_wrappers.RelationshipWrapper;
import utils.dataTransferClasses.Pair;

public class InfoToInsert {

	final List<NodeWrapper> nodeSet = new ArrayList<>();

	final List<RelationshipWrapper> relSet = new ArrayList<>();

	public static final InfoToInsert INFO_TO_INSERT = new InfoToInsert();

	public void addNewNode(Neo4jLazyServerDriverNode newNode) {
		nodeSet.add(newNode);
	}

	public void deleteNode(Neo4jLazyServerDriverNode node) {
		nodeSet.remove(node);
	}

	public void addNewRel(Neo4jLazyServerDriverRelationship newRel) {
		relSet.add(newRel);

	}

	public void deleteRel(Neo4jLazyServerDriverRelationship rel) {
		relSet.remove(rel);
	}

	public List<Pair<String, Map<String,Object>>> getNodeQueriesInfo() {

		final List<Pair<String,Map<String,Object>>> nodeQueries = new ArrayList<>();
		for (NodeWrapper n : nodeSet)
			nodeQueries.add(createParameterizedQueryFor(n));
		return nodeQueries;
	}
	public List<Pair<String, Map<String,Object>>> getRelQueriesInfo() {

		final List<Pair<String, Map<String,Object>>> relQueries = new ArrayList<>();
		for (RelationshipWrapper r : relSet)
			relQueries.add(createParameterizedQueryFor(r));
		return relQueries;
	}
	private static Pair<String, Map<String,Object>> createParameterizedQueryFor(RelationshipWrapper r) {
		Pair<String, Map<String,Object>> props = getParameterizedProps(r.getAllProperties());
		props.getSecond().put("startId", r.getStartNode().getId());
		props.getSecond().put("endId", r.getEndNode().getId());
		

		return Pair.create("MATCH (n),(m) WHERE ID(n)=$startId AND ID(m)=$endId CREATE (n)-[r:" + r.getTypeString()
				+ props.getFirst() + "]->(m)", props.getSecond());

	}

	private static String createQueryFor(Iterable<NodeWrapper> nodes, Iterable<RelationshipWrapper> rels) {
		String queryPart1 = "CREATE ", queryPart2 = "\nRETURN ";
		for (NodeWrapper n : nodes) {
			queryPart1 += "( n" + n.getId() + "), ";
			queryPart2 += " n" + n.getId() + ", ";
		}

		return queryPart1.substring(0, queryPart1.length() - 2) + queryPart2.substring(0, queryPart2.length() - 2);
	}

	private static String createQueryFor(RelationshipWrapper r) {

		return "MATCH (n),(m) WHERE ID(n)=" + r.getStartNode().getId() + " AND ID(m)=" + r.getEndNode().getId()
				+ " CREATE (n)-[r:" + r.getTypeString() + getProps(r.getAllProperties()) + "]->(m)";

	}

	private static String getProps(Set<Entry<String, Object>> props) {
		if (props.size() == 0)
			return "";
		String queryPart = "{";
		for (Entry<String, Object> prop : props)
			queryPart += prop.getKey() + ":" + prop.getValue() + ",";
		return queryPart.substring(0, queryPart.length() - 1) + "}";
	}

	private static Pair<String, Map<String,Object>> getParameterizedProps(Set<Entry<String, Object>> props) {
		if (props.size() == 0)
			return Pair.create("", new HashMap<String,Object>());
		String queryPart = "{";
		Map<String,Object> parameters = new HashMap<String,Object>();
		int i = 0;
		for (Entry<String, Object> prop : props) {
			queryPart += prop.getKey() + ":$p" + i + ",";
			parameters.put("p"+i, prop.getValue());
			}
		return Pair.create(queryPart.substring(0, queryPart.length() - 1) + "}", parameters);
	}

	private static String createQueryFor(NodeWrapper n) {
		final String queryEnd = ") RETURN ID(n)";
		String query = "CREATE (n";
		for (Label label : n.getLabels())
			query += ":" + label;

		return query + getProps(n.getAllProperties()) + queryEnd;

	}

	private static Pair<String, Map<String,Object>> createParameterizedQueryFor(NodeWrapper n) {
		final String queryEnd = ") RETURN ID(n)";
		String query = "CREATE (n";
		for (Label label : n.getLabels())
			query += ":" + label;
		Pair<String, Map<String,Object>> pair = getParameterizedProps(n.getAllProperties());
		return Pair.create(query + pair.getFirst() + queryEnd, pair.getSecond());

	}
}

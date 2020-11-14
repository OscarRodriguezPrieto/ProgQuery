package database.insertion.lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Label;

import database.nodes.NodeTypes;
import database.nodes.NodeUtils;
import node_wrappers.Neo4jLazyServerDriverNode;
import node_wrappers.Neo4jLazyServerDriverRelationship;
import node_wrappers.NodeWrapper;
import node_wrappers.RelationshipWrapper;
import utils.dataTransferClasses.Pair;

public class InfoToInsert {

	public final List<NodeWrapper> nodeSet = new ArrayList<>();

	final List<RelationshipWrapper> relSet = new ArrayList<>();

	public static final InfoToInsert INFO_TO_INSERT = new InfoToInsert();

	public void addNewNode(Neo4jLazyServerDriverNode newNode) {
		nodeSet.add(newNode);
	}

	public void deleteNode(Neo4jLazyServerDriverNode node) {
		nodeSet.remove(node);
		if (node.hasLabel(NodeTypes.PROGRAM))
			System.out.println("DELETING\n " + NodeUtils.nodeToString(node));
	}

	public void addNewRel(Neo4jLazyServerDriverRelationship newRel) {
		relSet.add(newRel);

	}

	public void deleteRel(Neo4jLazyServerDriverRelationship rel) {
		relSet.remove(rel);
	}

	public List<Pair<String, Object[]>> getNodeQueriesInfo() {

		final List<Pair<String, Object[]>> nodeQueries = new ArrayList<>();
		for (NodeWrapper n : nodeSet)
			nodeQueries.add(createParameterizedQueryFor(n));
		return nodeQueries;
	}

	public List<Pair<String, Object[]>> getRelQueriesInfo() {

		final List<Pair<String, Object[]>> relQueries = new ArrayList<>();
		for (RelationshipWrapper r : relSet)
			relQueries.add(createParameterizedQueryFor(r));
		return relQueries;
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

		return Pair.create("MATCH (n),(m) WHERE ID(n)=$startId AND ID(m)=$endId CREATE (n)-[r:" + r.getTypeString()
				+ props.getFirst() + "]->(m)", propArray);

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

	private static Pair<String, Object[]> getParameterizedProps(Set<Entry<String, Object>> props) {
		if (props.size() == 0)
			return Pair.create("", new Object[] {});
		String queryPart = "{";
		Object[] parameters = new Object[props.size() * 2];
		int i = 0;
		for (Entry<String, Object> prop : props) {
			queryPart += prop.getKey() + ":$p" + i + ",";
			parameters[i * 2] = "p" + i;
			parameters[i++ * 2 + 1] = prop.getValue();
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

	private static Pair<String, Object[]> createParameterizedQueryFor(NodeWrapper n) {
		final String queryEnd = ") RETURN ID(n)";
		String query = "CREATE (n";
		for (Label label : n.getLabels())
			query += ":" + label;
		Pair<String, Object[]> pair = getParameterizedProps(n.getAllProperties());
		return Pair.create(query + pair.getFirst() + queryEnd, pair.getSecond());

	}
}

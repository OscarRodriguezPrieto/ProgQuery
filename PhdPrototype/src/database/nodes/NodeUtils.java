package database.nodes;

import java.util.Map.Entry;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class NodeUtils {
	public static String nodeToString(Node n) {
		if (n == null)
			return "NULL";
		String res = "Node[" + n.getId() + "]\n";
		for (Relationship r : n.getRelationships())
			res += r.getType() + "\t" + (r.getStartNode().equals(n) ? "--->" + r.getEndNode().getProperty("nodeType")
					: "<---" + r.getStartNode().getProperty("nodeType")) + "\n";
		for (Entry<String, Object> prop : n.getAllProperties().entrySet())
			res += prop.getKey() + "=" + prop.getValue() + "\n";
		return res;
	}
}

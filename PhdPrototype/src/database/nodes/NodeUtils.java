package database.nodes;

import java.util.Map.Entry;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class NodeUtils {

	public static String nodeToString(Node n) {
		if (n == null)
			return "NULL";
		String res = "Node[" + n.getId() + "]\n";
		for (Relationship r : n.getRelationships()) {
			res += r.getType() + "\t"
					+ (r.getStartNode().equals(n)
							? "--->" + r.getEndNode().getLabels().iterator().next()
									+ "(ID " + r.getEndNodeId() + ")" + (r.getEndNode().hasProperty("lineNumber")
											? "(line " + r.getEndNode().getProperty("lineNumber") + ")" : "")
							: "<---" + r.getStartNode().getLabels().iterator().next()
									+ "(ID " + r.getStartNodeId() + ")" + (r.getStartNode().hasProperty("lineNumber")
											? "(line " + r.getStartNode().getProperty("lineNumber") + ")" : ""))

					+ "\n";
			for (Entry<String, Object> prop : r.getAllProperties().entrySet())
				res += prop.getKey() + "=" + prop.getValue() + "\n";
		}
		for (Label label : n.getLabels())
			res += "Label:\t" + label + "\n";
		for (Entry<String, Object> prop : n.getAllProperties().entrySet())
			res += prop.getKey() + "=" + prop.getValue() + "\n";

		return res;
	}
}

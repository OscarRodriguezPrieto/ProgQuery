package es.uniovi.reflection.progquery.database.nodes;

import java.util.Map.Entry;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;

public class NodeUtils {
	public static String getNameFromDec(NodeWrapper dec) {
		return dec.hasLabel(NodeTypes.THIS_REF) ? "THIS" : (String) dec.getProperty("name");
	}

	public static String nodeToString(NodeWrapper n) {
		if (n == null)
			return "NULL";
		String res = "Node[" + n.getId() + "]\n";
		for (RelationshipWrapper r : n.getRelationships()) {
			String relType = r.getTypeString();
			res += (r.getStartNode().equals(n)
					? "NODE--[" + relType + "]->"
							+ (r.getEndNode().getLabels().iterator().hasNext()
									? r.getEndNode().getLabels().iterator().next() : "NO LABEL")
							+ "(ID " + r.getEndNode().getId() + ")"
							+ (r.getEndNode().hasProperty("lineNumber")
									? "(line " + r.getEndNode().getProperty("lineNumber") + ")" : "")
					: "NODE<-[" + relType + "]--"
							+ (r.getStartNode().getLabels().iterator().hasNext()
									? r.getStartNode().getLabels().iterator().next() : "NO LABEL")
							+ "(ID " + r.getStartNode().getId() + ")" + (r.getStartNode().hasProperty("lineNumber")
									? "(line " + r.getStartNode().getProperty("lineNumber") + ")" : ""))

					+ "\n";
			for (Entry<String, Object> prop : r.getAllProperties())
				res += prop.getKey() + "=" + prop.getValue() + "\n";
		}
		for (Label label : n.getLabels())
			res += "Label:\t" + label + "\n";
		for (Entry<String, Object> prop : n.getAllProperties())
			res += prop.getKey() + "=" + prop.getValue() + "\n";

		return res;
	}

	public static String nodeToStringNoRels(NodeWrapper n) {
		if (n == null)
			return "NULL";
		String res = "Node[" + n.getId() + "]\n";
		for (Label label : n.getLabels())
			res += "Label:\t" + label + "\n";
		for (Entry<String, Object> prop : n.getAllProperties())
			res += prop.getKey() + "=" + prop.getValue() + "\n";

		return res;
	}
	public static String reducedClassMethodToString(NodeWrapper n) {
		if (n == null)
			return "NULL";
		String res = "Node[" + n.getId() + "]\n";
		res +=  n.getProperty("fullyQualifiedName")+ "\n";

//		for (Label label : n.getLabels())
//			res += "Label:\t" + label + "\n";
			
		return res;
	}
	public static String nodeToString(Node n) {
		if (n == null)
			return "NULL";
		String res = "Node[" + n.getId() + "]\n";
//		System.out.println(res);
		for (Label label : n.getLabels())
			res += "Label:\t" + label.name() + "\n";
		for (Entry<String, Object> prop : n.getAllProperties().entrySet())
			res += prop.getKey() + "=" + prop.getValue() + "\n";
//		System.out.println(res);
		for (Relationship r : n.getRelationships()) {
			String relType = r.getType().name();
			res += (r.getStartNode().equals(n)
					? "NODE--[" + relType + "]->"
							+ (r.getEndNode().getLabels().iterator().hasNext()
									? r.getEndNode().getLabels().iterator().next() : "NO LABEL")
							+ "(ID " + r.getEndNode().getId() + ")"
							+ (r.getEndNode().hasProperty("lineNumber")
									? "(line " + r.getEndNode().getProperty("lineNumber") + ")" : "")
					: "NODE<-[" + relType + "]--"
							+ (r.getStartNode().getLabels().iterator().hasNext()
									? r.getStartNode().getLabels().iterator().next() : "NO LABEL")
							+ "(ID " + r.getStartNode().getId() + ")" + (r.getStartNode().hasProperty("lineNumber")
									? "(line " + r.getStartNode().getProperty("lineNumber") + ")" : ""))

					+ "\n";
			for (Entry<String, Object> prop : r.getAllProperties().entrySet())
				res += prop.getKey() + "=" + prop.getValue() + "\n";
		}
		for (Label label : n.getLabels())
			res += "Label:\t" + label.name() + "\n";
		for (Entry<String, Object> prop : n.getAllProperties().entrySet())
			res += prop.getKey() + "=" + prop.getValue() + "\n";

		return res;
	}
}

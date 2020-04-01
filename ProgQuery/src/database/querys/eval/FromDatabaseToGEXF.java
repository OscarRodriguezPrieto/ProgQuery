package database.querys.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import database.embedded.EmbeddedDBManager;
import utils.dataTransferClasses.Pair;

public class FromDatabaseToGEXF {
	
	/*
	static Map<String, Pair<Integer, String>> nodeAttrs = new HashMap<String, Pair<Integer, String>>();
	static Map<String, Pair<Integer, String>> relAttrs = new HashMap<String, Pair<Integer, String>>();

	public static void main(String[] args) throws IOException {
		GraphDatabaseService gs = EmbeddedDBManager.getNewEmbeddedDBService();
		String nodes = nodesAsGEXF(gs), rels = relsAsGEXF(gs);
		String res = "<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">"
				+ "<meta> <creator>RodriguezPrieto</creator> <description>ProgQuery graph</description> </meta>"
				+ "<graph mode=\"static\" defaultedgetype=\"directed\">" + attributesAsGEXF() + nodes + rels
				+ "</graph></gexf>";
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("ProgQuery.gexf")));
		bw.write(res);
		bw.close();

	}

	private static String attributesAsGEXF() {
		*//*
		 * <attributes class="node"> <attribute id="0" title="url"
		 * type="string"/> <attribute id="1" title="indegree" type="float"/>
		 * <attribute id="2" title="frog" type="boolean">
		 * <default>true</default> </attribute> </attributes>
		 *//*
		String res = "<attributes class=\"node\">";
		for (Entry<String, Pair<Integer, String>> entry : nodeAttrs.entrySet())
			res += "<attribute id=\"" + entry.getValue().getFirst() + "\" title=\"" + entry.getKey() + "\" type=\""
					+ entry.getValue().getSecond() + "\"/>";
		res += "</attributes>\n"// <attributes class=\"node\">"
		;
		return res;
	}

	private static String nodesAsGEXF(GraphDatabaseService gs) {
		Transaction t = gs.beginTx();
		Result result = gs.execute("MATCH (n) RETURN n");
		t.success();
		String res = "<nodes>\n";
		while (result.hasNext())
			res += nodeToGEFXXML((Node) result.next().get("n")) + "\n";
		return res + "</nodes>\n";
	}

	private static String relsAsGEXF(GraphDatabaseService gs) {
		Transaction t = gs.beginTx();
		Result result = gs.execute("MATCH ()-[r]->() RETURN r");
		t.success();
		String res = "<edges>";
		while (result.hasNext())
			res += relToGEFXXML((Relationship) result.next().get("r")) + "\n";
		return res + "</edges>\n";
	}

	private static String relToGEFXXML(Relationship rel) {
		String relRep = "<edge id=\"" + rel.getId() + "\" label=\"" + rel.getType().name() + "\" source=\""
				+ rel.getStartNodeId() + "\" target=\"" + rel.getEndNodeId() + "\">\n";
		relRep += propertiesToGEFXXML(rel.getAllProperties().entrySet(), relAttrs);
		relRep += "</edge>";
		return relRep;
	}

	private static String propertiesToGEFXXML(Set<Entry<String, Object>> props,
			Map<String, Pair<Integer, String>> idPropMap) {
		String propsRep = "";
		if (props.size() > 0) {
			propsRep += "<attvalues>\n";
			for (Entry<String, Object> prop : props) {
				if (!idPropMap.containsKey(prop.getKey()))
					idPropMap.put(prop.getKey(), Pair.create(idPropMap.size(), fromObjetToTypeString(prop.getValue())));
				propsRep += "<attvalue for=\"" + idPropMap.get(prop.getKey()).getFirst() + "\" value=\""
						+ prop.getValue().toString().replaceAll("<", "**").replaceAll(">", "**") + "\"/>\n";
			}
			propsRep += "</attvalues>";
		}
		return propsRep;
	}

	private static String fromObjetToTypeString(Object value) {
		if (value instanceof String)
			return "string";
		else if (value instanceof Integer || value instanceof Long)
			return "integer";
		else if (value instanceof Boolean)
			return "boolean";
		throw new IllegalArgumentException("The value " + value + " has not an expected type " + value.getClass());
	}

	private static String nodeToGEFXXML(Node n) {
		String nodeRep = "<node id=\"" + n.getId() + "\"";
		if (n.getLabels().iterator().hasNext())
			nodeRep += " label=\"" + n.getLabels().iterator().next() + "\">\n";
		nodeRep += propertiesToGEFXXML(n.getAllProperties().entrySet(), nodeAttrs);
		nodeRep += "</node>";
		return nodeRep;
	}*/
}
/*
 * 
 * <node id="6" label="Champtercier"> <attvalues> <attvalue
 * for="modularity_class" value="0"></attvalue> </attvalues> <viz:size
 * value="4.0"></viz:size> <viz:position x="-332.6012" y="485.16974"
 * z="0.0"></viz:position> <viz:color r="236" g="81" b="72"></viz:color> </node>
 */
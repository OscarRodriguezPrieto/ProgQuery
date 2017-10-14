package cache.nodes;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Node;

import com.sun.source.tree.Tree;

public class TreeToNodeCache {

	private static Map<Tree, Node> treeToNode = new HashMap<Tree, Node>();

	public static void putNode(Tree t, Node n) {
		if (treeToNode.containsKey(t))
			System.err.println("Sobreescribiendo la entrada para el mismo tree:"+t.toString());
		treeToNode.put(t, n);
	}

	public static Node getNode(Tree t) {
		return treeToNode.get(t);
	}

	public static boolean contains(Tree t) {
		return treeToNode.containsKey(t);
	}

}

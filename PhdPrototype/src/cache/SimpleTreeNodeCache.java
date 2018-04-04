package cache;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Node;

import com.sun.source.tree.Tree;

import database.nodes.NodeUtils;

public class SimpleTreeNodeCache<K> {

	private final Map<K, Node> auxNodeCache = new HashMap<K, Node>();

	public void put(K tree, Node n) {

		if (auxNodeCache.containsKey(tree))
			throw new IllegalStateException("Duplicate tree in caché.\n" + tree + "\nCurrent node:\n"
					+ NodeUtils.nodeToString(auxNodeCache.get(tree)));
		auxNodeCache.put(tree, n);
	}

	public Node get(K tree) {
		return auxNodeCache.get(tree);
	}

	public void clear() {
		auxNodeCache.clear();
	}
}

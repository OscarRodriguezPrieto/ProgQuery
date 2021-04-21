package es.uniovi.reflection.progquery.cache;

import java.util.HashMap;
import java.util.Map;

import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class SimpleTreeNodeCache<K> {

	private final Map<K, NodeWrapper> auxNodeCache = new HashMap<>();

	public void put(K tree, NodeWrapper n) {

		if (auxNodeCache.containsKey(tree))
			throw new IllegalStateException("Duplicate tree in cach�.\n" + tree + "\nCurrent node:\n"
					+ NodeUtils.nodeToString(auxNodeCache.get(tree)));
		auxNodeCache.put(tree, n);
	}

	public NodeWrapper get(K tree) {
		return auxNodeCache.get(tree);
	}

	public void clearX() {
		auxNodeCache.clear();
	}
	
	public int size() {
		return auxNodeCache.size();
	}
}

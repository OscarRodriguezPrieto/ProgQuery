package cache.nodes;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class DefinitionCache {
	private static final boolean DEBUG = false;
	public static final DefinitionCache CLASS_TYPE_CACHE = new DefinitionCache();
	public static final DefinitionCache METHOD_TYPE_CACHE = new DefinitionCache();

	private final Map<String, Node> auxNodeCache = new HashMap<String, Node>();
	private final Map<String, Node> definitionNodeCache = new HashMap<String, Node>();

	public void put(String k, Node v) {
		if (DEBUG)
			System.out.println("putting " + k + " " + v);
		if (auxNodeCache.containsKey(k))
			throw new IllegalArgumentException("Key " + k + " twice ");
		if (!definitionNodeCache.containsKey(k))
			auxNodeCache.put(k, v);
	}

	public void putDefinition(String k, Node v) {
		if (DEBUG)
			System.out.println("putting def " + k + " " + v);
		if (auxNodeCache.containsKey(k)) {
			if (DEBUG)
				System.out.println("Removing " + auxNodeCache.get(k));
			// No me deja eliminalo porque todavía tiene relaciones

			// Abria que pasar las relaciones al nuevo type
			for (Relationship r : auxNodeCache.get(k).getRelationships(Direction.INCOMING)) {
				r.getStartNode().createRelationshipTo(v, r.getType());
				r.delete();
			}
			for (Relationship r : auxNodeCache.get(k).getRelationships(Direction.OUTGOING)) {
				v.createRelationshipTo(r.getEndNode(), r.getType());
				r.delete();
			}
			auxNodeCache.get(k).delete();

			auxNodeCache.remove(k);
		}

		definitionNodeCache.put(k, v);
	}

	public Node get(String k) {
		return definitionNodeCache.containsKey(k) ? definitionNodeCache.get(k) : auxNodeCache.get(k);
	}

	public boolean containsKey(String k) {
		return auxNodeCache.containsKey(k) || definitionNodeCache.containsKey(k);
	}

	public boolean containsDef(String k) {
		return definitionNodeCache.containsKey(k);
	}

	public int totalTypesCached() {
		return auxNodeCache.size();
	}

	public int totalDefsCached() {
		return definitionNodeCache.size();
	}
}

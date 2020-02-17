package database.procedures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import database.relations.CFGRelationTypes;
import database.relations.RelationTypes;

public class FunctionUtils {

	static Node getLastNodeInRels(Node node, RelationTypes... relTypes) {
		if(node==null)
			return null;
		boolean outExprReached = false;

		while (!outExprReached) {

			Iterator<Relationship> rels = node.getRelationships(Direction.INCOMING, relTypes).iterator();
			if (rels.hasNext())
				node = rels.next().getStartNode();
			else
				outExprReached = true;
		}
		return node;
	}



	static List<Node> getAllNext(Node node, CFGRelationTypes... relTypes) {
		List<Node> res = new ArrayList<>();
//		Set<Node> nodes = new HashSet<Node>();
		res.add(node);
		// while

		int currentIndex = 0;
		while (currentIndex < res.size()) {

			node = res.get(currentIndex++);
			System.out.println(node.getId());
			Iterable<Relationship> rels = node.getRelationships(Direction.OUTGOING, relTypes);
			for (Relationship r : rels)
				if (!res.contains(r.getEndNode()))
					res.add(r.getEndNode());
		}

		return res;
	}
}

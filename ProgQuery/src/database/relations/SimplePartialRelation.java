package database.relations;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class SimplePartialRelation<T extends RelationTypesInterface> implements PartialRelation<T> {
	private final Node startingNode;
	private final T relationType;

	public SimplePartialRelation(Node startingNode, T relationType) {
		this.startingNode = startingNode;
		this.relationType = relationType;
	}

	public Node getStartingNode() {
		return startingNode;
	}

	public T getRelationType() {
		return relationType;
	}

	// private static int i = 0;

	@Override
	public Relationship createRelationship(Node endNode) {
		//
		// if (startingNode.hasLabel(NodeTypes.CFG_LAST_STATEMENT_IN_FINALLY) &&
		// endNode.hasLabel(NodeTypes.FINALLY_BLOCK)
		// && ++i == 4)
		// throw new IllegalStateException();
		return startingNode.createRelationshipTo(endNode, relationType);
	}

}

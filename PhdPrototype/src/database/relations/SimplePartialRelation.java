package database.relations;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class SimplePartialRelation<T extends RelationTypesInterface> implements PartialRelation<T>
{
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

	@Override
	public Relationship createRelationship(Node endNode) {
		return startingNode.createRelationshipTo(endNode, relationType);
	}

	@Override
	public Relationship createRelationshipToCondition(Node endNode) {
		return startingNode.createRelationshipTo(endNode, ((CFGRelationTypes) relationType).toCondition());
	}

}

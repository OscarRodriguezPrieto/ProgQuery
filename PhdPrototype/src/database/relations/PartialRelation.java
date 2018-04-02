package database.relations;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public interface PartialRelation<T extends RelationTypesInterface> {

	public Node getStartingNode();

	public T getRelationType();

	Relationship createRelationship(Node endNode);

	Relationship createRelationshipToCondition(Node endNode);
}

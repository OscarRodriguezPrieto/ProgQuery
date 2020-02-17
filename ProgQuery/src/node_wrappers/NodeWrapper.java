package node_wrappers;

import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Label;

import database.querys.cypherWrapper.EdgeDirection;
import database.relations.RelationTypesInterface;

public interface NodeWrapper extends Propertiable{

	Long getId();

	RelationshipWrapper createRelationshipTo(NodeWrapper end, RelationTypesInterface relType);

	List<RelationshipWrapper> getRelationships();

	// List<RelationshipWrapper> getRelationshipsXX(RelationTypesInterface...
	// relTypes);

	RelationshipWrapper getSingleRelationship(EdgeDirection direction, RelationTypesInterface relTypes);

	List<RelationshipWrapper> getRelationships(EdgeDirection direction, RelationTypesInterface... possibleRelTypes);

	List<RelationshipWrapper> getRelationships(EdgeDirection direction);

	boolean hasRelationship(RelationTypesInterface relType, EdgeDirection incoming);

	Set<Label> getLabels();

	boolean hasLabel(Label label);

	void removeLabel(Label label);

	void addLabel(Label label);

	void delete();

	void setId(Long id);
	// Only it is deleted if there is no relationships, otherwise an exception
	// is
	// raised

	void removeIncomingRel(RelationshipWrapper neo4jLazyServerDriverRelationship);

	void removeOutgoingRel(RelationshipWrapper neo4jLazyServerDriverRelationship);

	// void deleteRelationship();

}

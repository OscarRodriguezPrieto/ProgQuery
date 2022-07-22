package es.uniovi.reflection.progquery.node_wrappers;

import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;

public interface NodeWrapper extends Propertiable{

	Long getId();

	RelationshipWrapper createRelationshipTo(NodeWrapper end, RelationTypesInterface relType);

	List<RelationshipWrapper> getRelationships();

	RelationshipWrapper getSingleRelationship(Direction direction, RelationTypesInterface relTypes);

	List<RelationshipWrapper> getRelationships(Direction direction, RelationTypesInterface... possibleRelTypes);

	List<RelationshipWrapper> getRelationships(Direction direction);

	boolean hasRelationship(RelationTypesInterface relType, Direction incoming);

	Set<Label> getLabels();

	boolean hasLabel(Label label);

	void removeLabel(Label label);

	void addLabel(Label label);

	// Only it is deleted if there is no relationships, otherwise an exception
	// is
	// raised
	void delete();

	void setId(long id);


}

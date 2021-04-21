package es.uniovi.reflection.progquery.node_wrappers;

import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;

public class Neo4jDriverQueriedNode extends Neo4jLazyServerDriverNode implements NodeWrapper {

	public Neo4jDriverQueriedNode(long id) {
		super(id);
	}


	

	@Override
	public List<RelationshipWrapper> getRelationships() {

		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	private List<RelationshipWrapper> getRelsFrom(Direction direction, RelationTypesInterface... relTypes) {

		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public RelationshipWrapper getSingleRelationship(Direction direction, RelationTypesInterface relTypes) {

		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public List<RelationshipWrapper> getRelationships(Direction direction, RelationTypesInterface... relTypes) {


		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public boolean hasRelationship(RelationTypesInterface relType, Direction direction) {

		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public Set<Label> getLabels() {

		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public boolean hasLabel(Label label) {


		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public void removeLabel(Label label) {

		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public void addLabel(Label newLabel) {

		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public void delete() {

		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");
	}

	@Override
	public void setId(long id) {

		throw new IllegalStateException("ID of queried nodes cannot be set.");
	}

	@Override
	public List<RelationshipWrapper> getRelationships(Direction direction) {
		throw new IllegalStateException("RIGHT NOW, QUERIED NODES SHOULD NOT INVOKE THIS METHOD AT ALL");

	}



}

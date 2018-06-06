package database.relations;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import utils.Pair;

public class PartialRelationWithProperties<T extends RelationTypesInterface> extends SimplePartialRelation<T> {
	private List<Pair<String, Object>> properties;

	public PartialRelationWithProperties(Node startingNode, T relationType, String key, Object value) {
		super(startingNode, relationType);
		this.properties = new ArrayList<Pair<String, Object>>();
		this.properties.add(Pair.create(key, value));
	}

	public PartialRelationWithProperties(Node startingNode, T relationType, Pair<String, Object> p1) {
		super(startingNode, relationType);
		this.properties = new ArrayList<Pair<String, Object>>();
		this.properties.add(p1);
	}

	public PartialRelationWithProperties(Node startingNode, T relationType, Pair<String, Object> p1,
			Pair<String, Object> p2) {
		super(startingNode, relationType);
		this.properties = new ArrayList<Pair<String, Object>>();
		this.properties.add(p1);
		this.properties.add(p2);
	}

	private Relationship addProperties(Relationship rel) {
		for (Pair<String, Object> property : properties)
			rel.setProperty(property.getFirst(), property.getSecond());
		return rel;
	}

	@Override
	public Relationship createRelationship(Node endNode) {
		return addProperties(super.createRelationship(endNode));

	}

}

package es.uniovi.reflection.progquery.database.relations;

import java.util.ArrayList;
import java.util.List;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;

public class PartialRelationWithProperties<T extends RelationTypesInterface> extends SimplePartialRelation<T> {
	private List<Pair<String, Object>> properties;

	public List<Pair<String, Object>> getProperties() {
		return properties;
	}

	public PartialRelationWithProperties(NodeWrapper startingNode, T relationType, String key, Object value) {
		
		this(startingNode, relationType,Pair.create(key, value));
	}

	public PartialRelationWithProperties(NodeWrapper startingNode, T relationType, Pair<String, Object> p1) {
		this(startingNode, relationType,new ArrayList<Pair<String, Object>>());
		this.properties.add(p1);
	}

	public PartialRelationWithProperties(NodeWrapper startingNode, T relationType, Pair<String, Object> p1,
			Pair<String, Object> p2) {
		this(startingNode, relationType,p1);
		this.properties.add(p2);
	}

	public PartialRelationWithProperties(NodeWrapper startingNode, T relationType,
			List<Pair<String, Object>> properties) {
		super(startingNode, relationType);
		this.properties = properties;
	}

	private RelationshipWrapper addProperties(RelationshipWrapper rel) {
		for (Pair<String, Object> property : properties)
			rel.setProperty(property.getFirst(), property.getSecond());
		return rel;
	}

	@Override
	public RelationshipWrapper createRelationship(NodeWrapper endNode) {
		return addProperties(super.createRelationship(endNode));

	}

}

package node_wrappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

import database.insertion.lazy.InfoToInsert;
import database.nodes.NodeTypes;
import database.relations.RelationTypesInterface;

public class Neo4jLazyServerDriverNode extends AbstractNeo4jLazyServerDriverElement implements NodeWrapper {

	Map<RelationTypesInterface, List<RelationshipWrapper>> incomingRels = new HashMap<>(),
			outgoingRels = new HashMap<>();
	List<RelationshipWrapper> allRels = new ArrayList<>();

	Set<Label> labels = new HashSet<>();
	Long id;
	
	public Neo4jLazyServerDriverNode(long id) {
		this.id=id;
	}
//	private static long counter=0;
	public Neo4jLazyServerDriverNode() {
//		id=counter++;
		id=null;
		InfoToInsert.INFO_TO_INSERT.addNewNode(this);
	}
	public Neo4jLazyServerDriverNode(NodeTypes... labels) {

		this();
		for (NodeTypes label : labels)
			this.labels.add(label);

	}

	public Neo4jLazyServerDriverNode(NodeTypes label, Object... props) {
		this(props);
		this.labels.add(label);

	}

	public Neo4jLazyServerDriverNode(Object... props) {
		super(props);
		InfoToInsert.INFO_TO_INSERT.addNewNode(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public RelationshipWrapper createRelationshipTo(NodeWrapper end, RelationTypesInterface r) {
		RelationshipWrapper rel = new Neo4jLazyServerDriverRelationship(this, end, r);
		List<RelationshipWrapper> relList = outgoingRels.get(r);
		if (relList == null)
			outgoingRels.put(r, relList = new ArrayList<>());
		relList.add(rel);
		Neo4jLazyServerDriverNode castEnd = (Neo4jLazyServerDriverNode) end;
		relList = castEnd.incomingRels.get(r);
		if (relList == null)
			castEnd.incomingRels.put(r, relList = new ArrayList<>());
		relList.add(rel);

		allRels.add(rel);
		castEnd.allRels.add(rel);
		return rel;
	}

	@Override
	public List<RelationshipWrapper> getRelationships() {

		return allRels;
	}

	private List<RelationshipWrapper> getRelsFrom(Direction direction, RelationTypesInterface... relTypes) {
		List<RelationshipWrapper> rels = new ArrayList<>();
		Map<RelationTypesInterface, List<RelationshipWrapper>> currentMap = direction == Direction.INCOMING
				? incomingRels
				: outgoingRels;
		for (RelationTypesInterface relType : relTypes) {
			List<RelationshipWrapper> relsFound = currentMap.get(relType);
			if (relsFound != null)
				rels.addAll(relsFound);
		}
		return rels;
	}

	@Override
	public RelationshipWrapper getSingleRelationship(Direction direction, RelationTypesInterface relTypes) {
		List<RelationshipWrapper> rels = getRelsFrom(direction, relTypes);
		if (rels.size() > 1)
			throw new IllegalArgumentException("More than one relationship");
		if (rels.size() == 0)
			return null;
		return rels.get(0);
	}

	@Override
	public List<RelationshipWrapper> getRelationships(Direction direction, RelationTypesInterface... relTypes) {

		return getRelsFrom(direction, relTypes);
	}

	@Override
	public boolean hasRelationship(RelationTypesInterface relType, Direction direction) {
		Map<RelationTypesInterface, List<RelationshipWrapper>> currentMap = direction == Direction.INCOMING
				? incomingRels
				: outgoingRels;

		return currentMap.get(relType) != null;
	}

	@Override
	public Set<Label> getLabels() {
		return labels;
	}

	@Override
	public boolean hasLabel(Label label) {

		return labels.contains(label);
	}

	@Override
	public void removeLabel(Label label) {
		labels.remove(label);
	}

	@Override
	public void addLabel(Label newLabel) {
		labels.add(newLabel);
	}

	@Override
	public void delete() {
		// Theoretically, a check is needed in order to asses that there are no
		// rels attached to this node
		// We also can implement detach, just to not to have to delete all the
		// rels... i dont know
		InfoToInsert.INFO_TO_INSERT.deleteNode(this);
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public List<RelationshipWrapper> getRelationships(Direction direction) {
		List<RelationshipWrapper> rels = new ArrayList<>();
		Map<RelationTypesInterface, List<RelationshipWrapper>> currentMap = direction == Direction.INCOMING
				? incomingRels
				: outgoingRels;
		for (List<RelationshipWrapper> relsOfType : currentMap.values())
			rels.addAll(relsOfType);

		return rels;
	}

	public void removeIncomingRel(RelationshipWrapper rel) {
		allRels.remove(rel);
		incomingRels.get(rel.getType()).remove(rel);
	}

	public void removeOutgoingRel(RelationshipWrapper rel) {
		allRels.remove(rel);
		outgoingRels.get(rel.getType()).remove(rel);

	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + (int) (id ^ (id >>> 32));
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Neo4jLazyServerDriverNode other = (Neo4jLazyServerDriverNode) obj;
//		if (id != other.id)
//			return false;
//		return true;
//	}



}

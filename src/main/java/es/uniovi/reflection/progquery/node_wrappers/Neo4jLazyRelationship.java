package es.uniovi.reflection.progquery.node_wrappers;

import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;

public class Neo4jLazyRelationship extends AbstractNeo4jLazyServerDriverElement
		implements RelationshipWrapper {
	private NodeWrapper start, end;
	private RelationTypesInterface rType;

	public Neo4jLazyRelationship(NodeWrapper start, NodeWrapper end, RelationTypesInterface rType) {
		this(start, rType);
		this.end = end;

	}

	public Neo4jLazyRelationship(NodeWrapper start, RelationTypesInterface rType) {
		this.start = start;
		this.rType = rType;
		InfoToInsert.INFO_TO_INSERT.addNewRel(this);
	}

	@Override
	public RelationTypesInterface getType() {
		return rType;
	}

	@Override
	public NodeWrapper getStartNode() {
		return start;
	}

	@Override
	public NodeWrapper getEndNode() {
		return end;
	}

	// private static int i = 0;

	@Override
	public void delete() {
		// System.out.println(i++);
		InfoToInsert.INFO_TO_INSERT.deleteRel(this);
		((Neo4jLazyNode) start).removeOutgoingRel(this);
		((Neo4jLazyNode) end).removeIncomingRel(this);
	}

	@Override
	public String getTypeString() {
		// TODO Auto-generated method stub
		return rType.toString();
	}

	@Override
	public String toString() {
		return NodeUtils.nodeToStringNoRels(start) + "--[" + rType.name() + "]-->" + NodeUtils.nodeToStringNoRels(end);
	}
}

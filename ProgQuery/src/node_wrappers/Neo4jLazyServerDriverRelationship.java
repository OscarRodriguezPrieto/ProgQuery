package node_wrappers;

import database.insertion.lazy.InfoToInsert;
import database.relations.RelationTypesInterface;

public class Neo4jLazyServerDriverRelationship extends AbstractNeo4jLazyServerDriverElement
		implements RelationshipWrapper {
	private NodeWrapper start, end;
	private RelationTypesInterface rType;

	public Neo4jLazyServerDriverRelationship(NodeWrapper start, NodeWrapper end, RelationTypesInterface rType) {
		this(start, rType);
		this.end = end;

	}

	public Neo4jLazyServerDriverRelationship(NodeWrapper start, RelationTypesInterface rType) {
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
		((Neo4jLazyServerDriverNode) start).removeOutgoingRel(this);
		((Neo4jLazyServerDriverNode) end).removeIncomingRel(this);
	}

	@Override
	public String getTypeString() {
		// TODO Auto-generated method stub
		return rType.toString();
	}

}

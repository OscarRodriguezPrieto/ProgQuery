package es.uniovi.reflection.progquery.node_wrappers;

<<<<<<< HEAD:src/main/java/node_wrappers/Neo4jLazyServerDriverRelationship.java
import database.insertion.lazy.InfoToInsert;
import database.nodes.NodeUtils;
import database.relations.RelationTypesInterface;
=======
import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
>>>>>>> 0ecfe6a91eb6d4ba2a6a5297c3940c89ab21f27c:src/main/java/es/uniovi/reflection/progquery/node_wrappers/Neo4jLazyServerDriverRelationship.java

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

	@Override
	public String toString() {
		return NodeUtils.nodeToStringNoRels(start) + "--[" + rType.name() + "]-->" + NodeUtils.nodeToStringNoRels(end);
	}
}

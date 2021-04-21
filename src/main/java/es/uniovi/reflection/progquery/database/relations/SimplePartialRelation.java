package es.uniovi.reflection.progquery.database.relations;

import java.util.ArrayList;
import java.util.List;

import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;

public class SimplePartialRelation<T extends RelationTypesInterface> implements PartialRelation<T> {
	private final NodeWrapper startingNode;
	private final T relationType;

	public SimplePartialRelation(NodeWrapper startingNode, T relationType) {
		this.startingNode = startingNode;
		this.relationType = relationType;
	}

	public NodeWrapper getStartingNode() {
		return startingNode;
	}

	public T getRelationType() {
		return relationType;
	}

	// private static int i = 0;

	@Override
	public RelationshipWrapper createRelationship(NodeWrapper endNode) {
		//
		// if (startingNode.hasLabel(NodeTypes.CFG_LAST_STATEMENT_IN_FINALLY) &&
		// endNode.hasLabel(NodeTypes.FINALLY_BLOCK)
		// && ++i == 4)
		// throw new IllegalStateException();
		return startingNode.createRelationshipTo(endNode, relationType);
	}

	@Override
	public String toString() {
		return NodeUtils.nodeToString(getStartingNode()) + "\n" + getRelationType() + "\n";
	}

	public List<Pair<String, Object>> getProperties() {
		return new ArrayList<Pair<String,Object>>();
	}
}

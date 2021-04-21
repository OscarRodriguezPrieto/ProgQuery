package es.uniovi.reflection.progquery.utils.dataTransferClasses;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Direction;

import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class ClassState {

	public NodeWrapper currentClassDec;
	public List<NodeWrapper> attrsInClassDec;

	public ClassState(NodeWrapper currentClassDec) {
		this.currentClassDec = currentClassDec;
		attrsInClassDec = new ArrayList<NodeWrapper>();
		currentClassDec.getRelationships(Direction.OUTGOING, RelationTypes.DECLARES_FIELD)
				.forEach(rel -> attrsInClassDec.add(rel.getEndNode()));
	}

}

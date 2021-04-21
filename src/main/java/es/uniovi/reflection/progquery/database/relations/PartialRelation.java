package es.uniovi.reflection.progquery.database.relations;

import java.util.List;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;

public interface PartialRelation<T extends RelationTypesInterface>  {

	NodeWrapper getStartingNode();

	T getRelationType();

	RelationshipWrapper createRelationship(NodeWrapper endNode);
	 List<Pair<String, Object>> getProperties();
}

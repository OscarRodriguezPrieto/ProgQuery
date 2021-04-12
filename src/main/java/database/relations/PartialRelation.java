package database.relations;

import java.util.List;

import node_wrappers.NodeWrapper;
import node_wrappers.RelationshipWrapper;
import utils.dataTransferClasses.Pair;

public interface PartialRelation<T extends RelationTypesInterface>  {

	NodeWrapper getStartingNode();

	T getRelationType();

	RelationshipWrapper createRelationship(NodeWrapper endNode);
	 List<Pair<String, Object>> getProperties();
}

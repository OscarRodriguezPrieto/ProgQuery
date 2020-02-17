package node_wrappers;

import database.relations.RelationTypesInterface;

public interface RelationshipWrapper extends Propertiable {

	RelationTypesInterface getType();

	String getTypeString();

	NodeWrapper getStartNode();

	NodeWrapper getEndNode();



	void delete();


}

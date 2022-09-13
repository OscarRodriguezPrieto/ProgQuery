package es.uniovi.reflection.progquery.node_wrappers;

import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;

public interface RelationshipWrapper extends Propertiable {

	RelationTypesInterface getType();

	String getTypeString();

	NodeWrapper getStartNode();

	NodeWrapper getEndNode();



	void delete();


}

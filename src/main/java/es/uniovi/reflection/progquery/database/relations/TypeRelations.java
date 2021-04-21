package es.uniovi.reflection.progquery.database.relations;

public enum TypeRelations implements RelationTypesInterface {

	INHERITS_FIELD, INHERITS_METHOD, IS_SUBTYPE_EXTENDS, 
	IS_SUBTYPE_IMPLEMENTS, ITS_TYPE_IS, OVERRIDES,
	
	RETURN_TYPE, THROWS_TYPE, PARAM_TYPE, INSTANCE_ARG_TYPE
	, WILDCARD_EXTENDS_BOUND, WILDCARD_SUPER_BOUND
}

package es.uniovi.reflection.progquery.database.relations;

public enum PDGRelationTypes implements RelationTypesInterface {

	USED_BY, MODIFIED_BY, STATE_MODIFIED_BY, STATE_MAY_BE_MODIFIED_BY,
	// For the future
	// RETURNS, RETURNS_A_PART_OF, MAY_RETURN, MAY_RETURN_A_PART_OF
	HAS_THIS_REFERENCE
}

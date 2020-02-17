package database.querys.services;

import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.RelationshipImpl;
import database.relations.RelationTypesWiggle;

public class TypeServicesWiggle {

	public static MatchElement getSuperTypesOf(MatchElement type, MatchElement superType) {
		return new RelationshipImpl(type, superType,
				new EdgeImpl(Cardinalidad.MIN_TO_INF(0), RelationTypesWiggle.IS_SUBTYPE_OF));
		
	}

	public static MatchElement getSuperTypesOf(MatchElement type) {
		return new RelationshipImpl(type, new NodeVar("superType"),
				new EdgeImpl(Cardinalidad.MIN_TO_INF(0), RelationTypesWiggle.IS_SUBTYPE_OF));

	}
}

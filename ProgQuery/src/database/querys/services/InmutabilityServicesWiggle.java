package database.querys.services;

import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.Expression;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Path;
import database.relations.RelationTypesWiggle;
import database.relations.TypeRelations;
import utils.dataTransferClasses.Pair;

public class InmutabilityServicesWiggle {
	public static Expression isInmutableField(String field, String typeDec) {
		return isInmutableField(new ExprImpl(field), new ExprImpl(typeDec));
	}

	public static Expression isInmutableField(Expression fieldModifiers, Expression typeDecModifiers) {
		return new ExprImpl("NOT(" + typeDecModifiers.expToString() + ".flags CONTAINS 'public'  AND NOT "
				+ fieldModifiers.expToString() + ".flags CONTAINS 'final' AND NOT " + fieldModifiers.expToString()
				+ ".flags CONTAINS 'static' AND ( " + fieldModifiers.expToString() + ".flags CONTAINS 'public'  OR ("
				+ fieldModifiers.expToString() + ".flags CONTAINS 'protected' AND NOT "
				+ typeDecModifiers.expToString() + ".flags CONTAINS 'final') ))");
	}

	public static Path getTypesSuperTypesAndFieldsTransitive(MatchElement initialTypeDec) {
		return new Path(initialTypeDec, "p",
				Pair.create(
						new EdgeImpl(Cardinalidad.ONE_TO_INF, RelationTypesWiggle.DECLARES_FIELD,
								TypeRelations.ITS_TYPE_IS, // This is not a
															// Wiggle rel, but
															// is added to the
															// graph in the
															// query
								RelationTypesWiggle.IS_SUBTYPE_EXTENDS),
						new CompleteNode("next", Pair.create("typeKind", "DECLARED"))));
	}

}

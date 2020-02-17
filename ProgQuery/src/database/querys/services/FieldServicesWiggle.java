package database.querys.services;

import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.querys.cypherWrapper.RelationshipImpl;
import database.relations.RelationTypes;
import database.relations.RelationTypesWiggle;
import database.relations.TypeRelations;
import utils.dataTransferClasses.Pair;

public class FieldServicesWiggle {

	public static MatchClause typesAndDeclaredFieldsPlusModifiersAndTypes(MatchElement declaringType) {
		return new MatchClause(
				new Path(declaringType,
						Pair.create(new EdgeImpl(RelationTypes.DECLARES_FIELD),
								new CompleteNode("attr", Pair.create("typeKind", "DECLARED"))),
						Pair.create(new EdgeImpl(TypeRelations.ITS_TYPE_IS), new NodeVar("typeDec"))),
				new RelationshipImpl(new NodeVar("attr"), new NodeVar("attrModifiers"),
						new EdgeImpl(RelationTypesWiggle.HAS_VARIABLEDECL_MODIFIERS)));
	}
}

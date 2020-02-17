package database.querys.services;

import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.querys.cypherWrapper.RelationshipImpl;
import database.relations.RelationTypes;
import database.relations.TypeRelations;
import utils.dataTransferClasses.Pair;

public class FieldServices {

	public static MatchClause typesAndDeclaredFields(Node declaringType) {
		return new MatchClause(
				new RelationshipImpl(declaringType, new CompleteNode("attr", Pair.create("typeKind", "DECLARED")),
						new EdgeImpl(RelationTypes.DECLARES_FIELD)));
	}

	public static MatchClause typesAndDeclaredFieldsAndTypes(Node declaringType) {
		return new MatchClause(new Path(declaringType,
				Pair.create(new EdgeImpl(RelationTypes.DECLARES_FIELD),
						new CompleteNode("attr", Pair.create("typeKind", "DECLARED"))),
				Pair.create(new EdgeImpl(TypeRelations.ITS_TYPE_IS), new NodeVar("typeDec"))));
	}
}

package database.querys.services;

import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.relations.RelationTypesWiggle;
import utils.dataTransferClasses.Pair;

public class ModifiersServiceWiggle {

	public static MatchElement getClassModifiers(MatchElement classNode) {
		return new Path(classNode,
				Pair.createP(new NodeVar("classModifiers"),
				RelationTypesWiggle.HAS_CLASS_MODIFIERS));
	}

	public static MatchElement getMethodModifiers(MatchElement methodNode) {
		return new Path(methodNode,
				Pair.createP(new NodeVar("methodModifiers"), RelationTypesWiggle.HAS_METHODDECL_MODIFIERS));
	}
}

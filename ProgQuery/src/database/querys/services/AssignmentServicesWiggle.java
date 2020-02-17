package database.querys.services;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.Path;
import database.relations.RelationTypesWiggle;
import utils.dataTransferClasses.Pair;

public class AssignmentServicesWiggle extends AssignmentServicesProgQueryImpl {
	@Override
	public Path getRightPartAssignments(Node rhs) {
		return getRightPartAssignments(Node.nodeForWiggle("assign", NodeTypes.ASSIGNMENT), rhs);
	}

	@Override
	public MatchElement getMemberSelectionsLeftSide(MatchElement assign) {
		return getLeftPartAssignments(assign).append(Pair.create(
				new EdgeImpl(Cardinalidad.MIN_TO_INF(0), RelationTypesWiggle.ARRAYACCESS_EXPR,
						RelationTypesWiggle.MEMBER_SELECT_EXPR),
				Node.nodeForWiggle("memberSelection", NodeTypes.MEMBER_SELECTION)));
	}

	@Override
	public MatchElement getLeftMostId(MatchElement assign) {
		return getLeftPartAssignments(assign).append(Pair.create(
				new EdgeImpl(Cardinalidad.MIN_TO_INF(0), RelationTypesWiggle.ARRAYACCESS_EXPR,
						RelationTypesWiggle.MEMBER_SELECT_EXPR),
				Node.nodeForWiggle("leftMostId", NodeTypes.IDENTIFIER)));
	}
}

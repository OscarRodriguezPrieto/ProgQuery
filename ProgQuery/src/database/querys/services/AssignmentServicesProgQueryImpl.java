package database.querys.services;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.relations.PDGRelationTypes;
import database.relations.RelationTypes;
import utils.dataTransferClasses.Pair;

public class AssignmentServicesProgQueryImpl implements AssignmentServices {

	public Path getRightPartAssignmentsAndVarDeclarations(Node varDec, Node rhsExpr) {
		return new Path(varDec,
				Pair.create(new EdgeImpl(PDGRelationTypes.MODIFIED_BY), new CompleteNode("ass", NodeTypes.ASSIGNMENT)),
				Pair.create(new EdgeImpl(RelationTypes.ASSIGNMENT_RHS),
						new CompleteNode(NodeTypes.LITERAL, Pair.create("typeKind", "NULL"))));
	}

	public Path getRightPartAssignmentsAndVarDeclarations(Node varDec) {
		return getRightPartAssignmentsAndVarDeclarations(varDec, new NodeVar("rhsExp"));
	}

	@Override
	public Path getRightPartAssignments(Node assign, Node rhs) {
		return new Path(rhs, Pair.create(new EdgeImpl(EdgeDirection.OUTGOING, RelationTypes.ASSIGNMENT_RHS), assign));
	}

	@Override
	public Path getRightPartAssignments(Node rhs) {
		return getRightPartAssignments(new CompleteNode("assign", NodeTypes.ASSIGNMENT), rhs);
	}

	@Override
	public MatchElement getLeftPartAssignments(MatchElement assign) {
		return getLeftPartAssignments(assign, new NodeVar("id"));
	}

	@Override
	public MatchElement getLeftPartAssignments(MatchElement assign, MatchElement id) {
		return new Path(assign, Pair.create(new EdgeImpl(RelationTypes.ASSIGNMENT_LHS), id));

	}

	@Override
	public MatchElement getMemberSelectionsLeftSide(MatchElement assign) {
		throw new IllegalStateException();
	}

	@Override
	public MatchElement getLeftMostId(MatchElement assign) {
		throw new IllegalStateException();
	}
}

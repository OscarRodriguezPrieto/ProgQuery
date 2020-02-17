package database.querys.services;

import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;

public interface AssignmentServices {

	public static final AssignmentServices PROG_QUERY = new AssignmentServicesProgQueryImpl();
	public static final AssignmentServices WIGGLE = new AssignmentServicesWiggle();

	public MatchElement getRightPartAssignments(Node assign, Node rhs);

	public MatchElement getRightPartAssignments(Node rhs);

	public MatchElement getLeftPartAssignments(MatchElement assign);

	public MatchElement getLeftPartAssignments(MatchElement assign, MatchElement id);

	public MatchElement getMemberSelectionsLeftSide(MatchElement assign);

	public MatchElement getLeftMostId(MatchElement assign);
}

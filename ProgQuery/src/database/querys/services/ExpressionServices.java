package database.querys.services;

import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;

public interface ExpressionServices {
	public static final ExpressionServicesProgQueryImpl PROG_QUERY = new ExpressionServicesProgQueryImpl();
	public static final ExpressionServices WIGGLE = new ExpressionServicesWiggle();

	public MatchElement getOuterMostExpressionFrom(Node exp);

	public MatchElement getOuterMostExpressionFrom(MatchElement p);


	public MatchElement getOuterMostExpressionFrom(MatchElement p, Node outExpr);

	public MatchElement getStatementFromOuterExp(MatchElement p);


	public MatchElement getStatementFromOuterExp(MatchElement p, Node stat);

	public MatchElement getStatementFromOuterExp(Node exp);

	public MatchElement getStatementFromOuterExp(Node exp, Node stat);

	public MatchElement getStatementFromExp(MatchElement p);

	public MatchElement getMethodFromExp(MatchElement exp);


	public MatchElement getStatementFromExp(MatchElement p, Node stat);

	public MatchElement getStatementFromExp(Node exp, Node stat);

	MatchElement getMethodFromExp(MatchElement exp, MatchElement method);

}

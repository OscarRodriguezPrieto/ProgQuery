package database.querys.services;

import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.Path;

public interface StatementServices {

	public static final StatementServices PROG_QUERY = new StatementServicesProgQueryImpl();
	public static final StatementServices WIGGLE = new StatementServicesWiggle();

	public MatchElement getEnclosingClassFromStatement(String statement);

	public MatchElement getEnclosingClassFromStatement(MatchElement statement);

	public MatchElement getEnclosingClassFromDeclaration(MatchElement statement);

	public MatchElement getMethodFromStatement(MatchElement statement);

	// public MatchElement getMethodFromDeclaration(MatchElement statement);

	public MatchElement getMethodFromStatement(MatchElement statement, MatchElement method);

	public MatchElement getOuterBlockFromStatement(MatchElement statement);

	public MatchElement getOuterBlockFromStatement(MatchElement statement, MatchElement block);

	public MatchElement getVarsAndParamsScopesInStatement(MatchElement statement);

	public MatchElement getVarsAndParamsScopesInStatement(MatchElement statement, Node scope);

	public MatchElement getVarDecsInStatement(Node statement, String varDecName);

	public MatchElement getVarDecsInStatement(MatchElement statement, String varDecName);

	public MatchElement getStatementsInStatement(MatchElement statement);

	public MatchElement getStatementsInStatement(MatchElement statement, MatchElement innerStatement);

	public MatchElement getUnconditionalStatementsInStatement(MatchElement statement, MatchElement innerStatement);

	Path getVarsAttrsAndParamsScopesInStatement(MatchElement statement, Node scope);

	Path getAttrAndVarDecsInStatement(MatchElement statement);

	Path getAttrAndVarDecsInStatement(MatchElement statement, MatchElement varDec);
}

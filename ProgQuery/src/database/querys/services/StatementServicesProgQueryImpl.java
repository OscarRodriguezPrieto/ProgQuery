package database.querys.services;

import database.querys.cypherWrapper.AnonymousNode;
import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.relations.RelationTypes;
import database.relations.RelationTypesInterface;
import utils.dataTransferClasses.Pair;

public class StatementServicesProgQueryImpl implements StatementServices {

	private final RelationTypes[] outerBlockToEnclClass = new RelationTypes[] { RelationTypes.DECLARES_FIELD,
			RelationTypes.DECLARES_METHOD, RelationTypes.DECLARES_CONSTRUCTOR, RelationTypes.HAS_STATIC_INIT };

	private final RelationTypes[] statToEnclMember = new RelationTypes[] { RelationTypes.CALLABLE_HAS_BODY,
			RelationTypes.CALLABLE_HAS_PARAMETER, RelationTypes.CASE_STATEMENTS, RelationTypes.CATCH_ENCLOSES_BLOCK,
			RelationTypes.CATCH_PARAM, RelationTypes.ENCLOSES,RelationTypes.DO_WHILE_STATEMENT, RelationTypes.WHILE_STATEMENT,  RelationTypes.FOREACH_STATEMENT,
			RelationTypes.FOREACH_VAR, RelationTypes.FORLOOP_INIT, RelationTypes.FORLOOP_STATEMENT,
			RelationTypes.FORLOOP_UPDATE, RelationTypes.CALLABLE_HAS_PARAMETER, RelationTypes.HAS_VARIABLEDECL_INIT,
			RelationTypes.IF_THEN, RelationTypes.IF_ELSE, RelationTypes.LABELED_STMT_ENCLOSES,
			RelationTypes.SWITCH_ENCLOSES_CASE, RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK, RelationTypes.TRY_BLOCK,
			RelationTypes.TRY_CATCH, RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES };
	static final RelationTypes[] statToOuterBlock = new RelationTypes[] { RelationTypes.CASE_STATEMENTS,
			RelationTypes.CATCH_ENCLOSES_BLOCK, RelationTypes.CATCH_PARAM, RelationTypes.ENCLOSES,RelationTypes.DO_WHILE_STATEMENT, RelationTypes.WHILE_STATEMENT, 
			RelationTypes.FOREACH_STATEMENT, RelationTypes.FOREACH_VAR, RelationTypes.FORLOOP_INIT,
			RelationTypes.FORLOOP_STATEMENT, RelationTypes.FORLOOP_UPDATE, RelationTypes.CALLABLE_HAS_PARAMETER,
			RelationTypes.IF_THEN, RelationTypes.IF_ELSE, RelationTypes.LABELED_STMT_ENCLOSES,
			RelationTypes.SWITCH_ENCLOSES_CASE, RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK, RelationTypes.TRY_BLOCK,
			RelationTypes.TRY_CATCH, RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES };

	static final RelationTypes[] unconditionalStatToOuterBlock = new RelationTypes[] { RelationTypes.CASE_STATEMENTS,
			RelationTypes.ENCLOSES, RelationTypes.FORLOOP_INIT, RelationTypes.CALLABLE_HAS_PARAMETER,
			RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.LABELED_STMT_ENCLOSES, RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK,
			RelationTypes.TRY_BLOCK, RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES , RelationTypes.DO_WHILE_STATEMENT};
	private final RelationTypes[] statNameScopes = new RelationTypes[] { RelationTypes.CASE_STATEMENTS,
			RelationTypes.CATCH_ENCLOSES_BLOCK, RelationTypes.CATCH_PARAM, RelationTypes.ENCLOSES,
			RelationTypes.FOREACH_STATEMENT, RelationTypes.FOREACH_VAR, RelationTypes.FORLOOP_INIT,
			RelationTypes.FORLOOP_STATEMENT, RelationTypes.FORLOOP_UPDATE, RelationTypes.CALLABLE_HAS_PARAMETER,
			RelationTypes.CALLABLE_HAS_BODY, RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_THEN,
			RelationTypes.IF_ELSE, RelationTypes.LABELED_STMT_ENCLOSES, RelationTypes.SWITCH_ENCLOSES_CASE,
			RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK, RelationTypes.TRY_BLOCK, RelationTypes.TRY_CATCH,RelationTypes.DO_WHILE_STATEMENT, RelationTypes.WHILE_STATEMENT, 
			RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES };
	private final RelationTypes[] statToVarDecs = new RelationTypes[] { RelationTypes.ENCLOSES,
			RelationTypes.FOREACH_VAR, RelationTypes.FORLOOP_INIT, RelationTypes.CALLABLE_HAS_PARAMETER,
			RelationTypes.TRY_RESOURCES };

	public Path getEnclosingClassFromStatement(String statement) {
		return getEnclosingClassFromStatement(new NodeVar(statement));
	}

	public Path getEnclosingClassFromStatement(MatchElement statement) {
		return getMethodFromStatement(statement, new AnonymousNode()).append(Pair.create(
				new EdgeImpl(outerBlockToEnclClass).setDirection(EdgeDirection.OUTGOING), new NodeVar("enclClass")));
	}

	@Override
	public MatchElement getEnclosingClassFromDeclaration(MatchElement statement) {
		// TODO Auto-generated method stub
		return getMethodFromDeclaration(statement).append(Pair.create(
				new EdgeImpl(outerBlockToEnclClass).setDirection(EdgeDirection.OUTGOING), new NodeVar("enclClass")));
	}
	public Path getOuterBlockFromStatement(MatchElement statement) {

		return getBlocksFromStatement(statement, statToOuterBlock);
	}

	Path getBlocksFromStatement(MatchElement statement, RelationTypesInterface[] rels) {
		return getBlocksFromStatement(statement, new AnonymousNode(), rels);
	}

	Path getBlocksFromStatement(MatchElement statement, MatchElement block, RelationTypesInterface[] rels) {
		return statement.append(Pair
				.create(new EdgeImpl(Cardinalidad.MIN_TO_INF(0), rels).setDirection(EdgeDirection.OUTGOING), block));
	}

	public Path getVarsAndParamsScopesInStatement(MatchElement statement) {
		return getBlocksFromStatement(statement, statNameScopes);
	}

	@Override
	public Path getVarsAndParamsScopesInStatement(MatchElement statement, Node scope) {
		return getBlocksFromStatement(statement, scope, statNameScopes);

	}

	@Override
	public Path getVarDecsInStatement(Node statement, String varDecName) {
		return getVarDecsInStatement(new Path(statement), varDecName);
	}

	@Override
	public Path getVarDecsInStatement(MatchElement statement, String varDecName) {
		throw new IllegalStateException();
	}

	private MatchElement getMethodFromDeclaration(MatchElement statement) {
		return statement.append(
				Pair.create(
						new EdgeImpl(Cardinalidad.MIN_TO_INF(0), statToEnclMember).setDirection(EdgeDirection.OUTGOING),
						new AnonymousNode()));
	}

	@Override
	public MatchElement getMethodFromStatement(MatchElement statement, MatchElement method) {
		return statement.append(Pair.create(
				new EdgeImpl(Cardinalidad.ONE_TO_INF, statToOuterBlock).setDirection(EdgeDirection.OUTGOING),
				new AnonymousNode()),
				Pair.create(new EdgeImpl(Cardinalidad.JUST_ONE, RelationTypes.CALLABLE_HAS_BODY)
						.setDirection(EdgeDirection.OUTGOING), method));
	}

	@Override
	public MatchElement getMethodFromStatement(MatchElement statement) {
		return getMethodFromStatement(statement, new CompleteNode("method"));
	}

	@Override
	public MatchElement getStatementsInStatement(MatchElement statement) {
		return getStatementsInStatement(statement, new NodeVar("innerStatement"));
	}

	MatchElement getStatementsInStatement(MatchElement statement, MatchElement innerStatement,
			RelationTypesInterface... rels) {
		return statement.append(Pair.create(new EdgeImpl(Cardinalidad.ONE_TO_INF, rels), innerStatement));
	}

	@Override
	public MatchElement getStatementsInStatement(MatchElement statement, MatchElement innerStatement) {
		return getStatementsInStatement(statement, innerStatement, statToOuterBlock);
	}

	@Override
	public MatchElement getOuterBlockFromStatement(MatchElement statement, MatchElement block) {
		return getBlocksFromStatement(statement, block, statToOuterBlock);
	}

	@Override
	public MatchElement getUnconditionalStatementsInStatement(MatchElement statement, MatchElement innerStatement) {
		return getStatementsInStatement(statement, innerStatement, unconditionalStatToOuterBlock);
	}

	@Override
	public Path getVarsAttrsAndParamsScopesInStatement(MatchElement statement, Node scope) {
		throw new IllegalStateException();
	}

	@Override
	public Path getAttrAndVarDecsInStatement(MatchElement statement) {

		throw new IllegalStateException();
	}

	@Override
	public Path getAttrAndVarDecsInStatement(MatchElement statement, MatchElement varDec) {


		throw new IllegalStateException();
	
	}



}

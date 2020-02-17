package database.querys.services;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.AnonymousNode;
import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.Path;
import database.relations.RelationTypesWiggle;
import utils.dataTransferClasses.Pair;

public class StatementServicesWiggle extends StatementServicesProgQueryImpl {

	private final RelationTypesWiggle[] statNameScopes = new RelationTypesWiggle[] {
			RelationTypesWiggle.CASE_STATEMENTS, RelationTypesWiggle.CATCH_BLOCK, RelationTypesWiggle.CATCH_PARAM,
			RelationTypesWiggle.ENCLOSES, RelationTypesWiggle.FOREACH_STATEMENT, RelationTypesWiggle.FOREACH_VAR,
			RelationTypesWiggle.FORLOOP_INIT, RelationTypesWiggle.FORLOOP_STATEMENT, RelationTypesWiggle.FORLOOP_UPDATE,
			RelationTypesWiggle.HAS_METHODDECL_PARAMETERS, RelationTypesWiggle.HAS_METHODDECL_BODY,
			RelationTypesWiggle.HAS_VARIABLEDECL_INIT, RelationTypesWiggle.IF_THEN, RelationTypesWiggle.IF_ELSE,
			RelationTypesWiggle.LABELED_STATEMENT, RelationTypesWiggle.SWITCH_ENCLOSES_CASES,
			RelationTypesWiggle.SYNCHRONIZED_BLOCK, RelationTypesWiggle.TRY_BLOCK, RelationTypesWiggle.TRY_CATCH,
			RelationTypesWiggle.TRY_FINALLY, RelationTypesWiggle.TRY_RESOURCES };
	private final RelationTypesWiggle[] completeStatNameScopes = new RelationTypesWiggle[] {
			RelationTypesWiggle.CASE_STATEMENTS, RelationTypesWiggle.CATCH_BLOCK, RelationTypesWiggle.CATCH_PARAM,
			RelationTypesWiggle.ENCLOSES, RelationTypesWiggle.FOREACH_STATEMENT, RelationTypesWiggle.FOREACH_VAR,
			RelationTypesWiggle.FORLOOP_INIT, RelationTypesWiggle.FORLOOP_STATEMENT, RelationTypesWiggle.FORLOOP_UPDATE,
			RelationTypesWiggle.HAS_METHODDECL_PARAMETERS, RelationTypesWiggle.HAS_METHODDECL_BODY,
			RelationTypesWiggle.HAS_VARIABLEDECL_INIT, RelationTypesWiggle.IF_THEN, RelationTypesWiggle.IF_ELSE,
			RelationTypesWiggle.LABELED_STATEMENT, RelationTypesWiggle.SWITCH_ENCLOSES_CASES,
			RelationTypesWiggle.SYNCHRONIZED_BLOCK, RelationTypesWiggle.TRY_BLOCK, RelationTypesWiggle.TRY_CATCH,
			RelationTypesWiggle.TRY_FINALLY, RelationTypesWiggle.TRY_RESOURCES,

			RelationTypesWiggle.DECLARES_METHOD
			,RelationTypesWiggle.HAS_CLASS_BODY
//			RelationTypesWiggle.IS_SUBTYPE_EXTENDS,	RelationTypesWiggle.IS_SUBTYPE_IMPLEMENTS, RelationTypesWiggle.IS_SUBTYPE_OF
			};
	private final RelationTypesWiggle[] scopesToVarDecs = new RelationTypesWiggle[] { RelationTypesWiggle.CATCH_PARAM,
			RelationTypesWiggle.ENCLOSES, RelationTypesWiggle.FOREACH_VAR, RelationTypesWiggle.FORLOOP_INIT,

			RelationTypesWiggle.HAS_METHODDECL_PARAMETERS, RelationTypesWiggle.LABELED_STATEMENT,
			RelationTypesWiggle.TRY_RESOURCES };
	private final RelationTypesWiggle[] completeScopesToVarDecs = new RelationTypesWiggle[] {
			RelationTypesWiggle.CATCH_PARAM, RelationTypesWiggle.ENCLOSES, RelationTypesWiggle.FOREACH_VAR,
			RelationTypesWiggle.FORLOOP_INIT, RelationTypesWiggle.HAS_METHODDECL_PARAMETERS,
			RelationTypesWiggle.LABELED_STATEMENT, RelationTypesWiggle.TRY_RESOURCES,
			RelationTypesWiggle.DECLARES_FIELD };
	static final RelationTypesWiggle[] unconditionalStatToOuterBlock = new RelationTypesWiggle[] {
			RelationTypesWiggle.CASE_STATEMENTS, RelationTypesWiggle.ENCLOSES, RelationTypesWiggle.FORLOOP_INIT,
			RelationTypesWiggle.HAS_METHODDECL_PARAMETERS, RelationTypesWiggle.HAS_VARIABLEDECL_INIT,
			RelationTypesWiggle.LABELED_STATEMENT, RelationTypesWiggle.SYNCHRONIZED_BLOCK,
			RelationTypesWiggle.TRY_BLOCK, RelationTypesWiggle.TRY_FINALLY, RelationTypesWiggle.TRY_RESOURCES };

	@Override
	public Path getVarsAndParamsScopesInStatement(MatchElement statement, Node scope) {
		return getBlocksFromStatement(statement, scope, statNameScopes);

	}

	@Override
	public Path getVarsAttrsAndParamsScopesInStatement(MatchElement statement, Node scope) {
		return getBlocksFromStatement(statement, new AnonymousNode(), completeStatNameScopes).append(Pair.create(new EdgeImpl(Cardinalidad.MIN_TO_INF(0), 
new RelationTypesWiggle[]{RelationTypesWiggle. IS_SUBTYPE_EXTENDS,RelationTypesWiggle. IS_SUBTYPE_IMPLEMENTS,RelationTypesWiggle. IS_SUBTYPE_OF}
				), scope))
				
				;

	}

	@Override
	public Path getVarDecsInStatement(MatchElement statement, String varDecName) {

		return statement
				.append(Pair.create(new EdgeImpl(scopesToVarDecs), Node.nodeForWiggle(varDecName, NodeTypes.LOCAL_VAR_DEF)));
	}

	@Override
	public Path getAttrAndVarDecsInStatement(MatchElement statement) {

		return statement.append(
				Pair.create(new EdgeImpl("scopeToVar", completeScopesToVarDecs),
						Node.nodeForWiggle("varDec", NodeTypes.LOCAL_VAR_DEF)));
	}
	@Override
	public Path getAttrAndVarDecsInStatement(MatchElement statement, MatchElement varDec) {

		return statement.append(
				Pair.create(new EdgeImpl("scopeToVar", completeScopesToVarDecs),
						varDec));
	}
	@Override
	public MatchElement getMethodFromStatement(MatchElement statement) {
		return getMethodFromStatement(statement, Node.nodeForWiggle(NodeTypes.METHOD_DEF));
	}

	@Override
	public MatchElement getUnconditionalStatementsInStatement(MatchElement statement, MatchElement innerStatement) {
		return getStatementsInStatement(statement, innerStatement, unconditionalStatToOuterBlock);
	}

}

package database.querys.services;

import database.querys.cypherWrapper.AnonymousNode;
import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.relations.RelationTypes;
import database.relations.RelationTypesInterface;
import utils.dataTransferClasses.Pair;

public class ExpressionServicesProgQueryImpl implements ExpressionServices {
	private static final RelationTypes[] EXPR_TO_OUT_EXPR = new RelationTypes[] { RelationTypes.ARRAYACCESS_EXPR,
			RelationTypes.ARRAYACCESS_INDEX, RelationTypes.ASSIGNMENT_LHS, RelationTypes.ASSIGNMENT_RHS,
			RelationTypes.BINOP_LHS, RelationTypes.BINOP_RHS, RelationTypes.CAST_ENCLOSES,
			RelationTypes.COMPOUND_ASSIGNMENT_LHS, RelationTypes.COMPOUND_ASSIGNMENT_RHS,
			RelationTypes.CONDITIONAL_EXPR_CONDITION, RelationTypes.CONDITIONAL_EXPR_THEN,
			RelationTypes.CONDITIONAL_EXPR_ELSE, RelationTypes.INITIALIZATION_EXPR, RelationTypes.INSTANCE_OF_EXPR,
			RelationTypes.MEMBER_REFERENCE_EXPRESSION, RelationTypes.MEMBER_SELECT_EXPR,
			RelationTypes.METHODINVOCATION_ARGUMENTS, RelationTypes.METHODINVOCATION_METHOD_SELECT,
			RelationTypes.NEW_CLASS_ARGUMENTS, RelationTypes.NEW_ARRAY_INIT, RelationTypes.NEW_ARRAY_DIMENSION,
			RelationTypes.UNARY_ENCLOSES };
	private static final RelationTypes[] EXPR_TO_STAT = new RelationTypes[] { RelationTypes.ASSERT_CONDITION,
			RelationTypes.DO_WHILE_CONDITION, RelationTypes.ENCLOSES_EXPR, RelationTypes.FOREACH_EXPR,
			RelationTypes.FORLOOP_CONDITION, RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_CONDITION,
			RelationTypes.RETURN_EXPR,
			RelationTypes.SWITCH_EXPR, RelationTypes.SYNCHRONIZED_EXPR, RelationTypes.THROW_EXPR,
			RelationTypes.WHILE_CONDITION };

	public Path getOuterMostExpressionFrom(Node exp) {
		return getOuterMostExpressionFrom(new Path(exp));
	}

	public Path getOuterMostExpressionFrom(MatchElement p) {
		return getOuterMostExpressionFrom(p, new AnonymousNode());
	}

	public Path getOuterMostExpressionFrom(MatchElement p, Node outExpr) {
		return getOuterMostExpressionFrom(p, outExpr, EXPR_TO_OUT_EXPR);
	}

	Path getOuterMostExpressionFrom(MatchElement p, Node outExpr, RelationTypesInterface... rels) {
		return p.append(Pair.create(new EdgeImpl(Cardinalidad.MIN_TO_INF(0), rels).setDirection(EdgeDirection.OUTGOING),
				outExpr));
	}

	public Path getStatementFromOuterExp(MatchElement p) {
		return getStatementFromOuterExp(p, new NodeVar("stat"));
	}

	public Path getStatementFromOuterExp(MatchElement p, Node stat) {
		return getStatementFromOuterExp(p, stat, EXPR_TO_STAT);
	}

	Path getStatementFromOuterExp(MatchElement p, Node stat, RelationTypesInterface... rels) {
		return p.append(Pair.create(new EdgeImpl(rels).setDirection(EdgeDirection.OUTGOING), stat));
	}

	public Path getStatementFromOuterExp(Node exp) {
		return getStatementFromOuterExp(new Path(exp));
	}

	public Path getStatementFromOuterExp(Node exp, Node stat) {
		return getStatementFromOuterExp(new Path(exp), stat);
	}

	public Path getStatementFromExp(MatchElement p) {
		return getStatementFromOuterExp(getOuterMostExpressionFrom(p));
		// return p.append(
		// Pair.create(new
		// EdgeImpl(EXPR_TO_STAT).setDirection(EdgeDirection.OUTGOING), new
		// NodeVar("stat")));
	}

	public Path getStatementFromExp(MatchElement p, Node stat) {
		return getStatementFromOuterExp(getOuterMostExpressionFrom(p), stat);
		// return p.append(
		// Pair.create(new
		// EdgeImpl(EXPR_TO_STAT).setDirection(EdgeDirection.OUTGOING), new
		// NodeVar("stat")));
	}

	public Path getStatementFromExp(Node exp, Node stat) {
		;
		return getStatementFromExp(new Path(exp), stat);
	}

	@Override
	public MatchElement getMethodFromExp(MatchElement exp) {
		return getStatementServices().getMethodFromStatement(getStatementFromExp(exp));
	}

	@Override
	public MatchElement getMethodFromExp(MatchElement exp, MatchElement method) {
		return getStatementServices().getMethodFromStatement(getStatementFromExp(exp), method);
	}
	StatementServices getStatementServices() {
		return StatementServices.PROG_QUERY;
	}

}

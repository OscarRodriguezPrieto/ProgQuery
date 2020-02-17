package database.querys.services;

import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.Path;
import database.relations.RelationTypesWiggle;

public class ExpressionServicesWiggle extends ExpressionServicesProgQueryImpl {
	private static final RelationTypesWiggle[] EXPR_TO_OUT_EXPR = new RelationTypesWiggle[] {
			RelationTypesWiggle.ARRAYACCESS_EXPR, RelationTypesWiggle.ARRAYACCESS_INDEX,
			RelationTypesWiggle.ASSIGNMENT_LHS, RelationTypesWiggle.ASSIGNMENT_RHS, RelationTypesWiggle.BINOP_LHS,
			RelationTypesWiggle.BINOP_RHS, RelationTypesWiggle.CAST_ENCLOSES,
			RelationTypesWiggle.COMPOUND_ASSIGNMENT_LHS, RelationTypesWiggle.COMPOUND_ASSIGNMENT_RHS,
			RelationTypesWiggle.CONDITIONAL_CONDITION, RelationTypesWiggle.CONDITIONAL_THEN,
			RelationTypesWiggle.CONDITIONAL_ELSE, RelationTypesWiggle.HAS_VARIABLEDECL_INIT,
			RelationTypesWiggle.INSTANCEOF_EXPR, RelationTypesWiggle.PARENTHESIZED_ENCLOSES,
			RelationTypesWiggle.MEMBER_SELECT_EXPR, RelationTypesWiggle.METHODINVOCATION_ARGUMENTS,
			RelationTypesWiggle.METHODINVOCATION_METHOD_SELECT, RelationTypesWiggle.NEW_CLASS_ARGUMENTS,
			RelationTypesWiggle.NEWARRAY_INIT, RelationTypesWiggle.NEWARRAY_DIMENSION,
			RelationTypesWiggle.UNARY_ENCLOSES };

	private static final RelationTypesWiggle[] EXPR_TO_STAT = new RelationTypesWiggle[] {
			RelationTypesWiggle.ASSERT_CONDITION, RelationTypesWiggle.DOWHILE_CONDITION,
			RelationTypesWiggle.EXPR_ENCLOSES, RelationTypesWiggle.FOREACH_EXPR, RelationTypesWiggle.FORLOOP_CONDITION,
			RelationTypesWiggle.HAS_VARIABLEDECL_INIT, RelationTypesWiggle.IF_CONDITION,
			RelationTypesWiggle.RETURN_EXPR,
			RelationTypesWiggle.SWITCH_EXPR, RelationTypesWiggle.SYNCHRONIZED_EXPR, RelationTypesWiggle.THROW_EXPR,
			RelationTypesWiggle.WHILE_CONDITION };

	@Override
	public Path getOuterMostExpressionFrom(MatchElement p, Node outExpr) {

		return ExpressionServices.PROG_QUERY.getOuterMostExpressionFrom(p, outExpr, EXPR_TO_OUT_EXPR);
	}

	@Override
	public Path getStatementFromOuterExp(MatchElement p, Node stat) {
		return getStatementFromOuterExp(p, stat, EXPR_TO_STAT);
	}

	@Override
	StatementServices getStatementServices() {
		return StatementServices.WIGGLE;
	}
}

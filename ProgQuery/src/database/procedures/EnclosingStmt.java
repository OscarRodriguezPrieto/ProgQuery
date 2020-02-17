package database.procedures;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import database.relations.RelationTypes;

public class EnclosingStmt {

	@UserFunction
	public Node getEnclosingClass(@Name("node") Node stmtOrVarDef) {

		
		stmtOrVarDef = getEnclosingMethod(stmtOrVarDef);
		Iterator<Relationship> rels = stmtOrVarDef.getRelationships(RelationTypes.DECLARES_FIELD, RelationTypes.DECLARES_METHOD,
				RelationTypes.DECLARES_CONSTRUCTOR, RelationTypes.HAS_STATIC_INIT).iterator();
		return rels.hasNext() ? rels.next().getStartNode() : null;
	}

	@UserFunction
	public Node getEnclosingMethod(@Name("node") Node stmtOrParam) {

		stmtOrParam = FunctionUtils.getLastNodeInRels(stmtOrParam, RelationTypes.CALLABLE_HAS_BODY,
				RelationTypes.CALLABLE_HAS_PARAMETER, RelationTypes.CASE_STATEMENTS, RelationTypes.CATCH_ENCLOSES_BLOCK,
				RelationTypes.CATCH_PARAM, RelationTypes.WHILE_STATEMENT, RelationTypes.DO_WHILE_STATEMENT,
				RelationTypes.ENCLOSES, RelationTypes.FOREACH_STATEMENT, RelationTypes.FOREACH_VAR,
				RelationTypes.FORLOOP_INIT, RelationTypes.FORLOOP_STATEMENT, RelationTypes.FORLOOP_UPDATE,
				RelationTypes.CALLABLE_HAS_PARAMETER, RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_THEN,
				RelationTypes.IF_ELSE, RelationTypes.LABELED_STMT_ENCLOSES, RelationTypes.SWITCH_ENCLOSES_CASE,
				RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK, RelationTypes.TRY_BLOCK, RelationTypes.TRY_CATCH,
				RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES);
		return stmtOrParam;
	}

	@UserFunction
	public Node getEnclMethodFromExpr(@Name("node") Node expr) {

		return getEnclosingMethod(getEnclosingStmt(expr));
	}

	@UserFunction
	public Node getEnclosingStmt(@Name("node") Node expr) {
	
		if (expr == null)
			return null;
		expr = FunctionUtils.getLastNodeInRels(expr, RelationTypes.ARRAYACCESS_EXPR, RelationTypes.ARRAYACCESS_INDEX,
				RelationTypes.ASSIGNMENT_LHS, RelationTypes.ASSIGNMENT_RHS, RelationTypes.BINOP_LHS,
				RelationTypes.BINOP_RHS, RelationTypes.CAST_ENCLOSES, RelationTypes.COMPOUND_ASSIGNMENT_LHS,
				RelationTypes.COMPOUND_ASSIGNMENT_RHS, RelationTypes.CONDITIONAL_EXPR_CONDITION,
				RelationTypes.CONDITIONAL_EXPR_THEN, RelationTypes.CONDITIONAL_EXPR_ELSE,
				RelationTypes.INITIALIZATION_EXPR, RelationTypes.INSTANCE_OF_EXPR,
				RelationTypes.MEMBER_REFERENCE_EXPRESSION, RelationTypes.MEMBER_SELECT_EXPR,
				RelationTypes.METHODINVOCATION_ARGUMENTS, RelationTypes.METHODINVOCATION_METHOD_SELECT,
				RelationTypes.NEW_CLASS_ARGUMENTS, RelationTypes.NEW_ARRAY_INIT, RelationTypes.NEW_ARRAY_DIMENSION,
				RelationTypes.UNARY_ENCLOSES); 
		Iterator<Relationship> rels = expr.getRelationships(Direction.INCOMING, RelationTypes.ASSERT_CONDITION,
				RelationTypes.DO_WHILE_CONDITION, RelationTypes.ENCLOSES_EXPR, RelationTypes.FOREACH_EXPR,
				RelationTypes.FORLOOP_CONDITION, RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_CONDITION,
				RelationTypes.RETURN_EXPR, RelationTypes.SWITCH_EXPR, RelationTypes.SYNCHRONIZED_EXPR,
				RelationTypes.THROW_EXPR, RelationTypes.WHILE_CONDITION).iterator();
		return rels.hasNext() ? rels.next().getStartNode() : null;
	}
}

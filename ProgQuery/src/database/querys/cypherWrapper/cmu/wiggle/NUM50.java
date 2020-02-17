package database.querys.cypherWrapper.cmu.wiggle;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Any;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.Extract;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.WhereClause;

public class NUM50 extends AbstractQuery {

	/*
	 * private static final String
	 * NUM50CONVERT_INT_TO_FLOAT_FOR_FLOAT_OPERATIONS =
	 * "MATCH (varDec{actualType:'float'})-[:MODIFIED_BY]->(mod)-[:ASSIGNMENT_RHS | :INITIALIZATION_EXPR]->(rightSide)"
	 * +
	 * " OPTIONAL MATCH (binopR{actualType:'int'})<-[:BINOP_RHS]-(rightSide)-[:BINOP_LHS]->({actualType:'int'})"
	 * +
	 * " WITH varDec, COLLECT(rightSide.actualType='float') as rightSidesAreFloat, "
	 * + new Filter("COLLECT([rightSide,binopR])",
	 * "NOT x[1] IS NULL AND x[0].operator='DIVIDE'").expToString() +
	 * " as lines" + " WHERE " + " NOT " + new Any("rightSidesAreFloat",
	 * "x").expToString() + " AND SIZE(lines)>0 "
	 * 
	 * // UN COLLECT DE FLOATS PAL ANY y un filter de lines que tiene que // ser
	 * mayor que 0 +
	 * " RETURN 'Warning [CMU-NUM50] A truncated integer division was detected in line(s) ' +"
	 * + new Extract("lines", "x[0].lineNumber").expToString() +
	 * " +', assigned to variables of type float. If you want to make a float division and assign the result to the variable ' + varDec.name+', you must include a operand as a float. Otherwise you can change the type of '+ varDec.name+' from float to int, as it is never used to store an actual float value.' "
	 * // + new Reduce("lines", "s+','+x", "''").expToString() ;
	 */

	public NUM50() {
		super(false);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initiate() {
		clauses = new Clause[] {
				// new SimpleWithClause("varDec"),
				new MatchClause(true,
						// LHS_ ASSIGN
						getPDGServices().getCompleteIdentification(new NodeVar("id"), "")),

				new WhereClause("dec.actualType='float'"),
				new MatchClause(true,
						new MatchImpl("(rhs)<-[:ASSIGNMENT_RHS]-(modif{nodeType:'JCAssign'})-[:ASSIGNMENT_LHS]->(id)")),

				new MatchClause(true, new MatchImpl("(dec)-[:HAS_VARIABLEDECL_INIT]->(init)")),
				new SimpleWithClause("init,rhs,dec"),
				new MatchClause(new MatchImpl(
						"(binopR{actualType:'int'})<-[:BINOP_RHS]-(division)<-[:BINOP_LHS | :BINOP_RHS | :UNARY_ENCLOSES |:CONDITIONAL_ELSE|:CONDITIONAL_THEN |:PARENTHESIZED_ENCLOSES*0..]-(rightSide), (division)-[:BINOP_LHS]->({actualType:'int'}) WHERE rightSide=rhs OR rightSide=init")),
				new SimpleWithClause(
						"FILTER(x IN CASE WHEN init IS NULL THEN COLLECT(rhs) ELSE COLLECT(rhs) +init END WHERE NOT x IS NULL) as rhs, dec as varDec"),
				new WhereClause("NOT " + new Any("rhs", "x.actualType='float'").expToString()),
				new ReturnClause("'Warning [CMU-NUM50] A truncated integer division was detected in line(s) ' +"
						+ new Extract("rhs", "x.lineNumber").expToString()
						+ " +', assigned to variable of type float. If you want to make a float division and assign the result to the variable ' + varDec.name+', you must include a operand as a float. Otherwise you can change the type of '+ varDec.name+' from float to int, as it is never used to store an actual float value.' "

				) };
	}

}

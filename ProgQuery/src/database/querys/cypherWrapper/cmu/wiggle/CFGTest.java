package database.querys.cypherWrapper.cmu.wiggle;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.NodeVar;

public class CFGTest extends AbstractQuery {

	public CFGTest() {
		super(false);
	}

	@Override
	protected void initiate() {

		clauses = new Clause[] { (Clause) getCFGServices().getCFGSuccesorsOf(new NodeVar("newStat"), ""),
				// new ClauseImpl("MATCH (prevStat) WHERE prevStat IN FILTER(x
				// IN succesors WHERE x[0]=modStat)[0][1]"),
//				new ReturnClause(
//						"DISTINCT newStat, if, innerStatement, ifRel, otherifRel  ")
				/*
																		 * , new
																		 * MatchClause
																		 * (
																		 * true,
																		 * new
																		 * MatchImpl
																		 * (
																		 * "(invSel)<-[:METHODINVOCATION_METHOD_SELECT]-(inv)"
																		 * ),
																		 * getExpressionServices
																		 * ().
																		 * getStatementFromExp
																		 * (new
																		 * NodeVar
																		 * (
																		 * "inv"
																		 * ),
																		 * new
																		 * NodeVar
																		 * (
																		 * "prevStat"
																		 * )),
																		 * getStatementServices
																		 * ().
																		 * getEnclosingClassFromStatement
																		 * (new
																		 * NodeVar
																		 * (
																		 * "prevStat"
																		 * ))),
																		 * new
																		 * MatchClause
																		 * (
																		 * true,
																		 * new
																		 * MatchImpl
																		 * (
																		 * "(invSel)-[:MEMBER_SELECT_EXPR]->(object)"
																		 * )),
																		 * new
																		 * MatchClause
																		 * (
																		 * true,
																		 * new
																		 * MatchImpl
																		 * (
																		 * "(classDec)-[:DECLARES_METHOD]->(methodDec)-[:HAS_METHODDECL_THROWS]->(throw)"
																		 * )),
																		 * new
																		 * WhereClause(
																		 * "(NOT object IS NULL AND object.actualType=classDec.fullyQualifiedName) OR (object IS NULL AND enclClass=classDec)"
																		 * ),
																		 * new
																		 * SimpleWithClause("identss, closeableDec, object, newStat, closeStat,modStat,succesors,throw,prevStat"
																		 * ),
																		 * new
																		 * MatchClause
																		 * (
																		 * true,
																		 * getStatementServices
																		 * ().
																		 * getOuterBlockFromStatement
																		 * (new
																		 * NodeVar
																		 * (
																		 * "prevStat"
																		 * ),
																		 * new
																		 * NodeVar
																		 * (
																		 * "try"
																		 * ))
																		 * 
																		 * , new
																		 * MatchImpl(
																		 * "(try)-[:TRY_FINALLY | :TRY_CATCH | :CATCH_BLOCK *]->(afterBlock{nodeType:'JCBlock'})"
																		 * ) ,
																		 * getStatementServices
																		 * ().
																		 * getStatementsInStatement
																		 * (new
																		 * NodeVar
																		 * (
																		 * "afterBlock"
																		 * )) ),
																		 * new
																		 * SimpleWithClause(
																		 * "try.nodeType='JCTry' AND (prevStat.nodeType='JCThrow' OR NOT throw IS NULL OR prevStat.nodeType='JCAssert')"
																		 * ),
																		 * new
																		 * ReturnClause(
																		 * "identss, closeableDec, object, prevStat, newStat, closeStat,modStat,succesors,throw, COLLECT(innerStatement)as statementsAfterEx "
																		 * ),
																		 * new
																		 * WhereClause(" closeStat IN FILTER(x IN succesors WHERE x[0]=prevStat)[0][1]"
																		 * ),
																		 * new
																		 * ReturnClause(
																		 * "identss, closeableDec, object, newStat,COLLECT(prevStat) as prevStats, closeStat,modStat,succesors,throw,  statementsAfterEx "
																		 * ),
																		 * 
																		 */
		};
	}

	public static void main(String[] args) {
		System.out.println(new CFGTest().queryToString());
	}
}

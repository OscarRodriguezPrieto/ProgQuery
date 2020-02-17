package database.querys.cypherWrapper.cmu.wiggle;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;

public class DCL53_MEDIO extends AbstractQuery {
	/*
	 * " MATCH (typeDec)-[:DECLARES_FIELD]->(attr:ATTR_DEC) " +
	 * "OPTIONAL MATCH (attr)-[:USED_BY]->(exprUse)<-[" + exprToOutExprQuery +
	 * "*0..]-(outUseExpr)<-[" + exprToStatQueryWithReturn +
	 * "]-(exprUseStat)<-[" + getAnyRel(statToOuterBlock) +
	 * "*]-(outerBlock)<-[:HAS_METHODDECL_BODY]-() WITH typeDec.fullyQualifiedName as className, attr,outerBlock, exprUseStat, exprUse"
	 * 
	 * 
	 * + " OPTIONAL MATCH (attr)-[:MODIFIED_BY]->(modif)<-[" +
	 * 
	 * 
	 * getAnyRel(assignToOutExprNoCond) + "*0..]-(outModExpr)<-[" +
	 * exprToStatQueryWithReturn + "]-(exprModStat)<-[" +
	 * getAnyRel(statToOuterBlock) + "*]-(outerBlock),
	 * 
	 * (modif)-[:ASSIGNMENT_LHS]->(lhs_expr) WHERE exprModStat.position <
	 * exprUseStat.position " +
	 * 
	 * 
	 * " OPTIONAL MATCH (exprUse)-[:MEMBER_SELECT_EXPR]->(memberSelectExprUse)<-[:USED_BY]-(varDec) "
	 * + " OPTIONAL MATCH p=(varDec)-[:STATE_MODIFIED_BY]->(modif)" +
	 * " OPTIONAL MATCH (lhs_expr)-[:MEMBER_SELECT_EXPR]->(memberSelectExprModif) "
	 * +
	 * " WITH p,className, attr.name as attr,exprUse, modif,lhs_expr, memberSelectExprUse, memberSelectExprModif, exprUseStat, exprModStat "
	 * + " OPTIONAL MATCH q=(exprModStat)<-[" + getAnyRel(statToOuterBlock) +
	 * "*0..]-(minimumCommonBlock), (minimumCommonBlock)-[" +
	 * getAnyRel(statToOuterBlock) + "*0..]->(exprUseStat) " +
	 * " WITH className, attr, exprUse, ANY(x IN COLLECT((NOT ANY(rel IN RELS(q) WHERE type(rel)='FORLOOP_UPDATE' OR type(rel)='IF_ELSE' OR type(rel)='FORLOOP_UPDATE' OR type(rel)='FORLOOP_STATEMENT' OR  type(rel)='FOREACH_STATEMENT' OR type(rel)='TRY_CATCH' OR type(rel)='SWITCH_ENCLOSES_CASES' OR type(rel)='IF_THEN') "
	 * +
	 * " AND ((exprUse:IDENTIFIER OR (NOT memberSelectExprUse IS NULL AND memberSelectExprUse:IDENTIFIER AND (memberSelectExprUse.name='this' OR memberSelectExprUse.name='super')))"
	 * +
	 * " AND (lhs_expr:IDENTIFIER OR (NOT memberSelectExprModif IS NULL AND memberSelectExprModif:IDENTIFIER  AND (memberSelectExprModif.name='this' OR memberSelectExprModif.name='super'))) ) OR "
	 * +
	 * " (p IS NOT NULL AND NOT memberSelectExprUse IS NULL AND NOT memberSelectExprModif IS NULL AND memberSelectExprUse:IDENTIFIER AND memberSelectExprModif:IDENTIFIER))) WHERE x) as useWithModif "
	 * +
	 * " WITH className, attr, ALL( x IN COLLECT(useWithModif) WHERE x) OR exprUse IS NULL as isSillyAttr WHERE isSillyAttr"
	 * +
	 * " RETURN 'Warning [CMU-DCL53] You must minimize the scope of the varaibles. You can minimize the scope of the attribute '+attr+' in class '+className + ' by transforming it into a local varaible (as everytime its value is used in a method, there is a previous unconditional assignment).'"
	 */
	public DCL53_MEDIO(boolean isProgQuery) {
		super(isProgQuery);
		
	}
	
	@Override
	protected void initiate() {
		clauses = new Clause[] { 
		/*		new MatchClause(false,"(var{nodeType:'JCVariableDecl'})"),
				new MatchClause(true, getPDGServices().getCompleteIdentificationFromVar(new NodeVar("var"), ""))
,new UnwindClause("ids","id"),
new ClauseImpl("CREATE (id)-[:HAS_DEC]->(var)"),
new SimpleWithClause("var, COLLECT(id) as ids"),*/
				new MatchClause(false,"(var{nodeType:'JCVariableDecl'})"),
				new MatchClause(true, getPDGServices().getCompleteIdentificationFromVar( "")),
//,new UnwindClause("ids","id"),
//new ClauseImpl("CREATE (id)-[:HAS_DEC]->(var)"),
new SimpleWithClause("COLLECT([var, ids]) as identss")

,new UnwindClause("identss","p"),

new SimpleWithClause("p[0] as attr, p[1] as ids, identss"),



				
				new MatchClause(new MatchImpl("(classMod)<-[:HAS_CLASS_MODIFIERS]-(typeDec)-[:DECLARES_FIELD]->(attr)-[:HAS_VARIABLEDECL_MODIFIERS]->(attrMod)")),
				new WhereClause("NOT ( (attrMod.flags CONTAINS 'public' OR attrMod.flags CONTAINS 'protected' AND NOT classMod.flags CONTAINS 'final') AND classMod.flags CONTAINS 'public' ) AND "
						+ " NOT( attrMod.flags  CONTAINS 'static' AND attr.actualType='long' AND attrMod.flags CONTAINS 'final'  AND attr.name='serialVersionUID') "
						),
				new MatchClause(true, "(attr)<-[:HAS_DEC]-(id)"),
				new SimpleWithClause("typeDec,attr,COLLECT( id) as ids"),
//								new MatchClause(true, getPDGServices().getCompleteIdentificationFromVar(new NodeVar("attr"), "typeDec,")),
//							
//								new MatchClause(true, getPDGServices().getCompleteIdentificationFromVar(new NodeVar("attr"), "typeDec,")),
//				new WhereClause(" attr=varDec "),
//				new SimpleWithClause("typeDec,attr,COLLECT( id) as ids"),
				new MatchClause(true, new MatchImpl("(modif{nodeType:'JCAssign'})-[:ASSIGNMENT_LHS ]->(id)")),
				 new WhereClause(" id IN ids"),
				 new SimpleWithClause("typeDec,attr,COLLECT( id) as assignIds, ids"),
				 
//				 new ReturnClause("typeDec.fullyQualifiedName,attr.name, EXTRACT( a IN assignIds |a.lineNumber), EXTRACT (a IN FILTER(id IN ids WHERE NOT id IN assignIds) |a.lineNumber)")
				 new SimpleWithClause("typeDec,attr, assignIds,FILTER(id IN ids WHERE NOT id IN assignIds) as useIds"),
//				, new ReturnClause("typeDec.fullyQualifiedName,attr.name, EXTRACT( a IN assignIds |a.lineNumber),  EXTRACT( a IN useIds|a.lineNumber)")
					
				 new SimpleWithClause("typeDec,attr,CASE WHEN SIZE(assignIds)=0 THEN [null] ELSE assignIds END as assignIds,"
				 		+ "CASE WHEN SIZE(useIds)=0 THEN [null] ELSE useIds END as useIds"),
				 new UnwindClause("useIds", "useId"),
				 new UnwindClause("assignIds", "assignId"),
//				 new ReturnClause("typeDec.fullyQualifiedName,attr.name,useId.lineNumber, assignId.lineNumber")
//					
				 
				 new MatchClause(true,getStatementServices().getMethodFromStatement(
							getExpressionServices().getStatementFromExp(new NodeVar("useId")), new NodeVar("method"))),
					new WhereClause("method.nodeType='JCMethodDecl'"),
					
					new SimpleWithClause("typeDec.fullyQualifiedName as className,attr,useId, assignId, method, stat as useStat"),
					
					new MatchClause(true, getStatementServices().getMethodFromStatement(
							getExpressionServices().getStatementFromExp(new NodeVar("assignId")), new NodeVar("method"))),
					new ClauseImpl(" OPTIONAL MATCH (useId)-[:MEMBER_SELECT_EXPR]->(memberSelectExprUse)-[:HAS_DEC]->(varDec)"
							+ " WITH attr, assignId,  useId, useStat,stat, method, memberSelectExprUse, className, varDec"),
					new MatchClause(true, new MatchImpl("(assignId)-[:MEMBER_SELECT_EXPR]->(memberSelectModif)-[:HAS_DEC]->(modifIdvarDec)")),
					new SimpleWithClause(
							"attr, useStat,stat, method, memberSelectExprUse, varDec,memberSelectModif,className,modifIdvarDec,useId, assignId"),
					new MatchClause(true,
							getStatementServices().getOuterBlockFromStatement(new NodeVar("useStat"),
									new NodeVar("commonBlock")),
							new Path(getStatementServices().getOuterBlockFromStatement(new NodeVar("stat"),
												new NodeVar("commonBlock")), "modifStatToCommonBlockPath")),
					new SimpleWithClause("className,attr,useId,"
//							+ " stat,RELS(modifStatToCommonBlockPath), (((useId.nodeType='JCIdent' OR (NOT memberSelectExprUse IS NULL AND memberSelectExprUse.nodeType='JCIdent' AND (memberSelectExprUse.name='this' OR memberSelectExprUse.name='super'))) AND (assignId.nodeType='JCIdent' OR (NOT memberSelectModif IS NULL AND memberSelectModif.nodeType='JCIdent'  AND (memberSelectModif.name='this' OR memberSelectModif.name='super'))) ) OR (NOT memberSelectExprUse IS NULL AND NOT memberSelectModif IS NULL AND varDec=modifIdvarDec))  "
							+ "ANY(x IN COLLECT(NOT stat IS NULL AND stat.position < useStat.position AND "
							+ "NOT ANY(rel IN RELS(modifStatToCommonBlockPath) WHERE type(rel)='FORLOOP_UPDATE' OR type(rel)='IF_ELSE' OR type(rel)='FORLOOP_STATEMENT' OR  type(rel)='FOREACH_STATEMENT' OR type(rel)='TRY_CATCH' OR type(rel)='SWITCH_ENCLOSES_CASES' OR type(rel)='IF_THEN') AND "
							+ "(((useId.nodeType='JCIdent' OR (NOT memberSelectExprUse IS NULL AND memberSelectExprUse.nodeType='JCIdent' AND (memberSelectExprUse.name='this' OR memberSelectExprUse.name='super'))) AND (assignId.nodeType='JCIdent' OR (NOT memberSelectModif IS NULL AND memberSelectModif.nodeType='JCIdent'  AND (memberSelectModif.name='this' OR memberSelectModif.name='super'))) ) OR (NOT memberSelectExprUse IS NULL AND NOT memberSelectModif IS NULL AND varDec=modifIdvarDec))"
							+ ") WHERE x) as assBefore "
							),
					new SimpleWithClause("className,attr,ALL( x IN COLLECT(assBefore) WHERE x) OR useId IS NULL as isSillyAttr "  ),
					new WhereClause("isSillyAttr"),
//					new WhereClause("ALL(x IN areSillyUses WHERE x)")	,	
					//notUseBefore
//						new ReturnClause("className, attr.name as a")
						//						new SimpleWithClause("typeDec, attr, ALL(x IN COLLECT(stat) WHERE x IS NULL) as isSillyAttr"),
//						
					 new ReturnClause("DISTINCT 'Warning [CMU-DCL53] You must minimize the scope of the varaibles. You can minimize the scope of the attribute '+attr.name+' in class '+className + ' by transforming it into a local variable (as everytime its value is used in a method, there is a previous unconditional assignment).' as w ORDER BY w")
						
				
		};
	}

	public static void main(String[] args) {
		System.out.println(new DCL53_MEDIO(false).queryToString());
	}

}

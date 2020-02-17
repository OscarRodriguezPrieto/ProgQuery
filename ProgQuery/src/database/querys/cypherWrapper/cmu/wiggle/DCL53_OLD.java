package database.querys.cypherWrapper.cmu.wiggle;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.Filter;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;

public class DCL53_OLD extends AbstractQuery {
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
	public DCL53_OLD(boolean isProgQuery) {
		super(isProgQuery);
	}
	
	@Override
	protected void initiate() {
		clauses = new Clause[] { new MatchClause(new MatchImpl("(typeDec)-[:DECLARES_FIELD]->(attr) ")),
				new MatchClause(true, getPDGServices().getCompleteIdentification(new NodeVar("id"), "typeDec,attr,")),
				new MatchClause(true, new MatchImpl("(modif{nodeType:'JCAssign'})-[:ASSIGNMENT_LHS]->()-[*0..]->(id)")),
				// new WhereClause("attr=dec"),
				new SimpleWithClause(
						"typeDec,attr,COLLECT( DISTINCT [id,dec,modif]) as identificationsWithOpModif, identss"),

				new UnwindClause("identificationsWithOpModif", "x"),
				new SimpleWithClause("identificationsWithOpModif,x,typeDec,attr,identss"),
				new UnwindClause("identificationsWithOpModif", "y"), new SimpleWithClause("typeDec,attr,y,x,identss"),
				new WhereClause("attr=x[1] AND attr=y[1] AND x[2] IS NULL AND NOT y[2] IS NULL"),

				new SimpleWithClause(
						"typeDec.fullyQualifiedName as className,attr,x[0] as useExpr, y[0] as modifId, y[2] as modif,identss"),
				new MatchClause(getStatementServices().getMethodFromStatement(
						getExpressionServices().getStatementFromExp(new NodeVar("useExpr")), new NodeVar("method"))),
				new WhereClause("method.nodeType='JCMethodDecl'"),
				new SimpleWithClause("className,attr,useExpr,stat as useStat,method, modifId, modif,identss"),
				new MatchClause(getStatementServices().getMethodFromStatement(
						getExpressionServices().getStatementFromExp(new NodeVar("modif")), new NodeVar("method")),
						new MatchImpl("(modif)-[:ASSIGNMENT_LHS]->(lhs_expr)")),
				new WhereClause("stat.position < useStat.position"),

				new SimpleWithClause("attr,useExpr, modifId, modif, lhs_expr, useStat,stat, method,identss,className"),
				new ClauseImpl(" OPTIONAL MATCH (useExpr)-[:MEMBER_SELECT_EXPR]->(memberSelectExprUse)"
						+ " WITH attr,useExpr, modifId, modif, lhs_expr, useStat,stat, method, memberSelectExprUse, identss,className,"
						+ new Filter("identss", "x[0]=memberSelectExprUse").expToString() + "[0][1] as varDec"),
				new MatchClause(true, new MatchImpl("(modifId)-[:MEMBER_SELECT_EXPR]->(memberSelectModif)")),
				new SimpleWithClause(
						"attr,useExpr, modifId, modif, lhs_expr, useStat,stat, method, memberSelectExprUse, varDec,memberSelectModif,className"
								+ ", " + new Filter("identss", "x[0]=memberSelectModif").expToString()
								+ "[0][1] as modifIdvarDec"),
				// new WhereClause(new Filter("identss",
				// "x[0]=memberSelectModif").expToString()+"[0][1]=varDec OR ()"
				// )
				new MatchClause(true,
						getStatementServices().getOuterBlockFromStatement(new NodeVar("useStat"),
								new NodeVar("commonBlock")),
						new Path(getStatementServices().getOuterBlockFromStatement(new NodeVar("stat"),
								new NodeVar("commonBlock")), "modifStatToCommonBlockPath")),
				new SimpleWithClause(
						"className,attr, useExpr, ANY(x IN COLLECT((NOT ANY(rel IN RELS(modifStatToCommonBlockPath) WHERE type(rel)='FORLOOP_UPDATE' OR type(rel)='IF_ELSE' OR type(rel)='FORLOOP_STATEMENT' OR  type(rel)='FOREACH_STATEMENT' OR type(rel)='TRY_CATCH' OR type(rel)='SWITCH_ENCLOSES_CASES' OR type(rel)='IF_THEN') "
								+ " AND ((useExpr.nodeType='JCIdent' OR (NOT memberSelectExprUse IS NULL AND memberSelectExprUse.nodeType='JCIdent' AND (memberSelectExprUse.name='this' OR memberSelectExprUse.name='super')))"
								+ " AND (modifId.nodeType='JCIdent' OR (NOT memberSelectModif IS NULL AND memberSelectModif.nodeType='JCIdent'  AND (memberSelectModif.name='this' OR memberSelectModif.name='super'))) ) OR "
								+ // tenemos la restriccion condicional y
									// (posibilidad de attr o this.super.attr o
									// posibilidad de z.attr z.attr)
								"(NOT memberSelectExprUse IS NULL AND NOT memberSelectModif IS NULL AND varDec=modifIdvarDec))) WHERE x) as useWithModif "

				),
				new ClauseImpl(
						"WITH className,attr, ALL( x IN COLLECT(useWithModif) WHERE x) OR useExpr IS NULL as isSillyAttr WHERE isSillyAttr "),
				new ReturnClause(
						"'Warning [CMU-DCL53] You must minimize the scope of the varaibles. You can minimize the scope of the attribute '+attr.name+' in class '+className + ' by transforming it into a local variable (as everytime its value is used in a method, there is a previous unconditional assignment).'")

		};
	}

	public static void main(String[] args) {
		System.out.println(new DCL53_OLD(false).queryToString());
	}

}

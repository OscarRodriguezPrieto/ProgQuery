package database.querys.services;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.AnonymousNode;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.MatchElements;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.OrderByClause;
import database.querys.cypherWrapper.Path;
import database.querys.cypherWrapper.PathEnd;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.cypherWrapper.WithClause;
import database.relations.RelationTypes;
import database.relations.RelationTypesWiggle;
import database.relations.TypeRelations;
import utils.dataTransferClasses.Pair;

public class PDGServicesWiggle extends PDGServicesProgQueryImpl {
	@Override
	public MatchElement getAssingnmentsAndVarDeclarations(MatchElement assignment) {

		return new PathEnd(
				StatementServices.WIGGLE
						.getVarDecsInStatement(StatementServices.WIGGLE
								.getVarsAndParamsScopesInStatement(ExpressionServices.WIGGLE.getStatementFromExp(
										new MatchElements(assignment,
												new RelationshipImpl(Node.nodeForWiggle("id", NodeTypes.IDENTIFIER),
														assignment.getLastNode(),
														new EdgeImpl(EdgeDirection.OUTGOING,
																RelationTypes.ASSIGNMENT_LHS))),
										new NodeVar("assignStat")), new NodeVar("scope")),
								"varDec"),
				new WhereClause("varDec.position<assignStat.position AND varDec.name=id.name"));
	}

	@Override
	public MatchElement getIdsAndVarDeclarations(MatchElement id) {
		return getIdsAndVarDeclarations(id, "");
	}

	@Override
	public MatchElement getIdsAndVarDeclarations(MatchElement id, String varsToPreserve) {
		String idName = id.getLastNode().getName();
		NodeVar scope = new NodeVar("scope");
		return new PathEnd(new MatchElements(ExpressionServices.WIGGLE.getStatementFromExp(id),
				StatementServices.WIGGLE.getVarsAndParamsScopesInStatement(new Path(new NodeVar("stat"), "p"), scope),

				StatementServices.WIGGLE.getVarDecsInStatement(scope, "varDec")),
				new WhereClause("varDec.position<stat.position AND varDec.name=" + idName + ".name"),
				new SimpleWithClause(varsToPreserve + (varsToPreserve.length() > 0 ? "," : "") + idName, "varDec, p"),
				new OrderByClause("-LENGTH(p)"),
				new WithClause(new String[] { varsToPreserve + (varsToPreserve.length() > 0 ? "," : "") + idName },
						Pair.create("varDec", new ExprImpl("COLLECT(varDec)[0]"))));
	}

	@Override
	public MatchElement getIdsAndVarDeclarations(MatchElement id, MatchElement varDec, String varsToPreserve) {
		String idName = id.getLastNode().getName();
		String varDecname = varDec.getLastNode().getName();
		NodeVar scope = new NodeVar("scope");
		return new PathEnd(new MatchElements(ExpressionServices.WIGGLE.getStatementFromExp(id),
				StatementServices.WIGGLE.getVarsAndParamsScopesInStatement(new Path(new NodeVar("stat"), "p"), scope),

				StatementServices.WIGGLE.getVarDecsInStatement(scope, varDecname)),
				new WhereClause(varDecname + ".position<stat.position AND " + varDecname + ".name=" + idName + ".name"),
				new SimpleWithClause(varsToPreserve, idName, varDecname, " p"), new OrderByClause("-LENGTH(p)"),
				new WithClause(new String[] { varsToPreserve, idName },
						Pair.create(varDecname, new ExprImpl("COLLECT(" + varDecname + ")[0]"))));
	}

	// @Override
	// public MatchElement getCompleteIdentification(MatchElement id, String
	// varsToPreserve) {
	// String idName = id.getLastNode().getName();
	// NodeVar scope = new NodeVar("scope");
	// return new PathEnd(new
	// MatchElements(ExpressionServices.WIGGLE.getStatementFromExp(id),
	// StatementServices.WIGGLE
	// .getVarsAttrsAndParamsScopesInStatement(new Path(new NodeVar("stat"),
	// "p"), scope),
	//
	// StatementServices.WIGGLE.getAttrAndVarDecsInStatement(scope)),
	// new WhereClause(
	// idName + ".nodeType='JCIdent' AND (varDec.position<stat.position OR
	// TYPE(scopeToVar)='DECLARES_FIELD' ) AND varDec.name="
	// + idName + ".name"),
	// new SimpleWithClause(varsToPreserve + idName, "varDec, p"), new
	// OrderByClause("-LENGTH(p)"),
	// new SimpleWithClause(varsToPreserve + idName,
	// new ExprImpl("COLLECT(varDec)[0]").expToString() + " as varDec "),
	// new SimpleWithClause(varsToPreserve + "COLLECT([" + idName + ",varDec])
	// as idents"),
	//
	// new MatchClause(true, new MatchImpl(
	// "(exp{nodeType:'JCFieldAccess'})-[:MEMBER_SELECT_EXPR]->(memberSelExpr),
	// p=(typeEnclosingField{fullyQualifiedName:memberSelExpr.actualType})-[:IS_SUBTYPE_OF
	// | :IS_SUBTYPE_EXTENDS*0..]->(superType),
	// (superType)-[:DECLARES_FIELD]->(fieldDec{name:exp.name})")),
	// new SimpleWithClause(varsToPreserve + "exp,idents, fieldDec ORDER BY
	// -SIZE(NODES(p))"),
	// new SimpleWithClause(varsToPreserve + "idents+COLLECT([exp,fieldDec]) as
	// idents "),
	// new UnwindClause("idents", "pair"),
	// new SimpleWithClause(varsToPreserve + "pair[0] as id, pair[1] as dec")
	//
	// // new WhereClause("fieldDec.name=exp.")
	//
	// );
	// }
	@Override
	public MatchElement getCompleteIdentificationFromVar(String varsToPreserve) {
//		String varName = varDec.getLastNode().getName();
		NodeVar scope = new NodeVar("scope");
		return new PathEnd(new MatchElements( ExpressionServices.WIGGLE.getStatementFromExp(new NodeVar("id")),StatementServices.WIGGLE
				.getVarsAttrsAndParamsScopesInStatement(new Path(new NodeVar("stat"), "p"), scope),

				StatementServices.WIGGLE.getAttrAndVarDecsInStatement(scope)),
//				new MatchClause(true,ExpressionServices.WIGGLE.getStatementFromExp(new NodeVar("id"))),
				new WhereClause(
						 "id.nodeType='JCIdent' AND (varDec.position<stat.position OR TYPE(scopeToVar)='DECLARES_FIELD' ) AND varDec.name=id.name"),
				new SimpleWithClause(varsToPreserve+"id" , " p, varDec"), new OrderByClause("CASE WHEN TYPE(scopeToVar)='DECLARES_FIELD' THEN 9999 ELSE LENGTH(p) END")
				,new SimpleWithClause(varsToPreserve + "id",
						new ExprImpl("COLLECT(varDec)[0]").expToString() + " as varDec")
//				,new SimpleWithClause(true,varsToPreserve + "CASE WHEN varDec="+varName+" THEN id ELSE NULL END as id, varDec,"+varName)
//				,new WhereClause("varDec="+varName+" OR id IS NULL "),
				,new SimpleWithClause(varsToPreserve + "varDec, COLLECT(id)  as ids")
//
				,new MatchClause(true, new MatchImpl(
						"(exp{nodeType:'JCFieldAccess'})-[:MEMBER_SELECT_EXPR]->(memberSelExpr), p=(typeEnclosingField)-[:IS_SUBTYPE_OF | :IS_SUBTYPE_EXTENDS*0..]->(superType), (superType)-[:DECLARES_FIELD]->(fieldDec{name:exp.name})")),

				new WhereClause(
						"varDec=fieldDec  AND SPLIT(typeEnclosingField.fullyQualifiedName, '<')[0]=SPLIT(memberSelExpr.actualType, '<')[0]"),
				
				new SimpleWithClause(varsToPreserve
						+ "varDec,ids+COLLECT(exp) as ids"
//						+ ", idents+COLLECT([exp,fieldDec]) as auxIdents"
						)
				/*,new SimpleWithClause(varsToPreserve + "COLLECT(["+varName+", ids]) as identss"
//								+ "ORDER BY -SIZE(NODES(p))"
								),new SimpleWithClause(varsToPreserve + "identss, identss as auxIdents"
//										+ "ORDER BY -SIZE(NODES(p))"
										)
				,new UnwindClause("auxIdents", "pair"),
				
				new SimpleWithClause(varsToPreserve +" pair[0] as "+varName+", pair[1] as ids, identss")
*/  
				
				//				new WhereClause(varName+"=pair[1]")
		// new WhereClause("fieldDec.name=exp.")

		);
	}
	
	@Override
	public MatchElement getCompleteIdentificationFromVar(MatchElement varDec, String varsToPreserve) {
		String varName = varDec.getLastNode().getName();
		NodeVar scope = new NodeVar("scope");
		return new PathEnd(new MatchElements( ExpressionServices.WIGGLE.getStatementFromExp(new NodeVar("id")),StatementServices.WIGGLE
				.getVarsAttrsAndParamsScopesInStatement(new Path(new NodeVar("stat"), "p"), scope),

				StatementServices.WIGGLE.getAttrAndVarDecsInStatement(scope)),
//				new MatchClause(true,ExpressionServices.WIGGLE.getStatementFromExp(new NodeVar("id"))),
				new WhereClause(
						 "id.nodeType='JCIdent' AND (varDec.position<stat.position OR TYPE(scopeToVar)='DECLARES_FIELD' ) AND varDec.name="+varName
								+ ".name=id.name"),
				new SimpleWithClause(varsToPreserve+"id" , varName+", p, varDec"), new OrderByClause("CASE WHEN TYPE(scopeToVar)='DECLARES_FIELD' THEN 9999 ELSE LENGTH(p) END")
				,new SimpleWithClause(varsToPreserve + "id",
						new ExprImpl("COLLECT(varDec)[0]").expToString() + " as varDec,"+varName)
				,new SimpleWithClause(true,varsToPreserve + "CASE WHEN varDec="+varName+" THEN id ELSE NULL END as id, varDec,"+varName)
//				,new WhereClause("varDec="+varName+" OR id IS NULL "),
				,new SimpleWithClause(varsToPreserve + varName+",FILTER(x IN COLLECT(id) WHERE NOT x IS NULL) as ids")
//
				,new MatchClause(true, new MatchImpl(
						"(exp{nodeType:'JCFieldAccess'})-[:MEMBER_SELECT_EXPR]->(memberSelExpr), p=(typeEnclosingField)-[:IS_SUBTYPE_OF | :IS_SUBTYPE_EXTENDS*0..]->(superType), (superType)-[:DECLARES_FIELD]->(fieldDec{name:exp.name})")),

				new WhereClause(
						varName+"=fieldDec  AND SPLIT(typeEnclosingField.fullyQualifiedName, '<')[0]=SPLIT(memberSelExpr.actualType, '<')[0]"),
				
				new SimpleWithClause(varsToPreserve
						+ "ids+COLLECT(exp) as ids,"+varName
//						+ ", idents+COLLECT([exp,fieldDec]) as auxIdents"
						)
				/*,new SimpleWithClause(varsToPreserve + "COLLECT(["+varName+", ids]) as identss"
//								+ "ORDER BY -SIZE(NODES(p))"
								),new SimpleWithClause(varsToPreserve + "identss, identss as auxIdents"
//										+ "ORDER BY -SIZE(NODES(p))"
										)
				,new UnwindClause("auxIdents", "pair"),
				
				new SimpleWithClause(varsToPreserve +" pair[0] as "+varName+", pair[1] as ids, identss")
*/  
				
				//				new WhereClause(varName+"=pair[1]")
		// new WhereClause("fieldDec.name=exp.")

		);
	}
	@Override
	public MatchElement getCompleteIdentification(MatchElement id, String varsToPreserve) {
		String idName = id.getLastNode().getName();
		NodeVar scope = new NodeVar("scope");
		return new PathEnd(new MatchElements(ExpressionServices.WIGGLE.getStatementFromExp(id), StatementServices.WIGGLE
				.getVarsAttrsAndParamsScopesInStatement(new Path(new NodeVar("stat"), "p"), scope),

				StatementServices.WIGGLE.getAttrAndVarDecsInStatement(scope)),
				new WhereClause(
						idName + ".nodeType='JCIdent' AND (varDec.position<stat.position OR TYPE(scopeToVar)='DECLARES_FIELD' ) AND varDec.name="
								+ idName + ".name"),
				new SimpleWithClause(varsToPreserve + idName, "varDec, p"), new OrderByClause("LENGTH(p)")
				,new SimpleWithClause(varsToPreserve + idName,
						new ExprImpl("COLLECT(varDec)[0]").expToString() + " as varDec "),
				new SimpleWithClause(varsToPreserve + "COLLECT([" + idName + ",varDec]) as idents"),

				new MatchClause(true, new MatchImpl(
						"(exp{nodeType:'JCFieldAccess'})-[:MEMBER_SELECT_EXPR]->(memberSelExpr), p=(typeEnclosingField)-[:IS_SUBTYPE_OF | :IS_SUBTYPE_EXTENDS*0..]->(superType), (superType)-[:DECLARES_FIELD]->(fieldDec{name:exp.name})")),

				new WhereClause(
						"SPLIT(typeEnclosingField.fullyQualifiedName, '<')[0]=SPLIT(memberSelExpr.actualType, '<')[0]"),
				new SimpleWithClause(varsToPreserve + "exp,idents, fieldDec ORDER BY -SIZE(NODES(p))"),
				new SimpleWithClause(varsToPreserve
						+ "idents+COLLECT([exp,fieldDec]) as identss, idents+COLLECT([exp,fieldDec]) as auxIdents"),
				new UnwindClause("auxIdents", "pair"),
				
				new SimpleWithClause(varsToPreserve + " pair[1] as dec, COLLECT(pair[0]) as ids, identss")

		// new WhereClause("fieldDec.name=exp.")

		);
	}

	@Override
	public Clause getModificationsOnFields(String fieldSetName, String varsToPreserve) {
		return new MatchClause(true,
				new Path(
						AssignmentServices.WIGGLE
								.getMemberSelectionsLeftSide(Node.nodeForWiggle("assignment", NodeTypes.ASSIGNMENT)),
						Pair.create(new EdgeImpl(RelationTypesWiggle.MEMBER_SELECT_EXPR), new NodeVar("memberExpr")),
						Pair.create(new EdgeImpl(TypeRelations.ITS_TYPE_IS), new AnonymousNode()),
						Pair.create(new EdgeImpl(RelationTypesWiggle.DECLARES_FIELD), new NodeVar("field"))),
				ExpressionServices.WIGGLE.getMethodFromExp(new NodeVar("assignment"),
						Node.nodeForWiggle("enclosingMethod", NodeTypes.METHOD_DEF)))
								.append(new WhereClause(" field IN " + fieldSetName
										+ " AND field.name=memberSelection.name AND NOT(memberExpr.nodeType='JCIdent' AND memberExpr.name='this' AND enclosingMethod.name='<init>')"))
								.append(new SimpleWithClause(varsToPreserve,
										"COLLECT(DISTINCT assignment) as assignsMods"))

								.append(new MatchClause(true,
										AssignmentServices.WIGGLE
												.getLeftMostId(Node.nodeForWiggle("assignment", NodeTypes.ASSIGNMENT)),
										new Path(
												ExpressionServices.WIGGLE.getMethodFromExp(new NodeVar("assignment"),
														Node.nodeForWiggle("enclosingMethod", NodeTypes.METHOD_DEF)),
												Pair.create(
														new EdgeImpl(EdgeDirection.OUTGOING,
																RelationTypesWiggle.DECLARES_METHOD),
														new AnonymousNode()),
												Pair.create(new EdgeImpl(RelationTypesWiggle.DECLARES_FIELD),
														new NodeVar("field"))))
																.append(new WhereClause(
																		"leftMostId.name=field.name AND enclosingMethod.name<>'<init>' AND field IN "
																				+ fieldSetName).append(

																						new MatchClause(true, new Path(
																								PDGServicesWiggle.WIGGLE
																										.getIdsAndVarDeclarations(
																												new NodeVar(
																														"leftMostId"),
																												varsToPreserve
																														+ ",assignsMods, assignment")

																						)).append(new WhereClause(" varDec IS NULL ")))))

		;
	}

}
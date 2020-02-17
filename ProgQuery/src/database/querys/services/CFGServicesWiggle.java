package database.querys.services;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.AnonymousNode;
import database.querys.cypherWrapper.Any;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.Element;
import database.querys.cypherWrapper.Extract;
import database.querys.cypherWrapper.Filter;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.MultipleClauses;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.querys.cypherWrapper.Reduce;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.WhereClause;
import database.relations.RelationTypesWiggle;
import utils.dataTransferClasses.Pair;

public class CFGServicesWiggle implements CFGServices {

	@Override
	public Element getCFGSuccesorsOf(MatchElement stat) {
		throw new IllegalStateException();
	}

	public Clause OLDgetCFGSuccesorsOf(MatchElement stat, String namesToPreserveInWith) {
		String statNodeName = stat.getLastNode().getName();
		return new MultipleClauses(
				new MatchClause(new Path(
						StatementServices.WIGGLE.getOuterBlockFromStatement(stat, new NodeVar("block")),
						Pair.create(new EdgeImpl(EdgeDirection.OUTGOING, RelationTypesWiggle.HAS_METHODDECL_BODY),
								database.querys.cypherWrapper.Node.nodeForWiggle(NodeTypes.METHOD_DEF)))),
				new MatchClause(true, StatementServices.WIGGLE.getStatementsInStatement(new NodeVar("block"))),
				getUnreachebleStats("if", statNodeName, RelationTypesWiggle.IF_ELSE, RelationTypesWiggle.IF_THEN),

				getUnreachebleStats("switch", new NodeVar("case"), new NodeVar("otherCase"),
						stat.getLastNode()
								.getName(),
						RelationTypesWiggle.SWITCH_ENCLOSES_CASES),
				new MatchClause(true,
						StatementServices.WIGGLE.getUnconditionalStatementsInStatement(
								new RelationshipImpl(new NodeVar("switch"), new NodeVar("c"),
										new EdgeImpl(RelationTypesWiggle.SWITCH_ENCLOSES_CASES)),
								Node.nodeForWiggle(NodeTypes.BREAK_STATEMENT)))
				// ,
				// new SimpleWithClause(namesToPreserveInWith, "ifRel",
				// "switch", "if", "case",
				// "otherCase IS NULL OR NOT "
				// + new Any("COLLECT(DISTINCT c)",
				// " x.position>=case.position AND x.position <
				// otherCase.position").expToString()
				// + " as isSucc, innerStatement,otherifRel,otherCase")
				,

				getUnreachebleStats("for",
						new WhereClause(WhereClause.isElementOfWiggleType("for", NodeTypes.FOR_LOOP,
								NodeTypes.WHILE_LOOP, NodeTypes.FOR_EACH_LOOP, NodeTypes.DO_WHILE_LOOP)),
						statNodeName, RelationTypesWiggle.FORLOOP_STATEMENT, RelationTypesWiggle.FORLOOP_UPDATE,
						RelationTypesWiggle.ENCLOSES, RelationTypesWiggle.FOREACH_STATEMENT),
				getUnreachebleStats("catch", statNodeName, RelationTypesWiggle.TRY_CATCH)

				// new WithClause(new String[] {
				// // "assign", "stat", "enclClass", "varDec",
				// // "identifications",
				// namesToPreserveInWith,
				// "innerStatement", "ifRel", "switch", "if", "otherifRel",
				// "otherCase", "for", "forRel",
				// "otherforRel", "catch", "catchRel", "othercatchRel", "case"
				// },
				// Pair.create("middleCases", new ExprImpl("COLLECT(c)")))
				,
				new SimpleWithClause(
						// "assign", "stat", "enclClass", "varDec",
						// "identifications",
						namesToPreserveInWith + statNodeName,
						new Filter("COLLECT([innerStatement,otherifRel,otherCase, otherforRel,othercatchRel])",
								"(innerInfo[0].position > " + statNodeName
										+ ".position OR innerInfo[0]=for OR NOT innerInfo[3] IS NULL )"
										+ "AND (innerInfo[1] IS NULL OR innerInfo[1]=ifRel)"
										+ " AND (innerInfo[4] IS NULL OR innerInfo[4]=catchRel) "
								// + "AND"+
								// " isSucc"
								// + " AND (innerInfo[2] IS NULL OR NOT "
								// + new Any("middleCases",
								// " x.position>=case.position AND x.position <
								// innerInfo[2].position")
								// .expToString()
								// + " )"
								, "innerInfo").expToString() + " as innerInfos",
						"ifRel", "switch", "if", "for", "forRel", "catch", "catchRel", "case",
						"COLLECT(DISTINCT c) as middleCases"),
				new SimpleWithClause(namesToPreserveInWith + statNodeName + ", ifRel, switch", "if", "catch",
						"catchRel", "case",
						"COLLECT("
								+ new Extract(new Filter("innerInfos",
										" innerInfo[2] IS NULL " + "OR  NOT "
												+ new Any(" middleCases ",
														" x.position>=case.position AND x.position <innerInfo[2].position")
																.expToString(),
										"innerInfo"), "x[0]").expToString()
								+ ") as inners "

				// + "ORDER BY assign"
				),

				new SimpleWithClause(namesToPreserveInWith + statNodeName + ", "
						+ new Reduce("inners", "l+" + new Filter("innersPerEl", "NOT x IN l").expToString(),
								"innersPerEl", "l=[]").expToString()
						+ "as inners "),
				new SimpleWithClause(namesToPreserveInWith + statNodeName + ", inners as succesors"
				// + new Reduce("inners", new Filter("innersPerEl", "x IN l"),
				// "innersPerEl", "l=inners[0]")
				// .expToString()
				// + " as succesors ")

				)

		);
		// return StatementServices.WIGGLE.getMethodFromStatement(stat);

	}

	@Override
	public Clause getCFGSuccesorsOf(MatchElement stat, String namesToPreserveInWith) {
		String statNodeName = stat.getLastNode().getName();
		return new MultipleClauses(
				new MatchClause(new Path(
						StatementServices.WIGGLE.getMethodFromStatement(stat, new NodeVar("method{nodeType:'JCMethodDecl'}")))),
						new MatchClause(new Path(new NodeVar("method"),
						Pair.create(new EdgeImpl(EdgeDirection.INCOMING, RelationTypesWiggle.HAS_METHODDECL_BODY),
								new NodeVar("block")))),
				new MatchClause(true, StatementServices.WIGGLE.getStatementsInStatement(new NodeVar("block"))),
				new ClauseImpl("OPTIONAL MATCH (" + statNodeName
						+ ")<-[:CASE_STATEMENTS|CATCH_ENCLOSES_BLOCK|CATCH_PARAM|ENCLOSES|FOREACH_STATEMENT|FOREACH_VAR|FORLOOP_INIT|FORLOOP_STATEMENT|FORLOOP_UPDATE|HAS_METHODDECL_PARAMETERS|IF_THEN|IF_ELSE|LABELED_STATEMENT|SWITCH_ENCLOSES_CASES|SYNCHRONIZED_BLOCK|TRY_BLOCK|TRY_CATCH|TRY_FINALLY|TRY_RESOURCES*0..]-()<-[:FORLOOP_STATEMENT|FORLOOP_UPDATE|ENCLOSES|FOREACH_STATEMENT*0..]-(for)"
						+ " WHERE for.nodeType='JCForLoop' OR for.nodeType='JCWhileLoop' OR for.nodeType='JCEnhancedForLoop' OR for.nodeType='JCDoWhileLoop'"
						+

						"OPTIONAL MATCH p=(innerStatement)<-[:CASE_STATEMENTS|CATCH_ENCLOSES_BLOCK|CATCH_PARAM|ENCLOSES|FOREACH_STATEMENT|FOREACH_VAR|FORLOOP_INIT|FORLOOP_STATEMENT|FORLOOP_UPDATE|HAS_METHODDECL_PARAMETERS|IF_THEN|IF_ELSE|LABELED_STATEMENT|SWITCH_ENCLOSES_CASES|SYNCHRONIZED_BLOCK|TRY_BLOCK|TRY_CATCH|TRY_FINALLY|TRY_RESOURCES*0..]-()<-[:FORLOOP_STATEMENT|FORLOOP_UPDATE|ENCLOSES|FOREACH_STATEMENT*0..]-(for)")

				,
				new SimpleWithClause(
						"DISTINCT " + namesToPreserveInWith + statNodeName + " , innerStatement, for, p  "),
				new SimpleWithClause(" DISTINCT "+ namesToPreserveInWith + statNodeName + 
						 ", CASE WHEN "+statNodeName+".position < innerStatement.position OR (NOT for IS NULL AND NOT p IS NULL)"
						 		+ " THEN innerStatement ELSE NULL END as innerStatement"),

//				new SimpleWithClause("DISTINCT " + namesToPreserveInWith + statNodeName + ", innerStatement "),

				getUnreachebleStats("if", statNodeName, RelationTypesWiggle.IF_ELSE, RelationTypesWiggle.IF_THEN),
				new SimpleWithClause(
						"DISTINCT " + namesToPreserveInWith + statNodeName + ",  innerStatement, ifRel, otherifRel  "),

				new SimpleWithClause(" DISTINCT "+namesToPreserveInWith + statNodeName+", CASE WHEN ifRel IS NULL OR otherifRel IS NULL OR ifRel=otherifRel THEN innerStatement ELSE NULL END as innerStatement"),
//				new SimpleWithClause("DISTINCT " + namesToPreserveInWith + statNodeName + ", innerStatement "),
				getUnreachebleStats("catch", statNodeName, RelationTypesWiggle.TRY_CATCH),
				new SimpleWithClause("DISTINCT " + namesToPreserveInWith + statNodeName
						+ " , innerStatement, catchRel, othercatchRel  "),

				new SimpleWithClause("DISTINCT "+namesToPreserveInWith + statNodeName+", CASE WHEN catchRel IS NULL OR othercatchRel IS NULL OR catchRel=othercatchRel THEN innerStatement ELSE NULL END as innerStatement"),
//				new SimpleWithClause("DISTINCT " + namesToPreserveInWith + statNodeName + " , innerStatement"),
				new ClauseImpl("OPTIONAL MATCH (" + statNodeName
						+ ")<-[:CASE_STATEMENTS|CATCH_ENCLOSES_BLOCK|CATCH_PARAM|ENCLOSES|FOREACH_STATEMENT|FOREACH_VAR|FORLOOP_INIT|FORLOOP_STATEMENT|FORLOOP_UPDATE|HAS_METHODDECL_PARAMETERS|IF_THEN|IF_ELSE|LABELED_STATEMENT|SWITCH_ENCLOSES_CASES|SYNCHRONIZED_BLOCK|TRY_BLOCK|TRY_CATCH|TRY_FINALLY|TRY_RESOURCES*0..]-(case)<-[:SWITCH_ENCLOSES_CASES]-(switch)\n"
						+ "OPTIONAL MATCH (innerStatement)<-[:CASE_STATEMENTS|CATCH_ENCLOSES_BLOCK|CATCH_PARAM|ENCLOSES|FOREACH_STATEMENT|FOREACH_VAR|FORLOOP_INIT|FORLOOP_STATEMENT|FORLOOP_UPDATE|HAS_METHODDECL_PARAMETERS|IF_THEN|IF_ELSE|LABELED_STATEMENT|SWITCH_ENCLOSES_CASES|SYNCHRONIZED_BLOCK|TRY_BLOCK|TRY_CATCH|TRY_FINALLY|TRY_RESOURCES*0..]-(otherCase)<-[:SWITCH_ENCLOSES_CASES]-(switch)\n"
						+ "OPTIONAL MATCH (switch)-[:SWITCH_ENCLOSES_CASES]->(c)-[:CASE_STATEMENTS|ENCLOSES|FORLOOP_INIT|HAS_METHODDECL_PARAMETERS|HAS_VARIABLEDECL_INIT|LABELED_STATEMENT|SYNCHRONIZED_BLOCK|TRY_BLOCK|TRY_FINALLY|TRY_RESOURCES*]->({nodeType:'JCBreak'})\r\n"),
				new WhereClause("c.position>=case.position AND c.position <otherCase.position"),
				new SimpleWithClause(namesToPreserveInWith + statNodeName
						+ " , innerStatement, COUNT(c) as middlecasesWithUncondBreak, case, otherCase"),
				new SimpleWithClause("DISTINCT "+namesToPreserveInWith + statNodeName+", CASE WHEN case IS NULL OR otherCase IS NULL OR otherCase=case OR middlecasesWithUncondBreak=0 THEN innerStatement ELSE NULL END as innerStatement"),
				new SimpleWithClause("DISTINCT " + namesToPreserveInWith + statNodeName
						+ ", COLLECT( DISTINCT innerStatement) as succesors"));
		// return StatementServices.WIGGLE.getMethodFromStatement(stat);

	}

	private Clause getUnreachebleStats(String statementName, WhereClause whereInThemiddle, MatchElement block1,
			MatchElement block2, String statNodeName, RelationTypesWiggle... conditionalRelations) {

		Clause firtClause = new MatchClause(true,
				StatementServices.WIGGLE.getOuterBlockFromStatement(new NodeVar(statNodeName), block1)
						.append(Pair.create(new EdgeImpl(statementName + "Rel", conditionalRelations).changeDirection(),
								new NodeVar(statementName))));

		firtClause = whereInThemiddle == null ? firtClause : new MultipleClauses(firtClause, whereInThemiddle);
		return new MultipleClauses(firtClause, new MatchClause(true,
				StatementServices.WIGGLE.getOuterBlockFromStatement(new NodeVar("innerStatement"), block2)
						.append(Pair.create(
								new EdgeImpl("other" + statementName + "Rel", conditionalRelations).changeDirection(),
								new NodeVar(statementName)))));
	}

	private Clause getUnreachebleStats(String statementName, MatchElement block1, MatchElement block2,
			String statNodeName, RelationTypesWiggle... conditionalRelations) {

		return getUnreachebleStats(statementName, null, block1, block2, statNodeName, conditionalRelations);
	}

	private Clause getUnreachebleStats(String statementName, String statNodeName,
			RelationTypesWiggle... conditionalRelations) {
		return getUnreachebleStats(statementName, null, new AnonymousNode(), new AnonymousNode(), statNodeName,
				conditionalRelations);
	}

	private Clause getUnreachebleStats(String statementName, WhereClause whereInThemiddle, String statNodeName,
			RelationTypesWiggle... conditionalRelations) {
		return getUnreachebleStats(statementName, whereInThemiddle, new AnonymousNode(), new AnonymousNode(),
				statNodeName, conditionalRelations);
	}

	@Override
	public MatchElement getCFGSuccesorsAndItSelfOf(MatchElement p) {
		throw new IllegalStateException();
	}

}

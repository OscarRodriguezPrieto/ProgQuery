package database.querys.cypherWrapper.cmu.wiggle;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.Expression;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.services.FieldServicesWiggle;
import database.relations.RelationTypesWiggle;

public class OBJ50_ERROR_TEST extends AbstractQuery {

	/*
	 * MATCH
	 * (declaringType{accessLevel:'public'})-[:DECLARES_FIELD]->(attr{typeKind:'
	 * DECLARED'})-[:ITS_TYPE_IS]->(typeDec) WHERE attr.isFinal AND
	 * attr.accessLevel='public' WITH attr, declaringType, typeDec
	 * 
	 * MATCH
	 * p=(typeDec)-[:DECLARES_FIELD|ITS_TYPE_IS|IS_SUBTYPE_EXTENDS*]->(next{
	 * typeKind:'DECLARED'}) WITH attr, declaringType, typeDec, NODES(p) as
	 * nodes
	 * 
	 * CREATE (res{res:true}) FOREACH (i IN RANGE(1,SIZE(nodes),1) | SET res.res
	 * = res.res AND (CASE WHEN nodes[i]:ATTR_DEC THEN
	 * NOT(nodes[i-1].accessLevel='public' AND NOT nodes[i].isFinal AND NOT
	 * nodes[i].isStatic AND ( nodes[i].accessLevel='public' OR
	 * (nodes[i].accessLevel='protected' AND NOT nodes[i-1].isFinal) )) ELSE
	 * TRUE END)) WITH attr, declaringType, typeDec, nodes, res ------> TO DO
	 * 
	 * 
	 * 
	 * OPTIONAL MATCH
	 * (field)-[mutationRel:STATE_MODIFIED_BY|STATE_MAY_BE_MODIFIED|MODIFIED_BY]
	 * ->(ass) WHERE field IN nodes AND NOT ass:INITIALIZATION WITH attr,
	 * declaringType, res, typeDec, COUNT(mutationRel)>0 as hasAnyMutationRel
	 * 
	 * WITH attr, declaringType, typeDec, ANY( x IN COLLECT( NOT res.res OR
	 * hasAnyMutationRel) WHERE x ) as isMutable WHERE isMutable RETURN 'Warning
	 * [CMU-OBJ50] Attribute '+ attr.name+' declared in
	 * '+declaringType.fullyQualifiedName+ ' is not actually final, only the
	 * reference. This is due to the type '+ typeDec.fullyQualifiedName+' is
	 * mutable ( the state of the attributes change in the program or may
	 * potentially be changed by a client).'
	 * 
	 * 
	 */
	private static final Expression ATTR_PROPERTIES = new ExprImpl(
			"declaringTypeModfiers.flags CONTAINS 'public' AND attrModifiers.flags CONTAINS 'public' AND  attrModifiers.flags CONTAINS 'final' AND attrModifiers.position>-1");
	private static final Clause[] PUBLIC_TYPES_AND_PUBLIC_FINAL_FIELDS = new WhereClause(ATTR_PROPERTIES)
			.addToClauses(FieldServicesWiggle.typesAndDeclaredFieldsPlusModifiersAndTypes(
					new RelationshipImpl(new NodeVar("declaringTypeModfiers"), new CompleteNode("declaringType"),
							new EdgeImpl(EdgeDirection.OUTGOING, RelationTypesWiggle.HAS_CLASS_MODIFIERS))));

	public OBJ50_ERROR_TEST() {
		super(false);
	}

	@Override
	protected void initiate() {
		// TO_ DO ADD DELETE RELS
		clauses = new Clause[] {
/*new ClauseImpl(
		new WiggleCallGraph().queryToString()+" WITH DISTINCT '' as aux "+
				" MATCH (m{nodeType:'JCMethodDecl'})<-[:CALLS|:HAS_DEC*0..]-(anyCaller)<-[:DECLARES_METHOD]-(encClass)-[:HAS_CLASS_MODIFIERS]->(classMod),(anyCaller)-[:HAS_METHODDECL_MODIFIERS]->(methodMod)  "),
new SimpleWithClause("m, NOT m.name='<init>' AND ANY ( caller IN COLLECT(anyCaller) WHERE NOT caller.name='<init>' AND ( methodMod.flags CONTAINS 'public' OR methodMod.flags CONTAINS 'protected' AND NOT classMod.flags CONTAINS 'final')) as notInit")
,new WhereClause("notInit")
,new SimpleWithClause("COLLECT(m) as notInitMethods"),
	
//				 
				 new MatchClause(true, getPDGServices().getCompleteIdentification(new NodeVar("id"), "notInitMethods,")),
	new MatchClause(false,"(id{nodeType:'JCIdent'}) "),
	new MatchClause(true,"(id)<-[:METHODINVOCATION_METHOD_SELECT]-()-[:HAS_DEC]->()-[:HAS_METHODDECL_MODIFIERS]->(decMod)"),
	new WhereClause("NOT decMod.flags CONTAINS 'static'"),
	new SimpleWithClause("id, dec, ids,notInitMethods,identss, ANY(decM IN COLLECT(decMod) WHERE NOT decM IS NULL) as isInstanceCallId"),
	new MatchClause(true,"(id)<-[:MEMBER_SELECT_EXPR]-(fieldAccess) "),
	new WhereClause("fieldAccess.name IN ['this' , 'super']"),
	new SimpleWithClause("id,isInstanceCallId, dec, ids,fieldAccess, notInitMethods,identss"),
	new WhereClause("id.name IN ['this','super'] OR id IN ids OR isInstanceCallId OR NOT fieldAccess IS NULL"),

	new SimpleWithClause("DISTINCT id, CASE WHEN id IN ids THEN dec ElSE NULL END as dec, ids, notInitMethods, identss"),

	new MatchClause(true,"(fieldMod)<-[:HAS_VARIABLEDECL_MODIFIERS]-(dec)<-[:DECLARES_FIELD]-(encC)"),
new MatchClause(true,"(dec)<-[:HAS_METHODDECL_PARAMETERS]-(method)"),
new MatchClause(true,"(p)<-[:HAS_METHODDECL_PARAMETERS]-(method)"),

new SimpleWithClause("notInitMethods,identss,id,fieldMod, p,dec,encC ORDER BY ID(p)"),

new SimpleWithClause("notInitMethods,identss,id, dec,encC,fieldMod, COLLECT(p) as params"),
new SimpleWithClause("notInitMethods,identss,dec,encC,fieldMod,id,  FILTER(i IN RANGE(0,SIZE(params)-1) WHERE dec=params[i])[0]+1 as pIndex")

,new SimpleWithClause("notInitMethods,identss,dec,encC,id, CASE WHEN pIndex IS NULL THEN CASE WHEN dec IS NULL THEN 0 ELSE CASE WHEN encC IS NULL THEN -1 ELSE CASE WHEN fieldMod.flags CONTAINS 'static' THEN -1 ELSE 0 END END END ELSE pIndex END as pIndex" )
, new WhereClause("pIndex>-1"),
new SimpleWithClause("DISTINCT notInitMethods,identss, COLLECT([id, pIndex]) as idDecType")

//AQUI LO HAREMOS
			 ,
			 new MatchClause(false, "(inv{nodeType:'JCMethodInvocation'})-[:METHODINVOCATION_METHOD_SELECT]->(methodSel)-[:ASSIGNMENT_RHS | :MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR *0..]->(id{nodeType:'JCIdent'})")
	
			 , new SimpleWithClause("notInitMethods,identss,idDecType,inv,methodSel,[FILTER(idPType IN idDecType WHERE idPType[0]=id)[0][1],0] as thisFlow")
			
			 , new MatchClause(true,"(inv)-[:METHODINVOCATION_ARGUMENTS]->()-[:ASSIGNMENT_RHS | :MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR *0..]->(id{nodeType:'JCIdent'})")
			, new SimpleWithClause("DISTINCT notInitMethods,identss, idDecType,inv, methodSel, thisFlow, id ORDER BY ID(id)")
		
			, new SimpleWithClause("notInitMethods, identss, idDecType,inv, methodSel, thisFlow, COLLECT(id) as argIds")
			, new SimpleWithClause("DISTINCT notInitMethods,identss, idDecType, COLLECT([inv, FILTER(x IN [thisFlow]+EXTRACT( i IN RANGE(0, SIZE(argIds)-1) | [FILTER(idPType IN idDecType WHERE idPType[0]=argIds[i])[0][1],i+1]) WHERE NOT x[0] IS NULL)]) as invIds"),
				 
				 new MatchClause(false,							"(modification)-[:ASSIGNMENT_LHS | :COMPOUND_ASSIGNMENT_LHS | :UNARY_ENCLOSES]->(lhs)-[:MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR*0..]->(firstId{nodeType:'JCIdent'}), "
		+ getStatementServices().getMethodFromStatement(getExpressionServices().getStatementFromExp(new NodeVar("modification")), new MatchImpl("(method)-[:HAS_METHODDECL_MODIFIERS]->(methodMod)")).matchToString()					
							),
				 //Si es id = tiene que ser atributo, el resto como antes
				 new SimpleWithClause("DISTINCT notInitMethods,identss, invIds,method as mutator,lhs, firstId, FILTER(idP IN idDecType WHERE idP[0]=firstId)[0][1] as pType "),

				 new WhereClause("lhs<>firstId OR pType=0"),
 new SimpleWithClause("DISTINCT  notInitMethods,identss, invIds, mutator, COLLECT(DISTINCT pType) as pTypes "), 
 new MatchClause(false,"invTrace=(anyMethod{nodeType:'JCMethodDecl'})-[:CALLS | :HAS_DEC*0..]->(mutator)"),
 new SimpleWithClause("DISTINCT  notInitMethods,identss, anyMethod,mutator, EXTRACT(inv IN FILTER(inv IN NODES(invTrace) WHERE inv.nodeType='JCMethodInvocation') | FILTER(invId IN invIds WHERE invId[0]=inv)[0][1]) as invTracePlusIdDec,  pTypes"), 
 new WhereClause("ALL(inv IN invTracePlusIdDec WHERE NOT inv IS NULL)"),
 new SimpleWithClause("notInitMethods,identss, anyMethod, "
 		+ "CASE WHEN SIZE(invTracePlusIdDec)=0 THEN pTypes ELSE EXTRACT( pair IN FILTER(finalDec IN "
 		+ "EXTRACT( w IN REDUCE(s=[],argsInv IN invTracePlusIdDec | CASE WHEN SIZE(s)=0 THEN "
 		+ " EXTRACT(a IN argsInv |[a[0],a]) ELSE "
 		+ " EXTRACT(a IN argsInv | [ "
 		+ " FILTER(x IN s WHERE x[1][1]=a[0])[0][0]"
 		+ ", a])"
 		+ " END) | [w[0],w[1][1]]) WHERE finalDec[1] IN pTypes) | pair[0]) END as mutableDecs"),
 new SimpleWithClause("DISTINCT notInitMethods,identss, anyMethod, REDUCE (s=[], x IN COLLECT(mutableDecs)  | s+x) as mutableDecs")
 , new SimpleWithClause("DISTINCT notInitMethods,identss, COLLECT([anyMethod, mutableDecs]) as mutatorMethods "),
 

//new MatchClause(true,"(dec)<-[:DECLARES_FIELD]-(encC)"), new MatchClause(true,"(dec)<-[:HAS_METHODDECL_PARAMETERS]-(encM)")
//				, new UnwindClause("identss", "p"),
//
//				new SimpleWithClause("p[0] as dec, p[1] as ids, identss"),
				new ClauseImpl(
						"MATCH (varDecl{nodeType:'JCVariableDecl',typeKind:'DECLARED'})-[:HAS_VARIABLEDECL_MODIFIERS]->(varModifiers)"
								),
				new WhereClause("varModifiers.flags CONTAINS 'final' "),
				new SimpleWithClause("DISTINCT varDecl,[] as identss,[] as mutatorMethods,[] as  notInitMethods"),
				new MatchClause(true,
						"(modification)-[:ASSIGNMENT_LHS | :COMPOUND_ASSIGNMENT_LHS | :UNARY_ENCLOSES]->()-[:MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR*]->(id)-[:MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR*0..]->(firstId{nodeType:'JCIdent'}), "
	+ getStatementServices().getMethodFromStatement(getExpressionServices().getStatementFromExp(new NodeVar("modification")), new NodeVar("method")).matchToString()					
						),
				new WhereClause(
//						"NOT (NOT method IN notInitMethods AND ) AND "+
						 "id IN EXTRACT(x IN FILTER(idDec IN identss WHERE idDec[1]=varDecl)| x[0]) AND ( modification.nodeType<>'JCUnary' OR modification.operator IN ['POSTFIX_INCREMENT', 'POSTFIX_DECREMENT', 'PREFIX_INCREMENT', 'PREFIX_DECREMENT']) "),
				new SimpleWithClause("DISTINCT mutatorMethods, notInitMethods,identss, varDecl,COLLECT([firstId, method]) as stateMods"),
				
				new MatchClause(true, "(invDec)<-[:HAS_DEC]-(inv)-[:METHODINVOCATION_METHOD_SELECT]->()-[ :MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR*1..]->(id)-[:MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR*0..]->(firstId{nodeType:'JCIdent'}),"
						+ "(inv)<-[:CALLS]-(method) "
									
						)
, new WhereClause("id IN EXTRACT(x IN FILTER(idDec IN identss WHERE idDec[1]=varDecl)| x[0]) "
		+ " AND 0 IN  FILTER(methodAndMutDecs IN mutatorMethods WHERE invDec=methodAndMutDecs[0])[0][1]"
		)
		, new SimpleWithClause("DISTINCT mutatorMethods, notInitMethods,identss, varDecl,stateMods + COLLECT([firstId, method]) as stateMods "),
		*/
				new ClauseImpl(
						"MATCH (varDecl{nodeType:'JCVariableDecl',typeKind:'DECLARED'})-[:HAS_VARIABLEDECL_MODIFIERS]->(varModifiers)"
								),
				new SimpleWithClause("varDecl, [] as identss, [] as mutatorMethods, [] as stateMods"),
		new MatchClause(true, "(invDec)<-[:HAS_DEC]-(inv)-[:METHODINVOCATION_ARGUMENTS]->(arg)-[ :MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR*0..]->(id)-[:MEMBER_SELECT_EXPR | :ARRAYACCESS_EXPR*0..]->(firstId{nodeType:'JCIdent'})"
				+ ",(inv)<-[:CALLS]-(method) "
							
				)
		, new WhereClause("id IN EXTRACT(x IN FILTER(idDec IN identss WHERE idDec[1]=varDecl)| x[0]) "
//				+ " AND 0 IN  FILTER(methodAndMutDecs IN mutatorMethods WHERE invDec=methodAndMutDecs[0])[0][1]"
				),
		new SimpleWithClause("mutatorMethods, varDecl,stateMods,method, invDec, arg, firstId ORDER BY ID(arg)"),
		new SimpleWithClause("mutatorMethods, varDecl,stateMods,method, invDec, COLLECT([firstId, method]) as argInfo ")
		, 
		new ReturnClause("DISTINCT varDecl,stateMods,method, invDec, EXTRACT( argIndex IN FILTER(i IN RANGE(0,SIZE(argInfo)-1) WHERE (i+1) IN  FILTER(methodAndMutDecs IN mutatorMethods WHERE invDec=methodAndMutDecs[0])[0][1]) | argInfo[argIndex])")
		};

	}

	public static void main(String[] args) {
		System.out.println(new OBJ50_ERROR_TEST().queryToString());
	}
}

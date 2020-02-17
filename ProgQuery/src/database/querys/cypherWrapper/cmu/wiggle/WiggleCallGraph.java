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
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.services.FieldServicesWiggle;
import database.relations.RelationTypesWiggle;

public class WiggleCallGraph extends AbstractQuery {

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

	public WiggleCallGraph() {
		super(false);
	}

	public static final String CREATE_ALL_SUBTYPE_RELS = "MATCH (n)-[:HAS_CLASS_EXTENDS |:HAS_CLASS_IMPLEMENTS]->()-[:PARAMETERIZEDTYPE_TYPE*0..]->(m),(t) WHERE SPLIT(t.fullyQualifiedName,'<')[0]=SPLIT(m.actualType,'<')[0] MERGE (n)-[r:IS_SUBTYPE_OF]->(t) ON CREATE SET r.created=TRUE ";

	@Override
	protected void initiate() {
		// TO_ DO ADD DELETE RELS
		clauses = new Clause[] {
//				new ClauseImpl("MATCH (m{nodeType:'JCMethodDecl'})-[r:CALLS]->() DELETE r WITH DISTINCT '' as aux"),
				new ClauseImpl(CREATE_ALL_SUBTYPE_RELS), 
				new SimpleWithClause("DISTINCT '' as aux "),
				new MatchClause(new MatchImpl(getStatementServices().getMethodFromStatement(getExpressionServices()
						.getStatementFromExp(new MatchImpl( "(invMember)<-[:METHODINVOCATION_METHOD_SELECT]-(inv{nodeType:'JCMethodInvocation'})")), new NodeVar("method{nodeType:'JCMethodDecl'}")).matchToString()+
						"<-[:DECLARES_METHOD]-(callerEncType)"
						)),
				new MatchClause(true, "(invMember)-[:MEMBER_SELECT_EXPR]->(memberSelection), (otherPossibleCallerEncType{nodeType:'JCClassDecl',fullyQualifiedName:memberSelection.actualType})"),
				new SimpleWithClause("invMember, inv,  method, CASE WHEN invMember.nodeType='JCIdent' THEN callerEncType ELSE otherPossibleCallerEncType END as callerEncType" )

,new WhereClause("NOT callerEncType IS NULL"),
								new MatchClause(true, "(callerEncType)<-[:IS_SUBTYPE_OF | :IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]-(encSubType)-[:DECLARES_METHOD]->(invDeclaration)-[:HAS_METHODDECL_BODY]->()"
						),new WhereClause("CASE WHEN invMember.name ='this' THEN '<init>' ELSE invMember.name END =invDeclaration.name "),
								new MatchClause(true,"(invDeclaration)-[:HAS_METHODDECL_PARAMETERS]->(p)  "),
								new SimpleWithClause("inv, invMember,  method, callerEncType,encSubType, invDeclaration, p ORDER BY ID(p)"),
								
								new SimpleWithClause("inv,invMember, FILTER(argType IN SPLIT(SPLIT(SUBSTRING(invMember.actualType,1),')')[0],',') WHERE argType<>'') as argTypes,  method, callerEncType, invDeclaration, COLLECT(p.actualType) as pTypes"),
								new SimpleWithClause("inv,invMember,  method, callerEncType,argTypes, CASE WHEN SIZE(pTypes)=SIZE(argTypes) AND ALL(index IN RANGE(0,SIZE(pTypes)-1) WHERE argTypes[index]=pTypes[index]) THEN invDeclaration ELSE NULL END as invDeclaration "),
								/*
								new WhereClause("SIZE(pTypes)=SIZE(argTypes) AND ALL(index IN RANGE(0,SIZE(pTypes)-1) WHERE argTypes[index]=pTypes[index])"
										
										),*/
								new SimpleWithClause("inv, invMember,argTypes,  method, callerEncType, COLLECT(invDeclaration) as invDecs"),
//								new SimpleWithClause("inv, invMember,argTypes,  method, callerEncType, invDecs "),
								
								// SI el método buscado no está T, entonces buscamos el primer super que tenga el método, y siempre le sumamos la lista anterior de T'::m donde T'<:T
				new MatchClause(true, "hierarchy=(callerEncType)-[:IS_SUBTYPE_OF | :IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*0..]->(encSuperType)-[:DECLARES_METHOD]->(superInvDeclaration)-[:HAS_METHODDECL_BODY]->()"
						),
//				new ReturnClause("DISTINCT method, callerEncType, inv, invDecs , hierarchy ORDER BY inv")
				
new WhereClause("CASE WHEN invMember.name IN ['this','super'] THEN '<init>' ELSE invMember.name END =superInvDeclaration.name AND (callerEncType<>encSuperType OR invMember.name<>'super')"),
				new MatchClause(true,"(superInvDeclaration)-[:HAS_METHODDECL_PARAMETERS]->(p)  "),
				new SimpleWithClause("hierarchy,inv, invMember, argTypes, method, callerEncType,encSuperType,superInvDeclaration, invDecs, p ORDER BY ID(p)"),
				//
				new SimpleWithClause("hierarchy,inv,invMember,argTypes,   method, callerEncType,encSuperType,superInvDeclaration, invDecs, COLLECT(p.actualType) as pTypes"),
				new SimpleWithClause("inv,invMember,  method, callerEncType, invDecs, CASE WHEN SIZE(pTypes)=SIZE(argTypes) "
						+ "AND ALL(index IN RANGE(0,SIZE(pTypes)-1) WHERE argTypes[index]=pTypes[index]) THEN superInvDeclaration ELSE NULL END as superInvDeclaration ORDER BY FILTER(i IN RANGE(0,CASE WHEN hierarchy IS NULL THEN 0 ELSE SIZE(NODES(hierarchy)) END) WHERE NODES(hierarchy)[i]=encSuperType)[0] "
						
						),
				new SimpleWithClause("method"
//						+ ", encSubType,  method, callerEncType"
						+ ",inv,invDecs,  COLLECT(superInvDeclaration)[0] as superDec"),
				new SimpleWithClause("method"
//						+ ", encSubType,  method, callerEncType"
						+ ",inv,invDecs+ CASE WHEN superDec IS NULL THEN [] ELSE [superDec] END as possibleDecs")
				
				
				 
, new UnwindClause("possibleDecs", "possibleCalle"),
//new ReturnClause("DISTINCT method, inv, possibleCalle ORDER BY inv")
new ClauseImpl("MERGE (method)-[r:CALLS]->(inv)-[r2:HAS_DEC]->(possibleCalle) ON CREATE SET r.created=TRUE, r2.created=TRUE ")
		
		};

	}

	public static void main(String[] args) {
		System.out.println(new WiggleCallGraph().queryToString());
	}
}

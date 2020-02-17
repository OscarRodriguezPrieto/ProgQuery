package database.querys.cypherWrapper.cmu.wiggle;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.AnonymousNode;
import database.querys.cypherWrapper.Any;
import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.Case;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.Extract;
import database.querys.cypherWrapper.Filter;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.querys.cypherWrapper.Reduce;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.services.InmutabilityServicesPQ;
import database.relations.RelationTypes;
import database.relations.RelationTypesWiggle;
import database.relations.TypeRelations;
import utils.dataTransferClasses.Pair;

public class OBJ56 extends AbstractQuery {

	public OBJ56() {
		super(false);

	}

	private static final String CREATE_ITS_TYPE_IS_RELS = 
			"MATCH (c)-[:DECLARES_FIELD]->(n),(m) WHERE EXISTS(n.actualType) AND n.actualType=m.fullyQualifiedName CREATE (n)-[:ITS_TYPE_IS{created:TRUE}]->(m) WITH DISTINCT 'l' as l "+
			 "MATCH (c)-[:DECLARES_FIELD]->(n) WHERE EXISTS(n.typeKind) AND n.typeKind='ARRAY' MERGE (n)-[:ITS_TYPE_IS{created:TRUE}]->(arrayType:ARRAY_TYPE{fullyQualifiedName:n.actualType}) ";
	public static final String CREATE_ALL_SUBTYPE_RELS = "MATCH (n)-[:HAS_CLASS_EXTENDS]->()-[:PARAMETERIZEDTYPE_TYPE*0..]->(m),(t) WHERE SPLIT(t.fullyQualifiedName,'<')[0]=SPLIT(m.actualType,'<')[0] MERGE (n)-[r:IS_SUBTYPE_EXTENDS]->(t) ON CREATE SET r.created=TRUE ";

	@Override
	protected void initiate() {
		Node typeDec = new NodeVar("typeDec"); 
		clauses = new Clause[] {

				new ClauseImpl(CREATE_ITS_TYPE_IS_RELS+" WITH DISTINCT 'l' as aux")
				, new ClauseImpl(CREATE_ALL_SUBTYPE_RELS
						+" WITH DISTINCT 'l' as aux"
						),  
				new MatchClause(getPDGServices().getCompleteIdentification(new NodeVar("id"), "")),

				new MatchClause(
						getStatementServices().getMethodFromStatement(
								new MatchImpl(
										"(classMod)<-[:HAS_CLASS_MODIFIERS]-(enclosingType)-[:DECLARES_FIELD]->(dec),(id)<-[:RETURN_EXPR]-()"),
								Node.nodeForWiggle("method", NodeTypes.METHOD_DEF)),
						new MatchImpl(
								"(enclosingMethodType)-[:DECLARES_METHOD]->(method)-[:HAS_METHODDECL_MODIFIERS]->(methodMod),(dec)-[:HAS_VARIABLEDECL_MODIFIERS]->(fieldMod) "
								+ ",(dec)-[:ITS_TYPE_IS]->()<-[:IS_SUBTYPE_EXTENDS|IS_SUBTYPE_IMPLEMENTS*0..]-(typeDec)"
							 			)),
				new WhereClause(
						"id IN ids AND NOT fieldMod.flags CONTAINS 'public' AND methodMod.flags CONTAINS 'public' AND classMod.flags CONTAINS 'public' AND (typeDec:ARRAY_TYPE OR typeDec.nodeType<>'ClassType') "),
				new SimpleWithClause("typeDec,enclosingType,dec as field,enclosingMethodType.fullyQualifiedName+':'+ method.name +'(line '+method.lineNumber+')' as method,identss"),
				// BIEN
				new MatchClause(true, InmutabilityServicesPQ.getTypesSuperTypesAndFieldsTransitive(typeDec)),

				new SimpleWithClause(
						"DISTINCT typeDec,enclosingType, field,identss, method,p, CASE WHEN p IS NULL THEN [typeDec] else NODES(p) END as nodes"),
				new UnwindClause("nodes", "nodeInP"),
				new SimpleWithClause("enclosingType, field, method,identss, typeDec, nodeInP,nodes"),
				// TESTING
				new MatchClause(true,
						new Path(new NodeVar("nodeInP"),
								Pair.create(new EdgeImpl(Cardinalidad.MIN_TO_INF(0), TypeRelations.IS_SUBTYPE_EXTENDS,
										TypeRelations.IS_SUBTYPE_IMPLEMENTS), new AnonymousNode()),
								Pair.createP("f", RelationTypes.DECLARES_FIELD)),

						new RelationshipImpl(new NodeVar("mod"), new NodeVar("id"),
								new EdgeImpl(Cardinalidad.ONE_TO_INF, RelationTypesWiggle.MEMBER_SELECT_EXPR,
										RelationTypesWiggle.ARRAYACCESS_EXPR, RelationTypesWiggle.ASSIGNMENT_LHS)),
						getExpressionServices().getMethodFromExp(new NodeVar("mod"),
								Node.nodeForWiggle("setMethod", NodeTypes.METHOD_DEF)),
						new MatchImpl(
								"(nodeInP)-[:DECLARES_METHOD]->(setMethod)-[:HAS_METHODDECL_MODIFIERS]->(setMod)")),

				new WhereClause("setMod.flags CONTAINS 'public' AND NOT setMod.flags CONTAINS 'static' AND [id,f] IN identss AND setMethod.name<>'<init>'"),

				new SimpleWithClause(
						"enclosingType, field, method, typeDec,identss, nodeInP,nodes, CASE WHEN setMethod IS NULL THEN [] ELSE COLLECT(DISTINCT setMethod) END as setters"),
//				new ReturnClause(
//						"enclosingType.fullyQualifiedName, field.name, method.name, typeDec.fullyQualifiedName, nodeInP,EXTRACT(n IN nodes |CASE WHEN EXISTS(n.fullyQualifiedName) THEN n.fullyQualifiedName ELSE n.name END), CASE WHEN setMethod IS NULL THEN [] ELSE COLLECT( DISTINCT setMethod) END as setters"),
			
					new MatchClause(true,
							new MatchImpl(
									"(anyMethod)<-[:DECLARES_METHOD]-(nodeInP)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->()-[:DECLARES_METHOD]->(ovMethod{name:anyMethod.name})")
						
			),
//					 new WhereClause("ovMethod.")	,
//					new ReturnClause("enclosingType, field, method, typeDec, nodeInP,nodes, setters, anyMethod, ovMethod")
					
						new MatchClause(true,
					new MatchImpl(
							"(anyMethod)-[:HAS_METHODDECL_PARAMETERS]->(p),(ovMethod)-[:HAS_METHODDECL_PARAMETERS]->(baseP)")
	),
					
				new SimpleWithClause(
						"enclosingType, field, method,anyMethod, typeDec"
						+ ",identss"
						+ ",ovMethod, nodeInP,nodes, setters, COLLECT(p.actualType) as params, COLLECT(baseP.actualType) as baseParams "),
				new SimpleWithClause(
						"enclosingType, field, method,anyMethod, typeDec"
						+ ",identss"
						+ ",ovMethod, nodeInP,nodes, setters, EXTRACT(p IN params |[p, SIZE(FILTER(x IN params WHERE x=p))]) as params, "
						+ "EXTRACT(p IN baseParams |[p, SIZE(FILTER(x IN baseParams WHERE x=p))]) as baseParams"),
		
	
				 new SimpleWithClause(" enclosingType, field, method, typeDec"
				 		+ ",identss"
				 		+ ", nodeInP,nodes, setters"
				 		+ ",COLLECT( DISTINCT  CASE WHEN "
				 		+ "SIZE(params)=SIZE(baseParams) AND ovMethod.name<>'<init>'"
				 		+ " AND ALL(pPair IN baseParams WHERE pPair IN params)"
				 		+ " THEN ovMethod ELSE NULL END) as methodsOverridenByThisType"
				 		),

//new ReturnClause("distinct method")
			 
				new MatchClause(true,
						new MatchImpl(
								"(nodeInP)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*0..]->()-[:DECLARES_METHOD]->(getMethod)-[:HAS_METHODDECL_MODIFIERS]->(methodMod)"),

						new Path(new NodeVar("nodeInP"), Pair.createP("f", RelationTypes.DECLARES_FIELD)),
						getStatementServices().getMethodFromStatement(
								new MatchImpl("(id)<-[:RETURN_EXP]-()"),new NodeVar("getMethod")
								
										)),
				new WhereClause(
						"[id,f] IN identss AND methodMod.flags CONTAINS 'public' AND NOT methodMod.flags CONTAINS 'static'"),
//			new ReturnClause("enclosingType, field, method, typeDec, nodeInP,nodes, setters,methodsOverridenByThisType, COLLECT(DISTINCT getMethod) as getters")
					new MatchClause(true,

						new RelationshipImpl(new NodeVar("f"), new NodeVar("fType"),
								new EdgeImpl(TypeRelations.ITS_TYPE_IS))),
				//WHERE
//				Pair.create("accessLevel", "public"), Pair.create("isStatic", false)
 
				new SimpleWithClause(
						"enclosingType, field, method, typeDec, nodeInP,nodes, setters,methodsOverridenByThisType, COLLECT(DISTINCT getMethod) as getters,  COLLECT(DISTINCT fType) as getterTypes"),
				new MatchClause(true, 
		new Path(new NodeVar("nodeInP"),
								Pair.createP(new NodeVar("f"), RelationTypes.DECLARES_FIELD),
				Pair.create(new EdgeImpl(TypeRelations.ITS_TYPE_IS), new NodeVar("fType")))
		, new MatchImpl("(f)-[:HAS_VARIABLEDECL_MODIFIERS]->(fMods)")
						),
				new WhereClause("fMods.flags CONTAINS 'public' AND NOT fMods.flags CONTAINS 'static'"),
//WHERE , Pair.create("isStatic", false),Pair.create("accessLevel", "public")
new SimpleWithClause(
						"enclosingType, field, method, typeDec,nodeInP,nodes,methodsOverridenByThisType, setters, getters,getterTypes, COLLECT(DISTINCT [f,fType,fMods.flags CONTAINS 'final']) as  otherMutableDependencies "),
 new SimpleWithClause(
		"enclosingType, field, method, typeDec, nodeInP, nodes, setters,methodsOverridenByThisType, getters,getterTypes, "
								+ new Extract(new Filter("otherMutableDependencies", "x[2]"), "y[1]", "y")
						.expToString()
				+ " as mutableDependenciesBis, "
								+ new Any("otherMutableDependencies", "NOT  x[2]").expToString()
				+ " as isMutableDueToPublicField "),
// new ReturnClause("enclosingType, field, method, typeDec,
// nodeInP, p, setters")
// new WhereClause("SIZE(NODES(p))=12"),
				new SimpleWithClause("DISTINCT enclosingType, field, method, typeDec, nodes"
//
		+ ", COLLECT([ID(nodeInP),[isMutableDueToPublicField,mutableDependenciesBis,methodsOverridenByThisType,setters, getters,getterTypes ]]) as map "
//
), 

				new SimpleWithClause("enclosingType, field, method,typeDec,nodes as prevNodes"

						+ ","
						+ new Extract("nodes", "x+HEAD(" + new Filter("map", "y[0]=ID(x)", "y").expToString() + ")[1]")
								.expToString()
						+ " as nodes"

				), new SimpleWithClause("DISTINCT enclosingType, field, method, typeDec,prevNodes, " + new Extract("nodes", "[x[0],CASE WHEN x[1] IS NULL THEN FALSE ELSE x[1] END, CASE WHEN x[2] IS NULL THEN [] ELSE x[2] END, CASE WHEN x[3] IS NULL THEN [] ELSE x[3] END, CASE WHEN x[4] IS NULL THEN [] ELSE x[4] END,CASE WHEN x[5] IS NULL THEN [] ELSE x[5] END, CASE WHEN x[6] IS NULL THEN [] ELSE x[6] END]").expToString() + " as nodes"
				//
				), new SimpleWithClause("enclosingType, field, method, typeDec,prevNodes, CASE WHEN nodes IS NULL THEN [] ELSE nodes END as nodes "),

				new SimpleWithClause(
						"enclosingType, field, method, typeDec,prevNodes, nodes, RANGE(0,SIZE(nodes)-1,1) as indexes"),
				new SimpleWithClause(
						"enclosingType, field, method, typeDec"
						+ ",prevNodes"
						+ ", nodes, indexes, EXTRACT(index IN indexes | CASE WHEN SIZE(FILTER(i IN indexes WHERE i<index AND NOT nodes[i][0].nodeType='JCClassDecl'))=0 THEN 0 ELSE   LAST(FILTER(i IN indexes WHERE i<index AND nodes[i][0].nodeType='JCClassDecl')) END) as lastAttrIndexes"
						+ " "),

				new SimpleWithClause("enclosingType, field, method, typeDec,prevNodes, nodes, " + new Extract("indexes",
						new Reduce(new Filter("indexes", "i<index AND i>=lastAttrIndexes[index]", "i").expToString(),
								"s+nodes[x][3]", "s=[]").expToString()

						, "index").expToString() + " as overrides, indexes, lastAttrIndexes"),
				new SimpleWithClause(" enclosingType, field, method, typeDec,prevNodes, " + new Extract("indexes",
						"[nodes[x][0], nodes[x][1] OR ANY(setter IN nodes[x][4] WHERE NOT setter IN overrides[x]) "

								+ ", nodes[x][2]+ EXTRACT( getterIndex IN FILTER(gf IN RANGE(0,SIZE(nodes[x][5])-1,1) "
								+ "WHERE NOT nodes[x][5][gf] IN overrides[x])  | nodes[x][6][getterIndex])] ")
										.expToString()
						+ " as nodes, indexes "),
				// new SimpleWithClause("enclosingType, field, method,
				// typeDec,p, " + new Extract("indexes",
				// "[nodes[x][0], nodes[x][1]," + new Case("x>0 AND NOT
				// nodes[x+1] IS NULL",
				// "CASE WHEN nodes[x][0]:ATTR_DEC THEN nodes[x+1][0] IN
				// nodes[x-1][2] ELSE TRUE END",
				// "TRUE").expToString() + "] ").expToString()
				// + " as nodes"),
				new SimpleWithClause(
						"enclosingType, field, method, typeDec,prevNodes,indexes, " + new Extract("indexes",
								"[nodes[x][0], nodes[x][1],"
										+ new Case("x>1 AND NOT nodes[x-1][0].nodeType='JCClassDecl'",
										" ANY(dep IN nodes[x-2][2] WHERE dep =nodes[x][0])", "TRUE").expToString()
										+ "] ").expToString()
								+ " as nodes"),
				new SimpleWithClause(" enclosingType, field, method, typeDec, " + new Extract("indexes",
						"[nodes[x][0], nodes[x][1]," + "NOT ANY(i IN indexes WHERE i<=x AND NOT nodes[i][2]) ]")
								.expToString()
						+ " as nodes "),
				// new SimpleWithClause(
				// "enclosingType, field, method,typeDec,p, CASE WHEN
				// SIZE(nodes)=0 THEN []+typeDec ELSE nodes END as nodes "),
				// new WhereClause("SIZE(nodes)=0"),
				new SimpleWithClause("DISTINCT enclosingType, field, method, typeDec," + new Reduce("nodes",
						// MISSING ADD ARRAY_TYPE AS ITS_TYPE_IS array AT THE
						// VERY BEGINNING
						"s AND CASE WHEN NOT x[0].nodeType='JCClassDecl' THEN TRUE ELSE NOT x[2] OR (NOT x[0]:ARRAY_TYPE AND NOT x[1]) END ",
						"s=true").expToString() + " AND NOT typeDec:ARRAY_TYPE as res"

				), new SimpleWithClause("enclosingType, field,method, typeDec,ANY (x IN COLLECT(res) WHERE NOT x) as isMutable"), 
				new SimpleWithClause("DISTINCT enclosingType, field,COLLECT(method) as methods, typeDec,isMutable"),

				new WhereClause("isMutable"),
				// FILTRAR
				// ,
		
						new SimpleWithClause("COLLECT([enclosingType, field, methods, typeDec]) as res"),
				new UnwindClause("res", "typeRes"),
				new SimpleWithClause(
						"typeRes[1] as field, typeRes[3] as typeDec, typeRes[0] as enclosingType,typeRes[2] as methods, res"),
				new MatchClause(true,
						new MatchImpl("(typeDec)<-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]-(subType)")),
				new SimpleWithClause("typeDec,methods,enclosingType,field, EXTRACT(x IN res | x[3]) as mutableTypes, COLLECT(subType) as subtypes"),
				new SimpleWithClause(
						" typeDec, methods,enclosingType,field, FILTER(y IN subtypes WHERE NOT y IN mutableTypes) as nonMutableSubTypes")
				,new SimpleWithClause(				" 'Warning[OBJ-56] Field ' +field.name+' declared in line ' +field.lineNumber+' in class '+enclosingType.fullyQualifiedName+' is not public, but it is exposed in public methods such as '+ methods+'. The problem is that the type '+typeDec.fullyQualifiedName+' can be mutated by a malicious client.'+ CASE WHEN SIZE(nonMutableSubTypes)=0 THEN ' You should use an appropiate inmutable subtype as a wrapper for your attribute.'ELSE 'Remember to use an appropiate inmutable subtype (such as '+EXTRACT(x IN nonMutableSubTypes | x.fullyQualifiedName)+') as a wrapper for your attribute.'END as warning ")

				, new SimpleWithClause("DISTINCT  COLLECT(warning) as warningList ")
				,
				new ClauseImpl(
						"MATCH ()-[r]->() WHERE r.created DELETE r WITH DISTINCT warningList MATCH (array:ARRAY_TYPE) DELETE array ")
				, new ReturnClause("DISTINCT warningList")
//				, new ReturnClause(" warningList")

		};
	}

	public static void main(String[] args) {
		System.out.println(new OBJ56().queryToString());
	}
}

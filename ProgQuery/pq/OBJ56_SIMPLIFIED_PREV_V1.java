package database.querys.cypherWrapper.cmu.pq;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.UnwindClause;
import database.querys.cypherWrapper.WhereClause;
import database.relations.CHGRelationTypes;
import database.relations.PDGRelationTypes;
import database.relations.RelationTypes;
import utils.dataTransferClasses.Pair;

public class OBJ56_SIMPLIFIED_PREV_V1 extends AbstractQuery {

	public OBJ56_SIMPLIFIED_PREV_V1() {
		super(true);

	}

	@Override
	protected void initiate() {
		Node typeDec = new NodeVar("typeDec");
		clauses = new Clause[] {
				new MatchClause(
						getStatementServices()
								.getMethodFromStatement(
										new MatchImpl("(enclosingType)-[:DECLARES_FIELD]->(field:ATTR_DEC)-[:USED_BY]->(retExpr)<-[:RETURN_EXPR]-()"),
										new CompleteNode("method", NodeTypes.METHOD_DEC,
												Pair.create("accessLevel", "public")))
								.append(Pair.create(new EdgeImpl(EdgeDirection.OUTGOING, RelationTypes.DECLARES_METHOD),
										new NodeVar("{accessLevel:'public'}")))),
				// DECLARED IN A PUBLIC TYPE
				// new MatchImpl(
				// "(field)-[:ITS_TYPE_IS]->()<-[:IS_SUBTYPE_EXTENDS|IS_SUBTYPE_IMPLEMENTS*0..]-(typeDec)"))

				new WhereClause("NOT field.accessLevel='public' "),
				// LUEGO HAY QUE METER EL CASO DE LOS ARRAYS
				new MatchClause(new RelationshipImpl(new NodeVar("field"), new NodeVar("fieldType"),
						new EdgeImpl(RelationTypes.ITS_TYPE_IS)),
						new Path(new NodeVar("fieldType"), "accessibleMembers",
								Pair.create(new EdgeImpl(Cardinalidad.MIN_TO_INF(0), RelationTypes.DECLARES_FIELD,
												RelationTypes.ITS_TYPE_IS, CHGRelationTypes.INHERITS_FIELD),
										new NodeVar("accessibleMember")))),
				new SimpleWithClause(
						"field,enclosingType, method,accessibleMember, NODES(accessibleMembers) as accessibleMembers"),
				new SimpleWithClause(
						"field,enclosingType, accessibleMembers,accessibleMember, COLLECT(DISTINCT method.fullyQualifiedName+'( line '+method.lineNumber+')') as publicGetters, EXTRACT(index IN RANGE(0,SIZE( accessibleMembers)-1,1) | [CASE WHEN index=0 THEN field ELSE accessibleMembers[index-1] END, accessibleMembers[index]]) as accessibleMembersAndPrevs"),
				new UnwindClause("accessibleMembersAndPrevs", "accMemberAndPrev"),
				new MatchClause(true,
						getStatementServices()
								.getMethodFromStatement(
										new MatchImpl("(accesibleField)-[:USED_BY]->(fieldExpr)<-[:RETURN_EXPR]-()"),
										new CompleteNode("method", Pair.create("accessLevel", "public")))
								.append(Pair.create(new EdgeImpl(EdgeDirection.OUTGOING, RelationTypes.DECLARES_METHOD,
										CHGRelationTypes.INHERITS_METHOD), new NodeVar("accessibleType")))),
				new WhereClause(
						"ID(accesibleField)=ID(accMemberAndPrev[1]) AND ID(accessibleType)=ID(accMemberAndPrev[0])"),
				new SimpleWithClause(
						" field,enclosingType, method,publicGetters,accMemberAndPrev as accMemberOrType, accessibleMembers, accessibleMember,  COLLECT(method)  as gettersForCurrentMember"),

				new SimpleWithClause(
						"field,enclosingType,publicGetters,accessibleMembers,accessibleMember,accMemberOrType, COLLECT( CASE WHEN accMemberOrType[1]:ATTR_DEC THEN [NOT accMemberOrType[1].isStatic AND (accMemberOrType[1].accessLevel='public' OR SIZE(gettersForCurrentMember)>0),CASE WHEN NOT accMemberOrType[1].isStatic AND accMemberOrType[1].accessLevel<>'public' THEN gettersForCurrentMember ELSE []END] ELSE CASE WHEN accMemberOrType[1]:ARRAY_TYPE THEN [TRUE,[]] ELSE [EXISTS(accMemberOrType[1].accessLevel) AND accMemberOrType[1].accessLevel='public',[]] END END) as relevantInfoAndGetters "),

//				new SimpleWithClause(
//						"field,enclosingType,publicGetters,accessibleMembers,accessibleMember, COLLECT( CASE WHEN accMemberOrType[1]:ATTR_DEC THEN NOT accMemberOrType[1].isStatic AND (accMemberOrType[1].accessLevel='public' OR SIZE(relevantGetters)>0) ELSE CASE WHEN accMemberOrType[1]:ARRAY_TYPE THEN TRUE ELSE accMemberOrType[1].accessLevel='public' END END ) as accMemberOrTypesInfo"),
				new WhereClause("ALL( accMemberOrTypeInfo IN relevantInfoAndGetters WHERE accMemberOrTypeInfo[0])"),
				new SimpleWithClause(
						"DISTINCT field, enclosingType, publicGetters,  accessibleMember, EXTRACT(relevantInfo IN relevantInfoAndGetters| relevantInfo[1]) as relevantGetters"),
				new MatchClause(true, new RelationshipImpl(new NodeVar("accessibleMember"),
						new CompleteNode("mutator", NodeTypes.METHOD_DEC, Pair.create("accessLevel", "public")),
						RelationTypes.DECLARES_METHOD, CHGRelationTypes.INHERITS_METHOD)
								.append(Pair.create(
										new EdgeImpl(EdgeDirection.OUTGOING, PDGRelationTypes.STATE_MAY_BE_MODIFIED,
												PDGRelationTypes.STATE_MODIFIED_BY),
										new CompleteNode(NodeTypes.THIS_REF)))),

				// new SimpleWithClause(
				// "field, enclosingType, publicGetters, accessibleMember,
				// COLLECT(mutator) as mutators"),
				new SimpleWithClause(
						"REDUCE(allRelGettters=[], getter IN relevantGetters |allRelGettters+getter)as relevantGetters,field,enclosingType, publicGetters,COLLECT(DISTINCT mutator) as allMut, EXTRACT(accField IN FILTER(accMember IN COLLECT(accessibleMember) WHERE accMember:ATTR_DEC AND NOT accMember.isStatic AND NOT accMember.isFinal AND accMember.accessLevel='public') | 'PUBLIC NON-FINAL FIELD '+accField.name+'( line'+accField.lineNumber+')') as externallyMutableFields"),

				new WhereClause("SIZE(allMut)>0 OR SIZE(externallyMutableFields)>0"), new ReturnClause(
						" relevantGetters, 'Warning[OBJ-56] Field ' +field.name+' declared in line ' +field.lineNumber+' in class '+enclosingType.fullyQualifiedName+' is not public, but it is exposed in public methods such as '+ publicGetters+'. The problem is that there is at least one member ( like '+EXTRACT(mutator IN allMut | 'MUTATOR METHOD '+mutator.fullyQualifiedName+'( line '+mutator.lineNumber +')')+externallyMutableFields+')that can be accessed by a client to change the state of the field '+field.name")
				// new ReturnClause(
				// "field.name,publicGetters,accessibleMember,
				// accessibleMembers,accMemberInfo, ALL(accMemberOrType IN
				// accMemberInfo WHERE CASE WHEN accMemberOrType[1]:ATTR_DEC
				// THEN NOT accMemberOrType[1].isStatic AND
				// (accMemberOrType[1].accessLevel='public' OR NOT
				// accMemberOrType[2] is NULL) ELSE CASE WHEN
				// accMemberOrType[1]:ARRAY_TYPE THEN TRUE ELSE FALSE END END
				// )")
				// ,
				// new WhereClause(new All("accessibleMembersAndPrevs",
				// " memberOrType[1]:ATTR_DEC AND (memberOrType[1].isStatic AND
				// ( memberOrType[1].accessLevel='public' ) )",
				// "memberOrType"))
				// , new
				// ReturnClause("field,publicGetters,accessibleMembersAndPrevs")
				// BIEN
				// new MatchClause(true,
				// InmutabilityServicesPQ.getTypesSuperTypesAndFieldsTransitive(typeDec)),
				//
				// new SimpleWithClause(
				// "DISTINCT typeDec,enclosingType, field, method,p, CASE WHEN p
				// IS NULL THEN [typeDec] else NODES(p) END as nodes"),
				// new UnwindClause("nodes", "nodeInP"),
				// new SimpleWithClause("enclosingType, field, method, typeDec,
				// nodeInP,nodes"),
				// new MatchClause(true,
				// new Path(new NodeVar("nodeInP"),
				// Pair.create(new EdgeImpl(Cardinalidad.MIN_TO_INF(0),
				// RelationTypes.IS_SUBTYPE_EXTENDS,
				// RelationTypes.IS_SUBTYPE_IMPLEMENTS), new AnonymousNode()),
				// Pair.createP("f", RelationTypes.DECLARES_FIELD),
				//
				// Pair.createP("mod", PDGRelationTypes.MODIFIED_BY,
				// PDGRelationTypes.STATE_MAY_BE_MODIFIED,
				// PDGRelationTypes.STATE_MODIFIED_BY)),
				// getExpressionServices().getMethodFromExp(new NodeVar("mod"),
				// new CompleteNode("setMethod", NodeTypes.METHOD_DEC,
				// Pair.create("accessLevel", "public"), Pair.create("isStatic",
				// false))),
				//
				// new MatchImpl("(nodeInP)-[:DECLARES_METHOD]->(setMethod)")),
				//
				// new SimpleWithClause(
				// "enclosingType, field, method, typeDec, nodeInP,nodes, CASE
				// WHEN setMethod IS NULL THEN [] ELSE COLLECT(setMethod) END as
				// setters"),
				//
				// new MatchClause(true,
				// new MatchImpl(new RelationshipImpl(new NodeVar("nodeInP"),
				// new AnonymousNode(),
				// new EdgeImpl(RelationTypes.DECLARES_METHOD)).relToString()
				// + "-[:OVERRIDES]->(ovMethod)")),
				// // new WhereClause("publicMethod.accessLevel='public'"),
				// new SimpleWithClause(
				// "enclosingType, field, method, typeDec, nodeInP,nodes,
				// setters, COLLECT(ovMethod) as methodsOverridenByThisType"),
				//
				// new MatchClause(true,
				// new MatchImpl(
				// "(nodeInP)-[:IS_SUBTYPE_EXTENDS |
				// :IS_SUBTYPE_IMPLEMENTS*0..]->()-[:DECLARES_METHOD]->(getMethod)"),
				// getStatementServices().getMethodFromStatement(
				// new Path(new NodeVar("nodeInP"), Pair.createP("f",
				// RelationTypes.DECLARES_FIELD),
				// Pair.createP("", PDGRelationTypes.USED_BY),
				// Pair.createInv("", RelationTypes.RETURN_EXPR)),
				// new CompleteNode("getMethod", NodeTypes.METHOD_DEC,
				// Pair.create("accessLevel", "public"), Pair.create("isStatic",
				// false)))),
				// new MatchClause(true,
				//
				// new RelationshipImpl(new NodeVar("f"), new NodeVar("fType"),
				// new EdgeImpl(RelationTypes.ITS_TYPE_IS))),
				//
				// new SimpleWithClause(
				// "enclosingType, field, method, typeDec, nodeInP,nodes,
				// setters,methodsOverridenByThisType, COLLECT(DISTINCT
				// getMethod) as getters, COLLECT(DISTINCT fType) as
				// getterTypes"),
				// new MatchClause(true,
				// new Path(new NodeVar("nodeInP"),
				// Pair.createP(new CompleteNode("f", Pair.create("isStatic",
				// false),
				// Pair.create("accessLevel", "public")),
				// RelationTypes.DECLARES_FIELD),
				// Pair.create(new EdgeImpl(RelationTypes.ITS_TYPE_IS), new
				// NodeVar("fType")))),
				// new SimpleWithClause(
				// "enclosingType, field, method,
				// typeDec,nodeInP,nodes,methodsOverridenByThisType, setters,
				// getters,getterTypes, COLLECT(DISTINCT [f,fType]) as
				// otherMutableDependencies "),
				// new SimpleWithClause(
				// "enclosingType, field, method, typeDec, nodeInP, nodes,
				// setters,methodsOverridenByThisType, getters,getterTypes, "
				// + new Extract(new Filter("otherMutableDependencies",
				// "x[0].isFinal"), "y[1]", "y")
				// .expToString()
				// + " as mutableDependenciesBis, "
				// + new Any("otherMutableDependencies", "NOT
				// x[0].isFinal").expToString()
				// + " as isMutableDueToPublicField "),
				// // new ReturnClause("enclosingType, field, method, typeDec,
				// // nodeInP, p, setters")
				// // new WhereClause("SIZE(NODES(p))=12"),
				// new SimpleWithClause("DISTINCT enclosingType, field, method,
				// typeDec, nodes"
				////
				// + ",
				// COLLECT([ID(nodeInP),[isMutableDueToPublicField,mutableDependenciesBis,methodsOverridenByThisType,setters,
				// getters,getterTypes ]]) as map "
				////
				// ),
				// new SimpleWithClause("enclosingType, field,
				// method,typeDec,nodes as prevNodes"
				//
				// + ","
				// + new Extract("nodes",
				// "x+HEAD(" + new Filter("map", "y[0]=ID(x)",
				// "y").expToString() + ")[1]").expToString()
				// + " as nodes"
				//
				// ),
				// new SimpleWithClause("DISTINCT enclosingType, field, method,
				// typeDec,prevNodes, " + new Extract("nodes",
				// "[x[0],CASE WHEN x[1] IS NULL THEN FALSE ELSE x[1] END, CASE
				// WHEN x[2] IS NULL THEN [] ELSE x[2] END, CASE WHEN x[3] IS
				// NULL THEN [] ELSE x[3] END, CASE WHEN x[4] IS NULL THEN []
				// ELSE x[4] END,CASE WHEN x[5] IS NULL THEN [] ELSE x[5] END,
				// CASE WHEN x[6] IS NULL THEN [] ELSE x[6] END]")
				// .expToString()
				// + " as nodes"
				////
				// )
				// , new SimpleWithClause(
				// "enclosingType, field, method, typeDec,prevNodes, CASE WHEN
				// nodes IS NULL THEN [] ELSE nodes END as nodes "),
				//
				// new SimpleWithClause(
				// "enclosingType, field, method, typeDec,prevNodes, nodes,
				// RANGE(0,SIZE(nodes)-1,1) as indexes"),
				// new SimpleWithClause(
				// "enclosingType, field, method, typeDec,prevNodes, nodes,
				// indexes, EXTRACT(index IN indexes | CASE WHEN SIZE(FILTER(i
				// IN indexes WHERE i<index AND nodes[i][0]:ATTR_DEC))=0 THEN 0
				// ELSE LAST(FILTER(i IN indexes WHERE i<index AND
				// nodes[i][0]:ATTR_DEC)) END) as lastAttrIndexes"),
				//
				// new SimpleWithClause("enclosingType, field, method,
				// typeDec,prevNodes, nodes, " + new Extract("indexes",
				// new Reduce(new Filter("indexes", "i<index AND
				// i>=lastAttrIndexes[index]", "i").expToString(),
				// "s+nodes[x][3]", "s=[]").expToString()
				//
				// , "index").expToString() + " as overrides, indexes,
				// lastAttrIndexes"),
				// new SimpleWithClause("enclosingType, field, method,
				// typeDec,prevNodes, " + new Extract("indexes",
				// "[nodes[x][0], nodes[x][1] OR ANY(setter IN nodes[x][4] WHERE
				// NOT setter IN overrides[x]) "
				//
				// + ", nodes[x][2]+ EXTRACT( getterIndex IN FILTER(gf IN
				// RANGE(0,SIZE(nodes[x][5])-1,1) "
				// + "WHERE NOT nodes[x][5][gf] IN overrides[x]) |
				// nodes[x][6][getterIndex])] ")
				// .expToString()
				// + " as nodes, indexes"),
				//// new SimpleWithClause("enclosingType, field, method,
				// typeDec,p, " + new Extract("indexes",
				// // "[nodes[x][0], nodes[x][1]," + new Case("x>0 AND NOT
				// // nodes[x+1] IS NULL",
				// // "CASE WHEN nodes[x][0]:ATTR_DEC THEN nodes[x+1][0] IN
				// // nodes[x-1][2] ELSE TRUE END",
				//// "TRUE").expToString() + "] ").expToString()
				//// + " as nodes"),
				// new SimpleWithClause(
				// "enclosingType, field, method, typeDec,prevNodes,indexes, " +
				// new Extract("indexes",
				// "[nodes[x][0], nodes[x][1]," + new Case("x>1 AND
				// nodes[x-1][0]:ATTR_DEC",
				// " ANY(dep IN nodes[x-2][2] WHERE dep =nodes[x][0])",
				// "TRUE").expToString()
				// + "] ").expToString()
				// + " as nodes"),
				// new SimpleWithClause("DISTINCT enclosingType, field, method,
				// typeDec, "
				// + new Extract("indexes",
				// "[nodes[x][0], nodes[x][1],"
				// + "NOT ANY(i IN indexes WHERE i<=x AND NOT nodes[i][2])
				// ]").expToString()
				// + " as nodes ORDER BY typeDec"),
				// // new SimpleWithClause(
				// // "enclosingType, field, method,typeDec,p, CASE WHEN
				// // SIZE(nodes)=0 THEN []+typeDec ELSE nodes END as nodes "),
				// // new WhereClause("SIZE(nodes)=0"),
				// new SimpleWithClause(
				// "DISTINCT enclosingType, field, method, typeDec,"
				// + new Reduce("nodes",
				// "s AND CASE WHEN x[0]:ATTR_DEC THEN TRUE ELSE NOT x[2] OR
				// (NOT x[0]:ARRAY_TYPE AND NOT x[1]) END ",
				// "s=true")
				// .expToString()
				// + " AND NOT typeDec:ARRAY_TYPE as res"
				//
				// ),
				// new SimpleWithClause(
				// "enclosingType, field,method, typeDec,ANY (x IN COLLECT(res)
				// WHERE NOT x) as isMutable"),
				// new SimpleWithClause("enclosingType, field,COLLECT(method) as
				// methods, typeDec,isMutable"),
				//
				// new WhereClause("isMutable"),
				// // FILTRAR
				// // ,
				// new SimpleWithClause("COLLECT([enclosingType, field, methods,
				// typeDec]) as res"),
				// new UnwindClause("res", "typeRes"),
				// new SimpleWithClause(
				// "typeRes[1] as field, typeRes[3] as typeDec, typeRes[0] as
				// enclosingType,typeRes[2] as methods, res"),
				// new MatchClause(true,
				// new MatchImpl("(typeDec)<-[:IS_SUBTYPE_EXTENDS |
				// :IS_SUBTYPE_IMPLEMENTS*0..]-(subType)")),
				// new
				// SimpleWithClause("typeDec,methods,enclosingType,field,res,
				// COLLECT(subType) as subtypes"),
				// new SimpleWithClause(
				// "typeDec,EXTRACT(m IN methods | m.fullyQualifiedName) as
				// methods,enclosingType,field,res, FILTER(y IN subtypes WHERE
				// NOT y IN EXTRACT(x IN res | x[3])) as nonMutableSubTypes"),
				// new ReturnClause(
				// " 'Warning[OBJ-56] Field ' +field.name+' declared in line '
				// +field.lineNumber+' in class
				// '+enclosingType.fullyQualifiedName+' is not public, but it is
				// exposed in public methods such as '+ methods+'. The problem
				// is that the type '+typeDec.fullyQualifiedName+' (a posible
				// class of the field '+field.name+') can be mutated by a
				// malicious client.'+CASE WHEN SIZE(nonMutableSubTypes)=0 THEN
				// ' You should implement an appropiate inmutable subtype as a
				// wrapper for your attribute, as you have not created any
				// yet.'ELSE 'Remember to use an appropiate inmutable subtype
				// (such as '+EXTRACT(x IN nonMutableSubTypes |
				// x.fullyQualifiedName)+') as a wrapper for your attribute.'END
				// ")
				// new ReturnClause(
				// "enclosingType.fullyQualifiedName, field.name, method.name,
				// typeDec.fullyQualifiedName, "
				// + new Extract("nodesWithInfo", "x[0]").expToString()
				// + ",p, SIZE(NODES(p)), SIZE(nodesWithInfo)")

				// new ReturnClause(
				// "enclosingType.fullyQualifiedName, field.name, method.name,
				// typeDec.fullyQualifiedName, NODES(p) ")
		};
	}

	// WTIH ATTR, DECLARING TYPE
	// public static void main(String[] args) {
	// System.out.println(new OBJ56_SIMPLIFIED().queryToString());
	// }
}

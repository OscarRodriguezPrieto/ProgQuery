package database.querys;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import database.DatabaseFachade;
import database.relations.RelationTypes;

public class CMUQueries {

	private static final RelationTypes[] assignToOutExpr = new RelationTypes[] { RelationTypes.ARRAYACCESS_EXPR,
			RelationTypes.ARRAYACCESS_INDEX, RelationTypes.ASSIGNMENT_LHS, RelationTypes.ASSIGNMENT_RHS,
			RelationTypes.BINOP_LHS, RelationTypes.BINOP_RHS, RelationTypes.CAST_ENCLOSES,
			RelationTypes.CONDITIONAL_EXPR_CONDITION, RelationTypes.CONDITIONAL_EXPR_THEN,
			RelationTypes.CONDITIONAL_EXPR_ELSE, RelationTypes.INITIALIZATION_EXPR, RelationTypes.INSTANCEOF_EXPR,
			RelationTypes.MEMBER_REFERENCE_EXPRESSION, RelationTypes.MEMBER_SELECT_EXPR,
			RelationTypes.METHODINVOCATION_ARGUMENTS, RelationTypes.METHODINVOCATION_METHOD_SELECT,
			RelationTypes.NEW_CLASS_ARGUMENTS, RelationTypes.NEWARRAY_INIT, RelationTypes.NEWARRAY_DIMENSION,
			RelationTypes.UNARY_ENCLOSES };
	private static final String assignToOutExprQuery = getAnyRel(assignToOutExpr);
	// NO RETURN FOR THIS PROBLEM
	private static final RelationTypes[] exprToStat = new RelationTypes[] { RelationTypes.ASSERT_CONDITION,
			RelationTypes.DOWHILE_CONDITION, RelationTypes.ENCLOSES_EXPR, RelationTypes.FOREACH_EXPR,
			RelationTypes.FORLOOP_CONDITION, RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_CONDITION,
			RelationTypes.SWITCH_EXPR, RelationTypes.SYNCHRONIZED_EXPR, RelationTypes.THROW_EXPR,
			RelationTypes.WHILE_CONDITION };

	private static final String exprToStatQuery = getAnyRel(exprToStat);

	public static String getAnyRel(RelationTypes[] rels) {
		String ret = "";
		for (RelationTypes r : rels)
			ret += ":" + r.name() + " | ";
		return ret.substring(0, ret.length() - 2);
	}

	private static final String PREV_ERR54_USE_TRY_RESOURCES_TO_SAFELY_CLOSE = " MATCH (closeableSubtype)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->(closeableInt:INTERFACE_DECLARATION{fullyQualifiedName:'java.lang.AutoCloseable'})"
			+ " WITH DISTINCT closeableSubtype.fullyQualifiedName as className "

			+ " MATCH (mInv:METHOD_INVOCATION)-[:METHODINVOCATION_METHOD_SELECT]->(mSelect:MEMBER_SELECTION{memberName:'close'})-[:MEMBER_SELECT_EXPR]->(id)<-[:USED_BY]-(closeableDec{actualType:className})-[:MODIFIED_BY]->(assign)"

			+ ", (assign)<-[" + assignToOutExprQuery + "*0..]-(expr)<-[" + exprToStatQuery + "]-(stat) "

			+ " RETURN  assign ,stat, id, mSelect, mInv";

	private static final String ERR54_USE_TRY_RESOURCES_TO_SAFELY_CLOSE = " MATCH (closeableSubtype)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->(closeableInt:INTERFACE_DECLARATION{fullyQualifiedName:'java.lang.AutoCloseable'})"
			+ " WITH DISTINCT closeableSubtype.fullyQualifiedName as className "

			+ " MATCH (mInv:METHOD_INVOCATION)-[:METHODINVOCATION_METHOD_SELECT]->(mSelect:MEMBER_SELECTION{memberName:'close'})-[:MEMBER_SELECT_EXPR]->(id)<-[:USED_BY]-(closeableDec{actualType:className})"

			+ " WITH id, closeableDec"

			+ " MATCH (closeableDec)-[:MODIFIED_BY]->(assign)<-[" + assignToOutExprQuery + "*0..]-(expr)<-["
			+ exprToStatQuery
			+ "]-(stat) "
			+ " WITH COLLECT(assign) as ass, id, closeableDec"


			+ " RETURN  closeableDec, id, ass";
	private static final String ALT2_MET55_RETURN_EMPTY_COLLECTIONS_INSTEAD_NULL = " MATCH (returnClass)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->(collection{fullyQualifiedName:'java.util.Collection'})"
			+ " WITH DISTINCT returnClass.fullyQualifiedName as className "
			+ " MATCH (enclosingCU)-[:HAS_TYPE_DEC | :HAS_INDIRECT_TYPE_DEC]->()-[:DECLARES_METHOD]->(md)-[:HAS_METHODDECL_RETURNS]->(rt) "
			+ ", (md)<-[*]-(ret:RETURN_STATEMENT)" + "" + " RETURN md, rt, labels(rt)";
	private static final String MET55_RETURN_EMPTY_COLLECTIONS_INSTEAD_NULL = " MATCH (returnClass)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->(collection{fullyQualifiedName:'java.util.Collection'})"
			+ " WITH DISTINCT returnClass.fullyQualifiedName as className "
			+ " MATCH (md)-[:HAS_METHODDECL_RETURNS]->(rt)" + " WHERE rt.actualType=className OR rt:ARRAY_TYPE"
			+ " WITH DISTINCT md "
			+ " MATCH (enclosingCU)-[:HAS_TYPE_DEC | :HAS_INDIRECT_TYPE_DEC]->()-[:DECLARES_METHOD]->(md)<-[:CFG_END_OF]-(normalEnd)<-[:CFG_NEXT_STATEMENT]-(return:RETURN_STATEMENT)-[:RETURN_EXPR]->(retExpr) "
			+ " OPTIONAL MATCH (retExpr)-[:CONDITIONAL_EXPR_THEN | :CONDITIONAL_EXPR_ELSE*]->(auxRet{typetag:'NULL_LITERAL'}) "
			+ " WHERE auxRet IS NOT NULL OR  retExpr.typetag='NULL_LITERAL'"
			+ " RETURN 'Warning [CMU-MET55], you must not return null when you can return an empty collection or array. Line ' + retExpr.lineNumber+ ' in '+  enclosingCU.fileName+'.' ";

	// Dos formas -[*]-> RETURN o CFG_END<-[]- RETURN
	private static final String ALT_MET55_RETURN_EMPTY_COLLECTIONS_INSTEAD_NULL = " MATCH (enclosingCU)-[:HAS_TYPE_DEC | :HAS_INDIRECT_TYPE_DEC]->()-[:DECLARES_METHOD]->(md)-[:HAS_METHODDECL_RETURNS]->(rt) "
			+ " WITH md, rt "
			+ " MATCH (returnClass{fullyQualifiedName:rt.actualType})-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->(collection{fullyQualifiedName:'java.util.Collection'})"

			+ " RETURN DISTINCT md";

	private static final String SEC56_DONT_SERIALIZE_SYSTEM_RESOURCES = "MATCH (enclosingCU)-[:HAS_TYPE_DEC | :HAS_INDIRECT_TYPE_DEC]->(class)-[:DECLARES_FIELD]->(f{isTransient:false,actualType:'java.io.File' })"
			+ ", (class)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]-> (superInt:INTERFACE_DECLARATION{fullyQualifiedName:'java.io.Serializable'})  "
			+ " RETURN 'Warning [CMU-SEC56], you must not serialize direct handles to system resources like field ' + f.name + '. Line '+ f.lineNumber + ' in '+ enclosingCU.fileName +'.'";

	private static final String DECL56_ORDINAL_ENUM = "MATCH (enclosingCU)-[:HAS_TYPE_DEC | :HAS_INDIRECT_TYPE_DEC]->()-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(enclosingM)-[:CALLS]->"
			+ "(inv)-[:HAS_DEC]->(md) " + "WHERE md.fullyQualifiedName ='java.lang.Enum:ordinal()int' "
			+ "RETURN 'Warning [CMU-DEC56], you should not attach significance to the ordinal of an enum. Line '+inv.lineNumber + ' in '+ enclosingCU.fileName +'.' ";

	private static final String MET50_AVOID_CONFUSING_OVERLOADING = "MATCH (enclosingCU)-[:HAS_TYPE_DEC | :HAS_INDIRECT_TYPE_DEC]->(class)"
			+ "-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(md)-[:HAS_METHODDECL_PARAMETERS]->(p:PARAMETER_DEC)" + " "
			+ "WITH enclosingCU, md, class, COLLECT(p.actualType) as params  "
			+ " MATCH (class)-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(md2)-[r:HAS_METHODDECL_PARAMETERS]->(p2:PARAMETER_DEC)"
			+ " WHERE md.name = md2.name AND NOT md = md2 "
			+ " WITH  enclosingCU, md, md2, params, COLLECT(p2.actualType) as params2 "
			+ "WHERE ALL(p IN params WHERE p IN params2) AND all(p IN params2 WHERE "
			+ "p IN params) AND ((SIZE(params)>=4 AND SIZE(params2)>=4) OR " + " SIZE(params)=SIZE(params2)) "
			+ " RETURN 'Warning [CMU-MET50], you must avoid confusing overloadings like '+ md.fullyQualifiedName + 'in line ' + md.lineNumber + ' ( very similar to declaration in line ' + md2.lineNumber +')j in'+ enclosingCU.fileName +'.' ";
	private static final String MET53_ENSURE_CALL_SUPER_IN_CLONE = "MATCH (enclosingCU)-[:HAS_TYPE_DEC | :HAS_INDIRECT_TYPE_DEC]->()-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(md) "
			+ " WHERE  md.fullyQualifiedName CONTAINS ':clone()' AND NOT md.fullyQualifiedName ENDS WITH ')void' "

			+ " OPTIONAL MATCH (md)-[:CALLS]->()-[:HAS_DEC]->(superDec)"
			+ " WHERE  superDec.fullyQualifiedName CONTAINS ':clone()' AND NOT superDec.fullyQualifiedName ENDS WITH ')void'"
			+ " WITH enclosingCU, md, COUNT(superDec) as superCallsCount" + " WHERE superCallsCount=0 "
			+ " RETURN  'Warning [CMU-MET53], you must call super.clone in every overriden clone method. Line '+ md.lineNumber + ' in ' + enclosingCU.fileName + '.'";
	private static final String ALTERN_MET53_ENSURE_CALL_SUPER_IN_CLONE = "MATCH (enclosingCU)-[:HAS_TYPE_DEC | :HAS_INDIRECT_TYPE_DEC]->()-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(md) "
			+ " WHERE  md.fullyQualifiedName CONTAINS ':clone()' AND NOT md.fullyQualifiedName ENDS WITH ')void' "

			+ " OPTIONAL MATCH (md)-[:CALLS]->()-[:HAS_DEC]->(superDec)<-[:OVERRIDES]-(md)"

			+ " WITH enclosingCU, md, COUNT(superDec) as superCallsCount" + " WHERE superCallsCount=0 "
			+ " RETURN  'Warning [CMU-MET53], you must call super.clone in every overriden clone method. Line '+ md.lineNumber + ' in ' + enclosingCU.fileName + '.'";

	public static void main(String[] args) throws IOException {
		System.out.println(ERR54_USE_TRY_RESOURCES_TO_SAFELY_CLOSE);
		// int queryIndex = args.length == 0 ? 4 : Integer.parseInt(args[0]);
		GraphDatabaseService gs = DatabaseFachade.getDB();
		// String query = QUERIES[queryIndex];
		long ini = System.nanoTime();
		Result res = gs.execute(ERR54_USE_TRY_RESOURCES_TO_SAFELY_CLOSE);
		long end = System.nanoTime();
		res.toString().length();
		System.out.println(res.resultAsString());
		System.out.print((end - ini) / 1000_000);
		// gs.execute(MainQuery.DELETE_ALL);
	}
}

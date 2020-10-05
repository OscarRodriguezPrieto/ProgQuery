package database.querys.eval;

import static java.util.Arrays.asList;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.kernel.lifecycle.LifecycleException;
import org.neo4j.test.TestGraphDatabaseFactory;

import database.embedded.EmbeddedDBManager;
import database.procedures.AnySucc;
import database.procedures.EnclosingStmt;
import database.procedures.FunctionUtils;
import database.relations.CFGRelationTypes;
import database.relations.RelationTypes;
import database.relations.RelationTypesInterface;
import evaluation.Rule;

public class QueriesWithProcedures {
	private static final RelationTypes[] assignToOutExprNoCond = new RelationTypes[] { RelationTypes.ARRAYACCESS_EXPR,
			RelationTypes.ARRAYACCESS_INDEX, RelationTypes.ASSIGNMENT_RHS, RelationTypes.BINOP_LHS,
			RelationTypes.BINOP_RHS, RelationTypes.CAST_ENCLOSES, RelationTypes.COMPOUND_ASSIGNMENT_RHS,
			RelationTypes.CONDITIONAL_EXPR_CONDITION, RelationTypes.INITIALIZATION_EXPR, RelationTypes.INSTANCE_OF_EXPR,
			RelationTypes.MEMBER_REFERENCE_EXPRESSION, RelationTypes.MEMBER_SELECT_EXPR,
			RelationTypes.METHODINVOCATION_ARGUMENTS, RelationTypes.METHODINVOCATION_METHOD_SELECT,
			RelationTypes.NEW_CLASS_ARGUMENTS, RelationTypes.NEW_ARRAY_INIT, RelationTypes.NEW_ARRAY_DIMENSION,
			RelationTypes.UNARY_ENCLOSES };
	private static final RelationTypes[] assignToOutExpr = new RelationTypes[] { RelationTypes.ARRAYACCESS_EXPR,
			RelationTypes.ARRAYACCESS_INDEX, RelationTypes.ASSIGNMENT_RHS, RelationTypes.BINOP_LHS,
			RelationTypes.BINOP_RHS, RelationTypes.BINOP_COND_RHS, RelationTypes.CAST_ENCLOSES,
			RelationTypes.COMPOUND_ASSIGNMENT_RHS, RelationTypes.CONDITIONAL_EXPR_CONDITION,
			RelationTypes.CONDITIONAL_EXPR_THEN, RelationTypes.CONDITIONAL_EXPR_ELSE, RelationTypes.INITIALIZATION_EXPR,
			RelationTypes.INSTANCE_OF_EXPR, RelationTypes.MEMBER_REFERENCE_EXPRESSION, RelationTypes.MEMBER_SELECT_EXPR,
			RelationTypes.METHODINVOCATION_ARGUMENTS, RelationTypes.METHODINVOCATION_METHOD_SELECT,
			RelationTypes.NEW_CLASS_ARGUMENTS, RelationTypes.NEW_ARRAY_INIT, RelationTypes.NEW_ARRAY_DIMENSION,
			RelationTypes.UNARY_ENCLOSES };
	private static final String assignToOutExprQuery = getAnyRel(assignToOutExpr);
	private static final RelationTypes[] exprToOutExpr = new RelationTypes[] { RelationTypes.ARRAYACCESS_EXPR,
			RelationTypes.ARRAYACCESS_INDEX, RelationTypes.ASSIGNMENT_LHS, RelationTypes.ASSIGNMENT_RHS,
			RelationTypes.BINOP_LHS, RelationTypes.BINOP_RHS, RelationTypes.BINOP_COND_RHS, RelationTypes.CAST_ENCLOSES,
			RelationTypes.COMPOUND_ASSIGNMENT_LHS, RelationTypes.COMPOUND_ASSIGNMENT_RHS,
			RelationTypes.CONDITIONAL_EXPR_CONDITION, RelationTypes.CONDITIONAL_EXPR_THEN,
			RelationTypes.CONDITIONAL_EXPR_ELSE, RelationTypes.INITIALIZATION_EXPR, RelationTypes.INSTANCE_OF_EXPR,
			RelationTypes.MEMBER_REFERENCE_EXPRESSION, RelationTypes.MEMBER_SELECT_EXPR,
			RelationTypes.METHODINVOCATION_ARGUMENTS, RelationTypes.METHODINVOCATION_METHOD_SELECT,
			RelationTypes.NEW_CLASS_ARGUMENTS, RelationTypes.NEW_ARRAY_INIT, RelationTypes.NEW_ARRAY_DIMENSION,
			RelationTypes.UNARY_ENCLOSES };

	private static final String exprToOutExprQuery = getAnyRel(exprToOutExpr);
	private static final RelationTypes[] statToEnclClass = new RelationTypes[] { RelationTypes.CASE_STATEMENTS,
			 RelationTypes.CATCH_PARAM, RelationTypes.DECLARES_FIELD,
			RelationTypes.DECLARES_METHOD, RelationTypes.DECLARES_CONSTRUCTOR, RelationTypes.ENCLOSES,
			RelationTypes.HAS_ENUM_ELEMENT, RelationTypes.FOREACH_STATEMENT, RelationTypes.FOREACH_VAR,
			RelationTypes.FORLOOP_INIT, RelationTypes.FORLOOP_STATEMENT, RelationTypes.FORLOOP_UPDATE,
			RelationTypes.CALLABLE_HAS_BODY, RelationTypes.CALLABLE_HAS_PARAMETER, RelationTypes.HAS_STATIC_INIT,
			RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_THEN, RelationTypes.IF_ELSE,
			RelationTypes.LABELED_STMT_ENCLOSES, RelationTypes.SWITCH_ENCLOSES_CASE, RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK,
			RelationTypes.TRY_BLOCK, RelationTypes.TRY_CATCH, RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES };
	private static final RelationTypes[] statToOuterBlock = new RelationTypes[] { RelationTypes.CASE_STATEMENTS,
	 RelationTypes.CATCH_PARAM, RelationTypes.ENCLOSES,
			RelationTypes.FOREACH_STATEMENT, RelationTypes.FOREACH_VAR, RelationTypes.FORLOOP_INIT,
			RelationTypes.FORLOOP_STATEMENT, RelationTypes.FORLOOP_UPDATE, RelationTypes.CALLABLE_HAS_PARAMETER,
			RelationTypes.HAS_STATIC_INIT, RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_THEN,
			RelationTypes.IF_ELSE, RelationTypes.LABELED_STMT_ENCLOSES, RelationTypes.SWITCH_ENCLOSES_CASE,
			RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK, RelationTypes.TRY_BLOCK, RelationTypes.TRY_CATCH,
			RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES };
	private static final RelationTypes[] exprToStat = new RelationTypes[] { RelationTypes.ASSERT_CONDITION,
			RelationTypes.DO_WHILE_CONDITION, RelationTypes.ENCLOSES_EXPR, RelationTypes.FOREACH_EXPR,
			RelationTypes.FORLOOP_CONDITION, RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_CONDITION,
			RelationTypes.SWITCH_EXPR, RelationTypes.SYNCHRONIZED_EXPR, RelationTypes.THROW_EXPR,
			RelationTypes.WHILE_CONDITION, RelationTypes.RETURN_EXPR };
	private static final String exprToStatQuery = getAnyRel(exprToStat);
	private static final String exprToStatQueryWithReturn = getAnyRel(exprToStat) + " | :RETURN_EXPR ";

	private static final CFGRelationTypes[] cfgUnconditionalSucc = new CFGRelationTypes[] {
			CFGRelationTypes.CFG_NEXT_STATEMENT, CFGRelationTypes.CFG_NO_EXCEPTION, CFGRelationTypes.CFG_THROWS };

	private static final CFGRelationTypes[] toCFGSuccesor = new CFGRelationTypes[] {
			CFGRelationTypes.CFG_NEXT_STATEMENT, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE,
			CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT,
			CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS, CFGRelationTypes.CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION,
			CFGRelationTypes.CFG_NO_EXCEPTION, CFGRelationTypes.CFG_CAUGHT_EXCEPTION,
			CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_BREAK, CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_CONTINUE,
			CFGRelationTypes.CFG_SWITCH_CASE_IS_EQUAL_TO, CFGRelationTypes.CFG_SWITCH_DEFAULT_CASE, CFGRelationTypes.CFG_MAY_THROW,
			CFGRelationTypes.CFG_THROWS };
	private static final CFGRelationTypes[] toCFGSuccesorNoEx = new CFGRelationTypes[] {
			CFGRelationTypes.CFG_NEXT_STATEMENT, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE,
			CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT,
			CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS, CFGRelationTypes.CFG_NO_EXCEPTION,
			CFGRelationTypes.CFG_CAUGHT_EXCEPTION, CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_BREAK,
			CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_CONTINUE, CFGRelationTypes.CFG_SWITCH_CASE_IS_EQUAL_TO,
			CFGRelationTypes.CFG_SWITCH_DEFAULT_CASE };
	private static final CFGRelationTypes[] toCFGSuccesorNoCondEx = new CFGRelationTypes[] {
			CFGRelationTypes.CFG_NEXT_STATEMENT, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE,
			CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT,
			CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS, CFGRelationTypes.CFG_NO_EXCEPTION,
			CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_BREAK, CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_CONTINUE,
			CFGRelationTypes.CFG_SWITCH_CASE_IS_EQUAL_TO, CFGRelationTypes.CFG_SWITCH_DEFAULT_CASE, CFGRelationTypes.CFG_THROWS };
	private static final String cfgSuccesor = getAnyRel(toCFGSuccesor);

	public static String getAnyRel(RelationTypesInterface[] rels) {
		String ret = "";
		for (RelationTypesInterface r : rels)
			ret += ":" + r.toString() + " | ";
		return ret.substring(0, ret.length() - 2);
	}

	public QueriesWithProcedures() {
		// TODO Auto-generated constructor stub
	}

	// public void registerProcedures(List<Class<?>> toRegister) {
	// EmbeddedDriver embeddedDriver = (EmbeddedDriver) Components.driver();
	// GraphDatabaseService databaseService =
	// embeddedDriver.getGraphDatabaseService();
	// Procedures procedures = ((GraphDatabaseAPI)
	// databaseService).getDependencyResolver().resolveDependency(Procedures.class);
	// toRegister.forEach((proc) -> {
	// try {
	// procedures.registerProcedure(proc);
	// } catch (KernelException e) {
	// throw new RuntimeException("Error registering " + proc, e);
	// }
	// });
	//
	// }
	private static GraphDatabaseService createInMemoryDatabase() throws Throwable {
		try {
			Map<String, String> config = MapUtil.stringMap("dbms.transaction.timeout", "2s", "mapped_memory_total_size",
					"5M", "dbms.pagecache.memory", "5M", "keep_logical_logs", "false", "cache_type", "none",
					"query_cache_size", "15");
			GraphDatabaseService db = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().setConfig(config)
					.newGraphDatabase();
			Procedures procedures = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(Procedures.class);
			List<Class<?>> apocProcedures = asList(FunctionUtils.class);
			apocProcedures.forEach((proc) -> {
				try {
					procedures.registerFunction(proc);
				} catch (KernelException e) {
					throw new RuntimeException("Error registering " + proc, e);
				}
			});
			return db;
		} catch (Throwable re) {
			Throwable t = re.getCause();
			if (re instanceof LifecycleException || t instanceof LifecycleException || t instanceof Error
					|| re instanceof Error) {
				re.printStackTrace();
				throw new IllegalStateException("Lifecycle Exception during creation of database " + re.getMessage());
			}
			if (t instanceof RuntimeException)
				throw (RuntimeException) t;
			if (t instanceof Error)
				throw (Error) t;
			throw t;
		}
	}
	private static final String[] REDUCED = {
			"MATCH (varDec)-[:MODIFIED_BY]->(ass:ASSIGNMENT)-[:ASSIGNMENT_RHS]->(:LITERAL{typeKind:'NULL'})\r\n"
					+ " WHERE varDec:VAR_DEC OR varDec:PARAMETER_DEC\r\n"
					+ "OPTIONAL MATCH (varDec)-[:USED_BY|STATE_MODIFIED_BY]->(use)"
					+ "WITH varDec,examples.getEnclosingStmt(ass) as assStat, COLLECT(examples.getEnclosingStmt(use)) as useStats\r\n"
				
					+ " WHERE SIZE(FILTER( succ IN examples.getAnySucc(assStat) WHERE succ IN useStats))=0\r\n"
					+ "RETURN "
					+ "'Warning [CMU-OBJ54] You must not try to help garbage collector setting references to null when they are no longer used. To make your code clearer, just delete the assignment in line ' + assStat.lineNumber + ' of the variable ' +varDec.name+ ' declared in class '+examples.getEnclosingClass(varDec).fullyQualifiedName+'.'\r\n"
					+ "\r\n" + "",
			"MATCH (declaration{isFinal:true})-[r:STATE_MODIFIED_BY|STATE_MAY_BE_MODIFIED_BY]->(mutatorExpr)\r\n"
					+ " WHERE NOT declaration:THIS_REF AND NOT mutatorExpr:INITIALIZATION AND (r.isInit IS NULL OR NOT r.isInit) \r\n"
				+ "WITH declaration,examples.getEnclosingClass(declaration) as enclClass,COLLECT(HEAD(LABELS(mutatorExpr))+', line '+mutatorExpr.lineNumber+ ', class '+ examples.getEnclosingClass(examples.getEnclosingStmt(mutatorExpr)).fullyQualifiedName) as mutatorsMessage\r\n"
					+ "RETURN 'Warning [CMU-OBJ50] Declaration with name '+ declaration.name+ ' in line ' + declaration.lineNumber +', class ' +enclClass.fullyQualifiedName + 'is not actually final, only the reference. Concretely, '+ declaration.name +' may be mutated in '+mutatorsMessage\r\n"
					+ "\r\n",

			" MATCH (closeableSubtype)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*0..]->(closeableInt:INTERFACE_DECLARATION{fullyQualifiedName:'java.lang.AutoCloseable'})"
					+ " WHERE closeableSubtype:CLASS_DECLARATION OR closeableSubtype:INTERFACE_DECLARATION "
					+ " WITH DISTINCT closeableSubtype.fullyQualifiedName as className "

					+ " MATCH (closeableDec{actualType:className})-[:MODIFIED_BY]->(assign)"
				
					+ " WHERE  closeableDec:VAR_DEC "
					+ " OPTIONAL MATCH (closeableDec)<-[r:TRY_RESOURCES]-()  WITH examples.getEnclosingStmt(assign) as assignStat,r, closeableDec WHERE r IS NULL"
				
					+ " UNWIND examples.getAnySuccNotItself(assignStat) as prev "
					+ " OPTIONAL MATCH (mInv:METHOD_INVOCATION)-[:METHODINVOCATION_METHOD_SELECT]->(mSelect:MEMBER_SELECTION{memberName:'close'})-[:MEMBER_SELECT_EXPR]->(id)<-[:USED_BY]-(closeableDec)"

					+ ",(prev)-[exceptionRel:CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION | :CFG_MAY_THROW | :CFG_THROWS]->(afterEx) "
					+ " WITH  COLLECT(DISTINCT examples.getEnclosingStmt(mInv)) AS closes, prev,closeableDec, afterEx,exceptionRel "
					+ "WHERE NOT prev IN closes AND ANY(prevSucc IN examples.getAnySuccNotItself(prev) WHERE prevSucc IN closes) "
	

					+ " OPTIONAL MATCH p=(afterEx)-[" + getAnyRel(toCFGSuccesor) // ESTE
																					// DEBERíA
																					// SER
																					// COND
																					// O
																					// NO
																					// COND
					+ "*0..]->(reachableAfterEx) WHERE reachableAfterEx IN closes "
					+ " WITH prev as prevs, closeableDec,p,exceptionRel, " + " CASE WHEN p IS NULL THEN NULL ELSE "
					+ " EXTRACT (index IN RANGE(0,SIZE(NODES(p))) | index=0 OR TYPE(RELATIONSHIPS(p)[index-1]) IN ['CFG_THROWS' ,'CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION' ,'CFG_MAY_THROW' ])"
					+ " END as previousThrow" + " WITH prevs, closeableDec,p,exceptionRel, "
					+ "CASE WHEN p IS NULL THEN NULL ELSE EXTRACT (index IN RANGE(0,SIZE(NODES(p))) | CASE WHEN  NODES(p)[index]:CATCH_BLOCK OR previousThrow[index] AND NODES(p)[index]:VAR_DEC   THEN 'catch' ELSE CASE WHEN previousThrow[index] THEN  CASE WHEN index=0 THEN exceptionRel.exceptionType ELSE RELATIONSHIPS(p)[index-1].exceptionType END ELSE CASE WHEN NODES(p)[index]:TRY_BLOCK THEN 'newtry' ELSE  NULL END END END )END"
					+ " as exFlow"
					+ " WITH p,closeableDec,prevs,CASE WHEN p IS NULL THEN NULL ELSE EXTRACT( relIndex IN RANGE(0,SIZE(RELATIONSHIPS(p))) | exFlow[LAST(FILTER( exIndex IN RANGE(0,SIZE(exFlow)) WHERE exIndex<=relIndex AND NOT exFlow[exIndex] IS NULL))]) END as exFlow "
					+ " WITH closeableDec,prevs,"
					+ " NOT ANY(x IN COLLECT(CASE WHEN p IS NULL THEN FALSE ELSE ALL(relIndex IN RANGE(0,SIZE(RELATIONSHIPS(p))) WHERE CASE WHEN TYPE(RELATIONSHIPS(p)[relIndex])='CFG_NO_EXCEPTION' THEN exFlow[relIndex] IN ['catch' , 'newtry'] ELSE CASE WHEN TYPE(RELATIONSHIPS(p)[relIndex])='CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION' THEN exFlow[relIndex]=RELATIONSHIPS(p)[relIndex].exceptionType ELSE TRUE END END  )END)WHERE x) as truePathToClose"
					+ " WITH closeableDec,COLLECT(prevs) as prevs, COLLECT(truePathToClose) as truePathToClose"
					+ " WHERE ANY ( x IN truePathToClose WHERE x) "
				
					+ "  RETURN  'Warning [CMU-ERR54] variable '+closeableDec.name+ '(defined in line'+closeableDec.lineNumber+', class '+examples.getEnclosingClass(closeableDec).fullyQualifiedName+') might not be properly closed, as statement(s) (in lines '+ EXTRACT(prev IN prevs | prev.lineNumber)+') may throw an exception.'",

			" MATCH (typeDec)-[:DECLARES_FIELD]->(attr:ATTR_DEC) "
					+ " WHERE NOT (attr.accessLevel='public' OR attr.accessLevel='protected' AND NOT typeDec.isFinal) AND "
					+ " NOT( attr.isStatic AND attr.actualType='long' AND attr.isFinal AND attr.name='serialVersionUID') "

					+ " OPTIONAL MATCH (attr)-[:USED_BY]->(exprUse)"
					
					+ "WITH typeDec.fullyQualifiedName as className, attr,examples.getEnclosingStmt(exprUse) as exprUseStat , exprUse"
					+ " OPTIONAL MATCH (attr)-[:MODIFIED_BY]->(modif)"
				
					+ "-[:ASSIGNMENT_LHS]->(lhs_expr) WHERE  examples.getEnclosingMethod(examples.getEnclosingStmt(modif))=examples.getEnclosingMethod(exprUseStat) AND examples.getEnclosingStmt(modif).position < exprUseStat.position "
					+ " OPTIONAL MATCH (exprUse)-[:MEMBER_SELECT_EXPR]->(memberSelectExprUse)<-[:USED_BY]-(varDec) "
					+ " OPTIONAL MATCH p=(varDec)-[:STATE_MODIFIED_BY]->(modif)"
					+ " OPTIONAL MATCH (lhs_expr)-[:MEMBER_SELECT_EXPR]->(memberSelectExprModif) "
					+ " WITH p,className, attr.lineNumber as line, attr.name as attr,exprUse, modif,lhs_expr, memberSelectExprUse, memberSelectExprModif, exprUseStat,examples.getEnclosingStmt(modif) as exprModStat "
					+ " OPTIONAL MATCH q=(exprModStat)<-[" + getAnyRel(statToOuterBlock)
					+ "*0..]-(minimumCommonBlock), (minimumCommonBlock)-[" + getAnyRel(statToOuterBlock)
					+ "*0..]->(exprUseStat) " + " WITH className, attr, exprUse,line, " + " ANY(x IN " + "COLLECT("
					+ "  " + "( "
					+ " NOT ANY(rel IN RELS(q) WHERE type(rel)='IF_ELSE' OR type(rel)='FORLOOP_UPDATE' OR type(rel)='FORLOOP_STATEMENT'  OR  type(rel)='FOREACH_STATEMENT' OR type(rel)='TRY_CATCH' OR type(rel)='SWITCH_ENCLOSES_CASES' OR type(rel)='IF_THEN') "
					+ " AND ("
					+ " (exprUse:IDENTIFIER OR (NOT memberSelectExprUse IS NULL AND memberSelectExprUse:IDENTIFIER AND (memberSelectExprUse.name='this' OR memberSelectExprUse.name='super')))"
					+ " AND (lhs_expr:IDENTIFIER OR (NOT memberSelectExprModif IS NULL AND memberSelectExprModif:IDENTIFIER  AND (memberSelectExprModif.name='this' OR memberSelectExprModif.name='super')))"
					+ " ) " + " OR "
					+ " (p IS NOT NULL AND NOT memberSelectExprUse IS NULL AND NOT memberSelectExprModif IS NULL AND memberSelectExprUse:IDENTIFIER AND memberSelectExprModif:IDENTIFIER)"
					+ ")" + ") " + " WHERE x) as useWithModif "
					+ " WITH line,className, attr , ALL( x IN COLLECT(useWithModif) WHERE x) OR exprUse IS NULL as isSillyAttr WHERE isSillyAttr"
					+ " RETURN 'Warning [CMU-DCL53] You must minimize the scope of the varaibles. You can minimize the scope of the attribute '+attr+'(declared in line '+line+') in class '+className + ' by transforming it into a local varaible (as everytime its value is used in a method, there is a previous unconditional assignment).' ORDER BY attr",

			"MATCH (enclosingType)-[:DECLARES_FIELD]->(field:ATTR_DEC)-[:USED_BY]->(retExpr)<-[:RETURN_EXPR]-(retStat)"
					+ " WITH enclosingType,field,examples.getEnclosingMethod(retStat) as method WHERE method:METHOD_DEC AND method.accessLevel='public' MATCH (method)"
					+ "<-[:DECLARES_METHOD]-(classExposingF{accessLevel:'public', isAbstract:false})\r\n"
					+ " WHERE NOT field.accessLevel='public' AND (NOT field.accessLevel='protected' OR classExposingF.isFinal)\r\n"
					+ "MATCH (field)-[:ITS_TYPE_IS]->(fieldType)<-[:IS_SUBTYPE_EXTENDS|IS_SUBTYPE_IMPLEMENTS*0..]-(fieldTypeOrSubtype),\r\n"
					+ "accessibleMembers=(fieldTypeOrSubtype)-[:DECLARES_FIELD|ITS_TYPE_IS|INHERITS_FIELD*0..]->(accessibleMember)\r\n"
					+ "WITH field,fieldType=fieldTypeOrSubtype as isFieldType,enclosingType, method,accessibleMember,fieldTypeOrSubtype, NODES(accessibleMembers) as accessibleMembers\r\n"
					+ "WITH field,isFieldType,enclosingType, accessibleMembers,accessibleMember,fieldTypeOrSubtype, COLLECT(DISTINCT method.fullyQualifiedName+'( line '+method.lineNumber+')') as publicGetters, EXTRACT(index IN RANGE(0,SIZE( accessibleMembers)-1,1) | [CASE WHEN index=0 THEN field ELSE accessibleMembers[index-1] END, accessibleMembers[index]]) as accessibleMembersAndPrevs\r\n"
					+ "WITH field,isFieldType,enclosingType, accessibleMembers,accessibleMember,fieldTypeOrSubtype, publicGetters,  accessibleMembersAndPrevs,LAST(accessibleMembersAndPrevs)[0] as accessibleMemberPrev\r\n"
					+ "UNWIND accessibleMembersAndPrevs as accMemberAndPrev\r\n"
					+ "OPTIONAL MATCH (accesibleField)-[:USED_BY]->(fieldExpr)<-[:RETURN_EXPR]-(returnStat),"
					+ "(method{accessLevel:'public'})<-[:DECLARES_METHOD|INHERITS_METHOD]-(accessibleType) WHERE examples.getEnclosingMethod(returnStat)=method "
						+ "\r\n"
					+ " AND ID(accesibleField)=ID(accMemberAndPrev[1]) AND ID(accessibleType)=ID(accMemberAndPrev[0])\r\n"
					+ "WITH  field,isFieldType,enclosingType, method,publicGetters,accMemberAndPrev as accMemberOrType, accessibleMembers, accessibleMember,accessibleMemberPrev,fieldTypeOrSubtype,  COLLECT(method)  as gettersForCurrentMember\r\n"
					+ "WITH field,isFieldType,enclosingType,fieldTypeOrSubtype,publicGetters,accessibleMember,accessibleMemberPrev, accessibleMembers, COLLECT( CASE WHEN accMemberOrType[1]:ATTR_DEC THEN NOT accMemberOrType[1].isStatic AND (accMemberOrType[1].accessLevel='public' OR SIZE(gettersForCurrentMember)>0) ELSE CASE WHEN accMemberOrType[1]:ARRAY_TYPE THEN TRUE ELSE EXISTS(accMemberOrType[1].accessLevel) AND accMemberOrType[1].accessLevel='public' END END) as isAccesible \r\n"
					+ "WITH DISTINCT field,isFieldType,fieldTypeOrSubtype, enclosingType, publicGetters,  accessibleMember,accessibleMemberPrev, ALL( isAccHere IN isAccesible WHERE isAccHere) as isExternallyAcc\r\n"
					+ "OPTIONAL MATCH (accessibleMember)-[:DECLARES_METHOD|INHERITS_METHOD]->(mutator:METHOD_DEC{accessLevel:'public'})<-[:STATE_MAY_BE_MODIFIED_BY|STATE_MODIFIED_BY]-(:THIS_REF)\r\n"
					+ "WITH field,isFieldType,fieldTypeOrSubtype,enclosingType, publicGetters,COLLECT(DISTINCT 'MUTATOR METHOD '+mutator.fullyQualifiedName+'( line '+mutator.lineNumber +')') as allMut, EXTRACT(accField IN FILTER(accMember IN COLLECT(DISTINCT accessibleMember) WHERE accMember:ATTR_DEC AND NOT accMember.isStatic AND NOT accMember.isFinal AND accMember.accessLevel='public') | 'PUBLIC NON-FINAL FIELD '+accField.name+'( line'+accField.lineNumber+')') as externallyMutableFields,FILTER(x IN COLLECT( DISTINCT [accessibleMember:ARRAY_TYPE AND  isExternallyAcc,'ACCESIBLE ARRAY FIELD ' + accessibleMemberPrev.name+' (line ' +accessibleMemberPrev.lineNumber+')']) WHERE x[0]) as arrayFieldsExposed\r\n"
					+ "WITH field,isFieldType,enclosingType,publicGetters,fieldTypeOrSubtype.fullyQualifiedName as fieldTypeOrSubtype,allMut,externallyMutableFields, arrayFieldsExposed, SIZE(allMut)>0 OR SIZE(externallyMutableFields)>0 OR SIZE(arrayFieldsExposed)>0 as isExtMutable\r\n"
					+ " WHERE CASE WHEN isFieldType THEN isExtMutable ELSE NOT isExtMutable END \r\n"
					+ "WITH field,enclosingType,publicGetters,EXTRACT(subtypeNameInfo IN FILTER(subtypeNameInfo IN COLLECT([isFieldType,fieldTypeOrSubtype]) WHERE NOT subtypeNameInfo[0])| subtypeNameInfo[1]) as immutableSubtypes,FILTER(subTypePair IN COLLECT([isFieldType, allMut+externallyMutableFields+arrayFieldsExposed]) WHERE subTypePair[0])[0][1] as mutabilityInfo\r\n"
					+ " WHERE NOT mutabilityInfo IS NULL\r\n"
					+ "RETURN 'Warning[OBJ-56] Field ' +field.name+' declared in line ' +field.lineNumber+' in class '+enclosingType.fullyQualifiedName+' is not public, but it is exposed in public methods such as '+ publicGetters+'. The problem is that there is at least one member ( like '+mutabilityInfo+')that can be accessed by a client to change the state of the field '+field.name + CASE WHEN SIZE(immutableSubtypes)=0 THEN '. You should implement an appropiate inmutable subtype as a wrapper for your attribute, as you have not created any yet.'ELSE '. Remember to use an appropiate inmutable subtype	 (such as '+immutableSubtypes+') as a wrapper for your attribute.'END\r\n"
					+ "\r\n" + "" };

	private static final String QUERY_EXAMPLE = "MATCH (variable:VARIABLE_DEF{isFinal:true})-[mutation:STATE_MODIFIED_BY|STATE_MAY_BE_MODIFIED_BY]->(mutatorExpr)\r\n"
			+ "WITH variable, mutation, mutatorExpr, database.procedures.getEnclMethodFromExpr(mutatorExpr) as mutatorMethod\r\n"
			+ "MATCH (mutatorMethod)<-[:DECLARES_METHOD|DECLARES_CONSTRUCTOR| HAS_STATIC_INIT]-(mutatorEnclClass)<-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]-(mutatorCU)\r\n"
			+ "WHERE NOT(variable:ATTR_DEF AND mutation.isOwnAccess AND mutatorMethod.isInitializer) \r\n"
			+ " WITH variable, database.procedures.getEnclosingClass(variable) as variableEnclClass, REDUCE(seed='', mutationWarn IN COLLECT( ' Line ' + mutatorExpr.lineNumber + ', column ' + mutatorExpr.column  +  ', file \\''+ mutatorCU.fileName + '\\'') | seed+'\\n'+ mutationWarn ) as mutatorsMessage\r\n"
			+ "MATCH (variableEnclClass)<-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]-(variableCU:COMPILATION_UNIT)\r\n"
			+ "RETURN 'Warning [CMU-OBJ50] The state of variable \\''+ variable.name + '\\'  (in line ' + variable.lineNumber +', file \\'' + variableCU.fileName + '\\') is mutated, but declared final. The state of, \\''+ variable.name +'\\' is mutated in :' + mutatorsMessage\r\n"

	;

	public static void main(String[] args) throws IOException {
		int queryIndex = args.length == 0 ? 7 : Integer.parseInt(args[0]);
		try {

			GraphDatabaseService gs = EmbeddedDBManager.getNewEmbeddedDBService();
			// EmbeddedDriver embeddedDriver = (EmbeddedDriver)
			// Components.driver();
			// GraphDatabaseService databaseService =
			// embeddedDriver.getGraphDatabaseService();
			for (Class c : new Class[] { EnclosingStmt.class, AnySucc.class })
				((GraphDatabaseAPI) gs).getDependencyResolver()
						.resolveDependency(Procedures.class, DependencyResolver.SelectionStrategy.FIRST)
						.registerFunction(c);
			// ((GraphDatabaseAPI)
			// gs).getDependencyResolver().resolveDependency(Join.class,null).register(Join.class);

			System.out.println(gs.getClass());
			// Rule rule = new Rule(" RETURN examples.join(['y','o'],',')");
			Rule rule = new Rule(QUERY_EXAMPLE
	
			);
			if (args.length == 0)
				System.out.println(rule.queries[0]);
			long ini = System.nanoTime();
			String res = rule.execute(gs).resultAsString();
			long end = System.nanoTime();
			System.err.println(res);
			System.out.print((end - ini) / 1000_000);
			if (args.length == 0) {
				int size = 0;

			}
		} catch (Throwable t) {
			t.printStackTrace(new PrintStream(new FileOutputStream("err" + queryIndex + ".txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter("out.txt"));
			// bw.write(RULES[queryIndex].queries[0]);
			bw.close();
			System.out.println("-1");
		}
	}
}

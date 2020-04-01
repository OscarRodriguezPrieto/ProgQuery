package database.querys.eval;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import database.embedded.EmbeddedDBManager;
import database.querys.cypherWrapper.Any;
import database.querys.cypherWrapper.Filter;
import database.querys.cypherWrapper.Reduce;
import database.querys.cypherWrapper.cmu.pq.OBJ50;
import database.querys.cypherWrapper.cmu.pq.OBJ54;
import database.querys.cypherWrapper.cmu.pq.OBJ56_SIMPLIFIED;
import database.relations.CFGRelationTypes;
import database.relations.RelationTypes;
import database.relations.RelationTypesInterface;
import evaluation.Rule;

public class CMUQueries {
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
			RelationTypes.CATCH_ENCLOSES_BLOCK, RelationTypes.CATCH_PARAM, RelationTypes.DECLARES_FIELD,
			RelationTypes.DECLARES_METHOD, RelationTypes.DECLARES_CONSTRUCTOR, RelationTypes.ENCLOSES,
			RelationTypes.DO_WHILE_STATEMENT, RelationTypes.WHILE_STATEMENT, RelationTypes.HAS_ENUM_ELEMENT,
			RelationTypes.FOREACH_STATEMENT, RelationTypes.FOREACH_VAR, RelationTypes.FORLOOP_INIT,
			RelationTypes.FORLOOP_STATEMENT, RelationTypes.FORLOOP_UPDATE, RelationTypes.CALLABLE_HAS_BODY,
			RelationTypes.CALLABLE_HAS_PARAMETER, RelationTypes.HAS_STATIC_INIT, RelationTypes.HAS_VARIABLEDECL_INIT,
			RelationTypes.IF_THEN, RelationTypes.IF_ELSE, RelationTypes.LABELED_STMT_ENCLOSES,
			RelationTypes.SWITCH_ENCLOSES_CASE, RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK, RelationTypes.TRY_BLOCK,
			RelationTypes.TRY_CATCH, RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES };
	private static final RelationTypes[] statToOuterBlock = new RelationTypes[] { RelationTypes.CASE_STATEMENTS,
			RelationTypes.CATCH_ENCLOSES_BLOCK, RelationTypes.CATCH_PARAM, RelationTypes.ENCLOSES,
			RelationTypes.DO_WHILE_STATEMENT, RelationTypes.WHILE_STATEMENT, RelationTypes.FOREACH_STATEMENT,
			RelationTypes.FOREACH_VAR, RelationTypes.FORLOOP_INIT, RelationTypes.FORLOOP_STATEMENT,
			RelationTypes.FORLOOP_UPDATE, RelationTypes.CALLABLE_HAS_PARAMETER, RelationTypes.HAS_STATIC_INIT,
			RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_THEN, RelationTypes.IF_ELSE,
			RelationTypes.LABELED_STMT_ENCLOSES, RelationTypes.SWITCH_ENCLOSES_CASE,
			RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK, RelationTypes.TRY_BLOCK, RelationTypes.TRY_CATCH,
			RelationTypes.TRY_FINALLY, RelationTypes.TRY_RESOURCES };

	private static final RelationTypes[] exprToStat = new RelationTypes[] { RelationTypes.ASSERT_CONDITION,
			RelationTypes.DO_WHILE_CONDITION, RelationTypes.ENCLOSES_EXPR, RelationTypes.FOREACH_EXPR,
			RelationTypes.FORLOOP_CONDITION, RelationTypes.HAS_VARIABLEDECL_INIT, RelationTypes.IF_CONDITION,
			RelationTypes.SWITCH_EXPR, RelationTypes.SYNCHRONIZED_EXPR, RelationTypes.THROW_EXPR,
			RelationTypes.WHILE_CONDITION, RelationTypes.RETURN_EXPR };
	private static final String exprToStatQuery = getAnyRel(exprToStat);
	// private static final String exprToStatQueryWithReturn =
	// getAnyRel(exprToStat) + " | :RETURN_EXPR ";

	private static final CFGRelationTypes[] cfgUnconditionalSucc = new CFGRelationTypes[] {
			CFGRelationTypes.CFG_NEXT_STATEMENT, CFGRelationTypes.CFG_NO_EXCEPTION, CFGRelationTypes.CFG_THROWS };

	private static final CFGRelationTypes[] toCFGSuccesor = new CFGRelationTypes[] {
			CFGRelationTypes.CFG_NEXT_STATEMENT, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE,
			CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT,
			CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS, CFGRelationTypes.CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION,
			CFGRelationTypes.CFG_NO_EXCEPTION, CFGRelationTypes.CFG_CAUGHT_EXCEPTION,
			CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_BREAK, CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_CONTINUE,
			CFGRelationTypes.CFG_SWITCH_CASE_IS_EQUAL_TO, CFGRelationTypes.CFG_SWITCH_DEFAULT_CASE,
			CFGRelationTypes.CFG_MAY_THROW, CFGRelationTypes.CFG_THROWS, CFGRelationTypes.CFG_ENTRIES };
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
			CFGRelationTypes.CFG_SWITCH_CASE_IS_EQUAL_TO, CFGRelationTypes.CFG_SWITCH_DEFAULT_CASE,
			CFGRelationTypes.CFG_THROWS };
	private static final String cfgSuccesor = getAnyRel(toCFGSuccesor);

	public static String getAnyRel(RelationTypesInterface[] rels) {
		String ret = "";
		for (RelationTypesInterface r : rels)
			ret += ":" + r.toString() + " | ";
		return ret.substring(0, ret.length() - 2);
	}

	private static final String OBJ56_SENSITIVE_MUTABLE_CLASSES_WITH_UNMODIFICABLE_WRAPPERS = " MATCH (enclosingType{accessLevel:'public'})-[:DECLARES_FIELD]->(field)-[:USED_BY]->(id)<-[:RETURN_EXPR]-()<-["
			+ getAnyRel(statToOuterBlock)
			+ "*]-(outerBlock)<-[:CALLABLE_HAS_BODY]-(method{accessLevel:'public'}), (field)-[:ITS_TYPE_IS]->(typeDec) WHERE field.accessLevel<>'public' AND (typeDec:CLASS_DECLARATION OR typeDec:ENUM_DECLARATION) "
			+ " WITH enclosingType.fullyQualifiedName AS enclosingTypeName, typeDec, field.name AS mutableFieldName "
			+ " OPTIONAL MATCH (field)-[r:MODIFIED_BY | :STATE_MODIFIED_BY]-(modif)<-[" + assignToOutExprQuery
			+ "*0..]-(outUseExpr)<-[" + exprToStatQuery + "]-(exprUseStat)<-[" + getAnyRel(statToOuterBlock)
			// This field Is Mutable From OutSide ?? Subconjunto de Is Mutable
			+ "*]-(outerBlock)<-[:CALLABLE_HAS_BODY]-(methodSet{accessLevel:'public'})<-[:DECLARES_METHOD]-(typeDec) "
			+ " WITH field, methodSet,typeDec, mutableFieldName, enclosingTypeName"
			+ " OPTIONAL MATCH (field{typeKind:'ARRAY'})-[:USED_BY]->(id)<-[:RETURN_EXPR]-()<-["
			+ getAnyRel(statToOuterBlock) + "*]-(outerBlock)<-[:CALLABLE_HAS_BODY]-(methodGet{accessLevel:'public'})"
			+ ""
			// GET
			// TO
			// MUTABLE
			// FIELD
			// ---
			// OUTSIDE
			// MUTABLE
			// ANALYSIS there is public field, there is public setter, there
			// is get to array or mutableType??
			+ "" + "<-[:DECLARES_METHOD]-(typeDec) "
			+ " WITH COLLECT(methodSet) + COLLECT(methodGet) AS methods, field,typeDec, mutableFieldName, enclosingTypeName  "
			+ " UNWIND methods AS m WITH COLLECT( DISTINCT m) as methods, mutableFieldName, enclosingTypeName, field, typeDec, (field.accessLevel='public' AND NOT field.isFinal AND NOT field.isStatic) AS anyPublicAttr"
			+ " WHERE SIZE(methods)>0 OR  anyPublicAttr"
			+ " UNWIND methods AS m WITH typeDec, mutableFieldName, enclosingTypeName, COLLECT(DISTINCT m) AS methods, anyPublicAttr "
			+ " OPTIONAL MATCH p=(typeDec)<-[:IS_SUBTYPE_EXTENDS*]-(subType)"
			+ " OPTIONAL MATCH (subType)-[:DECLARES_METHOD]->(method)-[:OVERRIDES]->(ovMethod) WHERE ovMethod IN methods "
			+ " WITH typeDec, mutableFieldName, enclosingTypeName, subType, COUNT( DISTINCT ovMethod) as ovMethodCount, SIZE( methods) as methodCount, p, anyPublicAttr "
			+ " OPTIONAL MATCH (field)<-[:DECLARES_FIELD]-(typeInHierarchy) WHERE typeInHierarchy IN nodes(p) WITH COLLECT(field) AS fields, mutableFieldName, enclosingTypeName,  typeDec, subType, ovMethodCount, methodCount, anyPublicAttr "
			+ " OPTIONAL MATCH (subType)-[:DECLARES_METHOD]->(method{accessLevel:'public'}) WITH COLLECT(method) as publicSubMethods, mutableFieldName, enclosingTypeName, fields,  typeDec, subType, ovMethodCount, methodCount, anyPublicAttr "
			+ " OPTIONAL MATCH q=(field)-[r:MODIFIED_BY |:STATE_MODIFIED_BY]-(modif)<-[" + assignToOutExprQuery
			+ "*0..]-(outUseExpr)<-[" + exprToStatQuery + "]-(exprUseStat)<-[" + getAnyRel(statToOuterBlock)
			+ "*]-(outerBlock)<-[:CALLABLE_HAS_BODY]-(method) WHERE field IN fields AND method IN publicSubMethods WITH typeDec, mutableFieldName, enclosingTypeName, subType, ovMethodCount, methodCount, COUNT(q) AS qs, anyPublicAttr, fields, publicSubMethods"
			+ " OPTIONAL MATCH s=(field)-[:USED_BY]->(id)<-[:RETURN_EXPR]-()<-[" + getAnyRel(statToOuterBlock)
			+ "*]-(outerBlock)<-[:CALLABLE_HAS_BODY]-(method) WHERE  field.typeKind='ARRAY' AND field IN fields AND method IN publicSubMethods"

			+ " WITH typeDec, mutableFieldName, enclosingTypeName,anyPublicAttr,subType, ovMethodCount, methodCount,qs, COUNT(s) AS ss, ANY(f IN fields WHERE f.accessLevel='public' AND NOT f.isStatic AND NOT f.isFinal) as updatedAnyPublicAttr"
			+ " WITH typeDec, mutableFieldName, enclosingTypeName,anyPublicAttr, COLLECT([subType, ovMethodCount, methodCount,qs, ss, updatedAnyPublicAttr]) as tuples"
			+ " WHERE anyPublicAttr OR ALL( tuple IN tuples WHERE tuple[1]<tuple[2] OR tuple[3]>0 OR tuple[4]>0 OR tuple[5]) "
			+ " RETURN 'Warning [CMU-OBJ56] You must provide sensitive mutable classes with unmodifiable wrappers. The class '+ enclosingTypeName + ' has a non-public atribute ' +mutableFieldName+' returned in a public method. The class of this attribute, ' +typeDec.fullyQualifiedName+', can be mutated by an unstrusted client and there are no unmodificable subclasses to be used as a wrapper.'"

	;

	private static final String MET52_DO_NOT_USE_CLONE_WITH_UNTRUSTED_PARAMETERS = " MATCH (enclosingCU)-[:HAS_TYPE_DEF |:HAS_INNER_TYPE_DEF]->(typeDec{accessLevel:'public'})"
			+ "-[:DECLARES_METHOD]->(method{accessLevel:'public'})-[:CALLABLE_HAS_PARAMETER]->(param)"
			+ " -[:USED_BY]->(id)<-[:MEMBER_SELECT_EXPR]-(mSelect:MEMBER_SELECTION{memberName:'clone'})"
			+ "<-[:METHODINVOCATION_METHOD_SELECT]-(mInv:METHOD_INVOCATION), (param)-[:HAS_VARIABLEDECL_TYPE]->"
			+ "()-[:PARAMETERIZED_TYPE*0..1]->()-[:ITS_TYPE_IS]->(pType) "
			+ " WHERE mSelect.actualType CONTAINS '()' AND NOT pType.isFinal AND "
			+ "(NOT pType.isDeclared OR pType.accessLevel='public') RETURN "
			+ "'Warning [CMU-MET52] You must not use the clone method to copy unstrasted parameters (like parameter ' + param.name+ ', cloned in line '+  mInv.lineNumber+ ' in method ' + method.name +', file '+enclosingCU.fileName+').'";

	// LO ÚNICO QUE ME MOSQUEA AHORA ES ESE OR DEL FINAL, PORQUE NO PERMITE
	// COSAS COMO
	/*
	 * if(){
	 * 
	 * a=b; Sysout(a);
	 * 
	 * }
	 *
	 * EN LUGAR DE Sólo COSAS COMO
	 *
	 * if(){ a=b; }
	 *
	 * Sysout(a)
	 *
	 * Y LAS REGLAS DE EN MEDIO NO PERMITEN ESTO??
	 *
	 * for() { a=b; Sysout(a) //PORQUE LA MODIF SE PUEDE ALCANZAR DESDE EL USO, PERO
	 * NUNCA SE EJECUTA ANTES EL USO QUE LA MODIFICACION }
	 *
	 * for() { Sysout(a) a=b; }
	 * 
	 * Y LOS ATRIBUTOS NO PUEDEN SER PÚBLICOS DE CLASES PúBLICAS, PORQUE ENTONCES
	 * IGUAL SIRVE PARA ALGO...
	 */

	// Que le preceda una modif
	// Una modif con id o this.id o un member_sel sobre el mismo objeto (menos
	// news) que el
	// uso
	// Movida de los bloques
	// controlar expresiones padres o hermanas anteriores
	// problema position
	// not (isStatic() and isFinal() and getType().getName()="long" and
	// getType() instanceof PrimitiveType and getName()="serialVersionUID") and

	private static final String DCL53_MINIMIZE_SCOPE_OF_VARIABLES = " MATCH (typeDec)-[:DECLARES_FIELD]->(attr:ATTR_DEC) "
			+ " WHERE NOT (attr.accessLevel='public' AND typeDec.accessLevel='public') AND "
			+ " NOT( attr.isStatic AND attr.actualType='long' AND attr.isFinal AND attr.name='serialVersionUID') "
			// +" RETURN attr.name as a, typeDec.fullyQualifiedName ORDER BY a"
			+ " OPTIONAL MATCH (attr)-[:USED_BY]->(exprUse)<-[" + exprToOutExprQuery + "*0..]-(outUseExpr)<-["
			+ exprToStatQuery + "]-(exprUseStat)" + "" + " OPTIONAL MATCH (exprUseStat)<-["
			+ getAnyRel(statToOuterBlock)
			+ "*]-(outerBlock)<-[:CALLABLE_HAS_BODY]-() WITH typeDec.fullyQualifiedName as className, attr,outerBlock, exprUseStat, exprUse"
			+ " OPTIONAL MATCH (attr)-[:MODIFIED_BY]->(modif)<-[" + getAnyRel(assignToOutExprNoCond)
			+ "*0..]-(outModExpr)<-[" + exprToStatQuery + "]-(exprModStat)<-[" + getAnyRel(statToOuterBlock)
			+ "*]-(outerBlock), (modif)-[:ASSIGNMENT_LHS]->(lhs_expr) WHERE  exprModStat.position < exprUseStat.position "
			+ " OPTIONAL MATCH (exprUse)-[:MEMBER_SELECT_EXPR]->(memberSelectExprUse)<-[:USED_BY]-(varDec) "
			+ " OPTIONAL MATCH p=(varDec)-[:STATE_MODIFIED_BY]->(modif)"
			+ " OPTIONAL MATCH (lhs_expr)-[:MEMBER_SELECT_EXPR]->(memberSelectExprModif) "
			+ " WITH outerBlock,p,className, attr.lineNumber as line, attr.name as attr,exprUse, modif,lhs_expr, memberSelectExprUse, memberSelectExprModif, exprUseStat, exprModStat "
			+ " OPTIONAL MATCH q=(exprModStat)<-[" + getAnyRel(statToOuterBlock)
			+ "*0..]-(minimumCommonBlock), (minimumCommonBlock)-[" + getAnyRel(statToOuterBlock)
			+ "*0..]->(exprUseStat) " + " WITH className, attr, exprUse,line, " + " ANY(x IN " + "COLLECT(" + "  "
			+ "( NOT outerBlock IS NULL AND"
			+ " NOT ANY(rel IN RELS(q) WHERE type(rel)='IF_ELSE' OR type(rel)='FORLOOP_UPDATE' OR type(rel)='FORLOOP_STATEMENT'  OR  type(rel)='FOREACH_STATEMENT' OR type(rel)='TRY_CATCH' OR type(rel)='SWITCH_ENCLOSES_CASES' OR type(rel)='IF_THEN') "
			+ " AND ("
			+ " (exprUse:IDENTIFIER OR (NOT memberSelectExprUse IS NULL AND memberSelectExprUse:IDENTIFIER AND (memberSelectExprUse.name='this' OR memberSelectExprUse.name='super')))"
			+ " AND (lhs_expr:IDENTIFIER OR (NOT memberSelectExprModif IS NULL AND memberSelectExprModif:IDENTIFIER  AND (memberSelectExprModif.name='this' OR memberSelectExprModif.name='super')))"
			+ " ) " + " OR "
			+ " (p IS NOT NULL AND NOT memberSelectExprUse IS NULL AND NOT memberSelectExprModif IS NULL AND memberSelectExprUse:IDENTIFIER AND memberSelectExprModif:IDENTIFIER)"
			+ ")" + ") " + " WHERE x) as useWithModif "
			+ " WITH line,className, attr , ALL( x IN COLLECT(useWithModif) WHERE x) OR exprUse IS NULL as isSillyAttr "
			// + "WHERE isSillyAttr"
			+ " RETURN 'Warning [CMU-DCL53] You must minimize the scope of the varaibles. You can minimize the scope of the attribute '+attr+'(declared in line '+line+') in class '+className + ' by transforming it into a local varaible (as everytime its value is used in a method, there is a previous unconditional assignment).' as war ORDER BY war"// Movida
																																																																																								// de
	; // los

	private static final String DCL53_MINIMIZE_SCOPE_OF_VARIABLES_V2 = " MATCH (typeDec)-[:DECLARES_FIELD]->(attr:ATTR_DEF) "
			+ " WHERE NOT ((attr.accessLevel='public' OR attr.accessLevel='protected' AND NOT typeDec.isFinal) AND typeDec.accessLevel='public') AND "
			+ " NOT( attr.isStatic AND attr.actualType='long' AND attr.isFinal AND attr.name='serialVersionUID') "
			// +" RETURN attr.name as a, typeDec.fullyQualifiedName ORDER BY a"
			+ " OPTIONAL MATCH (attr)-[attrUseRel:USED_BY | :STATE_MODIFIED_BY]->(exprUse)<-[" + exprToOutExprQuery
			+ "*0..]-()<-[" + exprToStatQuery + "]-(exprUseStat)<-[" + getAnyRel(statToOuterBlock)
			+ "*]-(outerBlock)<-[:CALLABLE_HAS_BODY]-(method) "
			+ " OPTIONAL MATCH (attr)-[attrModifRel:MODIFIED_BY]->(modif)<-[" + getAnyRel(assignToOutExprNoCond)
			+ "*0..]-()<-[" + exprToStatQuery + "]-(exprModStat)<-[" + getAnyRel(statToOuterBlock)
			+ "*]-(outerBlock), (modif)-[:ASSIGNMENT_LHS]->(lhs_expr) WHERE  exprModStat.position < exprUseStat.position AND ( NOT exprModStat IS NULL OR exprUse IS NULL) "
			+ " WITH attrModifRel,attrUseRel,typeDec.fullyQualifiedName as className, attr,outerBlock, exprUseStat, exprUse ,method, exprModStat, modif, lhs_expr "
			+ " MATCH q=(method)-[" + cfgSuccesor
			+ "*]->(exprUseStat) WITH attrModifRel,attrUseRel,exprModStat IN NODES(q) as modInPath,className, attr,outerBlock, exprUseStat, exprUse ,method, exprModStat, modif, lhs_expr "

			+ " OPTIONAL MATCH (exprUse)-[:MEMBER_SELECT_EXPR]->(memberSelectExprUse)<-[:USED_BY]-(varDec) "
			+ " OPTIONAL MATCH p=(varDec)-[:STATE_MODIFIED_BY]->(modif)"
			+ " OPTIONAL MATCH (lhs_expr)-[:MEMBER_SELECT_EXPR]->(memberSelectExprModif) "

			+ " WITH attrModifRel,attrUseRel,ALL( modInPath IN COLLECT(modInPath) WHERE modInPath) as unconditionalAssign, method,p,className, attr.lineNumber as line, attr.name as attr,exprUse, modif,lhs_expr, memberSelectExprUse, memberSelectExprModif, exprUseStat, exprModStat "
			+ " WITH attrModifRel,attrUseRel,className, attr,line, exprUse,unconditionalAssign, "
			+ " attrUseRel.isOwnAccess " + " AND attrModifRel.isOwnAccess " + "  OR "
			+ " (NOT p IS NULL AND NOT memberSelectExprUse IS NULL AND NOT memberSelectExprModif IS NULL AND memberSelectExprUse:IDENTIFIER AND memberSelectExprModif:IDENTIFIER)"
			+ " as isTheSameVar "

			+ " WITH line,className, attr ,exprUse, ANY( x IN COLLECT(unconditionalAssign AND isTheSameVar) WHERE x) as prevAssign "
			+ " WITH line,className, attr , ALL( x IN COLLECT(prevAssign) WHERE x) OR exprUse IS NULL as isSillyAttr"
			//
			+ " WHERE isSillyAttr" + " RETURN "
			// + "isSillyAttr, attr, className ORDER BY attr"
			+ "'Warning [CMU-DCL53] You must minimize the scope of the varaibles. You can minimize the scope of the attribute '+attr+'(declared in line '+line+') in class '+className + ' by transforming it into a local varaible (as everytime its value is used in a method, there is a previous unconditional assignment).' "// Movida
																																																																																	// de
	;
	private static final String DCL53_MINIMIZE_SCOPE_OF_VARIABLES_V3 = "MATCH (typeDec)-[:DECLARES_FIELD]->(attr:ATTR_DEF) "
			+ " WHERE NOT ((attr.accessLevel='public' OR attr.accessLevel='protected' AND NOT typeDec.isFinal) "
			+ " AND typeDec.accessLevel='public') AND  NOT( attr.isStatic AND attr.actualType='long' AND attr.isFinal"
			+ " AND attr.name='serialVersionUID')\r\n" + "WITH DISTINCT typeDec, attr \r\n"
			+ "MATCH (attr)-[:USED_BY | :STATE_MODIFIED_BY]->(exprUse)\r\n"
			+ "WITH  DISTINCT  attr,COUNT(exprUse) as allUsesCount\r\n"
			+ "MATCH (lhs_expr) <-[:ASSIGNMENT_LHS]-(modif)<-[attrModifRel:MODIFIED_BY]-(attr)"
			+ "-[attrUseRel:USED_BY | :STATE_MODIFIED_BY]->(exprUse)\r\n"
			+ "WHERE modif.position<exprUse.position AND attrUseRel.isOwnAccess = attrModifRel.isOwnAccess\r\n"
			+ "WITH DISTINCT attr, exprUse, modif, attrModifRel, lhs_expr, allUsesCount\r\n"
			+ "OPTIONAL MATCH (exprUse)-[:MEMBER_SELECT_EXPR]->(memberSelectExprUse)<-[:USED_BY]-(varDec)-[:STATE_MODIFIED_BY]->(modif),"
			+ " (lhs_expr)-[:MEMBER_SELECT_EXPR]->(memberSelectExprModif)\r\n"
			+ "WITH DISTINCT attr, exprUse, modif, attrModifRel, memberSelectExprModif, memberSelectExprUse, allUsesCount\r\n"
			+ "WHERE\r\n" + " attrModifRel.isOwnAccess   OR  (NOT memberSelectExprUse\r\n"
			+ "IS NULL AND NOT memberSelectExprUse IS NULL AND NOT memberSelectExprModif IS NULL "
			+ "AND memberSelectExprUse:IDENTIFIER AND memberSelectExprModif:IDENTIFIER)\r\n"
			+ "WITH DISTINCT attr, COLLECT(exprUse) as uses, modif, allUsesCount\r\n"
			+ "WHERE allUsesCount=SIZE(uses)\r\n" + "\r\n"
			+ "MATCH (modif)<-[:ARRAYACCESS_EXPR | :ARRAYACCESS_INDEX | :ASSIGNMENT_LHS | :ASSIGNMENT_RHS | :BINOP_LHS | :BINOP_RHS | :BINOP_COND_RHS | :CAST_ENCLOSES | :COMPOUND_ASSIGNMENT_LHS | :COMPOUND_ASSIGNMENT_RHS | :CONDITIONAL_EXPR_CONDITION | :CONDITIONAL_EXPR_THEN | :CONDITIONAL_EXPR_ELSE | :INITIALIZATION_EXPR | :INSTANCE_OF_EXPR | :MEMBER_REFERENCE_EXPRESSION | :MEMBER_SELECT_EXPR | :METHODINVOCATION_ARGUMENTS | :METHODINVOCATION_METHOD_SELECT | :NEW_CLASS_ARGUMENTS | :NEW_ARRAY_INIT | :NEW_ARRAY_DIMENSION | :UNARY_ENCLOSES *0..]-()<-[:ASSERT_CONDITION | :DO_WHILE_CONDITION | :ENCLOSES_EXPR | :FOREACH_EXPR | :FORLOOP_CONDITION | :HAS_VARIABLEDECL_INIT | :IF_CONDITION | :SWITCH_EXPR | :SYNCHRONIZED_EXPR | :THROW_EXPR | :WHILE_CONDITION | :RETURN_EXPR ]-(modifStat), p1=(modifStat)<-[:DO_WHILE_STATEMENT| :WHILE_STATEMENT | :CASE_STATEMENTS | :CATCH_ENCLOSES_BLOCK | :CATCH_PARAM | :ENCLOSES | :FOREACH_STATEMENT | :FOREACH_VAR | :FORLOOP_INIT | :FORLOOP_STATEMENT | :FORLOOP_UPDATE |  :IF_THEN | :IF_ELSE | :LABELED_STMT_ENCLOSES | :SWITCH_ENCLOSES_CASES | :SYNCHRONIZED_ENCLOSES_BLOCK | :TRY_BLOCK | :TRY_CATCH | :TRY_FINALLY | :TRY_RESOURCES *]-(outerBlock)<-[:CALLABLE_HAS_BODY]-()\r\n"
			+ "\r\n" + "\r\n" + "WITH DISTINCT attr, modif,outerBlock, modifStat, uses,p1, allUsesCount\r\n"
			+ "UNWIND uses as exprUse\r\n"
			+ "MATCH (exprUse)<-[:ARRAYACCESS_EXPR | :ARRAYACCESS_INDEX | :ASSIGNMENT_LHS | :ASSIGNMENT_RHS | :BINOP_LHS | :BINOP_RHS | :BINOP_COND_RHS | :CAST_ENCLOSES | :COMPOUND_ASSIGNMENT_LHS | :COMPOUND_ASSIGNMENT_RHS | :CONDITIONAL_EXPR_CONDITION | :CONDITIONAL_EXPR_THEN | :CONDITIONAL_EXPR_ELSE | :INITIALIZATION_EXPR | :INSTANCE_OF_EXPR | :MEMBER_REFERENCE_EXPRESSION | :MEMBER_SELECT_EXPR | :METHODINVOCATION_ARGUMENTS | :METHODINVOCATION_METHOD_SELECT | :NEW_CLASS_ARGUMENTS | :NEW_ARRAY_INIT | :NEW_ARRAY_DIMENSION | :UNARY_ENCLOSES *0..]-()<-[:ASSERT_CONDITION | :DO_WHILE_CONDITION | :ENCLOSES_EXPR | :FOREACH_EXPR | :FORLOOP_CONDITION | :HAS_VARIABLEDECL_INIT | :IF_CONDITION | :SWITCH_EXPR | :SYNCHRONIZED_EXPR | :THROW_EXPR | :WHILE_CONDITION | :RETURN_EXPR ]-(exprUseStat), p2=(exprUseStat)<-[:DO_WHILE_STATEMENT | :WHILE_STATEMENT  | :CASE_STATEMENTS | :CATCH_ENCLOSES_BLOCK | :CATCH_PARAM | :ENCLOSES | :FOREACH_STATEMENT | :FOREACH_VAR | :FORLOOP_INIT | :FORLOOP_STATEMENT | :FORLOOP_UPDATE |  :IF_THEN | :IF_ELSE | :LABELED_STMT_ENCLOSES | :SWITCH_ENCLOSES_CASES | :SYNCHRONIZED_ENCLOSES_BLOCK | :TRY_BLOCK | :TRY_CATCH | :TRY_FINALLY | :TRY_RESOURCES *]-(outerBlock)\r\n"
			+ "\r\n" + "WITH DISTINCT attr,exprUseStat, p1,RELATIONSHIPS(p2) as exprUseBlockRels, allUsesCount\r\n"
			+ "WHERE ALL(r IN RELATIONSHIPS(p1) WHERE NOT TYPE(r) IN ['SWITCH_ENCLOSES_CASES','IF_ELSE','IF_THEN', 'FORLOOP_STATEMENT', 'FORLOOP_UPDATE', 'FOREACH_STATEMENT', 'TRY_CATCH', 'WHILE_STATEMENT' ] OR r IN exprUseBlockRels)\r\n"
			+ "WITH DISTINCT attr, COUNT(exprUseStat) as usesWithPrevModif, allUsesCount\r\n"
			+ "WHERE usesWithPrevModif=allUsesCount\r\n" + "MATCH (attr)<-[:DECLARES_FIELD]-(enclClass)\r\n"
			+ "RETURN DISTINCT 'Warning [CMU-DCL53] You must minimize the scope of the varaibles. You can minimize the scope of the attribute '+attr.name+'(declared in line '+attr.lineNumber+') in class '+enclClass.fullyQualifiedName + ' by transforming it into a local varaible (as everytime its value is used in a method, there is a previous unconditional assignment).'";
	// bloques
	/*
	 * private static final String DCL53_MINIMIZE_SCOPE_OF_VARIABLES =
	 * " MATCH (typeDec)-[:DECLARES_FIELD]->(attr:ATTR_DEC) " +
	 * "OPTIONAL MATCH (attr)-[:USED_BY]->(exprUse)<-[" + exprToOutExprQuery +
	 * "*0..]-(outUseExpr)<-[" + exprToStatQueryWithReturn + "]-(exprUseStat)<-[" +
	 * getAnyRel(statToOuterBlock) +
	 * "*]-(outerBlock)<-[:CALLABLE_HAS_BODY]-() WITH typeDec.fullyQualifiedName as className, attr,outerBlock, exprUseStat, exprUse"
	 * + " OPTIONAL MATCH (attr)-[:MODIFIED_BY]->(modif)<-[" +
	 * getAnyRel(assignToOutExprNoCond) + "*0..]-(outModExpr)<-[" +
	 * exprToStatQueryWithReturn + "]-(exprModStat)<-[" +
	 * getAnyRel(statToOuterBlock) +
	 * "*]-(outerBlock), (modif)-[:ASSIGNMENT_LHS]->(lhs_expr) WHERE  exprModStat.position < exprUseStat.position "
	 * +
	 * " OPTIONAL MATCH (exprUse)-[:MEMBER_SELECT_EXPR]->(memberSelectExprUse)<-[:USED_BY]-(varDec) "
	 * + " OPTIONAL MATCH p=(varDec)-[:STATE_MODIFIED_BY]->(modif)" +
	 * " OPTIONAL MATCH (lhs_expr)-[:MEMBER_SELECT_EXPR]->(memberSelectExprModif) "
	 * +
	 * " WITH p,className, attr.lineNumber as line, attr.name as attr,exprUse, modif,lhs_expr, memberSelectExprUse, memberSelectExprModif, exprUseStat, exprModStat "
	 * + " OPTIONAL MATCH q=(exprModStat)<-[" + getAnyRel(statToOuterBlock) +
	 * "*0..]-(minimumCommonBlock), (minimumCommonBlock)-[" +
	 * getAnyRel(statToOuterBlock) + "*0..]->(exprUseStat) " +
	 * " WITH className, attr, exprUse,line, " + " ANY(x IN " + "COLLECT(" + "  " +
	 * "( NOT ANY(rel IN RELS(q) WHERE type(rel)='IF_ELSE' OR type(rel)='FORLOOP_UPDATE' OR type(rel)='FORLOOP_STATEMENT' OR  type(rel)='FORLOOP_INIT' OR  type(rel)='FOREACH_STATEMENT' OR type(rel)='TRY_CATCH' OR type(rel)='SWITCH_ENCLOSES_CASES' OR type(rel)='IF_THEN') "
	 * + " AND (" +
	 * " (exprUse:IDENTIFIER OR (NOT memberSelectExprUse IS NULL AND memberSelectExprUse:IDENTIFIER AND (memberSelectExprUse.name='this' OR memberSelectExprUse.name='super')))"
	 * +
	 * " AND (lhs_expr:IDENTIFIER OR (NOT memberSelectExprModif IS NULL AND memberSelectExprModif:IDENTIFIER  AND (memberSelectExprModif.name='this' OR memberSelectExprModif.name='super')))"
	 * + " ) " + " OR " // + " (NOT memberSelectExprUse IS NULL AND NOT //
	 * memberSelectExprModif IS NULL AND memberSelectExprUse:IDENTIFIER // AND
	 * memberSelectExprModif:IDENTIFIER)" // + " OR " +
	 * " (p IS NOT NULL AND NOT memberSelectExprUse IS NULL AND NOT memberSelectExprModif IS NULL AND memberSelectExprUse:IDENTIFIER AND memberSelectExprModif:IDENTIFIER)"
	 * + ")" + ") " + " WHERE x) as useWithModif " +
	 * " WITH line,className, attr , ALL( x IN COLLECT(useWithModif) WHERE x) OR exprUse IS NULL as isSillyAttr WHERE isSillyAttr"
	 * +
	 * " RETURN 'Warning [CMU-DCL53] You must minimize the scope of the varaibles. You can minimize the scope of the attribute '+attr+'(declared in line '+line+') in class '+className + ' by transforming it into a local varaible (as everytime its value is used in a method, there is a previous unconditional assignment).'"
	 * 
	 */
	private static final String DCL53_MINIMIZE_SCOPE_OF_VARIABLES_BEST = " MATCH (vardec:VAR_DEC)-[:USED_BY | :MODIFIED_BY]->(useOrModif)<-["
			+ exprToOutExprQuery + "*0..]-(outExpr)<-[" + exprToStatQuery + "]-(exprStat), p=(vardec)-[" + cfgSuccesor
			+ "*]->(exprStat) WITH vardec, exprStat, p  ORDER BY LENGTH(p) WITH vardec,COLLECT( DISTINCT exprStat) as stats WITH vardec, stats"
			+ " MATCH p=(block)-[" + getAnyRel(statToOuterBlock) + "*0..]->(stat) WHERE stat IN stats "
			+ " WITH vardec, block, COLLECT(DISTINCT stat) as statsInBlock, stats, MAX(LENGTH(p)) as size "
			+ " ORDER BY size WHERE SIZE(stats)=SIZE( statsInBlock) WITH vardec, HEAD(COLLECT( DISTINCT block)) as outerBlock, HEAD(stats) as headStat"
			+ " MATCH (vardec)-[" + cfgSuccesor + "*]->(middle)-[" + cfgSuccesor + "*]->(headStat)"
			+ " OPTIONAL MATCH p=(middle)-[" + getAnyRel(statToOuterBlock) + "*]->(outerBlock) "
			+ " OPTIONAL MATCH q=(middle)<-[" + getAnyRel(statToOuterBlock) + "]-(outerBlock) "
			+ " OPTIONAL MATCH r=(headStat)-[" + cfgSuccesor + "*]->(middle)"
			+ " WITH vardec, middle, headStat, outerBlock, p ,q, COUNT(r) as rCount WHERE rCount=0 AND NOT(p IS NULL AND q IS NULL) RETURN DISTINCT vardec, middle, LABELS(middle), headStat,LABELS(headStat), outerBlock,LABELS(outerBlock), p ,q ORDER BY vardec";
	private static final String DCL53_MINIMIZE_SCOPE_OF_VARIABLES_SAVE = " MATCH (vardec:VAR_DEC)-[:USED_BY | :MODIFIED_BY]->(useOrModif)<-["
			+ exprToOutExprQuery + "*0..]-(outExpr)<-[" + exprToStatQuery + "]-(exprStat), p=(vardec)-[" + cfgSuccesor
			+ "*]->(exprStat) WITH vardec, exprStat, p  ORDER BY LENGTH(p) WITH vardec,COLLECT( DISTINCT exprStat) as stats WITH vardec, stats"
			+ " MATCH p=(block)-[" + getAnyRel(statToOuterBlock) + "*0..]->(stat) WHERE stat IN stats "
			+ " WITH vardec, block, COLLECT(DISTINCT stat) as statsInBlock, stats, MAX(LENGTH(p)) as size "
			+ " ORDER BY size WHERE SIZE(stats)=SIZE( statsInBlock) RETURN  vardec, HEAD(COLLECT( DISTINCT block)) as outerBlock,LABELS(HEAD(COLLECT( DISTINCT block))), HEAD(stats) as headStat, stats ORDER BY vardec";

	private static final String NUM50CONVERT_INT_TO_FLOAT_FOR_FLOAT_OPERATIONS = "MATCH (varDec{actualType:'float'})-[:MODIFIED_BY | :HAS_VARIABLEDECL_INIT]->(mod)"
			+ "-[:ASSIGNMENT_RHS | :INITIALIZATION_EXPR]->(rightSide)"
			+ " OPTIONAL MATCH (binopR{actualType:'int'})<-[:BINOP_RHS]-(division)"
			+ "<-[:BINOP_LHS | :BINOP_RHS | :UNARY_ENCLOSES |:CONDITIONAL_EXPR_ELSE|:CONDITIONAL_EXPR_THEN |:PARENTHESIZED_ENCLOSES*0..]-(rightSide)"
			+ ",(division)-[:BINOP_LHS]->({actualType:'int'})"
			+ " WITH varDec, COLLECT(rightSide.actualType='float') as rightSidesAreFloat, "
			+ new Filter("COLLECT([rightSide,binopR])", "NOT x[1] IS NULL AND x[0].operator='DIVIDE'").expToString()
			+ " as lines" + " WHERE " + " NOT " + new Any("rightSidesAreFloat", "x").expToString()
			+ " AND SIZE(lines)>0 "
			+ " RETURN 'Warning [CMU-NUM50] A truncated integer division was detected in line(s) ' +"
			+ new Reduce("lines",
					"CASE WHEN seed CONTAINS (x[0].lineNumber+'') THEN seed ELSE seed+x[0].lineNumber+',' END",
					"seed=''").expToString()
			+ " +', assigned to variables of type float. If you want to make a float division and assign the result to the variable ' + varDec.name+', you must include a operand as a float. Otherwise you can change the type of '+ varDec.name+' from float to int, as it is never used to store an actual float value.' "
	// + new Reduce("lines", "s+','+x", "''").expToString()
	;

	private static final String OBJ51_MINIMIZE_ACCESSIBILITY_OF_CLASSES_AND_MEMBERS_PART_ONE = "MATCH (typeDec)-[:DECLARES_FIELD | :DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(member{accessLevel:'public'}) "
			+ " WHERE (typeDec:ENUM_DECLARATION OR typeDec:CLASS_DECLARATION) AND typeDec.accessLevel<>'public' "
			+ " OPTIONAL MATCH (member)-[ovRel:OVERRIDES]->({accessLevel:'public'}) "
			+ " WITH typeDec, member, COLLECT(ovRel) AS ovRels WHERE SIZE(ovRels)=0 "
			+ " RETURN 'Warning [CMU-OBJ54] You should minimize accessibility of members. You can reduce the access level of the attribute ' + member.name + ' declared in the class '+ typeDec.fullyQualifiedName + ', as this class is not public and the member does not override any public method.' AS warning";
	private static final String OBJ51_MINIMIZE_ACCESSIBILITY_OF_CLASSES_AND_MEMBERS_PART_TWO = "MATCH (typeDec{accessLevel:'package'}) WHERE typeDec:ENUM_DECLARATION OR typeDec:CLASS_DECLARATION OR typeDec:INTERFACE_DECLARATION "
			+ " OPTIONAL MATCH (typeDec)<-[:USES_TYPE_DEF]-(typeDecUser) WHERE typeDecUser<>typeDec"
			+ " WITH typeDec, COLLECT(DISTINCT typeDecUser) AS typeDecUsers"
			+ " OPTIONAL MATCH (typeDec)-[:DECLARES_FIELD | :DECLARES_METHOD ]->(member)-[:HAS_DEF | :MODIFIED_BY | :USED_BY]-(expr)"
			+ "<-[" + exprToOutExprQuery + "*0..]-(outExpr)<-[" + exprToStatQuery + "]-(exprStat)"
			+ " OPTIONAL MATCH (exprStat)<-[" + getAnyRel(statToEnclClass) + "*]-(classDec)" + " WHERE   "
			+ " (classDec:ENUM_DECLARATION OR classDec:CLASS_DECLARATION OR classDec:INTERFACE_DECLARATION) AND classDec<>typeDec AND  NOT classDec IN typeDecUsers "
			+ " WITH typeDec, COLLECT(DISTINCT classDec)+typeDecUsers AS classesUsingMembers "
			+ " WHERE SIZE(classesUsingMembers)=1 "
			+ " RETURN 'Warning [CMU-OBJ54] You should minimize accessibility of classes. The class ' + typeDec.fullyQualifiedName +' is marked as package-private but is only used by the class'+ HEAD(classesUsingMembers).fullyQualifiedName + ', you should reduce its accessibility level by making it a private static nested class.' AS warning";
	private static final String OBJ51_MINIMIZE_ACCESSIBILITY_OF_CLASSES_AND_MEMBERS = OBJ51_MINIMIZE_ACCESSIBILITY_OF_CLASSES_AND_MEMBERS_PART_ONE
			+ " UNION " + OBJ51_MINIMIZE_ACCESSIBILITY_OF_CLASSES_AND_MEMBERS_PART_TWO;
	private static final String OBJ54_DONT_TRY_HELP_GC_SETTING_REFS_TO_NULL = "MATCH (:LITERAL{typeKind:'NULL'})<-[:ASSIGNMENT_RHS]-(ass:ASSIGNMENT)<-[:MODIFIED_BY | STATE_MODIFIED_BY]-(localVar:VAR_DEC{typeKind:'DECLARED'})-[:USED_BY]->(use)<-["
			+ exprToOutExprQuery + "*]-(outExpr)<-[" + exprToStatQuery + "]-(useStat)," + " (ass)<-["
			+ assignToOutExprQuery + "*0..]-(expr)<-[" + exprToStatQuery + "]-(assignStat) "
			+ " WITH COLLECT(useStat) AS useStats, localVar, assignStat " + " OPTIONAL MATCH (assignStat)-["
			+ cfgSuccesor + "*0..]->(useStat) " + " WHERE useStat IN useStats"
			+ " WITH  localVar, COLLECT(DISTINCT useStat) AS reachableUseStats, assignStat "
			+ " WHERE SIZE(reachableUseStats)=0"
			+ " RETURN 'Warning [CMU-OBJ54] You must not try to help garbage collector setting references to null when they are no longer used. To make your code clearer, just delete the assignment in line ' + assignStat.lineNumber + ' of the varaible ' +localVar.name+ ' declared in line ' +localVar.lineNumber+'.'";

	private static final String DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_END = " MATCH (package1)-[:DEPENDS_ON_PACKAGE]->(package2),"
			+ "p=shortestPath((package2)-[:DEPENDS_ON_PACKAGE*]->(package1))"

			+ " WITH REDUCE(warning='',package IN package1+NODES(p)| warning +  '->'  +package.name) as packageDepList"
			// + " ,RANGE(0,SIZE(NODES(p))-2) as firstIndexes"
			// + " WHERE ALL(i1 IN firstIndexes WHERE NOT ANY(i2 IN firstIndexes
			// WHERE ID(packages[i1])=ID(packages[i2]) AND i1<>i2))"
			// + " WITH DISTINCT "
			+ " RETURN "
			+ " 'Warning [CMU-DCL60] There is a cycle between packages caused by the dependencies between ' +"
			+ " SUBSTRING(packageDepList,2,LENGTH(packageDepList))+ '. You should undo them.'"
	// + " REDUCE(warning='Warning [CMU-DCL60] There is a cycle between packages
	// caused by the dependencies between ',package IN packages | warning +
	// (CASE WHEN SIZE(warning)>89 THEN '->' ELSE '' END) +package.name)+ '. You
	// should undo them.'"
	;
	private static final String DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_END_HARD = " MATCH p=(package1)-[:DEPENDS_ON_PACKAGE*]->(package2) WHERE package1<>package2"
			+ " MATCH (package2)-[:DEPENDS_ON_PACKAGE]->(package1)"

			// + " WITH REDUCE(warning='',package IN package1+NODES(p)| warning
			// + '->' +package.name) as packageDepList"
			// + " ,RANGE(0,SIZE(NODES(p))-2) as firstIndexes"
			// + " WHERE ALL(i1 IN firstIndexes WHERE NOT ANY(i2 IN firstIndexes
			// WHERE ID(packages[i1])=ID(packages[i2]) AND i1<>i2))"
			// + " WITH DISTINCT "
			+ " RETURN p, package1";
	// + " 'Warning [CMU-DCL60] There is a cycle between packages caused by the
	// dependencies between ' +"
	// + " SUBSTRING(packageDepList,2,LENGTH(packageDepList))+ '. You should
	// undo them.'";

	private static final String DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES = "MATCH p=(typeDec1)-[:USES_TYPE_DEF]->(typeDec2)-[:USES_TYPE_DEF]->(typeDec3),"
			+ "(cu1:COMPILATION_UNIT)-[:HAS_INNER_TYPE_DEF | :HAS_TYPE_DEF]->(typeDec1),"
			+ "(cu2:COMPILATION_UNIT)-[:HAS_INNER_TYPE_DEF | :HAS_TYPE_DEF]->(typeDec2),"
			+ "(cu3:COMPILATION_UNIT)-[:HAS_INNER_TYPE_DEF | :HAS_TYPE_DEF]->(typeDec3)"
			+ " WHERE cu1.packageName<>cu2.packageName AND cu3.packageName=cu1.packageName "
			+ " RETURN DISTINCT cu1,cu2";
	// + " RETURN 'Warning [CMU-DCL60] There is a cycle between packages caused
	// by the dependencies between ' +EXTRACT(x IN nodes(p) |
	// x.fullyQualifiedName)+'.'";
	private static final String DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_REFINED_PART1_B = "MATCH (cu:COMPILATION_UNIT) WITH DISTINCT cu.packageName as package CREATE (p:PACKAGE{name:package})"
	// + " WITH p as package1, p as package2 "
	;
	private static final String DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_REFINED_PART2_B = "MATCH (typeDec1)-[:USES_TYPE_DEF]->(typeDec2),"
			+ "(cu1:COMPILATION_UNIT)-[:HAS_INNER_TYPE_DEF | :HAS_TYPE_DEF]->(typeDec1),"
			+ "(cu2:COMPILATION_UNIT)-[:HAS_INNER_TYPE_DEF | :HAS_TYPE_DEF]->(typeDec2) WHERE cu1.packageName <>cu2.packageName WITH DISTINCT  cu1.packageName as c1,cu2.packageName  as c2 MATCH (package1:PACKAGE{name:c1}),(package2:PACKAGE{name:c2}) CREATE (package1)-[r:DEPENDS_ON_PACKAGE]->(package2) ";

	private static final String DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_REFINED_PART3_B = "WITH package1 MATCH p=(package1)-[:DEPENDS_ON_PACKAGE*]->(package1) WITH "
			+ " REDUCE(warning='Warning [CMU-DCL60] There is a cycle between packages caused by	the dependencies between ',package IN nodes(p) | warning + (CASE WHEN SIZE(warning)>89 THEN '->' ELSE '' END) +package.name)+ '. You should undo them.'"
			+ "as warning WITH COLLECT(warning) as wList  MATCH (p:PACKAGE) DETACH DELETE p WITH DISTINCT wList RETURN wList";
	private static final String ERR54_USE_TRY_RESOURCES_TO_SAFELY_CLOSE = " MATCH (closeableSubtype)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*0..]->"
			+ "(closeableInt:INTERFACE_DEF{fullyQualifiedName:'java.lang.AutoCloseable'})"
			+ " WHERE closeableSubtype:CLASS_DEF OR closeableSubtype:INTERFACE_DEF "
			+ " WITH DISTINCT closeableSubtype.fullyQualifiedName as className "

			+ " MATCH (closeableDec:LOCAL_VAR_DEF{actualType:className})-[:MODIFIED_BY | HAS_VARIABLEDECL_INIT]->(assignOrInit)"
			+ "MATCH  (assignOrInit)<-[" + assignToOutExprQuery + "*0..]-(expr)<-[" + exprToStatQuery
			+ "]-(assignStat) "
			+ " OPTIONAL MATCH (closeableDec)<-[r:TRY_RESOURCES]-()  WITH assignStat,r, closeableDec WHERE r IS NULL"
			// + " RETURN assignStat,r, closeableDec"

			+ " OPTIONAL MATCH (mInv:METHOD_INVOCATION)-[:METHODINVOCATION_METHOD_SELECT]->(mSelect:MEMBER_SELECTION{memberName:'close'})-[:MEMBER_SELECT_EXPR]->(id)<-[:USED_BY]-(closeableDec),"
			+ " (mInv)<-[" + exprToOutExprQuery + "*0..]-(expr)<-[" + exprToStatQuery + "]-(closeStat)"
			+ ",(assignStat)-[" + cfgSuccesor + "*]->(prev)"
			+ "-[exceptionRel:CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION | :CFG_MAY_THROW | :CFG_THROWS]->(afterEx)"
			+ " WITH  COLLECT(DISTINCT closeStat) AS closes, prev,closeableDec, afterEx,exceptionRel "
			+ "WHERE NOT prev IN closes " + " MATCH (prev)-[" + cfgSuccesor
			+ "*]->(closeStat) WHERE closeStat IN closes"

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
			+ "CASE WHEN p IS NULL THEN NULL ELSE EXTRACT (index IN RANGE(0,SIZE(NODES(p))) | CASE WHEN  NODES(p)[index]:CATCH_BLOCK OR previousThrow[index] AND NODES(p)[index]:LOCAL_VAR_DEF"
			+ "   THEN 'catch' ELSE CASE WHEN previousThrow[index] THEN  CASE WHEN index=0 THEN exceptionRel.exceptionType "
			+ " ELSE RELATIONSHIPS(p)[index-1].exceptionType END ELSE CASE WHEN NODES(p)[index]:TRY_BLOCK THEN 'newtry' ELSE  NULL END END END )END"
			+ " as exFlow"
			+ " WITH p,closeableDec,prevs,CASE WHEN p IS NULL THEN NULL ELSE EXTRACT( relIndex IN RANGE(0,SIZE(RELATIONSHIPS(p))) | exFlow[LAST(FILTER( exIndex IN RANGE(0,SIZE(exFlow)) WHERE exIndex<=relIndex AND NOT exFlow[exIndex] IS NULL))]) END as exFlow "
			+ " WITH closeableDec,prevs,"
			+ " NOT ANY(x IN COLLECT(CASE WHEN p IS NULL THEN FALSE ELSE ALL(relIndex IN RANGE(0,SIZE(RELATIONSHIPS(p))) WHERE CASE WHEN TYPE(RELATIONSHIPS(p)[relIndex])='CFG_NO_EXCEPTION' THEN exFlow[relIndex] IN ['catch' , 'newtry'] ELSE CASE WHEN TYPE(RELATIONSHIPS(p)[relIndex])='CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION' THEN exFlow[relIndex]=RELATIONSHIPS(p)[relIndex].exceptionType ELSE TRUE END END  )END)WHERE x) as truePathToClose"
			+ " WITH closeableDec,COLLECT(prevs) as prevs, COLLECT(truePathToClose) as truePathToClose"
			+ " WHERE ANY ( x IN truePathToClose WHERE x) " + " MATCH (closeableDec)<-[" + getAnyRel(statToEnclClass)
			+ "*]-(enclClass) WHERE enclClass:TYPE_DEFINITION "
			+ "  RETURN DISTINCT 'Warning [CMU-ERR54] variable '+closeableDec.name+ '(defined in line'+closeableDec.lineNumber+', class '+enclClass.fullyQualifiedName+') might not be properly closed, as statement(s) (in lines '+ REDUCE(seed='',prev IN prevs | seed+prev.lineNumber+',')+') may throw an exception.'"

	;

	private static final String ALT2_MET55_RETURN_EMPTY_COLLECTIONS_INSTEAD_NULL = " MATCH (returnClass)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->(collection{fullyQualifiedName:'java.util.Collection'})"
			+ " WITH DISTINCT returnClass.fullyQualifiedName as className "
			+ " MATCH (enclosingCU)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->()-[:DECLARES_METHOD]->(md)-[:CALLABLE_RETURN_TYPE]->(rt) "
			+ ", (md)<-[*]-(ret:RETURN_STATEMENT)" + "" + " RETURN md, rt, labels(rt)";
	private static final String MET55_RETURN_EMPTY_COLLECTIONS_INSTEAD_NULL =

			" MATCH (md)-[:CALLABLE_RETURN_TYPE]->(rt)-[:ITS_TYPE_IS |:PARAMETERIZED_TYPE*0..]->()-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*0..]->(collection)\r\n"
					+ "WHERE  collection.fullyQualifiedName='java.util.Collection<E>' OR collection:ARRAY_TYPE  WITH DISTINCT md \r\n"
					+ "MATCH (enclosingCU)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->()-[:DECLARES_METHOD]->(md)<-[:CFG_END_OF]-(normalEnd)\r\n"
					+ "		<-[:CFG_NEXT_STATEMENT]-(:RETURN_STATEMENT)-[:RETURN_EXPR]->()\r\n"
					+ "		-[:CONDITIONAL_EXPR_THEN |:CONDITIONAL_EXPR_ELSE*0..]->(nullRet{typetag:'NULL_LITERAL'})\r\n"
					+ "WITH	 enclosingCU, nullRet  WHERE nullRet IS NOT NULL\r\n"
					+ "RETURN 'Warning [CMU-MET55], you must not return null when you can return an empty collection or array.Line' +nullRet.lineNumber+' in '+enclosingCU.fileName+'.'";

	// Dos formas -[*]-> RETURN o CFG_END<-[]- RETURN
	private static final String ALT_MET55_RETURN_EMPTY_COLLECTIONS_INSTEAD_NULL = " MATCH (enclosingCU)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->()-[:DECLARES_METHOD]->(md)-[:CALLABLE_RETURN_TYPE]->(rt) "
			+ " WITH md, rt "
			+ " MATCH (returnClass{fullyQualifiedName:rt.actualType})-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]->(collection{fullyQualifiedName:'java.util.Collection'})"

			+ " RETURN DISTINCT md";
	// ,actualType:'java.io.File'
	private static final String SEC56_DONT_SERIALIZE_SYSTEM_RESOURCES = "MATCH (class)-[:DECLARES_FIELD]->(f{isTransient:false})-[:ITS_TYPE_IS]->(aux)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*0..]->(fTypeOrSupertype) "
			+ ", (class)-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*]-> (superInt:INTERFACE_DEF{fullyQualifiedName:'java.io.Serializable'})  "
			+ " WHERE fTypeOrSupertype.fullyQualifiedName IN ['java.io.File' ,'org.omg.CosNaming.NamingContext' ,'org.omg.CORBA.DomainManager' ,'org.omg.PortableInterceptor.ObjectReferenceFactory']"
			+ " RETURN DISTINCT "
			// + " enclosingCU, class, f, fTypeOrSupertype, superInt,aux,p"
			+ "'Warning [CMU-SEC56], you must not serialize direct handles to system resources like field ' + f.name + '(an instance of '+fTypeOrSupertype.fullyQualifiedName+'). Line '+ f.lineNumber + ' in '+ class.fullyQualifiedName+'.'";

	private static final String DECL56_ORDINAL_ENUM = "MATCH (enclosingCU)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->()-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(enclosingM)-[:CALLS]->"
			+ "(inv)-[:HAS_DEF]->(md) " + " WHERE md.fullyQualifiedName ='java.lang.Enum:ordinal()int' "
			+ "RETURN 'Warning [CMU-DEC56], you should not attach significance to the ordinal of an enum. Line '+inv.lineNumber + ' in '+ enclosingCU.fileName +'.' ";

	private static final String MET50_AVOID_CONFUSING_OVERLOADING = "MATCH (enclosingCU)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->(class)"
			+ "-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(md)-[:CALLABLE_HAS_PARAMETER]->(p:PARAMETER_DEF)" + " "
			+ "WITH enclosingCU, md, class, COLLECT(p.actualType) as params  "
			+ " MATCH (class)-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(md2)-[:CALLABLE_HAS_PARAMETER]->(p2)"
			+ " WHERE md.name = md2.name AND md <> md2 "
			+ " WITH  enclosingCU, md, md2, params, COLLECT(p2.actualType) as params2 "
			+ "WHERE ALL(p IN params WHERE p IN params2) AND all(p IN params2 WHERE "
			+ "p IN params) AND ((SIZE(params)>=4 AND SIZE(params2)>=4) OR " + " SIZE(params)=SIZE(params2)) "
			+ " RETURN 'Warning [CMU-MET50], you must avoid confusing overloadings like '+ md.fullyQualifiedName + ' in line ' + md.lineNumber + ' ( very similar to declaration in line ' + md2.lineNumber +') in '+ enclosingCU.fileName +'.' ";

	private static final String MET53_ENSURE_CALL_SUPER_IN_CLONE = "MATCH (enclosingCU)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->(typeDec)-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(md) WHERE "
			+ "  md.fullyQualifiedName CONTAINS ':clone()'"

			+ " OPTIONAL MATCH (md)-[:CALLS | :HAS_DEF*]->(superDec)" + " WHERE "
			+ " superDec.fullyQualifiedName CONTAINS ':clone()' AND "
			+ " NOT superDec.fullyQualifiedName CONTAINS (typeDec.fullyQualifiedName+':')"
			+ " WITH enclosingCU, md, COUNT(superDec) as superCallsCount " + " WHERE superCallsCount=0 "
			+ " RETURN  'Warning [CMU-MET53], you must call super.clone in every overriden clone method. Line '+ md.lineNumber + ' in ' + enclosingCU.fileName + '.'";

	// OJO, AQUI COMO SABES QUE NO ES Object o; o.clone()... donde
	// Object:clone<-ov-method_clone SE necesita comprobación super.clone
	private static final String ALTERN_MET53_ENSURE_CALL_SUPER_IN_CLONE = "MATCH (enclosingCU)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->()-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]->(md) "
			+ " WHERE  md.fullyQualifiedName CONTAINS ':clone()' AND NOT md.fullyQualifiedName ENDS WITH ')void' "

			+ " OPTIONAL MATCH (md)-[:CALLS]->(inv)-[:HAS_DEF]->(superDec)<-[:OVERRIDES]-(md), (inv)-[:METHODINVOCATION_METHOD_SELECT]->()-[:MEMBER_SELECT_EXPR]->({name:'super'})"

			+ " WITH enclosingCU, md, COUNT(superDec) as superCallsCount" + " WHERE superCallsCount=0 "
			+ " RETURN  'Warning [CMU-MET53], you must call super.clone in every overriden clone method. Line '+ md.lineNumber + ' in ' + enclosingCU.fileName + '.'";
	private static final String OBJ50_DONT_CONFUSE_INMUTABLE_REFS_WITH_OBJECTS = "MATCH (declaringType{accessLevel:'public'})-[:DECLARES_FIELD]->(attr:ATTR_DEC{typeKind:'DECLARED'}) WHERE attr.isFinal AND attr.accessLevel='public'"
			// First we find all the declared public final attributes
			+ " WITH attr, declaringType"
			+ " MATCH (typeDec)-[:DECLARES_FIELD]->(field) WHERE (typeDec:CLASS_DECLARATION OR typeDec:ENUM_DECLARATION) AND typeDec.fullyQualifiedName=attr.actualType"
			// Here, we find the type of each of these attributes, and for each
			// type we get all the declared attributes
			+ " OPTIONAL MATCH (field)-[r:STATE_MODIFIED_BY | :STATE_MAY_BE_MODIFIED_BY | :MODIFIED_BY]->(ass)"
			// Here we look for modifications or state changes in this collected
			// attributes
			+ " WITH attr,declaringType,field, typeDec, COUNT(DISTINCT r) as relsC"

			+ " WHERE (field.accessLevel='public' AND NOT field.isFinal) OR relsC>0 "
			// Finally, if any of these attributes has a relationship that
			// implies a modification or a state change,
			// or, if any of these attributes is public and not final, we emit a
			// warning to the programmer
			+ " RETURN 'Warning [CMU-OBJ50] Attribute '+ attr.name+' declared in '+declaringType.fullyQualifiedName+ ' is not actually final, only the reference. This is due to the type '+ typeDec.fullyQualifiedName+' is mutable ( the state of the attributes '+COLLECT(field.name)+' change in the program or may potentially be changed by a client).'";

	// "MATCH p=(typeDec)-[:DECLARES_FIELD | :ITS_TYPE_IS*]->(fieldOrType) WHERE
	// typeDec:CLASS_DECLARATION OR typeDec:ENUM_DECLARATION RETURN
	// typeDec.fullyQualifiedName,COLLECT(EXTRACT(n IN NODES(p) | CASE WHEN
	// n:ATTR_DEC THEN n.name+'('+n.actualType+')' ELSE n.fullyQualifiedName
	// END))"
	public static void Pmain(String[] args) throws IOException {
		int queryIndex = args.length == 0 ? 10 : Integer.parseInt(args[0]);
		// GraphDatabaseService gs = DatabaseFachade.getDB();
		// String query = QUERIES[queryIndex];
		String query = // "MATCH (n:METHOD_DEC)-[r]-(m) WHERE n.name='confusing'
						// RETURN n, LABELS(n), m,r, LABELS(m)"
				new OBJ50(true).queryToString();
		// System.out.println(query);
		long ini = System.nanoTime();
		String[] queries = new String[] { "MATCH (n) RETURN DISTINCT n", "MATCH ()-[r]->() RETURN DISTINCT r",
				"MATCH (n) WHERE n:CLASS_DECLARATION OR n:INTERFACE_DECLARATION OR n:ENUM_DECLARATION RETURN DISTINCT n",
				"MATCH (n) WHERE (n:CLASS_DECLARATION OR n:INTERFACE_DECLARATION OR n:ENUM_DECLARATION) AND n.isDeclared RETURN DISTINCT n",
				"MATCH (n) WHERE n:METHOD_DEC OR n:CONSTRUCTOR_DEC RETURN DISTINCT n",
				"MATCH (n) WHERE (n:METHOD_DEC OR n:CONSTRUCTOR_DEC) AND n.isDeclared RETURN DISTINCT n",
				"MATCH ()-[r:IS_SUBTYPE_EXTENDS | IS_SUBTYPE_IMPLEMENTS ]->() RETURN DISTINCT r",
				"MATCH ()-[r:USES_TYPE_DEF]->() RETURN DISTINCT r",
				"MATCH ()-[r:" + CFGRelationTypes.getCFGRelations() + "]->() RETURN DISTINCT r",
				"MATCH ()-[r: MODIFIED_BY | STATE_MODIFIED_BY | USED_BY | STATE_MAY_BE_MODIFIED_BY ]->() RETURN DISTINCT r",
				"MATCH ()-[r:CALLS | HAS_DEF | OVERRIDES | REFER_TO | MAY_REFER_TO]->() RETURN DISTINCT r",
				"MATCH ()-[r:HAS_INNER_TYPE_DEF]->() RETURN DISTINCT r",
				"MATCH ()-[r:ITS_TYPE_IS]->() RETURN DISTINCT r",
				"MATCH (n) WHERE n:INITIALIZATION OR  n:CFG_METHOD_END OR n:CFG_METHOD_ENTRY OR n:CFG_EXCEPTIONAL_END OR n:CFG_LAST_STATEMENT_IN_FINALLY RETURN DISTINCT n",
				"MATCH (n:COMPILATION_UNIT) RETURN n", "MATCH (n) RETURN DISTINCT SUM(SIZE(keys(n))) as res",
				"MATCH ()-[r]->() RETURN DISTINCT SUM(SIZE(keys(r))) as  res"

		};
		// Result res = gs.execute(queries[queryIndex]);
		// System.out.println("4444");
		long end = System.nanoTime();
		// BufferedWriter bw = new BufferedWriter(new FileWriter(new
		// File("outputPlan.txt")));
		// bw.write(res.resultAsString());
		// bw.write(res.getExecutionPlanDescription().toString());
		// bw.close();
		//
		// if (queryIndex < queries.length - 2)
		// // System.out.print(res.resultAsString());
		// System.out.print(res.stream().count());
		// else
		// System.out.println((Long) res.next().get("res"));

		// gs.execute(MainQuery.DELETE_ALL);
		// gs.shutdown();

	}

	private static final String OBJ50_Q7_PAPER_VERSION = "MATCH (declaration:VARIABLE_DEF{isFinal:true})-[r:STATE_MODIFIED_BY|STATE_MAY_BE_MODIFIED_BY]->(mutatorExpr)\r\n"
			+ "\r\n"
			+ "MATCH (mutatorExpr)<-[:ARRAYACCESS_EXPR|ARRAYACCESS_INDEX|ASSIGNMENT_LHS|ASSIGNMENT_RHS|BINOP_LHS|BINOP_RHS|CAST_ENCLOSES|COMPOUND_ASSIGNMENT_LHS|COMPOUND_ASSIGNMENT_RHS|CONDITIONAL_EXPR_CONDITION|CONDITIONAL_EXPR_THEN|CONDITIONAL_EXPR_ELSE|INITIALIZATION_EXPR|INSTANCE_OF_EXPR|MEMBER_REFERENCE_EXPRESSION|MEMBER_SELECT_EXPR|METHODINVOCATION_ARGUMENTS|METHODINVOCATION_METHOD_SELECT|NEW_CLASS_ARGUMENTS|NEW_ARRAY_INIT|NEW_ARRAY_DIMENSION|UNARY_ENCLOSES*0..]-()<-[:ASSERT_CONDITION|DO_WHILE_CONDITION|ENCLOSES_EXPR|FOREACH_EXPR|FORLOOP_CONDITION|HAS_VARIABLEDECL_INIT|IF_CONDITION|RETURN_EXPR|SWITCH_EXPR|SYNCHRONIZED_EXPR|THROW_EXPR|WHILE_CONDITION]-(stat)<-[:CASE_STATEMENTS|CATCH_ENCLOSES_BLOCK|CATCH_PARAM|ENCLOSES|DO_WHILE_STATEMENT|WHILE_STATEMENT|FOREACH_STATEMENT|FOREACH_VAR|FORLOOP_INIT|FORLOOP_STATEMENT|FORLOOP_UPDATE|CALLABLE_HAS_PARAMETER|IF_THEN|IF_ELSE|LABELED_STMT_ENCLOSES|SWITCH_ENCLOSES_CASE|SYNCHRONIZED_ENCLOSES_BLOCK|TRY_BLOCK|TRY_CATCH|TRY_FINALLY|TRY_RESOURCES*]-()<-[:CALLABLE_HAS_BODY]-(method)<-[:DECLARES_FIELD|DECLARES_METHOD|DECLARES_CONSTRUCTOR|HAS_STATIC_INIT]-(exprEnclClass)\r\n"
			+ " WHERE  NOT declaration:ATTR_DEF  OR r.isOwnAccess=FALSE OR NOT method.isInitializer \r\n"
			+ "WITH declaration, mutatorExpr,exprEnclClass \r\n"
			+ "MATCH (declaration)<-[:CALLABLE_HAS_BODY|CALLABLE_HAS_PARAMETER|CASE_STATEMENTS|CATCH_ENCLOSES_BLOCK|CATCH_PARAM|ENCLOSES|DO_WHILE_STATEMENT|WHILE_STATEMENT|FOREACH_STATEMENT|FOREACH_VAR|FORLOOP_INIT|FORLOOP_STATEMENT|FORLOOP_UPDATE|HAS_VARIABLEDECL_INIT|IF_THEN|IF_ELSE|LABELED_STMT_ENCLOSES|SWITCH_ENCLOSES_CASE|SYNCHRONIZED_ENCLOSES_BLOCK|TRY_BLOCK|TRY_CATCH|TRY_FINALLY|TRY_RESOURCES*0..]-()<-[:DECLARES_FIELD|DECLARES_METHOD|DECLARES_CONSTRUCTOR|HAS_STATIC_INIT]-(enclClass)\r\n"
			+ "WITH declaration, enclClass,COLLECT(LAST(LABELS(mutatorExpr))+', line '+mutatorExpr.lineNumber+ ', column '+mutatorExpr.column+ ', class '+ exprEnclClass.fullyQualifiedName) as mutatorsMessage\r\n"
			+ "RETURN 'Warning [CMU-OBJ50] Declaration with name '+ declaration.name+ '( in line ' + declaration.lineNumber +', class ' +enclClass.fullyQualifiedName + ') is not actually final, only the reference. Concretely, '+ declaration.name +' may be mutated in '+ REDUCE(seed=mutatorsMessage[0], x IN mutatorsMessage[1..] | seed+','+x) \r\n"

	;
	public static final Rule[] RULES = new Rule[] { new Rule(MET53_ENSURE_CALL_SUPER_IN_CLONE),
			new Rule(MET55_RETURN_EMPTY_COLLECTIONS_INSTEAD_NULL), new Rule(SEC56_DONT_SERIALIZE_SYSTEM_RESOURCES),
			new Rule(DECL56_ORDINAL_ENUM), new Rule(MET50_AVOID_CONFUSING_OVERLOADING),
			/*
			 * new Rule( DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_REFINED_PART1_B
			 * 
			 * + DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_REFINED_PART2_B +
			 * DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_REFINED_PART3_B) ,
			 */
			new Rule(DCL60_AVOID_CYCLIC_DEPENDENCIES_BETWEEN_PACKAGES_END), new Rule(

					new OBJ54(true).queryToString()),
			// new Rule(new OBJ50_SIMPLIFIED().queryToString())
			new Rule(OBJ50_Q7_PAPER_VERSION), new Rule(ERR54_USE_TRY_RESOURCES_TO_SAFELY_CLOSE),
			new Rule(MET52_DO_NOT_USE_CLONE_WITH_UNTRUSTED_PARAMETERS),
			// MINIMIZE V2
			new Rule(DCL53_MINIMIZE_SCOPE_OF_VARIABLES_V3),
			// new
			// Rule(OBJ51_MINIMIZE_ACCESSIBILITY_OF_CLASSES_AND_MEMBERS_PART_ONE
			// + " UNION ALL "
			// + OBJ51_MINIMIZE_ACCESSIBILITY_OF_CLASSES_AND_MEMBERS_PART_TWO)
			new Rule(new OBJ56_SIMPLIFIED().queryToString()),
			new Rule(NUM50CONVERT_INT_TO_FLOAT_FOR_FLOAT_OPERATIONS) };
	private static final String DEPS_OPENNLP = "[['opennlp.PrepAttachDataUtil','opennlp.model.AbstractModel'],['opennlp.PrepAttachDataUtil','opennlp.model.EventStream'],['opennlp.PrepAttachDataUtil','opennlp.model.Event'],['opennlp.PrepAttachDataUtil','opennlp.model.ListEventStream'],['opennlp.PrepAttachDataUtil','opennlp.perceptron.PerceptronPrepAttachTest'],['opennlp.model.AbstractModel','opennlp.model.MaxentModel'],['opennlp.model.AbstractModel','opennlp.model.Prior'],['opennlp.model.AbstractModel','opennlp.model.EvalParameters'],['opennlp.model.AbstractModel','opennlp.model.Context'],['opennlp.model.AbstractModel','opennlp.model.AbstractModel.ModelType'],['opennlp.model.AbstractModel','opennlp.model.IndexHashTable'],['opennlp.model.EventStream','opennlp.model.Event'],['opennlp.maxent.BasicContextGenerator','opennlp.maxent.ContextGenerator'],['opennlp.maxent.BasicEventStream','opennlp.maxent.BasicContextGenerator'],['opennlp.maxent.BasicEventStream','opennlp.maxent.ContextGenerator'],['opennlp.maxent.BasicEventStream','opennlp.model.AbstractEventStream'],['opennlp.maxent.BasicEventStream','opennlp.model.Event'],['opennlp.maxent.BasicEventStream','opennlp.maxent.DataStream'],['opennlp.model.AbstractEventStream','opennlp.model.EventStream'],['opennlp.maxent.DomainToModelMap','opennlp.maxent.ModelDomain'],['opennlp.maxent.DomainToModelMap','opennlp.model.MaxentModel'],['opennlp.maxent.Evalable','opennlp.model.MaxentModel'],['opennlp.maxent.Evalable','opennlp.model.EventCollector'],['opennlp.model.EventCollector','opennlp.model.Event'],['opennlp.maxent.GIS','opennlp.model.EventStream'],['opennlp.maxent.GIS','opennlp.model.DataIndexer'],['opennlp.maxent.GIS','opennlp.model.Prior'],['opennlp.maxent.GIS','opennlp.maxent.GISModel'],['opennlp.maxent.GIS','opennlp.maxent.GISTrainer'],['opennlp.maxent.GIS','opennlp.model.UniformPrior'],['opennlp.maxent.GISModel','opennlp.model.AbstractModel'],['opennlp.maxent.GISModel','opennlp.model.Prior'],['opennlp.maxent.GISModel','opennlp.model.EvalParameters'],['opennlp.maxent.GISModel','opennlp.model.Context'],['opennlp.maxent.GISModel','opennlp.maxent.io.SuffixSensitiveGISModelReader'],['opennlp.maxent.GISModel','opennlp.model.AbstractModel.ModelType'],['opennlp.maxent.GISModel','opennlp.model.UniformPrior'],['opennlp.model.EvalParameters','opennlp.model.Context'],['opennlp.maxent.GISTrainer.ModelExpactationComputeTask','opennlp.maxent.GISModel'],['opennlp.maxent.GISTrainer.ModelExpactationComputeTask','opennlp.maxent.GISTrainer'],['opennlp.maxent.GISTrainer','opennlp.model.EventStream'],['opennlp.maxent.GISTrainer','opennlp.model.DataIndexer'],['opennlp.maxent.GISTrainer','opennlp.model.Prior'],['opennlp.maxent.GISTrainer','opennlp.maxent.GISModel'],['opennlp.maxent.GISTrainer','opennlp.model.EvalParameters'],['opennlp.maxent.GISTrainer','opennlp.maxent.GISTrainer.ModelExpactationComputeTask'],['opennlp.maxent.GISTrainer','opennlp.model.MutableContext'],['opennlp.maxent.GISTrainer','opennlp.model.OnePassDataIndexer'],['opennlp.maxent.GISTrainer','opennlp.model.UniformPrior'],['opennlp.maxent.MaxentPrepAttachTest','opennlp.PrepAttachDataUtil'],['opennlp.maxent.MaxentPrepAttachTest','opennlp.model.AbstractModel'],['opennlp.maxent.MaxentPrepAttachTest','opennlp.maxent.GISTrainer'],['opennlp.maxent.MaxentPrepAttachTest','opennlp.model.TrainUtil'],['opennlp.maxent.MaxentPrepAttachTest','opennlp.model.TwoPassDataIndexer'],['opennlp.maxent.MaxentPrepAttachTest','opennlp.model.UniformPrior'],['opennlp.maxent.ModelApplier','opennlp.model.EventStream'],['opennlp.maxent.ModelApplier','opennlp.maxent.BasicContextGenerator'],['opennlp.maxent.ModelApplier','opennlp.maxent.ContextGenerator'],['opennlp.maxent.ModelApplier','opennlp.maxent.BasicEventStream'],['opennlp.maxent.ModelApplier','opennlp.model.Event'],['opennlp.maxent.ModelApplier','opennlp.model.MaxentModel'],['opennlp.maxent.ModelApplier','opennlp.maxent.DoubleStringPair'],['opennlp.maxent.ModelApplier','opennlp.maxent.PlainTextByLineDataStream'],['opennlp.maxent.ModelApplier','opennlp.model.GenericModelReader'],['opennlp.maxent.ModelApplier','opennlp.model.RealValueFileEventStream'],['opennlp.maxent.ModelReplacementManager','opennlp.model.MaxentModel'],['opennlp.maxent.ModelReplacementManager','opennlp.maxent.ModelSetter'],['opennlp.maxent.ModelSetter','opennlp.model.MaxentModel'],['opennlp.maxent.ModelTrainer','opennlp.model.AbstractModel'],['opennlp.maxent.ModelTrainer','opennlp.model.EventStream'],['opennlp.maxent.ModelTrainer','opennlp.maxent.BasicEventStream'],['opennlp.maxent.ModelTrainer','opennlp.maxent.GIS'],['opennlp.maxent.ModelTrainer','opennlp.maxent.PlainTextByLineDataStream'],['opennlp.maxent.ModelTrainer','opennlp.maxent.RealBasicEventStream'],['opennlp.maxent.ModelTrainer','opennlp.model.AbstractModelWriter'],['opennlp.maxent.ModelTrainer','opennlp.maxent.io.SuffixSensitiveGISModelWriter'],['opennlp.maxent.ModelTrainer','opennlp.model.OnePassDataIndexer'],['opennlp.maxent.ModelTrainer','opennlp.model.OnePassRealValueDataIndexer'],['opennlp.maxent.ModelTrainer','opennlp.perceptron.PerceptronTrainer'],['opennlp.maxent.ModelTrainer','opennlp.perceptron.SuffixSensitivePerceptronModelWriter'],['opennlp.maxent.PlainTextByLineDataStream','opennlp.maxent.DataStream'],['opennlp.maxent.RealBasicEventStream','opennlp.model.EventStream'],['opennlp.maxent.RealBasicEventStream','opennlp.maxent.BasicContextGenerator'],['opennlp.maxent.RealBasicEventStream','opennlp.maxent.ContextGenerator'],['opennlp.maxent.RealBasicEventStream','opennlp.model.AbstractEventStream'],['opennlp.maxent.RealBasicEventStream','opennlp.model.Event'],['opennlp.maxent.RealBasicEventStream','opennlp.maxent.DataStream'],['opennlp.maxent.RealBasicEventStream','opennlp.maxent.PlainTextByLineDataStream'],['opennlp.maxent.RealBasicEventStream','opennlp.model.RealValueFileEventStream'],['opennlp.maxent.RealValueModelTest','opennlp.maxent.GIS'],['opennlp.maxent.RealValueModelTest','opennlp.maxent.GISModel'],['opennlp.maxent.RealValueModelTest','opennlp.model.FileEventStream'],['opennlp.maxent.RealValueModelTest','opennlp.model.OnePassRealValueDataIndexer'],['opennlp.maxent.RealValueModelTest','opennlp.model.RealValueFileEventStream'],['opennlp.maxent.ScaleDoesntMatterTest','opennlp.model.EventStream'],['opennlp.maxent.ScaleDoesntMatterTest','opennlp.model.MaxentModel'],['opennlp.maxent.ScaleDoesntMatterTest','opennlp.maxent.GIS'],['opennlp.maxent.ScaleDoesntMatterTest','opennlp.maxent.PlainTextByLineDataStream'],['opennlp.maxent.ScaleDoesntMatterTest','opennlp.maxent.RealBasicEventStream'],['opennlp.maxent.ScaleDoesntMatterTest','opennlp.model.OnePassRealValueDataIndexer'],['opennlp.maxent.ScaleDoesntMatterTest','opennlp.model.RealValueFileEventStream'],['opennlp.maxent.TrainEval','opennlp.model.EventStream'],['opennlp.maxent.TrainEval','opennlp.model.Event'],['opennlp.maxent.TrainEval','opennlp.model.MaxentModel'],['opennlp.maxent.TrainEval','opennlp.maxent.Evalable'],['opennlp.maxent.TrainEval','opennlp.maxent.GIS'],['opennlp.maxent.io.BinaryGISModelReader','opennlp.maxent.io.GISModelReader'],['opennlp.maxent.io.BinaryGISModelReader','opennlp.model.BinaryFileDataReader'],['opennlp.maxent.io.GISModelReader','opennlp.model.AbstractModel'],['opennlp.maxent.io.GISModelReader','opennlp.maxent.GISModel'],['opennlp.maxent.io.GISModelReader','opennlp.model.Context'],['opennlp.maxent.io.GISModelReader','opennlp.model.AbstractModelReader'],['opennlp.maxent.io.GISModelReader','opennlp.model.DataReader'],['opennlp.maxent.io.BinaryGISModelWriter','opennlp.model.MaxentModel'],['opennlp.maxent.io.BinaryGISModelWriter','opennlp.maxent.io.GISModelWriter'],['opennlp.maxent.io.GISModelWriter','opennlp.model.MaxentModel'],['opennlp.maxent.io.GISModelWriter','opennlp.model.Context'],['opennlp.maxent.io.GISModelWriter','opennlp.model.AbstractModelWriter'],['opennlp.maxent.io.GISModelWriter','opennlp.model.ComparablePredicate'],['opennlp.maxent.io.GISModelWriter','opennlp.model.IndexHashTable'],['opennlp.model.AbstractModelReader','opennlp.model.AbstractModel'],['opennlp.model.AbstractModelReader','opennlp.model.Context'],['opennlp.model.AbstractModelReader','opennlp.model.DataReader'],['opennlp.model.AbstractModelReader','opennlp.model.BinaryFileDataReader'],['opennlp.model.AbstractModelReader','opennlp.model.PlainTextFileDataReader'],['opennlp.maxent.io.ObjectGISModelReader','opennlp.maxent.io.GISModelReader'],['opennlp.maxent.io.ObjectGISModelReader','opennlp.model.ObjectDataReader'],['opennlp.maxent.io.ObjectGISModelWriter','opennlp.model.AbstractModel'],['opennlp.maxent.io.ObjectGISModelWriter','opennlp.maxent.io.GISModelWriter'],['opennlp.maxent.io.OldFormatGISModelReader','opennlp.model.Context'],['opennlp.maxent.io.OldFormatGISModelReader','opennlp.model.AbstractModelReader'],['opennlp.maxent.io.OldFormatGISModelReader','opennlp.maxent.io.PlainTextGISModelReader'],['opennlp.maxent.io.OldFormatGISModelReader','opennlp.maxent.io.SuffixSensitiveGISModelWriter'],['opennlp.maxent.io.PlainTextGISModelReader','opennlp.maxent.io.GISModelReader'],['opennlp.maxent.io.PlainTextGISModelReader','opennlp.model.PlainTextFileDataReader'],['opennlp.maxent.io.PlainTextGISModelWriter','opennlp.model.AbstractModel'],['opennlp.maxent.io.PlainTextGISModelWriter','opennlp.maxent.io.GISModelWriter'],['opennlp.maxent.io.PooledGISModelReader','opennlp.maxent.io.SuffixSensitiveGISModelReader'],['opennlp.maxent.io.SuffixSensitiveGISModelReader','opennlp.model.AbstractModel'],['opennlp.maxent.io.SuffixSensitiveGISModelReader','opennlp.maxent.io.GISModelReader'],['opennlp.maxent.io.SuffixSensitiveGISModelReader','opennlp.maxent.io.SuffixSensitiveGISModelWriter'],['opennlp.maxent.io.RealValueFileEventStreamTest','opennlp.model.OnePassRealValueDataIndexer'],['opennlp.maxent.io.RealValueFileEventStreamTest','opennlp.model.RealValueFileEventStream'],['opennlp.maxent.io.SuffixSensitiveGISModelWriter','opennlp.model.AbstractModel'],['opennlp.maxent.io.SuffixSensitiveGISModelWriter','opennlp.maxent.io.BinaryGISModelWriter'],['opennlp.maxent.io.SuffixSensitiveGISModelWriter','opennlp.maxent.io.GISModelWriter'],['opennlp.maxent.io.SuffixSensitiveGISModelWriter','opennlp.maxent.io.PlainTextGISModelWriter'],['opennlp.maxent.io.TwoPassRealValueDataIndexerTest','opennlp.model.EventStream'],['opennlp.maxent.io.TwoPassRealValueDataIndexerTest','opennlp.model.MaxentModel'],['opennlp.maxent.io.TwoPassRealValueDataIndexerTest','opennlp.maxent.GIS'],['opennlp.maxent.io.TwoPassRealValueDataIndexerTest','opennlp.maxent.PlainTextByLineDataStream'],['opennlp.maxent.io.TwoPassRealValueDataIndexerTest','opennlp.maxent.RealBasicEventStream'],['opennlp.maxent.io.TwoPassRealValueDataIndexerTest','opennlp.model.OnePassRealValueDataIndexer'],['opennlp.maxent.io.TwoPassRealValueDataIndexerTest','opennlp.model.RealValueFileEventStream'],['opennlp.maxent.io.TwoPassRealValueDataIndexerTest','opennlp.model.TwoPassRealValueDataIndexer'],['opennlp.model.AbstractDataIndexer','opennlp.model.DataIndexer'],['opennlp.model.AbstractDataIndexer','opennlp.model.ComparableEvent'],['opennlp.model.AbstractModel.ModelType','opennlp.model.AbstractModel'],['opennlp.model.BinaryFileDataReader','opennlp.model.DataReader'],['opennlp.model.DynamicEvalParameters','opennlp.model.Context'],['opennlp.model.EventCollectorAsStream','opennlp.model.AbstractEventStream'],['opennlp.model.EventCollectorAsStream','opennlp.model.Event'],['opennlp.model.EventCollectorAsStream','opennlp.model.EventCollector'],['opennlp.model.FileEventStream','opennlp.model.AbstractModel'],['opennlp.model.FileEventStream','opennlp.model.EventStream'],['opennlp.model.FileEventStream','opennlp.model.AbstractEventStream'],['opennlp.model.FileEventStream','opennlp.model.Event'],['opennlp.model.FileEventStream','opennlp.maxent.GIS'],['opennlp.model.FileEventStream','opennlp.maxent.io.SuffixSensitiveGISModelWriter'],['opennlp.model.GenericModelReader','opennlp.model.AbstractModel'],['opennlp.model.GenericModelReader','opennlp.maxent.io.GISModelReader'],['opennlp.model.GenericModelReader','opennlp.model.AbstractModelReader'],['opennlp.model.GenericModelReader','opennlp.model.DataReader'],['opennlp.model.GenericModelReader','opennlp.model.GenericModelWriter'],['opennlp.model.GenericModelReader','opennlp.perceptron.PerceptronModelReader'],['opennlp.model.GenericModelWriter','opennlp.model.AbstractModel'],['opennlp.model.GenericModelWriter','opennlp.maxent.io.BinaryGISModelWriter'],['opennlp.model.GenericModelWriter','opennlp.model.AbstractModelWriter'],['opennlp.model.GenericModelWriter','opennlp.maxent.io.PlainTextGISModelWriter'],['opennlp.model.GenericModelWriter','opennlp.model.AbstractModel.ModelType'],['opennlp.model.GenericModelWriter','opennlp.perceptron.BinaryPerceptronModelWriter'],['opennlp.model.GenericModelWriter','opennlp.perceptron.PlainTextPerceptronModelWriter'],['opennlp.model.HashSumEventStream','opennlp.model.EventStream'],['opennlp.model.HashSumEventStream','opennlp.model.Event'],['opennlp.model.IndexHashTableTest','opennlp.model.IndexHashTable'],['opennlp.model.ListEventStream','opennlp.model.EventStream'],['opennlp.model.ListEventStream','opennlp.model.Event'],['opennlp.model.MutableContext','opennlp.model.Context'],['opennlp.model.ObjectDataReader','opennlp.model.DataReader'],['opennlp.model.OnePassDataIndexer','opennlp.model.EventStream'],['opennlp.model.OnePassDataIndexer','opennlp.model.Event'],['opennlp.model.OnePassDataIndexer','opennlp.model.AbstractDataIndexer'],['opennlp.model.OnePassDataIndexer','opennlp.model.ComparableEvent'],['opennlp.model.OnePassRealValueDataIndexer','opennlp.model.EventStream'],['opennlp.model.OnePassRealValueDataIndexer','opennlp.model.Event'],['opennlp.model.OnePassRealValueDataIndexer','opennlp.model.AbstractDataIndexer'],['opennlp.model.OnePassRealValueDataIndexer','opennlp.model.ComparableEvent'],['opennlp.model.OnePassRealValueDataIndexer','opennlp.model.OnePassDataIndexer'],['opennlp.model.PlainTextFileDataReader','opennlp.model.DataReader'],['opennlp.model.RealValueFileEventStream','opennlp.model.AbstractModel'],['opennlp.model.RealValueFileEventStream','opennlp.model.EventStream'],['opennlp.model.RealValueFileEventStream','opennlp.model.Event'],['opennlp.model.RealValueFileEventStream','opennlp.maxent.GIS'],['opennlp.model.RealValueFileEventStream','opennlp.maxent.io.SuffixSensitiveGISModelWriter'],['opennlp.model.RealValueFileEventStream','opennlp.model.FileEventStream'],['opennlp.model.RealValueFileEventStream','opennlp.model.OnePassRealValueDataIndexer'],['opennlp.model.RealValueFileEventStream2','opennlp.model.AbstractModel'],['opennlp.model.RealValueFileEventStream2','opennlp.model.EventStream'],['opennlp.model.RealValueFileEventStream2','opennlp.model.AbstractEventStream'],['opennlp.model.RealValueFileEventStream2','opennlp.model.Event'],['opennlp.model.RealValueFileEventStream2','opennlp.maxent.GIS'],['opennlp.model.RealValueFileEventStream2','opennlp.maxent.io.SuffixSensitiveGISModelWriter'],['opennlp.model.RealValueFileEventStream2','opennlp.model.FileEventStream'],['opennlp.model.Sequence','opennlp.model.Event'],['opennlp.model.SequenceStream','opennlp.model.AbstractModel'],['opennlp.model.SequenceStream','opennlp.model.Event'],['opennlp.model.SequenceStream','opennlp.model.Sequence'],['opennlp.model.SequenceStreamEventStream','opennlp.model.EventStream'],['opennlp.model.SequenceStreamEventStream','opennlp.model.Event'],['opennlp.model.SequenceStreamEventStream','opennlp.model.Sequence'],['opennlp.model.SequenceStreamEventStream','opennlp.model.SequenceStream'],['opennlp.model.TrainUtil','opennlp.model.AbstractModel'],['opennlp.model.TrainUtil','opennlp.model.EventStream'],['opennlp.model.TrainUtil','opennlp.maxent.GIS'],['opennlp.model.TrainUtil','opennlp.model.DataIndexer'],['opennlp.model.TrainUtil','opennlp.model.HashSumEventStream'],['opennlp.model.TrainUtil','opennlp.model.OnePassDataIndexer'],['opennlp.model.TrainUtil','opennlp.model.SequenceStream'],['opennlp.model.TrainUtil','opennlp.model.TwoPassDataIndexer'],['opennlp.model.TrainUtil','opennlp.perceptron.PerceptronTrainer'],['opennlp.model.TrainUtil','opennlp.perceptron.SimplePerceptronSequenceTrainer'],['opennlp.model.TwoPassDataIndexer','opennlp.model.EventStream'],['opennlp.model.TwoPassDataIndexer','opennlp.model.Event'],['opennlp.model.TwoPassDataIndexer','opennlp.model.AbstractDataIndexer'],['opennlp.model.TwoPassDataIndexer','opennlp.model.ComparableEvent'],['opennlp.model.TwoPassDataIndexer','opennlp.model.FileEventStream'],['opennlp.model.TwoPassRealValueDataIndexer','opennlp.model.EventStream'],['opennlp.model.TwoPassRealValueDataIndexer','opennlp.model.Event'],['opennlp.model.TwoPassRealValueDataIndexer','opennlp.model.AbstractDataIndexer'],['opennlp.model.TwoPassRealValueDataIndexer','opennlp.model.ComparableEvent'],['opennlp.model.TwoPassRealValueDataIndexer','opennlp.model.RealValueFileEventStream2'],['opennlp.model.TwoPassRealValueDataIndexer','opennlp.model.TwoPassDataIndexer'],['opennlp.model.UniformPrior','opennlp.model.Prior'],['opennlp.perceptron.BinaryPerceptronModelReader','opennlp.model.BinaryFileDataReader'],['opennlp.perceptron.BinaryPerceptronModelReader','opennlp.perceptron.PerceptronModelReader'],['opennlp.perceptron.PerceptronModelReader','opennlp.model.AbstractModel'],['opennlp.perceptron.PerceptronModelReader','opennlp.model.Context'],['opennlp.perceptron.PerceptronModelReader','opennlp.model.AbstractModelReader'],['opennlp.perceptron.PerceptronModelReader','opennlp.model.DataReader'],['opennlp.perceptron.PerceptronModelReader','opennlp.perceptron.PerceptronModel'],['opennlp.perceptron.BinaryPerceptronModelWriter','opennlp.model.MaxentModel'],['opennlp.perceptron.BinaryPerceptronModelWriter','opennlp.perceptron.PerceptronModelWriter'],['opennlp.perceptron.PerceptronModelWriter','opennlp.model.MaxentModel'],['opennlp.perceptron.PerceptronModelWriter','opennlp.model.Context'],['opennlp.perceptron.PerceptronModelWriter','opennlp.model.AbstractModelWriter'],['opennlp.perceptron.PerceptronModelWriter','opennlp.model.ComparablePredicate'],['opennlp.perceptron.PerceptronModelWriter','opennlp.model.IndexHashTable'],['opennlp.perceptron.PerceptronModel','opennlp.model.AbstractModel'],['opennlp.perceptron.PerceptronModel','opennlp.model.EvalParameters'],['opennlp.perceptron.PerceptronModel','opennlp.model.Context'],['opennlp.perceptron.PerceptronModel','opennlp.model.AbstractModel.ModelType'],['opennlp.perceptron.PerceptronModel','opennlp.model.IndexHashTable'],['opennlp.perceptron.PerceptronModel','opennlp.perceptron.PerceptronModelReader'],['opennlp.perceptron.PerceptronPrepAttachTest','opennlp.PrepAttachDataUtil'],['opennlp.perceptron.PerceptronPrepAttachTest','opennlp.model.AbstractModel'],['opennlp.perceptron.PerceptronPrepAttachTest','opennlp.model.TrainUtil'],['opennlp.perceptron.PerceptronPrepAttachTest','opennlp.model.TwoPassDataIndexer'],['opennlp.perceptron.PerceptronPrepAttachTest','opennlp.perceptron.PerceptronTrainer'],['opennlp.perceptron.PerceptronTrainer','opennlp.model.AbstractModel'],['opennlp.perceptron.PerceptronTrainer','opennlp.model.DataIndexer'],['opennlp.perceptron.PerceptronTrainer','opennlp.model.EvalParameters'],['opennlp.perceptron.PerceptronTrainer','opennlp.model.MutableContext'],['opennlp.perceptron.PerceptronTrainer','opennlp.perceptron.PerceptronModel'],['opennlp.perceptron.PlainTextPerceptronModelReader','opennlp.model.PlainTextFileDataReader'],['opennlp.perceptron.PlainTextPerceptronModelReader','opennlp.perceptron.PerceptronModelReader'],['opennlp.perceptron.PlainTextPerceptronModelWriter','opennlp.model.MaxentModel'],['opennlp.perceptron.PlainTextPerceptronModelWriter','opennlp.perceptron.PerceptronModelWriter'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.AbstractModel'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.Event'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.DataIndexer'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.IndexHashTable'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.MutableContext'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.OnePassDataIndexer'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.Sequence'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.SequenceStream'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.model.SequenceStreamEventStream'],['opennlp.perceptron.SimplePerceptronSequenceTrainer','opennlp.perceptron.PerceptronModel'],['opennlp.perceptron.SuffixSensitivePerceptronModelWriter','opennlp.model.AbstractModel'],['opennlp.perceptron.SuffixSensitivePerceptronModelWriter','opennlp.model.AbstractModelWriter'],['opennlp.perceptron.SuffixSensitivePerceptronModelWriter','opennlp.perceptron.BinaryPerceptronModelWriter'],['opennlp.perceptron.SuffixSensitivePerceptronModelWriter','opennlp.perceptron.PerceptronModelWriter'],['opennlp.perceptron.SuffixSensitivePerceptronModelWriter','opennlp.perceptron.PlainTextPerceptronModelWriter']]\r\n";

	public static void main(String[] args) throws IOException {
		final boolean MEASURING_MEMORY = false;
		int queryIndex = args.length == 0 ? 7 : Integer.parseInt(args[0]);

		GraphDatabaseService gs = EmbeddedDBManager.getNewEmbeddedDBService();
		Transaction tx = null;
		try {
			tx = gs.beginTx();
			Rule rule = RULES[queryIndex];

			if (args.length == 0)
				System.out.println(rule.queries[0]);
			long ini = System.nanoTime();
			String res = rule.execute(tx).resultAsString();
			long end = System.nanoTime();
			if (!MEASURING_MEMORY) {
				System.err.println(res);
				// String resString = res.resultAsString();
				System.out.print((end - ini) / 1000_000);
			} else {
//				System.out.println("Press any key...");
//				new Scanner(System.in).nextLine();
			}
			if (args.length == 0) {
				// System.out.println(res.hashCode());
				// System.out.println(res.resultAsString());
				int size = 0;
				// System.out.println(size);
				// while (res.hasNext()) {
				// System.out.println(size);
				// res.next();
				// size++;
				//
				// }
				// System.out.println(size);
				// BufferedWriter bw = new BufferedWriter(new
				// FileWriter("outXX.txt"));
				// bw.write(resString);
				// bw.close();

			}
			// gs.execute(DELETE_ALL);}
		} catch (Throwable t) {
			t.printStackTrace(new PrintStream(new FileOutputStream("err" + queryIndex + ".txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter("out.txt"));
			bw.write(RULES[queryIndex].queries[0]);
			bw.close();
			System.out.println("-1");
		} finally {
			tx.commit();
			tx.close();
		}
	}
}

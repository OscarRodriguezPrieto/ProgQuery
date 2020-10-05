package database.queries.sa.bloch;

import database.querys.cypherWrapper.AbstractQuery;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ClauseImpl;
import database.querys.cypherWrapper.CompleteNode;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.Expression;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchImpl;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.RelationshipImpl;
import database.querys.cypherWrapper.ReturnClause;
import database.querys.cypherWrapper.SimpleWithClause;
import database.querys.cypherWrapper.WhereClause;
import database.querys.services.FieldServices;
import database.relations.PDGRelationTypes;
import database.relations.RelationTypes;
import utils.dataTransferClasses.Pair;

public class BLOCH_8_45_V2 extends AbstractQuery {

	public BLOCH_8_45_V2() {
		super(true);

	}

	// WTIH ATTR, DECLARING TYPE
	public static void main(String[] args) {
		System.out.println(new BLOCH_8_45_V2().queryToString());
	}

	@Override
	protected void initiate() {
		clauses = new Clause[] {
new ClauseImpl("MATCH (var_def:LOCAL_VAR_DEF)\r\n" + 
		"OPTIONAL MATCH (var_def)-[r:USED_BY| :STATE_MODIFIED_BY |:MODIFIED_BY | :STATE_MAY_BE_MODIFIED_BY]->(useExpr)//ANTES SIN RELACION STATE_MAY_BE_MODIFIED_BY\r\n" + 
		"WHERE //NOT useExpr:METHOD_INVOCATION \r\n" + 
		"NOT EXISTS((var_def)-[:CATCH_PARAM | :FOREACH_VAR | :TRY_RESOURCES]-())//EVITAMOS CONTABILIZAR LOS PARAMETROS DE LOS CATCH\r\n" + 
		"WITH var_def, useExpr ORDER BY useExpr.lineNumber, useExpr.column\r\n" + 
		"MATCH (fatherStmt)-[:CASE_STATEMENTS | :CATCH_BLOCK | :CATCH_PARAM | :ENCLOSES | :DO_WHILE_STATEMENT | :WHILE_STATEMENT | :FOREACH_STATEMENT | :FOREACH_VAR | :FORLOOP_INIT | :FORLOOP_STATEMENT | :FORLOOP_UPDATE | :CALLABLE_HAS_PARAMETER | :HAS_STATIC_INIT | :HAS_VARIABLEDECL_INIT | :IF_THEN | :IF_ELSE | :LABELED_STATEMENT | :SWITCH_ENCLOSES_CASE | :SYNCHRONIZED_BLOCK | :TRY_BLOCK | :TRY_CATCH | :TRY_FINALLY | :TRY_RESOURCES *]->(useStmt)-[:ASSERT_CONDITION | :DO_WHILE_CONDITION | :ENCLOSES_EXPR | :FOREACH_EXPR | :FORLOOP_CONDITION | :HAS_VARIABLEDECL_INIT | :IF_CONDITION | :SWITCH_EXPR | :SYNCHRONIZED_EXPR | :THROW_EXPR | :WHILE_CONDITION | :RETURN_EXPR ]->(expression_with_father_statement)-[:ARRAYACCESS_EXPR | :ARRAYACCESS_INDEX | :ASSIGNMENT_LHS | :ASSIGNMENT_RHS | :BINOP_LHS | :BINOP_RHS | :BINOP_COND_RHS | :CAST_ENCLOSES | :COMPOUND_ASSIGNMENT_LHS | :COMPOUND_ASSIGNMENT_RHS | :CONDITIONAL_EXPR_CONDITION | :CONDITIONAL_EXPR_THEN | :CONDITIONAL_EXPR_ELSE | :INITIALIZATION_EXPR | :INSTANCE_OF_EXPRESSION | :MEMBER_REFERENCE_EXPRESSION | :MEMBER_SELECT_EXPR | :METHODINVOCATION_ARGUMENTS | :METHODINVOCATION_METHOD_SELECT | :NEW_CLASS_ARGUMENTS | :NEW_ARRAY_INIT | :NEW_ARRAY_DIMENSION | :UNARY_ENCLOSES*0..]->(useExpr)\r\n" + 
		"WITH var_def,useStmt,fatherStmt,useExpr ORDER BY fatherStmt.position\r\n" + 
		"WITH var_def,useStmt,COLLECT(fatherStmt) as fatherStmtsFromEachUse,useExpr ORDER BY useStmt.position\r\n" + 
		"WITH var_def,COLLECT(useStmt) AS useStmts,COLLECT(fatherStmtsFromEachUse) AS fatherStmtsFromAllUses,COLLECT(useExpr) AS useExprs\r\n" + 
		"WITH var_def,useStmts,fatherStmtsFromAllUses, HEAD(fatherStmtsFromAllUses) AS fatherStmtsFromFirstUse,useExprs\r\n" + 
		"WITH var_def, REDUCE(s = HEAD(fatherStmtsFromAllUses), n IN TAIL(fatherStmtsFromAllUses) | FILTER(b in s where b in n) ) AS commonFatherStmtsFromAllUses, HEAD(fatherStmtsFromAllUses) AS fatherStmtsFromFirstUse, LAST(HEAD(fatherStmtsFromAllUses)) AS InnerMostFatherStmtFromFirstUse, HEAD(useStmts) AS firstUseStmt,HEAD(useExprs) AS firstUseExpr\r\n" + 
		"WITH var_def,LAST(commonFatherStmtsFromAllUses) as InnerMostCommonFatherStmtFromAllUses,InnerMostFatherStmtFromFirstUse,fatherStmtsFromFirstUse,firstUseStmt,firstUseExpr\r\n" + 
		"WITH var_def,\r\n" + 
		"CASE \r\n" + 
		"WHEN InnerMostCommonFatherStmtFromAllUses=InnerMostFatherStmtFromFirstUse THEN firstUseStmt ELSE HEAD(FILTER(b IN fatherStmtsFromFirstUse WHERE EXISTS((InnerMostCommonFatherStmtFromAllUses)-[:CASE_STATEMENTS | :CATCH_BLOCK | :CATCH_PARAM | :ENCLOSES | :DO_WHILE_STATEMENT | :WHILE_STATEMENT | :FOREACH_STATEMENT | :FOREACH_VAR | :FORLOOP_INIT | :FORLOOP_STATEMENT | :FORLOOP_UPDATE | :CALLABLE_HAS_PARAMETER | :HAS_STATIC_INIT | :HAS_VARIABLEDECL_INIT | :IF_THEN | :IF_ELSE | :LABELED_STATEMENT | :SWITCH_ENCLOSES_CASE | :SYNCHRONIZED_BLOCK | :TRY_BLOCK | :TRY_CATCH | :TRY_FINALLY | :TRY_RESOURCES]->(b)))) END AS StmtAfterCorrectDeclarationPosition,InnerMostCommonFatherStmtFromAllUses\r\n" + 
		"//'StmtAfterCorrectDeclarationPosition' representa el statement situado a continuación de la nueva posición correcta de declaración. Si antes de entrar al else, el padre mas pequeño común a todos los usos coincide con el padre mas pequeño del primer uso (es decir,su padre directo), el primer uso sería el 'StmtAfterCorrectDeclarationPosition' . Si entramos al else, se obtiene el padre del primer uso (buscando entre todos los padres del primer uso) que sea hijo directo del InnerMostCommonStmt a todos los usos (utilizamos el HEAD para sacarlo de la lista). Este sería el 'StmtAfterCorrectDeclarationPosition'. \r\n" + 
		"MATCH (statement)-[:CFG_NEXT_STATEMENT | :CFG_NEXT_STATEMENT_IF_TRUE | :CFG_NEXT_STATEMENT_IF_FALSE | :CFG_FOR_EACH_HAS_NEXT | :CFG_FOR_EACH_NO_MORE_ELEMENTS | :CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION | :CFG_NO_EXCEPTION | :CFG_CAUGHT_EXCEPTION | :CFG_AFTER_FINALLY_PREVIOUS_BREAK | :CFG_AFTER_FINALLY_PREVIOUS_CONTINUE | :CFG_SWITCH_CASE_IS_EQUAL_TO | :CFG_SWITCH_DEFAULT_CASE | :CFG_MAY_THROW | :CFG_THROWS | :CFG_ENTRY ]->(StmtAfterCorrectDeclarationPosition)\r\n" + 
		"WHERE NOT statement:LOCAL_VAR_DEF // Comprobamos que el stmt situado antes de StmtAfterCorrectDeclarationPosition no es la propia declaración, ya que no tendríamos que lanzar el mensaje al encontrarse bien situada\r\n" + 
		"AND statement.lineNumber < StmtAfterCorrectDeclarationPosition.lineNumber //Esta segunda condición sirve para evitar que tenga en cuenta 'predecedores falsos' en bucles\r\n" + 
		"OPTIONAL MATCH (var_def)<-[:CASE_STATEMENTS | :CATCH_BLOCK | :CATCH_PARAM | :ENCLOSES | :DO_WHILE_STATEMENT | :WHILE_STATEMENT | :FOREACH_STATEMENT | :FOREACH_VAR | :FORLOOP_INIT | :FORLOOP_STATEMENT | :FORLOOP_UPDATE | :CALLABLE_HAS_PARAMETER | :HAS_STATIC_INIT | :HAS_VARIABLEDECL_INIT | :IF_THEN | :IF_ELSE | :LABELED_STATEMENT | :SWITCH_ENCLOSES_CASE | :LABELED_STMT_ENCLOSES | :SYNCHRONIZED_BLOCK | :TRY_BLOCK | :TRY_CATCH | :TRY_FINALLY | :TRY_RESOURCES |:CATCH_ENCLOSES_BLOCK *]-(a)<-[:CALLABLE_HAS_BODY]-(method)<-[:DECLARES_METHOD | :DECLARES_CONSTRUCTOR]-(classType)<-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]-(enclosingCU:COMPILATION_UNIT) \r\n" + 
		"WITH '[BLOCH-8.45;'+enclosingCU.fileName+';variable_definition;'+var_def.name+';'+var_def.lineNumber+';You should move the declaration of local variable ['+var_def.name+'] in line '+var_def.lineNumber+' to inmediately before the statement in line '+ StmtAfterCorrectDeclarationPosition.lineNumber +' in method '+method.fullyQualifiedName+ ' in file '+enclosingCU.fileName +', avoiding any intermediate statement to keep it clear]' as message ORDER BY method.completeName\r\n" + 
		"RETURN DISTINCT message")};
		}

}

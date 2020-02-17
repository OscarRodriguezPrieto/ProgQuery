package database.querys.eval;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import database.embedded.EmbeddedDBManager;
import database.relations.CFGRelationTypes;

public class MainQuery {
	public static final String CFG_NODES_FOR_TESTING = "MATCH (stat) WHERE (stat:VAR_DEF OR stat:TRY_BLOCK OR stat:EXPRESSION_STATEMENT OR stat:IF_STATEMENT  OR stat:THROW_STATEMENT  OR stat:CATCH_BLOCK OR stat:FINALLY_BLOCK OR stat: LABELED_STATEMENT OR stat:FOR_LOOP OR stat:ASSERT_STATEMENT OR stat:CONTINUE_STATEMENT OR stat:BREAK_STATEMENT OR stat:WHILE_LOOP OR stat:FOR_EACH_LOOP OR stat:DO_WHILE_LOOP OR stat:EMPTY_STATEMENT) AND  (stat.lineNumber>=12 AND stat.lineNumber<=118) RETURN stat, labels(stat) ORDER BY stat.lineNumber, stat.position";

	/*
	 * TODO ESTO SON CONSULTAS DE PRUEBA, NINGUNA REPRESENTA NINGUNA REGLA DE
	 * PROGRAMACIóN
	 */
	public static final String ALL_NODES = "MATCH (n)-[r]->(m) RETURN n,r,m";
	public static final String DELETE_ALL = "MATCH (n) DETACH DELETE n";

	private static final String TYPE_HIERARCHY = "MATCH (n)-[r:IS_SUBTYPE_IMPLEMENTS | IS_SUBTYPE_EXTENDS]->(m) RETURN n,r,m";

	private static final String ALL_CALLS = "MATCH (n)-[r:CALLS]->(mi)-[r2:HAS_DEF]->(md) RETURN n,r,mi,md";
	private static final String CFG_RELS = " MATCH (m)-[r:" + CFGRelationTypes.getCFGRelations()
			+ " ]->(n) RETURN m, labels(m),r,n, labels(n)";

	private static final String ALL_METHOD_DECS_RELS = "MATCH (m:METHOD_DEC)-[r]-(n) WHERE m.isDeclared RETURN m,r,n, labels(n)";
	private static final String OVERRIDES = "MATCH (m)-[:OVERRIDES]->(n) RETURN m.fullyQualifiedName,n.fullyQualifiedName";
	private static final String REFER_RELS = "MATCH (inv)-[r:REFERS_TO | MAY_REFER_TO]->(m) RETURN inv,r,m.fullyQualifiedName";

	public static final String MAY_THROW_REL = "MATCH (stat)-[r:CFG_MAY_THROW]->(statDos)  RETURN stat, labels(stat), r, statDos,labels(statDos)";
	public static final String STATE_MODS = "MATCH (n)-[r :STATE_MAY_BE_MODIFIED_BY | :STATE_MODIFIED_BY]->(m) RETURN n, labels(n),r, m";
	public static final String MAY_AND_REFER = "MATCH (n)-[r :REFERS_TO | :MAY_REFER_TO]->(m) RETURN n, labels(n),r, m";
	public static final String CDG = "MATCH (n)-[r :USES_TYPE_DEF ]->(m) RETURN n.fullyQualifiedName, m.fullyQualifiedName ORDER BY n.fullyQualifiedName";
	/**
	 * Aquí te paso consultas que modelan algunas reglas sencillas
	 **/
	// Un metodo que retorne una colección o un array, debe retornal colección
	// o array vación en lugar de null
	private static final String MET55_RETURN_EMPTY_COLLECTIONS_INSTEAD_NULL =

			" MATCH (md)-[:CALLABLE_HAS_BODY]->(rt)-[:ITS_TYPE_IS |:PARAMETERIZED_TYPE*0..]->()-[:IS_SUBTYPE_EXTENDS | :IS_SUBTYPE_IMPLEMENTS*0..]->(collection) WHERE  collection.fullyQualifiedName='java.util.Collection<E>' OR collection:ARRAY_TYPE  WITH DISTINCT md  MATCH (enclosingCU)-[:HAS_TYPE_DEF | :HAS_INNER_TYPE_DEF]->()-[:DECLARES_METHOD]->(md)<-[:CFG_END_OF]-(normalEnd)<-[:CFG_NEXT_STATEMENT]-(:RETURN_STATEMENT)-[:RETURN_EXPR]->() -[:CONDITIONAL_EXPR_THEN |:CONDITIONAL_EXPR_ELSE*0..]->(nullRet{typetag:'NULL_LITERAL'}) WITH	 enclosingCU, nullRet  WHERE nullRet IS NOT NULL RETURN 'Warning [CMU-MET55], you must not return null when you can return an empty collection or array.Line' +nullRet.lineNumber+' in '+enclosingCU.fileName+'.'"
;
	// Se llama a clone en un método publico de clase publica, y sobre un
	// parámetro cuya clase es pública y no está marcada como final
	// Hay riesgo de que te llegue un parámetro de un cliente maligno que haya
	// extendido la clase del parámetro de modo que al clonarlo tenga un
	// comportamiento inesperado/malicioso
	private static final String MET52_DO_NOT_USE_CLONE_WITH_UNTRUSTED_PARAMETERS = " MATCH (enclosingCU)-[:HAS_TYPE_DEF |:HAS_INNER_TYPE_DEF]->(typeDec{accessLevel:'public'})-[:DECLARES_METHOD]->(method{accessLevel:'public'})-[:CALLABLE_HAS_PARAMETER]->(param) -[:USED_BY]->(id)<-[:MEMBER_SELECT_EXPR]-(mSelect:MEMBER_SELECTION{memberName:'clone'})<-[:METHODINVOCATION_METHOD_SELECT]-(mInv:METHOD_INVOCATION), (param)-[:HAS_VARIABLEDECL_TYPE]->()-[:PARAMETERIZED_TYPE*0..1]->()-[:ITS_TYPE_IS]->(pType)  WHERE mSelect.actualType CONTAINS '()' AND NOT pType.isFinal AND (NOT pType.isDeclared OR pType.accessLevel='public') RETURN 'Warning [CMU-MET52] You must not use the clone method to copy unstrasted parameters (like parameter ' + param.name+ ', cloned in line '+  mInv.lineNumber+ ' in method ' + method.name +', file '+enclosingCU.fileName+').'";

	public static void main(String[] args) throws IOException {

		GraphDatabaseService gs = EmbeddedDBManager.getNewEmbeddedDBService();

		Result res = gs.execute(args[0]);
		System.out.println(res.resultAsString());
		// gs.execute(DELETE_ALL);

	}

}	
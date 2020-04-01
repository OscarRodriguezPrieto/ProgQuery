package database.querys.eval;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import database.embedded.EmbeddedDBManager;

public class RuleDetectionQueries {
	public static final String ALL_NODES = "MATCH (n)-[r]->(m) RETURN n, labels(n),r,m, labels(m)";
	public static final String COVARIANT_ASSIGNMENTS = "match (n)-[:ASSIGNMENT_LHS]->(lhsNode), "
			+ "(n)-[:RHS]->(rhsNode)" + ", (u:COMPILATION_UNIT)-[]->(c:CLASS_DECLARATION)-[]->(temp)-[:ENCLOSES]->(n) "
			+ "WHERE(  lhsNode.typeKind='ARRAY'" + " AND rhsNode.typeKind='ARRAY'"
			+ " AND lhsNode.actualType <> rhsNode.actualType)"
			+ " return distinct u.projectName, u.fileName, c.name, rhsNode.lineNumber, lhsNode.actualType, rhsNode.actualType LIMIT 10;";
	public static final String COUNT_OVERLOADED = "MATCH (p:COMPILATION_UNIT)-[]->(c:CLASS_DECLARATION)-[:DECLARES]->(n: METHOD_DEC)"
			+ "WHERE NOT(n.name = '')  AND c.name <> \"\" "
			+ "WITH n.name +\"[sep]\"+ c.name + \"[sep]\"+ p.fileName as method, count(method) as overloadedCount "
			+ "WHERE overloadedCount > 1 RETURN SUM(overloadedCount) as total;";

	public static final String WILDCARDS = "MATCH (p:COMPILATION_UNIT)-[:HAS_TYPE_DEF]->(c:CLASS_DECLARATION)-[*]->(n) "
			+ " RETURN DISTINCT labels(n), p.fileName, n.lineNumber,n, c.fullyQualifiedName ;";

	public static final String COUNT_DISTINCT_ONLY_METHODS = "MATCH (c)-[:DECLARES_METHOD]->(n:METHOD_DEC) "
			+ "WHERE c:CLASS_DECLARATION OR c:INTERFACE_DECLARATION   RETURN n.fullyQualifiedName";

	public static final String GET_DISTINCT_METHODS_SIMPLE = "MATCH (n:METHOD_DEC) "
			+ "  RETURN DISTINCT n.fullyQualifiedName";

	public static final String COUNT_DISTINCT_ONLY_METHODS_UNION_BIS = "MATCH (c:CLASS_DECLARATION)-[:DECLARES_METHOD]->(n) UNION MATCH (c:INTERFACE_DECLARATION)-[:DECLARES_METHOD]->(n)  RETURN n.fullyQualifiedName";

	public static final String COUNT_DISTINCT_TYPE_DECS = "MATCH (c)"
			+ "WHERE c:CLASS_DECLARATION OR c:INTERFACE_DECLARATION OR c:ENUM_DECLARATION  RETURN COUNT(c)";

	public static final String DELETE_ALL = "MATCH (n) DETACH DELETE n";

	// Lvl0 Wiggle can do it as quickly as our system
	public static final String ALL_NODES_COUNT = "MATCH (n) RETURN COUNT(n)";
	public static final String COMPILATION_UNITS_COUNT = "MATCH (cu:COMPILATION_UNIT) RETURN COUNT(cu)";

	// lvl 1 Wiggle can do it with a more complex query and not 100% correctly
	// like uor system
	public static final String GET_DISTINCT_DECLARED_METHODS_SIMPLE = "MATCH ()-[:DECLARES_METHOD]->(n) "
			+ "  RETURN DISTINCT n.fullyQualifiedName";

	public static final String GET_INTERFACE_COUNT = "MATCH (n:INTERFACE_DECLARATION) " + "  RETURN  n";

	// COVARIANCE ARRAYS

	public static final String COVARIANT_ARRAY_ASSIGNMENTS = "MATCH (c)-[:DECLARES_METHOD ]->(m)-[*]->(a), (l)<-[:ASSIGNMENT_LHS]-(a:ASSIGNMENT)-[:ASSIGNMENT_RHS]->(r) "
			+ "WHERE l.typeKind='ARRAY'  AND l.actualType<>r.actualType " + "return distinct a,l,r,m,c";

	public static final String OVERLOADED_FETCH = "MATCH ()-[:DECLARES_METHOD]->(n) WITH count(n.completeName) as overloadedCount, n.completeName as name WHERE overloadedCount>1 RETURN name,overloadedCount ;";

	public static final String FOR_PARAMS_FAVOR_INTERFACES_OVER_CLASSES = "MATCH (c)-[:DECLARES_METHOD]->(m)"
			+ "-[:CALLABLE_HAS_PARAMETER]->(param)-[:USED_BY]->(id)<-[:MEMBER_SELECT_EXPR]-(ms)<-[:METHODINVOCATION_METHOD_SELECT]-(mi)-[:HAS_DEF]->(mid)<-[:DECLARES_METHOD]-(interface),"
			+ "(paramClass)-[:IS_SUBTYPE_IMPLEMENTS]->(interface) "

			+ "WHERE paramClass.fullyQualifiedName=param.actualType AND c.accessLevel='public' AND m.accessLevel='public' RETURN c,m,param, mid,interface";

	public static final String[] QUERIES = new String[] { COMPILATION_UNITS_COUNT, GET_DISTINCT_METHODS_SIMPLE,
			GET_INTERFACE_COUNT, COVARIANT_ARRAY_ASSIGNMENTS, OVERLOADED_FETCH, FOR_PARAMS_FAVOR_INTERFACES_OVER_CLASSES };

	// Controlar si se puede sobreescribir el equals metiendo primitivas al
	// parametro
	public static void main(String[] args) throws IOException {
		int queryIndex = args.length == 0 ? 4 : Integer.parseInt(args[0]);
		GraphDatabaseService gs = EmbeddedDBManager.getNewEmbeddedDBService();
		Transaction tx=gs.beginTx();
		String query = QUERIES[queryIndex];
		System.err.println(queryIndex + "\n" + query);
		long ini = System.nanoTime();
		Result res = tx.execute(query);
		long end = System.nanoTime();
		res.toString().length();
		System.out.print((end - ini) / 1000_000);
		tx.close();
		// gs.execute(DELETE_ALL);
	}
}

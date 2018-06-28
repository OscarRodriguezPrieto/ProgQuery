package database.querys;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import database.DatabaseFachade;
import database.relations.CFGRelationTypes;

public class MainQuery {
	public static final String ALL_NODES = "MATCH (n)-[r]->(m) RETURN n,r,m";
	public static final String DELETE_ALL = "MATCH (n) DETACH DELETE n";

	private static final String TYPE_HIERARCHY = "MATCH (n)-[r:IS_SUBTYPE_IMPLEMENTS | IS_SUBTYPE_EXTENDS]->(m) RETURN n,r,m";

	private static final String ALL_CALLS = "MATCH (n)-[r:CALLS]->(mi)-[r2:HAS_DEC]->(md) RETURN n,r,mi,md";
	private static final String CFG_RELS = " MATCH (m)-[r:" + CFGRelationTypes.getCFGRelations()
			+ " ]->(n) RETURN m, labels(m),r,n, labels(n)";

	private static final String ALL_METHOD_DECS_RELS = "MATCH (m:METHOD_DEC)-[r]-(n) WHERE m.isDeclared RETURN m,r,n, labels(n)";
	private static final String OVERRIDES = "MATCH (m)-[:OVERRIDES]->(n) RETURN m.fullyQualifiedName,n.fullyQualifiedName";
	private static final String REFER_RELS = "MATCH (inv)-[r:REFER_TO | MAY_REFER_TO]->(m:CONSTRUCTOR_DEC) RETURN inv,r,m.fullyQualifiedName";

	public static final String CFG_NODES_FOR_TESTING = "MATCH (stat) WHERE (stat:VAR_DEC OR stat:TRY_BLOCK OR stat:EXPRESSION_STATEMENT OR stat:IF_STATEMENT  OR stat:THROW_STATEMENT  OR stat:CATCH_BLOCK OR stat:FINALLY_BLOCK OR stat: LABELED_STATEMENT OR stat:FOR_LOOP OR stat:ASSERT_STATEMENT OR stat:CONTINUE_STATEMENT OR stat:BREAK_STATEMENT OR stat:WHILE_LOOP OR stat:ENHANCED_FOR OR stat:DO_WHILE_LOOP OR stat:EMPTY_STATEMENT) AND  (stat.lineNumber>=12 AND stat.lineNumber<=118) RETURN stat, labels(stat) ORDER BY stat.lineNumber, stat.position";
	public static final String MAY_THROW_REL = "MATCH (stat)-[r:MAY_THROW]->(statDos)  RETURN stat, labels(stat), r, statDos,labels(statDos)";
	public static final String STATE_MODS = "MATCH (n)-[r :STATE_MAY_BE_MODIFIED | :STATE_MODIFIED_BY]->(m) RETURN n, labels(n),r, m";
	public static final String MAY_AND_REFER = "MATCH (n)-[r :REFER_TO | :MAY_REFER_TO]->(m) RETURN n, labels(n),r, m";
	public static final String CDG = "MATCH (n)-[r :USES_TYPE_DEC ]->(m) RETURN n.fullyQualifiedName, m.fullyQualifiedName ORDER BY n.fullyQualifiedName";

	public static void main(String[] args) throws IOException {

		GraphDatabaseService gs = DatabaseFachade.getDB();

		Result res = gs.execute(CDG);
		System.out.println(res.resultAsString());
		// gs.execute(DELETE_ALL);

	}

}

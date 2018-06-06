package database.querys;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import database.DatabaseFachade;
import database.relations.CFGRelationTypes;

public class MainQuery {
	private static final String ALL_METHODS_OF_ALL_CLASSES_QUERY = "START c=node:node_auto_index(nodeType='JCClassDecl') MATCH c-[r:DECLARES_METHOD]->m RETURN ('Class '+ c.fullyQualifiedName+ ' with method ' + m.name) AS warning";
	private static final String ALL_CLASSES_WITH_EQUALS = "START c=node:node_auto_index(nodeType='JCClassDecl') MATCH c-[r:DECLARES_METHOD]->(m {name:'equals'}) RETURN ('Class '+ c.fullyQualifiedName+ ' has a method named equals.') AS warning";
	public static final String ALL_NODES = "MATCH (n)-[r]->(m) RETURN n,r,m";
	private static final String ALL_CLASSES_OVERRITING_EQUALS = "START m=node:node_auto_index(nodeType='JCMethodDecl') MATCH c-[:DECLARES_METHOD]->(m{name:'equals'})-[:HAS_METHODDECL_PARAMETERS]->p WITH c AS c,m AS m,count(p) AS paramCount WHERE paramCount=1 RETURN 'Clase ' +c.fullyQualifiedName+ ' has an equals method overriding its superclass.' AS warning";
	public static final String DELETE_ALL = "MATCH (n) DETACH DELETE n";
	private static final String WARNING_EQUALS_HASHCODE = "START m=node:node_auto_index(nodeType='JCMethodDecl') MATCH c-[:DECLARES_METHOD]->(m{name:'equals'})-[:HAS_METHODDECL_PARAMETERS]->p WITH c AS c,m AS m,count(p) AS paramCount WHERE paramCount=1 AND NOT (c)-[:DECLARES_METHOD]->({name:'hashCode'}) RETURN 'Clase ' +c.fullyQualifiedName+ ' has an equals method overriding its superclass.' AS warning";
	private static final String WARNING_EQUALS_HASHCODE_R = "START m=node:node_auto_index(nodeType='JCMethodDecl') MATCH c-[:DECLARES_METHOD]->(m{name:'equals'})-[:HAS_METHODDECL_PARAMETERS]->p OPTIONAL MATCH (hash{name:'hashCode'})<-[:DECLARES_METHOD]-c WITH hash AS hash ,c AS c,m AS m,count(p) AS paramCount WHERE paramCount=1 AND (hash IS NULL OR NOT hash-[:HAS_METHODDECL_PARAMETERS]->())RETURN '[WARNING] Class ' +c.fullyQualifiedName+ ' has an equals method overriding its superclass. An appropiate hashCode method must be redefined too.' AS warning";

	private static final String ALL_NODES_EXTENDS = "START n=node(*) MATCH n-[r:IS_SUBTYPE_IMPLEMENTS|HAS_CLASS_IMPLEMENTS]->m RETURN n,r,m";

	private static final String ALL_NODES_EXTENDS_AND_MORE = "START n=node(*) "
			+ "MATCH n-[r:IS_SUBTYPE_IMPLEMENTS|HAS_CLASS_IMPLEMENTS]->m " + "MATCH m-[r2]->o " + "RETURN n,r,m,r2,o";
	private static final String ALL_NODES_EXTENDS_AND_MORE_BIS = "START n=node(*) "
			+ "MATCH n-[r:IS_SUBTYPE_IMPLEMENTS|HAS_CLASS_IMPLEMENTS]->m " + "MATCH o-[r2]->n " + "RETURN n,r,m,r2,o";
	private static final String TYPE_HIERARCHY = "MATCH (n)-[r:IS_SUBTYPE_IMPLEMENTS | IS_SUBTYPE_EXTENDS]->(m) RETURN n,r,m";

	private static final String METHOD_INV_RELATIONS_OUT = "START n=node:node_auto_index(nodeType='METHOD_INVOCATION') MATCH n-[r]->m RETURN n,r,m";
	private static final String METHOD_INV_RELATIONS_IN = "START m=node:node_auto_index(nodeType='METHOD_INVOCATION') MATCH n-[r]->m RETURN n,r,m";
	private static final String ALL_CALLS = "MATCH (n)-[r:CALLS]->(mi)-[r2:HAS_DEC]->(md) RETURN n,r,mi,md";
	private static final String CFG_RELS = " MATCH (m)-[r:" + CFGRelationTypes.getCFGRelations()
			+ " ]->(n) RETURN m, labels(m),r,n, labels(n)";
	private static final String PDG_RELS = "start n=node(*) MATCH m-[r:USED_BY | MODIFIED_BY | STATE_MODIFIED_BY ]->n RETURN m,r,n";
	private static final String STAT_MOD_RELS = "start n=node(*) MATCH m-[r: STATE_MODIFIED_BY |  STATE_MAY_BE_MODIFIED ]->n RETURN m,r,n";
	private static final String USED_BY_RELS = "start n=node(*) MATCH m-[r: USED_BY ]->n RETURN m,r,n";
	private static final String THIS_RELS = "START m=node:node_auto_index(nodeType='THIS_REF') MATCH m-[r]->n RETURN m,r,n";

	private static final String CYPHER_EXPRESSIVENESS_TEST = "MATCH (n:CFG_METHOD_ENTRY)-[:CFG_NEXT_STATEMENT | CFG_NEXT_CONDITION*]->(s1) RETURN s1, labels(s1)";

	private static final String ALL_METHOD_DECS_RELS = "MATCH (m:METHOD_DEC)-[r]-(n) WHERE m.isDeclared RETURN m,r,n, labels(n)";
	private static final String OVERRIDES = "MATCH (m)-[:OVERRIDES]->(n) RETURN m.fullyQualifiedName,n.fullyQualifiedName";
	private static final String REFER_RELS = "MATCH (inv)-[r:REFER_TO | MAY_REFER_TO]->(m:CONSTRUCTOR_DEC) RETURN inv,r,m.fullyQualifiedName";

	public static final String CFG_NODES_FOR_TESTING = "MATCH (stat) WHERE (stat:VAR_DEC OR stat:TRY_BLOCK OR stat:EXPRESSION_STATEMENT OR stat:IF_STATEMENT  OR stat:THROW_STATEMENT  OR stat:CATCH_BLOCK OR stat:FINALLY_BLOCK OR stat: LABELED_STATEMENT OR stat:FOR_LOOP OR stat:ASSERT_STATEMENT OR stat:CONTINUE_STATEMENT OR stat:BREAK_STATEMENT OR stat:WHILE_LOOP OR stat:ENHANCED_FOR OR stat:DO_WHILE_LOOP OR stat:EMPTY_STATEMENT) AND  (stat.lineNumber>=12 AND stat.lineNumber<=118) RETURN stat, labels(stat) ORDER BY stat.lineNumber, stat.position";
	public static final String MAY_THROW_REL = "MATCH (stat)-[r:MAY_THROW]->(statDos)  RETURN stat, labels(stat), r, statDos,labels(statDos)";

	// Controlar si se puede sobreescribir el equals metiendo primitivas al
	// parametro
	public static void main(String[] args) throws IOException {

		// GraphDatabaseService db = DatabaseFachade.getDB();
		// DatabaseFachade.setDB(db);
		// Transaction t = db.beginTx();
		// Node n = DatabaseFachade.createNode();
		// n.createRelationshipTo(DatabaseFachade.createNode(),
		// PDGRelationTypes.MAY_RETURN);
		// t.success();
		// System.out.println(n.getSingleRelationship(PDGRelationTypes.MAY_RETURN,
		// Direction.OUTGOING));
		// System.out.println(n.getSingleRelationship(PDGRelationTypes.MAY_RETURN,
		// Direction.OUTGOING).getType());
		// System.out.println(n.getSingleRelationship(PDGRelationTypes.MAY_RETURN,
		// Direction.OUTGOING)
		// .getType() == PDGRelationTypes.MAY_RETURN);
		// System.out
		// .println(n.getSingleRelationship(PDGRelationTypes.MAY_RETURN,
		// Direction.OUTGOING).getType().getClass());
		// System.out
		// .println(n.getSingleRelationship(PDGRelationTypes.MAY_RETURN,
		// Direction.OUTGOING).getType()
		// .getProperty("name"));

		GraphDatabaseService gs = DatabaseFachade.getDB();

		Result res = gs.execute(MAY_THROW_REL);
		// gs.execute(TYPE_HIERARCHY);

		// System.out.println(res.resultAsString());
		// res = gs.execute(METHOD_INV_RELATIONS_IN);
		// FileUtil.writeFile("selfAnalisysOutput.txt", res.resultAsString());
		System.out.println(res.resultAsString());
		gs.execute(DELETE_ALL);
		// String cad = "Hoy +";
		// cad += "apruebo";
		// System.out.println(cad.contentEquals("Hoy apruebo"));
		// System.out.println((int) Math.random() * 1);
	}

}

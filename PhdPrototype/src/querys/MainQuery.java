package querys;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class MainQuery {
	private static final String ALL_METHODS_OF_ALL_CLASSES_QUERY = "START c=node:node_auto_index(nodeType='JCClassDecl') MATCH c-[r:DECLARES_METHOD]->m RETURN ('Class '+ c.fullyQualifiedName+ ' with method ' + m.name) AS warning";
	private static final String ALL_CLASSES_WITH_EQUALS="START c=node:node_auto_index(nodeType='JCClassDecl') MATCH c-[r:DECLARES_METHOD]->(m {name:'equals'}) RETURN ('Class '+ c.fullyQualifiedName+ ' has a method named equals.') AS warning";
	private static final String ALL_NODES="START n=node(*) MATCH n-[r]->m RETURN n,r,m";
	private static final String ALL_CLASSES_OVERRITING_EQUALS="START m=node:node_auto_index(nodeType='JCMethodDecl') MATCH c-[:DECLARES_METHOD]->(m{name:'equals'})-[:HAS_METHODDECL_PARAMETERS]->p WITH c AS c,m AS m,count(p) AS paramCount WHERE paramCount=1 RETURN 'Clase ' +c.fullyQualifiedName+ ' has an equals method overriding its superclass.' AS warning";
	private static final String DELETE_ALL = "MATCH (n) DETACH DELETE n";
	private static final String WARNING_EQUALS_HASHCODE="START m=node:node_auto_index(nodeType='JCMethodDecl') MATCH c-[:DECLARES_METHOD]->(m{name:'equals'})-[:HAS_METHODDECL_PARAMETERS]->p WITH c AS c,m AS m,count(p) AS paramCount WHERE paramCount=1 AND NOT (c)-[:DECLARES_METHOD]->({name:'hashCode'}) RETURN 'Clase ' +c.fullyQualifiedName+ ' has an equals method overriding its superclass.' AS warning";
	private static final String WARNING_EQUALS_HASHCODE_R="START m=node:node_auto_index(nodeType='JCMethodDecl') MATCH c-[:DECLARES_METHOD]->(m{name:'equals'})-[:HAS_METHODDECL_PARAMETERS]->p OPTIONAL MATCH (hash{name:'hashCode'})<-[:DECLARES_METHOD]-c WITH hash AS hash ,c AS c,m AS m,count(p) AS paramCount WHERE paramCount=1 AND (hash IS NULL OR NOT hash-[:HAS_METHODDECL_PARAMETERS]->())RETURN '[WARNING] Class ' +c.fullyQualifiedName+ ' has an equals method overriding its superclass. An appropiate hashCode method must be redefined too.' AS warning";
	
	

//Controlar si se puede sobreescribir el equals metiendo primitivas al parametro
	public static void main(String[] args) {
		GraphDatabaseService gs=loadDB();
		Result res = gs.execute(ALL_NODES);
		System.out.println(res.resultAsString());
		gs.execute(DELETE_ALL);
	}

	public static GraphDatabaseService loadDB() {
		String wiggleDbPath = "./neo4j/data/wiggle.db";
		return new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(wiggleDbPath)
				.setConfig(GraphDatabaseSettings.node_keys_indexable, "nodeType")
				.setConfig(GraphDatabaseSettings.relationship_keys_indexable, "typeKind")
				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
				.setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true").newGraphDatabase();
	}
}

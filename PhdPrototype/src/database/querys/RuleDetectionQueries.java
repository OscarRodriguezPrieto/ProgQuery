package database.querys;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

public class RuleDetectionQueries {
	public static final String ALL_PUBLIC_NON_FINAL_CLASSES = "MATCH (n) DETACH DELETE n";

	public static final String DELETE_ALL = "MATCH (n) DETACH DELETE n";

	// Controlar si se puede sobreescribir el equals metiendo primitivas al
	// parametro
	public static void main(String[] args) throws IOException {

		GraphDatabaseService gs = loadDB();

		Result res = gs.execute(METHOD_MOD_RELS);
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

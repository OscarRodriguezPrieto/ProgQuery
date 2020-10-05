package database.querys.eval;

import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import database.embedded.EmbeddedDBManager;

public class EmbeddedCountQuery {

	public static void main(String[] args) throws IOException {

		GraphDatabaseService gs = EmbeddedDBManager.getNewEmbeddedDBService();

		Result res = gs.execute(args[0]);
		System.out.println(res.next().values().iterator().next());
		// gs.execute(DELETE_ALL);

	}

}
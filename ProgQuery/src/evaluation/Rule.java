package evaluation;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class Rule {

	public String[] queries;

	public Rule(String... querys) {
		super();
		this.queries = querys;
	}

	public Result execute(Transaction tx) {
		Result res = null;
		for (String query : queries)
			res = tx.execute(query);

		return res;
	}

}

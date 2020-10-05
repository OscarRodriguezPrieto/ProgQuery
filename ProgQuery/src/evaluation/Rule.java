package evaluation;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

public class Rule {

	public String[] queries;

	public Rule(String... querys) {
		super();
		this.queries = querys;
	}

	public Result execute(GraphDatabaseService gs) {
		Result res = null;
		for (String query : queries)
			res = gs.execute(query);

		return res;
	}

}

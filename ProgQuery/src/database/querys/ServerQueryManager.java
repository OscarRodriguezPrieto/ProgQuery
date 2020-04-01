package database.querys;

import java.util.List;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
public class ServerQueryManager implements AutoCloseable {
	public static ServerQueryManager SERVER_MANAGER;

	// private static final String ADDRESS = "https://156.35.94.128:7474";
	final Driver driver;
	Session session ;

	public ServerQueryManager(String user, String pass, String address) {
		driver = GraphDatabase.driver(address, AuthTokens.basic(user, pass));
		session= driver.session();
	}

	public List<Record> executeQuery(String query) {
		// Transaction t = session.beginTransaction();
		// System.err.println("EXECUTING:\n" + query);
		return session.writeTransaction(new TransactionWork<List<Record>>() {

			@Override
			public List<Record> execute(Transaction tx) {
				// tx.

				Result result = tx.run(query);
				// result.list().get(0).asMap().entrySet()
				// .forEach(e -> System.out.println(e.getKey() + "," +
				// e.getValue()));
				// return result.list().stream().map(r ->
				// r.get(0).asString()).collect( Collectors.toList());
				return result.list();

			}

		});
	}

	@Override
	public void close() throws Exception {
		session.close();
		driver.close();
	}

}

package es.uniovi.reflection.progquery;
import java.io.Serializable;
import java.time.ZonedDateTime;

import es.uniovi.reflection.progquery.database.*;
import org.kohsuke.MetaInfServices;

import com.sun.source.util.JavacTask;

@MetaInfServices(com.sun.source.util.Plugin.class)
public class ProgQueryPlugin implements com.sun.source.util.Plugin {

	private static final String PLUGIN_NAME = "es.uniovi.reflection.progquery.ProgQueryPlugin";

	@Override
	public void init(JavacTask task, String[] args) {
		// final GraphDatabaseService graphDb = args.length > 0 ?
		// DatabaseFachade.getDB(args[0]) : DatabaseFachade.getDB();
		// First argument if any contents the db path
		// DatabaseFachade.setDB(graphDb);
		System.out.println(task.getClass());
		System.out.println(task instanceof Serializable);

		Thread.currentThread().setContextClassLoader(ProgQueryPlugin.class.getClassLoader());

		final String ANONYMOUS_PROGRAM = "ANONYMOUS_PROGRAM_", ANONYMOUS_USER = "ANONYMOUS_USER";
		String programID, userID;
		if (args.length == 0) {
			programID = ANONYMOUS_PROGRAM + ZonedDateTime.now();
			userID = ANONYMOUS_USER;
		} else if (args[0].contains(";")) {
			String[] IDInfo = args[0].split(";");
			programID = IDInfo[0];
			userID = IDInfo[1];
		} else {
			// ONLY PROGRAM ID
			programID = args[0];
			userID = ANONYMOUS_USER;
		}
		DatabaseFachade
				.init(args.length == 1 ? new NotPersistentLazyInsertion():
						args[1].contains("S")
								? args.length == 2 ? invalidArgs()
										: args.length == 3 ? serverTwoArgs(args[2])
												: new Neo4jDriverLazyInsertion(Integer.parseInt(args[3]),
														args[2])
								: args.length > 2 ? new EmbeddedInsertion(args[2])
										: new EmbeddedInsertion());

		MultiCompilationScheduler scheduler=new MultiCompilationScheduler(programID, userID);
		scheduler.addListener(task, null);
		scheduler.endAnalysis();
	}

	private InsertionStrategy invalidArgs() {
		throw new IllegalArgumentException("You need to specify the connection string to run PQ Server");
	}

	private InsertionStrategy serverTwoArgs(String arg2) {

		return new Neo4jDriverLazyInsertion(arg2);
	}

	@Override
	public String getName() {
		return PLUGIN_NAME;
	}	
}

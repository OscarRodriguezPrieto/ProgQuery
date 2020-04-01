package database.querys.eval;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.driver.Record;

import database.querys.ServerQueryManager;
import evaluation.Rule;

public class ServerQueries {

	public static void main(String[] args) throws Exception {
		int queryIndex = args.length == 0 ?8 : Integer.parseInt(args[0]);
		try {
			Rule rule = new Rule(CMUQueries.RULES[queryIndex].queries[0]);
			Set<Integer> listQueryIndexes = new HashSet<>();
			listQueryIndexes.add(11);
			listQueryIndexes.add(7);
			// if (args.length == 0)
			// System.out.println(rule.queries[0]);
			ServerQueryManager.SERVER_MANAGER = new ServerQueryManager("Oscar", "pass", "address");

			long ini = System.nanoTime();
			List<Record> l = ServerQueryManager.SERVER_MANAGER.executeQuery(rule.queries[0]);
			long end = System.nanoTime();
//			l.stream().map(r-> r.);
			l.stream().map(r -> listQueryIndexes.contains(queryIndex) ? r.get(0).asList() : r.get(0).asString()).collect(Collectors.toList()).forEach(System.err::println);
			System.out.print((end - ini) / 1000_000);
			if (args.length == 0) {

			}
		} catch (Throwable t) {
			t.printStackTrace(new PrintStream(new FileOutputStream("err" + queryIndex + ".txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter("out.txt"));
			bw.write(CMUQueries.RULES[queryIndex].queries[0]);
			bw.close();
			System.out.println("-1");
		} finally {
			ServerQueryManager.SERVER_MANAGER.close();
			System.exit(0);
		}
	}
}

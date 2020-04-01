package database.querys.eval;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.List;

import org.neo4j.driver.Record;

import database.querys.ServerQueryManager;
import evaluation.Rule;

public class ServerAnyQueries {

	public static void main(String[] args) throws Exception {

		try {
			Rule rule = new Rule(args[0]);
			// if (args.length == 0)
			// System.out.println(rule.queries[0]);
			// long ini = System.nanoTime();
			List<Record> l = ServerQueryManager.SERVER_MANAGER.executeQuery(rule.queries[0]);
			System.out.println("Number of results: " + l.size());
			l.get(0).keys().forEach(recordColumn -> System.out.print(recordColumn + "\t"));
			System.out.println();
			l.forEach(r -> {
				r.values().forEach(v -> {

					System.out.print("NODE[" + v.asNode().id() + "]:");
					v.asNode().labels().forEach(label -> System.out.println(label + ","));
					v.asNode().keys().forEach(
							nodeProp -> System.out.print("\t" + nodeProp + ":" + v.asNode().get(nodeProp)));
				});
				System.out.println();
			});
			// long end = System.nanoTime();
			// l.stream().map(r ->
			// r.get(0).asString()).collect(Collectors.toList()).forEach(System.err::println);
			// System.out.println((end - ini) / 1000_000);
			if (args.length == 0) {

			}
		} catch (Throwable t) {
			t.printStackTrace();
			t.printStackTrace(new PrintStream(new FileOutputStream("err_" + args[0] + ".txt")));
			BufferedWriter bw = new BufferedWriter(new FileWriter("out.txt"));
			bw.write(args[0]);
			bw.close();
			System.out.println("-1");
		} finally {
			ServerQueryManager.SERVER_MANAGER.close();
		}
	}
}

package database.querys.eval;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.List;

import org.neo4j.driver.Record;

import database.querys.ServerQueryManager;
import evaluation.Rule;

public class ServerCountQueries {

	public static void main(String[] args) throws Exception {

		try {
			Rule rule = new Rule(args[0]);
			// if (args.length == 0)
			// System.out.println(rule.queries[0]);
			// long ini = System.nanoTime();
			List<Record> l = ServerQueryManager.SERVER_MANAGER.executeQuery(rule.queries[0]);
			System.out.println(l.get(0).get(0).asInt() + "");
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

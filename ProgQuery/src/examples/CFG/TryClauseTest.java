package examples.CFG;

import java.io.BufferedReader;
import java.io.IOException;


public class TryClauseTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		int i = 0;
		// BufferedReader brd = m();
		// w: while (true)
		try (BufferedReader br = m()) {

				// break w;
			} catch (Exception e) {
				System.out.println("CAUGHT");
			throw e;
			// break w;
			} finally {
			System.out.println("FIRST FINALLY");
			}
		// bb: {
		// try {
		// break bb;
		// } finally {
		//
		// }
		//
		// }
		// while (true)
		// try {
		// if (args.length == 0)
		// continue;
		// else
		// break;
		// } finally {
		// System.out.println("LOOP FINALLY");
		// }
		//
		// tr: try {
		// // if (args.length == 0)
		// // break tr;
		// try {
		// System.out.println("TRY");
		// // break tr;
		// } finally {
		// System.out.println("FIRST FINALLY");
		// if (args.length == 0)
		// break tr;
		// System.out.println("FIRST FINALLY END");
		// }
		// } catch (Exception f) {
		// System.out.println("CAUGHT");
		// if (args.length == 0)
		// break tr;
		// System.out.println("POST CAUGHT");
		// } finally {
		// System.out.println("FINALLY");
		// if (args.length == 0)
		// break tr;
		// System.out.println("POST-BREAK");
		// }
		//
		// // try {
		// // try {
		// // System.out.println("FIRST NESTED TRY");
		// // if (args.length == 0)
		// // throw new IllegalAccessError();
		// // } finally {
		// //// throw new IllegalStateException();
		// // }
		// // }
		// //// catch (IllegalAccessError e) {
		// //// System.out.println("SECOND CATCHED " + e);
		// //// }
		// // finally {
		// // throw new IllegalArgumentException();
		// // System.out.println("FIRST FINALLY StARTING");
		// // try {
		// // System.out.println("NESTED TRY");
		// // } catch (IllegalAccessError e) {
		// // System.out.println("CATCHED " + e);
		// // } finally {
		// // System.out.println("SECOND FINALLY");
		// // throw new IllegalAccessError();
		// // }
		// // System.out.println("FIRST FINALLY END");
	}
	// try (BufferedReader br = new BufferedReader(null); BufferedReader br2
	// = null) {
	//
	// assert (false);
	// System.out.println("FIN");
	// }
	// // catch(Exception e){
	// //
	// // }
	// finally {
	// Thread.sleep(3000);
	// System.out.println("Finally");
	// }

	private static BufferedReader m() throws IOException {
		throw new IOException();
	}

	// int i = 0;
	// i++;

	// }
}

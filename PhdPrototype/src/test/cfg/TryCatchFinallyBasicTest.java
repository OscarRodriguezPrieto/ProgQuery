package test.cfg;

import org.junit.Test;
import org.neo4j.graphdb.Result;

import database.querys.MainQuery;
import test.GDBAPIBasedTest;

public class TryCatchFinallyBasicTest extends GDBAPIBasedTest {

	@Test
	public void test() {
		Result result = graphDb.execute(MainQuery.ALL_NODES);
		graphDb.execute(MainQuery.DELETE_ALL);
		System.out.println(result.resultAsString());
		// try (Result result = graphDb.execute(
		// "MATCH (try) WHERE try.nodeType='TRY_BLOCK' OR
		// try.nodeType='CATCH_BLOCK' OR try.nodeType='THROW_STATEMENT' OR
		// try.nodeType='ASSERT_STATEMENT' OPTIONAL MATCH
		// (try)-[r:TRY_FINALLY]->(finally) RETURN try,finally ORDER BY
		// try.lineNumber")) {
		// List<List<Node>> tryBLocks =
		// TestUtils.bidimensionalListFromResult(result);
		// Node resourceDec =
		// TestUtils.assertHasNextWith(tryBLocks.get(0).get(0),
		// RelationTypes.TRY_RESOURCES,
		// NodeTypes.VAR_DEC);
		// TestUtils.assertHasNextWith(resourceDec,
		// CFGRelationTypes.CFG_NEXT_STATEMENT, Direction.INCOMING,
		// NodeTypes.CFG_METHOD_ENTRY);
		// Node firstStatement = TestUtils.assertHasNextWith(resourceDec,
		// CFGRelationTypes.CFG_NEXT_STATEMENT,
		// NodeTypes.VAR_DEC);
		// System.out.println(NodeUtils.nodeToString(tryBLocks.get(0).get(0)));
		// assertEquals(TestUtils.assertHasNextWith(tryBLocks.get(0).get(0),
		// CFGRelationTypes.UNCAUGHT_EXCEPTION,
		// NodeTypes.CATCH_BLOCK), tryBLocks.get(11).get(0));
		//
		// }
	}

	@Override
	public String getFileName() {

		return "testClasses/CFG/TryCatchFinally.java";
	}

}

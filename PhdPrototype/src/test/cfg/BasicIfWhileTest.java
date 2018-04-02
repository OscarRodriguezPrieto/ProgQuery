package test.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;

import database.nodes.NodeTypes;
import database.relations.CFGRelationTypes;
import test.GDBAPIBasedTest;
import test.utils.TestUtils;

public class BasicIfWhileTest extends GDBAPIBasedTest {

	@Test
	public void basicLoopAndIfTest() throws Exception {
		try (Result result = graphDb.execute("start method=node:node_auto_index(nodeType='METHOD_DEC'),"
				+ "CFGentry=node:node_auto_index(nodeType='CFG_METHOD_ENTRY') "

				+ "MATCH method-[:CFG_ENTRY]->CFGentry, CFGEnd-[:CFG_END_OF]->method "
				+ " RETURN method,CFGentry,CFGEnd")) {
			Map<String, Object> uniqueRow = result.next();
			Node methodDec = (Node) uniqueRow.get("method"), cfgEntry = (Node) uniqueRow.get("CFGentry"),
					cfgEnd = (Node) uniqueRow.get("CFGEnd");
			assertEquals(methodDec.getProperty("name"), "m");
			assertEquals(TestUtils.relationshipsWith(cfgEntry, cfgEntry.getRelationships()), 2);
			Node firstIfCond = TestUtils.getNextNode(cfgEntry, CFGRelationTypes.CFG_NEXT_CONDITION);
			TestUtils.assertBinopCondition(firstIfCond, "EQUAL_TO");

			Node firstSta = TestUtils.getNextNode(firstIfCond, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE);
			TestUtils.assertExpressionStatement(firstSta, NodeTypes.ASSIGNMENT);

			Node secondSta = TestUtils.getNextNode(firstSta, CFGRelationTypes.CFG_NEXT_STATEMENT);
			TestUtils.assertExpressionStatement(secondSta, NodeTypes.METHOD_INVOCATION);
			// FAIL
			TestUtils.assertHasSingleRel(secondSta, cfgEnd, CFGRelationTypes.CFG_NEXT_STATEMENT, Direction.OUTGOING);
			Node firstWhileCond = TestUtils.getNextNode(firstIfCond, CFGRelationTypes.CFG_NEXT_COND_IF_FALSE);
			TestUtils.assertBinopCondition(firstWhileCond, "LESS_THAN");
			assertFalse(TestUtils.hasAnyRelationshipsWith(secondSta, firstWhileCond));

			Node thirdSta = TestUtils.getNextNode(firstWhileCond, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE);
			TestUtils.assertExpressionStatement(thirdSta, NodeTypes.UNARY_OPERATION);

			Node secondIfCond = TestUtils.getNextNode(thirdSta, CFGRelationTypes.CFG_NEXT_CONDITION);
			TestUtils.assertHasSingleRel(secondIfCond, firstWhileCond, CFGRelationTypes.CFG_NEXT_COND_IF_FALSE,
					Direction.OUTGOING);
			TestUtils.assertBinopCondition(secondIfCond, "REMAINDER");
			assertEquals(secondIfCond.getProperty("typeKind"), "ERROR");

			Node returnSta = TestUtils.getNextNode(secondIfCond, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE);
			assertEquals(returnSta.getProperty("nodeType"), NodeTypes.RETURN_STATEMENT.toString());
			assertFalse(TestUtils.hasAnyRelationshipsWith(returnSta, firstWhileCond));
			// FAIL
			TestUtils.assertHasSingleRel(returnSta, cfgEnd, CFGRelationTypes.CFG_NEXT_STATEMENT, Direction.OUTGOING);

			Node thirdIfCond = TestUtils.getNextNode(firstWhileCond, CFGRelationTypes.CFG_NEXT_COND_IF_FALSE);
			assertEquals(thirdIfCond.getProperty("nodeType"), "LITERAL");
			assertFalse(TestUtils.hasAnyRelationshipsWith(secondIfCond, thirdIfCond));
			assertFalse(TestUtils.hasAnyRelationshipsWith(returnSta, thirdIfCond));
			TestUtils.assertHasSingleRel(thirdIfCond, cfgEnd, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE,
					Direction.OUTGOING);

			Node fourthSta = TestUtils.getNextNode(thirdIfCond, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE);
			assertEquals(fourthSta.getProperty("nodeType"), NodeTypes.EMPTY_STATEMENT.toString());
			// FAIL
			TestUtils.assertHasSingleRel(fourthSta, cfgEnd, CFGRelationTypes.CFG_NEXT_STATEMENT, Direction.OUTGOING);
		} 
	}

	@Override
	public String getFileName() {

		return "testClasses/CFG/IfAndWhile.java";
	}

}

package test.cfg;

import database.querys.eval.MainQuery;
import test.PluginBasedTestWithCommands;

public class TryCatchFinallyComplexTest extends PluginBasedTestWithCommands {
	
	public TryCatchFinallyComplexTest() {
		super("cfgTest2.bat", MainQuery.CFG_NODES_FOR_TESTING, 78);
	}/* TEST DESIGNED TO USE EMBEEDDED, MUST BE RE-IMPLEMENTED

	@Test
	public void test() {
		printNode(74);

		// System.out.println(NodeUtils.nodeToString(nodesToTest.get(0)));

		TestUtils.assertHasNextWith(nodesToTest.get(0), CFGRelationTypes.CFG_NEXT_STATEMENT, Direction.INCOMING,
				NodeTypes.CFG_METHOD_ENTRY);
		assertNext(0, 1, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.LABELED_STATEMENT);
		assertNext(1, 2, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.TRY_BLOCK);
		assertNext(2, 3, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.VAR_DEC);
		assertNext(2, 45, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);

		assertNext(3, 4, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.VAR_DEC);
		assertNext(3, 61, CFGRelationTypes.MAY_THROW, NodeTypes.FINALLY_BLOCK);
		assertNext(4, 5, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(5, 6, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.TRY_BLOCK);
		assertOneThrowsRel(5, 49, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC, "java.lang.AssertionError");
		assertOneThrowsRel(5, 61, CFGRelationTypes.MAY_THROW, NodeTypes.FINALLY_BLOCK,
				"java.lang.ClassNotFoundException");
		assertOneThrowsRel(5, 61, CFGRelationTypes.MAY_THROW, NodeTypes.FINALLY_BLOCK,
				"java.lang.CloneNotSupportedException");

		assertNext(6, 7, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(6, 14, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);
		assertNext(7, 8, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.VAR_DEC);
		assertOneThrowsRel(7, 18, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC, "java.lang.IllegalArgumentException");
		assertOneThrowsRel(7, 18, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC, "java.lang.SecurityException");
		assertOneThrowsRel(7, 22, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC, "java.lang.IllegalAccessException");

		assertNext(8, 9, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.IF_STATEMENT);
		assertNext(9, 10, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.THROW_STATEMENT);
		assertNext(9, 11, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.TRY_BLOCK);
		assertNext(10, 18, CFGRelationTypes.THROWS, NodeTypes.VAR_DEC);
		assertNext(11, 12, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.VAR_DEC);
		assertNext(11, 14, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);

		assertNext(12, 13, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(13, 44, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.THROW_STATEMENT);
		assertOneThrowsRel(13, 18, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC, "java.lang.IllegalArgumentException");
		assertOneThrowsRel(13, 22, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC, "java.lang.IllegalAccessException");
		assertOneThrowsRel(13, 22, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC,
				"java.lang.reflect.InvocationTargetException");

		assertNext(14, 15, CFGRelationTypes.CAUGHT_EXCEPTION, NodeTypes.VAR_DEC);
		assertNext(14, 17, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);
		assertNext(15, 16, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(16, 44, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.THROW_STATEMENT);

		assertNext(17, 18, CFGRelationTypes.CAUGHT_EXCEPTION, NodeTypes.VAR_DEC);
		assertNext(17, 21, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);
		assertNext(18, 19, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(19, 20, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(20, 44, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.THROW_STATEMENT);
		assertOneThrowsRel(20, 61, CFGRelationTypes.MAY_THROW, NodeTypes.FINALLY_BLOCK,
				"java.lang.IllegalAccessException");
		assertOneThrowsRel(20, 61, CFGRelationTypes.MAY_THROW, NodeTypes.FINALLY_BLOCK,
				"java.lang.InstantiationException");

		assertNext(21, 22, CFGRelationTypes.CAUGHT_EXCEPTION, NodeTypes.VAR_DEC);
		assertNext(21, 45, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);
		assertNext(22, 23, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.LABELED_STATEMENT);
		assertNext(23, 25, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.VAR_DEC);
		assertNext(25, 24, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FOR_LOOP);
		assertNext(24, 27, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.TRY_BLOCK);
		assertNext(24, 44, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.THROW_STATEMENT);

		assertNext(27, 28, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.ASSERT_STATEMENT);
		assertNext(27, 36, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.FINALLY_BLOCK);
		assertNext(28, 29, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.IF_STATEMENT);
		assertNext(28, 36, CFGRelationTypes.MAY_THROW, NodeTypes.FINALLY_BLOCK);
		assertNext(29, 30, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.CONTINUE_STATEMENT);
		assertNext(29, 31, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.IF_STATEMENT);
		assertNext(30, 36, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);
		assertNext(31, 32, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.BREAK_STATEMENT);
		assertNext(31, 33, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.IF_STATEMENT);
		assertNext(32, 36, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);
		assertNext(33, 34, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.CONTINUE_STATEMENT);
		assertNext(33, 35, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.BREAK_STATEMENT);
		assertNext(34, 36, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);
		assertNext(35, 36, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);

		assertNext(36, 37, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(37, 38, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.VAR_DEC);
		assertNext(38, 39, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.IF_STATEMENT);
		assertNext(39, 40, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.THROW_STATEMENT);
		assertNext(39, 41, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.IF_STATEMENT);
		assertNext(40, 46, CFGRelationTypes.THROWS, NodeTypes.VAR_DEC);
		assertNext(41, 42, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.ASSERT_STATEMENT);
		assertNext(41, 43, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.THROW_STATEMENT);

		Node lastStatFinally = TestUtils.assertHasNextWith(nodesToTest.get(42),
				CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.CFG_LAST_STATEMENT_IN_FINALLY);
		assertNext(42, 49, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC);
		assertNext(43, 61, CFGRelationTypes.THROWS, NodeTypes.FINALLY_BLOCK);

		assertNext(lastStatFinally, 26, CFGRelationTypes.NO_EXCEPTION, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(lastStatFinally, 45, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);
		assertOneRelBetweenMany(lastStatFinally, 24, CFGRelationTypes.AFTER_FINALLY_PREVIOUS_CONTINUE,
				NodeTypes.FOR_LOOP, r -> !r.hasProperty("label"));
		assertOneRelBetweenMany(lastStatFinally, 24, CFGRelationTypes.AFTER_FINALLY_PREVIOUS_CONTINUE,
				NodeTypes.FOR_LOOP,
				r -> r.hasProperty("label") && r.getProperty("label").toString().contentEquals("fr"));
		assertOneRelBetweenMany(lastStatFinally, 44, CFGRelationTypes.AFTER_FINALLY_PREVIOUS_BREAK,
				NodeTypes.THROW_STATEMENT, r -> !r.hasProperty("label"));
		assertOneRelBetweenMany(lastStatFinally, 61, CFGRelationTypes.AFTER_FINALLY_PREVIOUS_BREAK,
				NodeTypes.FINALLY_BLOCK,
				r -> r.hasProperty("label") && r.getProperty("label").toString().contentEquals("tr"));
		assertNext(44, 45, CFGRelationTypes.THROWS, NodeTypes.CATCH_BLOCK);
		assertNext(45, 46, CFGRelationTypes.CAUGHT_EXCEPTION, NodeTypes.VAR_DEC);
		assertNext(45, 48, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);
		assertNext(46, 47, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.BREAK_STATEMENT);
		assertNext(47, 61, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);
		assertNext(48, 49, CFGRelationTypes.CAUGHT_EXCEPTION, NodeTypes.VAR_DEC);
		assertNext(48, 61, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.FINALLY_BLOCK);
		assertNext(49, 50, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.LABELED_STATEMENT);
		assertNext(50, 51, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.ENHANCED_FOR);
		assertNext(51, 52, CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT, NodeTypes.VAR_DEC);
		assertNext(51, 61, CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS, NodeTypes.FINALLY_BLOCK);
		assertNext(52, 53, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.WHILE_LOOP);
		assertNext(53, 54, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.TRY_BLOCK);
		assertNext(53, 51, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.ENHANCED_FOR);
		assertNext(54, 55, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.IF_STATEMENT);
		assertNext(54, 60, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.FINALLY_BLOCK);
		assertNext(55, 56, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.BREAK_STATEMENT);
		assertNext(55, 57, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.IF_STATEMENT);
		assertNext(56, 60, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);
		assertNext(57, 58, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.BREAK_STATEMENT);
		assertNext(57, 59, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.CONTINUE_STATEMENT);
		assertNext(58, 60, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);
		assertNext(59, 60, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);

		lastStatFinally = TestUtils.assertHasNextWith(nodesToTest.get(60), CFGRelationTypes.CFG_NEXT_STATEMENT,
				NodeTypes.CFG_LAST_STATEMENT_IN_FINALLY);
		assertNext(lastStatFinally, 53, CFGRelationTypes.NO_EXCEPTION, NodeTypes.WHILE_LOOP);
		assertNext(lastStatFinally, 61, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.FINALLY_BLOCK);
		assertOneRelBetweenMany(lastStatFinally, 51, CFGRelationTypes.AFTER_FINALLY_PREVIOUS_BREAK,
				NodeTypes.ENHANCED_FOR, r -> !r.hasProperty("label"));
		assertOneRelBetweenMany(lastStatFinally, 61, CFGRelationTypes.AFTER_FINALLY_PREVIOUS_BREAK,
				NodeTypes.FINALLY_BLOCK,
				r -> r.hasProperty("label") && r.getProperty("label").toString().contentEquals("tr"));
		assertOneRelBetweenMany(lastStatFinally, 51, CFGRelationTypes.AFTER_FINALLY_PREVIOUS_CONTINUE,
				NodeTypes.ENHANCED_FOR,
				r -> r.hasProperty("label") && r.getProperty("label").toString().contentEquals("fr"));
		assertNext(61, 62, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(62, 63, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.TRY_BLOCK);
		assertNext(63, 64, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.ASSERT_STATEMENT);
		assertNext(63, 70, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CATCH_BLOCK);
		assertNext(64, 65, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.EXPRESSION_STATEMENT);
		assertNext(64, 71, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC);
		assertNext(65, 67, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.IF_STATEMENT);
		assertOneThrowsRel(65, 71, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC, "java.lang.InstantiationException");
		assertOneThrowsRel(65, 71, CFGRelationTypes.MAY_THROW, NodeTypes.VAR_DEC, "java.lang.IllegalAccessException");

		assertNext(67, 68, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.BREAK_STATEMENT);
		assertNext(67, 66, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.DO_WHILE_LOOP);
		assertNext(68, 69, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.THROW_STATEMENT);
		assertNext(66, 67, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.IF_STATEMENT);
		assertNext(66, 69, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, NodeTypes.THROW_STATEMENT);
		assertNext(69, 71, CFGRelationTypes.THROWS, NodeTypes.VAR_DEC);
		assertNext(70, 71, CFGRelationTypes.CAUGHT_EXCEPTION, NodeTypes.VAR_DEC);
		assertNext(70, 73, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.FINALLY_BLOCK);
		assertNext(71, 72, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.EMPTY_STATEMENT);
		assertNext(72, 73, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.FINALLY_BLOCK);
		assertNext(73, 74, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.TRY_BLOCK);
		assertNext(74, 75, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.VAR_DEC);
		Node exceptionalEnd = TestUtils.assertHasNextWith(nodesToTest.get(74),
				CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION, NodeTypes.CFG_EXCEPTIONAL_END);
		assertNext(75, 76, CFGRelationTypes.CFG_NEXT_STATEMENT, NodeTypes.IF_STATEMENT);
		assertNext(76, 77, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, NodeTypes.BREAK_STATEMENT);

		assertOneThrowsRel(76, exceptionalEnd, CFGRelationTypes.MAY_THROW, NodeTypes.CFG_EXCEPTIONAL_END,
				"java.io.IOException");
		assertOneThrowsRel(76, exceptionalEnd, CFGRelationTypes.MAY_THROW, NodeTypes.CFG_EXCEPTIONAL_END,
				"java.lang.ClassNotFoundException");
		assertOneThrowsRel(76, exceptionalEnd, CFGRelationTypes.MAY_THROW, NodeTypes.CFG_EXCEPTIONAL_END,
				"java.lang.AssertionError");
		lastStatFinally = TestUtils.assertHasNextWith(nodesToTest.get(76), CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE,
				NodeTypes.CFG_LAST_STATEMENT_IN_FINALLY);
		Node methodEnd = TestUtils.assertHasNextWith(nodesToTest.get(77), CFGRelationTypes.CFG_NEXT_STATEMENT,
				NodeTypes.CFG_METHOD_END);
		assertNext(lastStatFinally, exceptionalEnd, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION,
				NodeTypes.CFG_EXCEPTIONAL_END);

		lastStatFinally = TestUtils.assertHasNextWith(lastStatFinally, CFGRelationTypes.NO_EXCEPTION,
				NodeTypes.CFG_LAST_STATEMENT_IN_FINALLY);
		assertNext(lastStatFinally, exceptionalEnd, CFGRelationTypes.IF_THERE_IS_UNCAUGHT_EXCEPTION,
				NodeTypes.CFG_EXCEPTIONAL_END);
		assertNext(lastStatFinally, methodEnd, CFGRelationTypes.NO_EXCEPTION, NodeTypes.CFG_METHOD_END);

		assertNRelsBetweenMany(lastStatFinally, methodEnd, CFGRelationTypes.AFTER_FINALLY_PREVIOUS_BREAK,

				r -> r.getEndNode().hasLabel(NodeTypes.CFG_METHOD_END) && r.hasProperty("label")
						&& r.getProperty("label").toString().contentEquals("tr"),
				3);

		Node methodNode = TestUtils.assertHasNextWith(methodEnd, CFGRelationTypes.CFG_END_OF, NodeTypes.METHOD_DEC);
		assertNext(exceptionalEnd, methodNode, CFGRelationTypes.CFG_END_OF, NodeTypes.METHOD_DEC);

	}
*/
}

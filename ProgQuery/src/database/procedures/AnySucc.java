package database.procedures;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import database.relations.CFGRelationTypes;

public class AnySucc {

	@UserFunction
	public List<Node> getAnySucc(@Name("node") Node stmt) {
		return new ArrayList<Node>(FunctionUtils.getAllNext(stmt, CFGRelationTypes.CFG_NEXT_STATEMENT,
				CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE,
				CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT, CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS,
				CFGRelationTypes.CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION, CFGRelationTypes.CFG_NO_EXCEPTION,
				CFGRelationTypes.CFG_CAUGHT_EXCEPTION, CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_BREAK,
				CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_CONTINUE, CFGRelationTypes.CFG_SWITCH_CASE_IS_EQUAL_TO,
				CFGRelationTypes.CFG_SWITCH_DEFAULT_CASE, CFGRelationTypes.CFG_MAY_THROW, CFGRelationTypes.CFG_THROWS));

	}
	@UserFunction
	public List<Node> getAnySuccNotItself(@Name("node") Node stmt) {
		List<Node> nodes=FunctionUtils.getAllNext(stmt, CFGRelationTypes.CFG_NEXT_STATEMENT,
				CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE,
				CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT, CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS,
				CFGRelationTypes.CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION, CFGRelationTypes.CFG_NO_EXCEPTION,
				CFGRelationTypes.CFG_CAUGHT_EXCEPTION, CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_BREAK,
				CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_CONTINUE, CFGRelationTypes.CFG_SWITCH_CASE_IS_EQUAL_TO,
				CFGRelationTypes.CFG_SWITCH_DEFAULT_CASE, CFGRelationTypes.CFG_MAY_THROW, CFGRelationTypes.CFG_THROWS);
		nodes.remove(0);
return  nodes;
	}
}

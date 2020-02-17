package database.querys.services;

import database.querys.cypherWrapper.Cardinalidad;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.Element;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.relations.CFGRelationTypes;
import utils.dataTransferClasses.Pair;

public class CFGServicesProgQueryImpl implements CFGServices {
	private static final CFGRelationTypes[] toCFGSuccesor = new CFGRelationTypes[] {
			CFGRelationTypes.CFG_NEXT_STATEMENT, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE,
			CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE, CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT,
			CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS, CFGRelationTypes.CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION,
			CFGRelationTypes.CFG_NO_EXCEPTION, CFGRelationTypes.CFG_CAUGHT_EXCEPTION,
			CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_BREAK, CFGRelationTypes.CFG_AFTER_FINALLY_PREVIOUS_CONTINUE,
			CFGRelationTypes.CFG_SWITCH_CASE_IS_EQUAL_TO, CFGRelationTypes.CFG_SWITCH_DEFAULT_CASE, CFGRelationTypes.CFG_MAY_THROW,
			CFGRelationTypes.CFG_THROWS };

	public Path getCFGSuccesorsOf(MatchElement stat) {
		return getCFGSuccesorsAndItSelfOf(new Path(stat));
	}

	// (assignStat)-[" + cfgSuccesor + "*0..]->(useStat) "
	public Path getCFGSuccesorsAndItSelfOf(MatchElement p) {
		return p.append(Pair.create(new EdgeImpl(Cardinalidad.MIN_TO_INF(0), toCFGSuccesor), new NodeVar("succ")));
	}

	@Override
	public Element getCFGSuccesorsOf(MatchElement stat, String elementsToPreserve) {
		throw new IllegalStateException();
	}

}

package es.uniovi.reflection.progquery.database.relations;

public enum CFGRelationTypes implements RelationTypesInterface {

	CFG_ENTRIES, CFG_END_OF, CFG_FINALLY_TO_LAST_STMT, CFG_NEXT_STATEMENT, 
	CFG_NEXT_STATEMENT_IF_TRUE, CFG_NEXT_STATEMENT_IF_FALSE,
	CFG_FOR_EACH_HAS_NEXT, CFG_FOR_EACH_NO_MORE_ELEMENTS, CFG_SWITCH_CASE_IS_EQUAL_TO, CFG_SWITCH_DEFAULT_CASE,
	CFG_AFTER_FINALLY_PREVIOUS_BREAK, CFG_AFTER_FINALLY_PREVIOUS_CONTINUE,
	CFG_NO_EXCEPTION, 
	CFG_IF_THERE_IS_UNCAUGHT_EXCEPTION,
	CFG_CAUGHT_EXCEPTION, CFG_MAY_THROW, CFG_THROWS
	;


	public static String getCFGRelations() {
		String ret = "";
		for (CFGRelationTypes cfgRel : CFGRelationTypes.values())
			ret += cfgRel.name() + " | ";
		return ret.substring(0, ret.length() - 3);
	}

}

package database.relations;

public enum CFGRelationTypes implements RelationTypesInterface {

	CFG_ENTRY, CFG_END_OF, CFG_NEXT_STATEMENT, 
	CFG_NEXT_STATEMENT_IF_TRUE, CFG_NEXT_STATEMENT_IF_FALSE,
	CFG_FOR_EACH_HAS_NEXT, CFG_FOR_EACH_NO_MORE_ELEMENTS, 
	UNCAUGHT_EXCEPTION, NO_EXCEPTION, CAUGHT_EXCEPTION,
	AFTER_FINALLY_PREVIOUS_BREAK, AFTER_FINALLY_PREVIOUS_CONTINUE, 
	SWITCH_CASE_IS_EQUAL_TO, SWITCH_DEFAULT_CASE, MAY_THROW;


	public static String getCFGRelations() {
		String ret = "";
		for (CFGRelationTypes cfgRel : CFGRelationTypes.values())
			ret += cfgRel.name() + " | ";
		return ret.substring(0, ret.length() - 3);
	}

}

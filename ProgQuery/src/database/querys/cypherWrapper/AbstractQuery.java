package database.querys.cypherWrapper;

import database.querys.eval.Query;
import database.querys.services.AssignmentServices;
import database.querys.services.CFGServices;
import database.querys.services.ExpressionServices;
import database.querys.services.PDGServices;
import database.querys.services.StatementServices;

public abstract class AbstractQuery implements Query {
	protected Clause[] clauses;
	public boolean isProgQuery;

	public AbstractQuery(boolean isProgQuery) {
		super();
		this.isProgQuery = isProgQuery;
	}

	public ExpressionServices getExpressionServices() {
		return isProgQuery ? ExpressionServices.PROG_QUERY : ExpressionServices.WIGGLE;
	}

	public PDGServices getPDGServices() {
		return isProgQuery ? PDGServices.PROG_QUERY : PDGServices.WIGGLE;
	}
	
	public CFGServices getCFGServices() {
		return isProgQuery ? CFGServices.PROG_QUERY : CFGServices.WIGGLE;
	}

	public AssignmentServices getAssignmentServices() {
		return isProgQuery ? AssignmentServices.PROG_QUERY : AssignmentServices.WIGGLE;
	}

	public StatementServices getStatementServices() {
		return isProgQuery ? StatementServices.PROG_QUERY : StatementServices.WIGGLE;
	}
	@Override
	public String queryToString() {
		initiate();
		String res = "";
		for (Clause clause : clauses)
			res += clause.clauseToString() + "\n";
		return res;
	}

	protected abstract void initiate();

}

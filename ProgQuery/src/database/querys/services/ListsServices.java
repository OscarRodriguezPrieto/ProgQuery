package database.querys.services;

import database.querys.cypherWrapper.Binop;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.ExprImpl;
import database.querys.cypherWrapper.Expression;
import database.querys.cypherWrapper.ForEach;
import database.querys.cypherWrapper.Function;
import database.querys.cypherWrapper.SetClause;

public class ListsServices {
	public static Clause forAllEvensInList(String length, int space, String accum, String condition) {
		return forAllEvensInList(new ExprImpl(length), space, new ExprImpl(accum), new ExprImpl(condition));
	}

	public static Clause forAllEvensInList(Expression lenght, int space, Expression accum, Expression condition) {
		return new ForEach("i", new Function("RANGE", new ExprImpl("0"), lenght, new ExprImpl(space + "")),
				new SetClause(accum, new Binop("AND", accum, condition)));
	}
}

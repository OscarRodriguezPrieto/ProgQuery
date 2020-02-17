package database.querys.services;

import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.CreateClause;
import database.querys.cypherWrapper.Node;

public class CreationServices {

	public Clause createNode(Node n) {
		return new CreateClause(n);
	}
}

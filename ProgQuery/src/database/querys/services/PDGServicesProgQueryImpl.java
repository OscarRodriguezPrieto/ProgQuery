package database.querys.services;

import database.querys.cypherWrapper.AnonymousNode;
import database.querys.cypherWrapper.Clause;
import database.querys.cypherWrapper.EdgeDirection;
import database.querys.cypherWrapper.EdgeImpl;
import database.querys.cypherWrapper.MatchClause;
import database.querys.cypherWrapper.MatchElement;
import database.querys.cypherWrapper.Node;
import database.querys.cypherWrapper.NodeVar;
import database.querys.cypherWrapper.Path;
import database.relations.PDGRelationTypes;
import utils.dataTransferClasses.Pair;

public class PDGServicesProgQueryImpl implements PDGServices {

	public MatchClause getOptionalUsesAndStatementsOf(Node declaration) {
		return getOptionalUsesAndStatementsOf(declaration, new AnonymousNode());
	}

	public MatchClause getOptionalUsesAndStatementsOf(Node declaration, Node use) {
		return new MatchClause(true, ExpressionServices.PROG_QUERY.getStatementFromExp(getUsesOf(declaration, use)));
	}

	public Path getUsesOf(Node declaration, Node use) {
		return new Path(declaration, Pair.create(new EdgeImpl(PDGRelationTypes.USED_BY), use));
	}

	@Override
	public MatchElement getAssingnmentsAndVarDeclarations(MatchElement assignment) {
		return assignment.append(
				Pair.create(new EdgeImpl(EdgeDirection.OUTGOING, PDGRelationTypes.MODIFIED_BY), new NodeVar("varDec")));
	}

	@Override
	public MatchElement getAssingnmentsAndVarDeclarations(Node assignment) {
		return getAssingnmentsAndVarDeclarations(new Path(assignment));
	}

	@Override
	public MatchElement getIdsAndVarDeclarations(MatchElement assignment) {
		throw new IllegalStateException();
	}

	@Override
	public Clause getModificationsOnFields(String fieldSetName, String elementsToPreserve) {
		throw new IllegalStateException();
	}

	@Override
	public MatchElement getIdsAndVarDeclarations(MatchElement id, String varsToPreserve) {
		throw new IllegalStateException();
	}

	@Override
	public MatchElement getCompleteIdentification(MatchElement id, String varsToPreserve) {
		throw new IllegalStateException();
	}

	@Override
	public MatchElement getIdsAndVarDeclarations(MatchElement id, MatchElement varDec, String varsToPreserve) {
		throw new IllegalStateException();
	}

	@Override
	public MatchClause getOptionalUsesAndStateModsAndStatementsOf(Node declaration) {
		// TODO Auto-generated method stub
		return getOptionalUsesAndStateModsAndStatementsOf(declaration, new AnonymousNode());
	}

	public MatchClause getOptionalUsesAndStateModsAndStatementsOf(Node declaration, Node use) {
		return new MatchClause(true,
				ExpressionServices.PROG_QUERY.getStatementFromExp(getUsesAndStatModsOf(declaration, use)));
	}

	private MatchElement getUsesAndStatModsOf(Node declaration, Node use) {
		return new Path(declaration,
				Pair.create(new EdgeImpl(PDGRelationTypes.USED_BY, PDGRelationTypes.STATE_MODIFIED_BY), use));

	}

	@Override
	public MatchElement getUsesOf(Node declaration) {
		throw new IllegalStateException();
	}

	@Override
	public MatchElement getCompleteIdentificationFromVar( String varsToPreserve) {
		// TODO Auto-generated method stub
		throw new IllegalStateException();
	}

	@Override
	public MatchElement getCompleteIdentificationFromVar(MatchElement varDec, String varsToPreserve) {
		// TODO Auto-generated method stub
		return null;
	}
}

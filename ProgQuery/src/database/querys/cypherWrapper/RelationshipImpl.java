package database.querys.cypherWrapper;

import database.relations.RelationTypesInterface;

public class RelationshipImpl implements Relationship {

	private MatchElement start, end;
	private Edge edge;

	public RelationshipImpl(MatchElement start, MatchElement end, Edge edge) {
		super();
		this.start = start;
		this.end = end;
		this.edge = edge;
	}

	public RelationshipImpl(MatchElement start, Edge edge) {
		super();
		this.start = start;
		this.end = new AnonymousNode();
		this.edge = edge;
	}

	public RelationshipImpl(MatchElement start, RelationTypesInterface... rels) {
		super();
		this.start = start;
		this.end = new AnonymousNode();
		this.edge = new EdgeImpl(rels);
	}

	public RelationshipImpl(MatchElement start, MatchElement end, RelationTypesInterface... rels) {
		super();
		this.start = start;
		this.end = end;
		this.edge = new EdgeImpl(rels);
	}
	// Posible patrón state si fuera necesario
	@Override
	public String relToString() {
		return start.matchToString() + edge.edgeToString() + end.matchToString();
	}

	@Override
	public Node getLastNode() {
		return end.getLastNode();
	}

}

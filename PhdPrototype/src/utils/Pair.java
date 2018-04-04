package utils;

import org.neo4j.graphdb.Node;

import database.relations.PartialRelation;
import database.relations.RelationTypes;
import database.relations.SimplePartialRelation;

public class Pair<X, Y> {
	
	private final X x;
	private final Y y;
	
	private Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	public static <X,Y>  Pair<X,Y> create(X x, Y y)
	{
		return new Pair<X, Y>(x, y);
	}

	public X getFirst() {
		return x;
	}

	public Y getSecond() {
		return y;
	}


	public static Pair<PartialRelation<RelationTypes>, Object> createPair(Node node, RelationTypes r, Object arg) {
		return Pair.create(new SimplePartialRelation<RelationTypes>(node, r), arg);
	}

	public static Pair<PartialRelation<RelationTypes>, Object> createPair(Node node, RelationTypes r) {
		return Pair.create(new SimplePartialRelation<RelationTypes>(node, r), null);
	}

	public static Pair<PartialRelation<RelationTypes>, Object> createPair(PartialRelation<RelationTypes> rel) {
		return Pair.create(rel, null);
	}
}

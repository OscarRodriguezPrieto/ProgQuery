package utils;

<<<<<<< HEAD
import org.neo4j.graphdb.Node;

import database.relations.PartialRelation;
import database.relations.RelationTypes;
import database.relations.SimplePartialRelation;

public class Pair<X, Y> {

	private final X x;
	private final Y y;

=======
public class Pair<X, Y> {
	
	private final X x;
	private final Y y;
	
>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f
	private Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
<<<<<<< HEAD
=======
	
	public static <X,Y>  Pair<X,Y> create(X x, Y y)
	{
		return new Pair<>(x,y);
	}

>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f

	public X getFirst() {
		return x;
	}

<<<<<<< HEAD
	public Y getSecond() {
		return y;
	}

	public static <X, Y> Pair<X, Y> create(X x, Y y) {
		return new Pair<>(x, y);
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
<<<<<<< HEAD
=======

=======

	public Y getSecond() {
		return y;
	}
	
	

	
>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f
>>>>>>> 35eb70e6a97b8ef16fe55a3f0ce9611eb967a81c
}

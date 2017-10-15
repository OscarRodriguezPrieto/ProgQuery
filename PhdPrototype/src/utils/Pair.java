package utils;

<<<<<<< HEAD
import org.neo4j.graphdb.Node;

import com.sun.source.tree.Tree;

import relations.RelationTypes;

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

	public static Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> createPair(Pair<Tree, Node> treeNodePair,
			RelationTypes r, Object arg) {
		return Pair.create(Pair.create(treeNodePair, r), arg);
	}

	public static Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> createPair(Pair<Tree, Node> treeNodePair,
			RelationTypes r) {
		return Pair.create(Pair.create(treeNodePair, r), null);
	}

	public static Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> createPair(Tree tree, Node node,
			RelationTypes r) {
		return Pair.create(Pair.create(Pair.create(tree, node), r), null);
	}

	public static Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> createPair(Tree tree, Node node, RelationTypes r,
			Object arg) {
		return Pair.create(Pair.create(Pair.create(tree, node), r), arg);
	}

=======

	public Y getSecond() {
		return y;
	}
	
	

	
>>>>>>> 2efd75eb383cfcfe52622098e67722a31ae3861f
}

package es.uniovi.reflection.progquery.utils.dataTransferClasses;

import es.uniovi.reflection.progquery.database.relations.PartialRelation;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
import es.uniovi.reflection.progquery.database.relations.SimplePartialRelation;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class Pair<X, Y> {
	
	private final X x;
	private final Y y;
	
	private Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	public static <X,Y>  Pair<X,Y> create(X x, Y y)
	{
		return new Pair<>(x, y);
	}
	public X getFirst() {
		return x;
	}

	public Y getSecond() {
		return y;
	}


	public static <T extends RelationTypesInterface> Pair<PartialRelation<T>, Object> createPair(NodeWrapper node, T r,
			Object arg) {
		return Pair.create(new SimplePartialRelation<>(node, r), arg);
	}

	public static <T extends RelationTypesInterface> Pair<PartialRelation<T>, Object> createPair(NodeWrapper node, T r) {
		return Pair.create(new SimplePartialRelation<>(node, r), null);
	}

	public static <T extends RelationTypesInterface> Pair<PartialRelation<T>, Object> createPair(PartialRelation<T> rel) {
		return Pair.create(rel, null);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Pair [x=" + x + ", y=" + y + "]";
	}

}

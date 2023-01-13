package es.uniovi.reflection.progquery.utils.types.keys;

import es.uniovi.reflection.progquery.utils.types.TypeKey;

import java.util.List;

public class CompoundTypeKey implements TypeKey{
	//
private boolean isIntersection;

	private List<TypeKey> types;

	public List<TypeKey> getTypes() {
		return types;
	}

	public CompoundTypeKey(boolean isIntersection, List<TypeKey> types) {
		super();
		this.isIntersection = isIntersection;
		this.types = types;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isIntersection ? 1231 : 1237);
		result = prime * result + ((types == null) ? 0 : types.hashCode());
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
		CompoundTypeKey other = (CompoundTypeKey) obj;
		if (isIntersection != other.isIntersection)
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		return true;
	}

	



}

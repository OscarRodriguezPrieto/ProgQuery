package es.uniovi.reflection.progquery.utils.types.keys;

import es.uniovi.reflection.progquery.utils.types.TypeKey;

import java.util.List;

public class ParameterizedTypeKey implements TypeKey{

	private List<TypeKey> typeArgs;
	private TypeKey genericType;

	

	public ParameterizedTypeKey(List<TypeKey> typeArgs, TypeKey genericType) {
		super();
		this.typeArgs = typeArgs;
		this.genericType = genericType;
	}

	public List<TypeKey> getTypeArgs() {
		return typeArgs;
	}

	public TypeKey getGenericType() {
		return genericType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((genericType == null) ? 0 : genericType.hashCode());
		result = prime * result + ((typeArgs == null) ? 0 : typeArgs.hashCode());
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
		ParameterizedTypeKey other = (ParameterizedTypeKey) obj;
		if (genericType == null) {
			if (other.genericType != null)
				return false;
		} else if (!genericType.equals(other.genericType))
			return false;
		if (typeArgs == null) {
			if (other.typeArgs != null)
				return false;
		} else if (!typeArgs.equals(other.typeArgs))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GenericTypeKey [typeArgs=" + typeArgs + ", parameterizedType=" + genericType + "]";
	}

}

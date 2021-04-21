package es.uniovi.reflection.progquery.utils.types;

import java.util.List;

public class GenericTypeKey {

	private List<Object> typeArgs;
	private Object parameterizedType;

	

	public GenericTypeKey(List<Object> typeArgs, Object parameterizedType) {
		super();
		this.typeArgs = typeArgs;
		this.parameterizedType = parameterizedType;
	}

	public List<Object> getTypeArgs() {
		return typeArgs;
	}

	public Object getParameterizedType() {
		return parameterizedType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parameterizedType == null) ? 0 : parameterizedType.hashCode());
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
		GenericTypeKey other = (GenericTypeKey) obj;
		if (parameterizedType == null) {
			if (other.parameterizedType != null)
				return false;
		} else if (!parameterizedType.equals(other.parameterizedType))
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
		return "GenericTypeKey [typeArgs=" + typeArgs + ", parameterizedType=" + parameterizedType + "]";
	}

}

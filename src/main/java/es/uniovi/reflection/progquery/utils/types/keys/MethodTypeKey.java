package es.uniovi.reflection.progquery.utils.types.keys;

import es.uniovi.reflection.progquery.utils.types.TypeKey;

import java.util.List;
import java.util.Objects;

public class MethodTypeKey implements TypeKey{

	private List<TypeKey> paramTypes;
	private List<TypeKey> thrownTypes;
	private List<TypeKey> typeVars;

	private TypeKey returnType;
	private TypeKey receiverType;

	private boolean isCons;

	public MethodTypeKey(List<TypeKey> paramTypes, List<TypeKey> thrownTypes, List<TypeKey> typeVars,
						 TypeKey returnType, TypeKey receiverType, boolean isCons) {
		this.paramTypes = paramTypes;
		this.thrownTypes = thrownTypes;
		this.typeVars = typeVars;
		this.returnType = returnType;
		this.receiverType = receiverType;
		this.isCons = isCons;
	}

	public List<TypeKey> getParamTypes() {
		return paramTypes;
	}

	public List<TypeKey> getThrownTypes() {
		return thrownTypes;
	}

	public TypeKey getReturnType() {
		return returnType;
	}

	public TypeKey getReceiverType() {
		return receiverType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		MethodTypeKey that = (MethodTypeKey) o;
		return isCons == that.isCons && Objects.equals(paramTypes, that.paramTypes) &&
				Objects.equals(thrownTypes, that.thrownTypes) && Objects.equals(typeVars, that.typeVars) &&
				Objects.equals(returnType, that.returnType) && Objects.equals(receiverType, that.receiverType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(paramTypes, thrownTypes, typeVars, returnType, receiverType, isCons);
	}
}

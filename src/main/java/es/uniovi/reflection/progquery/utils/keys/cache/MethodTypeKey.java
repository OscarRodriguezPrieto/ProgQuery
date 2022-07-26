package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

import java.util.List;

public class MethodTypeKey extends AbstractTypeKey{

	private List<TypeKey> paramTypes;
	private List<TypeKey> thrownTypes;
	private TypeKey returnType;
	private TypeKey instanceType;
	private boolean isCons;

	public MethodTypeKey(String fullSignature,List<TypeKey> paramTypes, List<TypeKey> thrownTypes, TypeKey returnType, TypeKey instanceType,
			boolean isCons) {
		super((isCons?"<init> ":"")+fullSignature, NodeTypes.CALLABLE_TYPE.toString());
		this.paramTypes = paramTypes;
		this.thrownTypes = thrownTypes;
		this.returnType = returnType;
		this.instanceType = instanceType;
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

	public TypeKey getInstanceType() {
		return instanceType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instanceType == null) ? 0 : instanceType.hashCode());
		result = prime * result + (isCons ? 1231 : 1237);
		result = prime * result + ((paramTypes == null) ? 0 : paramTypes.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result + ((thrownTypes == null) ? 0 : thrownTypes.hashCode());
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
		MethodTypeKey other = (MethodTypeKey) obj;
		if (instanceType == null) {
			if (other.instanceType != null)
				return false;
		} else if (!instanceType.equals(other.instanceType))
			return false;
		if (isCons != other.isCons)
			return false;
		if (paramTypes == null) {
			if (other.paramTypes != null)
				return false;
		} else if (!paramTypes.equals(other.paramTypes))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		if (thrownTypes == null) {
			if (other.thrownTypes != null)
				return false;
		} else if (!thrownTypes.equals(other.thrownTypes))
			return false;
		return true;
	}


}

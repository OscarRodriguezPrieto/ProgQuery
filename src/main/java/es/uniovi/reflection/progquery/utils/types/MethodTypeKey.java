package es.uniovi.reflection.progquery.utils.types;

import java.util.List;

public class MethodTypeKey {

	private List<Object> paramTypes;
	private List<Object> thrownTypes;
	private Object returnType;
	private Object receiverType;

	private boolean isCons;

	public MethodTypeKey(List<Object> paramTypes, List<Object> thrownTypes, Object returnType, Object receiverType,
			boolean isCons) {
		this.paramTypes = paramTypes;
		this.thrownTypes = thrownTypes;
		this.returnType = returnType;
		this.receiverType = receiverType;
		this.isCons = isCons;
	}

	public List<Object> getParamTypes() {
		return paramTypes;
	}

	public List<Object> getThrownTypes() {
		return thrownTypes;
	}

	public Object getReturnType() {
		return returnType;
	}

	public Object getReceiverType() {
		return receiverType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((receiverType == null) ? 0 : receiverType.hashCode());
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
		if (receiverType == null) {
			if (other.receiverType != null)
				return false;
		} else if (!receiverType.equals(other.receiverType))
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

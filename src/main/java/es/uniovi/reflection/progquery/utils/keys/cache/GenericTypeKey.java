package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

import java.util.List;

public class GenericTypeKey implements TypeKey {

    private List<TypeKey> typeArgs;
    private TypeKey parameterizedType;


    public GenericTypeKey(List<TypeKey> typeArgs, TypeKey parameterizedType) {
        this.typeArgs = typeArgs;
        this.parameterizedType = parameterizedType;
    }

    public List<TypeKey> getTypeArgs() {
        return typeArgs;
    }

    public TypeKey getParameterizedType() {
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
    public String nodeType() {
        return NodeTypes.GENERIC_TYPE.toString();
    }

    @Override
    public String toString() {
        return parameterizedType.toString() + "<" +
                typeArgs.stream().reduce("", (s, typeKey) -> s + typeKey.toString(), (s1, s2) -> s1 + "," + s2) + ">";
    }
}

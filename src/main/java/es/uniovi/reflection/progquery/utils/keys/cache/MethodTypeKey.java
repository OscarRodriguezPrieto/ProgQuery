package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

import java.util.List;
import java.util.Objects;

public class MethodTypeKey implements TypeKey {

    private List<TypeKey> paramTypes;
    private List<TypeKey> thrownTypes;
    private List<TypeKey> typeVars;
    private TypeKey returnType;
    private TypeKey instanceType;
    private boolean isCons;

    public MethodTypeKey(List<TypeKey> paramTypes, List<TypeKey> thrownTypes, List<TypeKey> typeVars,
                         TypeKey returnType, TypeKey instanceType, boolean isCons) {
        this.paramTypes = paramTypes;
        this.thrownTypes = thrownTypes;
        this.typeVars = typeVars;
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MethodTypeKey that = (MethodTypeKey) o;
        return isCons == that.isCons && Objects.equals(paramTypes, that.paramTypes) &&
                Objects.equals(thrownTypes, that.thrownTypes) && Objects.equals(typeVars, that.typeVars) &&
                Objects.equals(returnType, that.returnType) && Objects.equals(instanceType, that.instanceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramTypes, thrownTypes, typeVars, returnType, instanceType, isCons);
    }

    @Override
    public String nodeType() {
        return NodeTypes.CALLABLE_TYPE.toString();
    }

    @Override
    public String toString() {
        String toString = isCons ? "[init] " : "";
        if (typeVars.size() > 0)
            toString += "<" +
                    typeVars.stream().reduce("", (s, typeKey) -> s + typeKey.toString(), (s1, s2) -> s1 +","+ s2) +
                    "> ";
        toString+=returnType.toString() +" (";
        toString +=
                paramTypes.stream().reduce("", (s, typeKey) -> s + typeKey.toString(), (s1, s2) -> s1 +","+ s2) +
                ")";
        if (thrownTypes.size() > 0)
            toString += " throws " + thrownTypes.stream()
                    .reduce("", (s, typeMirror) -> s + typeMirror.toString(), (s1, s2) -> s1 +","+ s2);
        return toString;
    }

    public List<TypeKey> getTypeVars() {
        return typeVars;
    }
}

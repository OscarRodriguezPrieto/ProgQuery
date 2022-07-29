package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

import java.util.Objects;

public class ArrayTypeKey implements TypeKey{
    private TypeKey elemTypeKey;

    public ArrayTypeKey(TypeKey elemTypeKey) {
        this.elemTypeKey = elemTypeKey;
    }

    @Override
    public String toString() {
        return  elemTypeKey.toString()+"[]";
    }

    @Override
    public String nodeType() {
        return NodeTypes.ARRAY_TYPE.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArrayTypeKey that = (ArrayTypeKey) o;
        return Objects.equals(elemTypeKey, that.elemTypeKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elemTypeKey);
    }
}

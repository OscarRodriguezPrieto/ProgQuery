package es.uniovi.reflection.progquery.utils.types.keys;

import es.uniovi.reflection.progquery.utils.types.TypeKey;

import java.util.Objects;

public class ArrayTypeKey implements TypeKey {

    private TypeKey typeOf;

    public ArrayTypeKey(TypeKey typeOf) {
        this.typeOf = typeOf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArrayTypeKey that = (ArrayTypeKey) o;
        return Objects.equals(typeOf, that.typeOf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeOf);
    }

    public TypeKey getTypeOf() {
        return typeOf;
    }

}

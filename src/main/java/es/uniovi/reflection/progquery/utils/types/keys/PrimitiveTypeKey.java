package es.uniovi.reflection.progquery.utils.types.keys;

import es.uniovi.reflection.progquery.utils.types.TypeKey;

import javax.lang.model.type.PrimitiveType;
import java.util.Objects;

public class PrimitiveTypeKey implements TypeKey {

    private String kind;

    public PrimitiveTypeKey(PrimitiveType primitiveType) {
        kind = primitiveType.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PrimitiveTypeKey that = (PrimitiveTypeKey) o;
        return Objects.equals(kind, that.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind);
    }
}

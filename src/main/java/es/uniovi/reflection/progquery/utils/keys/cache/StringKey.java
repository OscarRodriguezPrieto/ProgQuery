package es.uniovi.reflection.progquery.utils.keys.cache;

import java.util.Objects;

public class StringKey extends AbstractTypeKey{


    public StringKey(String key, String nodeType) {
        super(key, nodeType);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StringKey stringKey = (StringKey) o;
        return Objects.equals(toString, stringKey.toString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString);
    }
}

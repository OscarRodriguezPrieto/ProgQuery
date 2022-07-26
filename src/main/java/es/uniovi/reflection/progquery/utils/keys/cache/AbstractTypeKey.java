package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.utils.keys.external.ExternalNotDefinedTypeKey;

public abstract class AbstractTypeKey implements TypeKey{
    String toString;
    private String nodeType;

    public AbstractTypeKey(String toString, String nodeType) {
        this.toString = toString;
        this.nodeType = nodeType;
    }

    @Override
    public ExternalNotDefinedTypeKey getExternalKey() {
        return  new ExternalNotDefinedTypeKey(toString,nodeType);
    }

    @Override
    public String toString() {
        return toString;
    }
}

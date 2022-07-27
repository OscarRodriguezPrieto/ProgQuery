package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

public class WildcardKey implements TypeKey {

    private TypeKey superBound, extendsBound;

    public TypeKey getSuperBound() {
        return superBound;
    }

    public TypeKey getExtendsBound() {
        return extendsBound;
    }

    public WildcardKey(TypeKey superBound, TypeKey extendsBound) {
        this.superBound = superBound;
        this.extendsBound = extendsBound;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((extendsBound == null) ? 0 : extendsBound.hashCode());
        result = prime * result + ((superBound == null) ? 0 : superBound.hashCode());
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
        WildcardKey other = (WildcardKey) obj;
        if (extendsBound == null) {
            if (other.extendsBound != null)
                return false;
        } else if (!extendsBound.equals(other.extendsBound))
            return false;
        if (superBound == null) {
            if (other.superBound != null)
                return false;
        } else if (!superBound.equals(other.superBound))
            return false;
        return true;
    }

    @Override
    public String nodeType() {
        return NodeTypes.WILDCARD_TYPE.toString();
    }

    @Override
    public String toString() {
        String toString = "?";
        if (extendsBound == null && superBound == null)
            return toString;
        return toString +
                (extendsBound != null ? " extends " + extendsBound : " super " + superBound);
    }
}

package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

import java.util.List;

public class CompoundTypeKey extends AbstractTypeKey {
    //
    private boolean isIntersection;

    private List<TypeKey> types;
    private String toString;

    public List<TypeKey> getTypes() {
        return types;
    }

    public CompoundTypeKey(String toString, boolean isIntersection, List<TypeKey> types) {
        super((isIntersection ? NodeTypes.INTERSECTION_TYPE : NodeTypes.UNION_TYPE).toString(), toString);
        this.isIntersection = isIntersection;
        this.types = types;
        this.toString = toString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isIntersection ? 1231 : 1237);
        result = prime * result + ((types == null) ? 0 : types.hashCode());
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
        CompoundTypeKey other = (CompoundTypeKey) obj;
        if (isIntersection != other.isIntersection)
            return false;
        if (types == null) {
            if (other.types != null)
                return false;
        } else if (!types.equals(other.types))
            return false;
        return true;
    }

}

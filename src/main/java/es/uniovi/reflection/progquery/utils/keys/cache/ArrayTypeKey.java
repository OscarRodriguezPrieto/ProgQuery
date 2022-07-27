package es.uniovi.reflection.progquery.utils.keys.cache;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

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
}

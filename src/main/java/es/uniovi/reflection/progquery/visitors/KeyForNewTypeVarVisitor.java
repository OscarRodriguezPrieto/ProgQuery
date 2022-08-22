package es.uniovi.reflection.progquery.visitors;

import es.uniovi.reflection.progquery.utils.keys.cache.TypeKey;
import es.uniovi.reflection.progquery.utils.keys.cache.TypeVarKey;

import javax.lang.model.type.TypeVariable;

public class KeyForNewTypeVarVisitor extends KeyTypeVisitor {
    private String ownerFullName;

    public KeyForNewTypeVarVisitor(String ownerFullName) {
        this.ownerFullName = ownerFullName;
    }

    @Override
    public TypeKey visitTypeVariable(TypeVariable t, Object param) {
        return new TypeVarKey(t,ownerFullName);
    }
}

package es.uniovi.reflection.progquery.utils.types.keys;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import es.uniovi.reflection.progquery.utils.MethodNameInfo;
import es.uniovi.reflection.progquery.utils.types.TypeKey;

import javax.lang.model.type.TypeVariable;
import java.util.Objects;


public class TypeVariableKey implements TypeKey {

    private String name;
    private Object ownerKey; //Could be a method key (String) or a type_def key (TypeKey) => Object

    public TypeVariableKey(TypeVariable typeVar) {
        name = typeVar.toString();

        ownerKey =
                new MethodNameInfo((Symbol.MethodSymbol) ((Type.TypeVar) typeVar).tsym.owner).getFullyQualifiedName();
    }

    public TypeVariableKey(TypeVariable typeVar, TypeKey ownerKey) {
        name = typeVar.toString();
        this.ownerKey = ownerKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TypeVariableKey that = (TypeVariableKey) o;
        return Objects.equals(name, that.name) && Objects.equals(ownerKey, that.ownerKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ownerKey);
    }

    @Override
    public String toString() {
        return ownerKey + ":" + name ;
    }
}

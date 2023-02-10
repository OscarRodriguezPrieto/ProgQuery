package es.uniovi.reflection.progquery.utils.types.keys;

import com.sun.tools.javac.code.Symbol;
import es.uniovi.reflection.progquery.utils.types.TypeKey;

import java.util.Objects;

public class TypeDefinitionKey implements TypeKey {
    private String ownerName, typeName;

    public TypeDefinitionKey(Symbol symbol) {
        Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) symbol;
        ownerName = classSymbol.owner.toString();
        typeName = classSymbol.name.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TypeDefinitionKey that = (TypeDefinitionKey) o;
        return Objects.equals(ownerName, that.ownerName) && Objects.equals(typeName, that.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerName, typeName);
    }

    @Override
    public String toString() {
        return ownerName + "." + typeName;
    }
}

package es.uniovi.reflection.progquery.utils.keys.cache;

import com.sun.tools.javac.code.Symbol;
import es.uniovi.reflection.progquery.database.nodes.NodeCategory;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.keys.external.ExternalTypeDefKey;

import java.util.Objects;


public class ClassSymbolKey extends AbstractTypeKey{
    Symbol.ClassSymbol symbol;

    public ClassSymbolKey(Symbol.ClassSymbol symbol) {
        super(symbol.fullname.toString(), NodeCategory.TYPE_DEFINITION.toString());
        this.symbol = symbol;
    }
    @Override
    public ExternalNotDefinedTypeKey getExternalDeclaredKey() {
        if (symbol.sourcefile == null)
            return super.getExternalKey();
       return new ExternalTypeDefKey(symbol.sourcefile.getName(), symbol.getSimpleName().toString());

    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClassSymbolKey that = (ClassSymbolKey) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
}

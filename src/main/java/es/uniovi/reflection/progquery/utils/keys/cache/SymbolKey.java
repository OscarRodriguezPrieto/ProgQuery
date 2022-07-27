package es.uniovi.reflection.progquery.utils.keys.cache;

import com.sun.tools.javac.code.Symbol;

import java.util.Objects;

public class SymbolKey extends  AbstractTypeKey{

    private Symbol symbol;

    public SymbolKey(String nodeType, Symbol symbol) {
        super(symbol.toString(),nodeType);
        this.symbol = symbol;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SymbolKey symbolKey = (SymbolKey) o;
        return Objects.equals(symbol, symbolKey.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

}

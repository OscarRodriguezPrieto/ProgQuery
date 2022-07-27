package es.uniovi.reflection.progquery.utils.keys.cache;

import com.sun.tools.javac.code.Symbol;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

import java.util.HashMap;
import java.util.Map;

public class TypeVarKey extends SymbolKey {
    private static Map<Symbol, Integer> symbolToId = new HashMap<>();
    private static Map<String, Integer> nameCount = new HashMap<>();
    private int index;


    public TypeVarKey(Symbol symbol) {
        super(NodeTypes.TYPE_VARIABLE.toString(), symbol);
        if (symbolToId.containsKey(symbol))
            index = symbolToId.get(symbol);
        else {
            int count = 0;
            if (nameCount.containsKey(symbol.toString()))
                count = nameCount.get(symbol.toString());
            index = count;
            nameCount.put(symbol.toString(), index + 1);
            symbolToId.put(symbol, index + 1);

        }
    }

    @Override
    public String toString() {
        return super.toString() + (index == 0 ? "" : "[" + index + "]");
    }
}

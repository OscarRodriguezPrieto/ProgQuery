package es.uniovi.reflection.progquery.utils.keys.cache;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;

import javax.lang.model.type.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeVarKey extends SymbolKey {
    private static Map<Symbol, Integer> symbolToId = new HashMap<>();
    private static Map<String, Integer> nameCount = new HashMap<>();
    private int index;

    public static void initNameCount(Stream<Pair<String,Integer>> retrievedCounts){
        nameCount.putAll(retrievedCounts.collect(Collectors.toMap(pair->pair.getFirst(),pair->pair.getSecond())));
    }

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

    public static final String WILDCARD_CLUE="capture#";
    public static SymbolKey getKeyForTypeVar(TypeVariable t){
        Symbol symbol=((Type.TypeVar) t).tsym;
        if(t.toString().contains(WILDCARD_CLUE))
            return new SymbolKey(NodeTypes.TYPE_VARIABLE.toString(),symbol);
        return new TypeVarKey(symbol);

    }
}

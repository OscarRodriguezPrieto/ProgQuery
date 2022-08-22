package es.uniovi.reflection.progquery.utils.keys.cache;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

import javax.lang.model.type.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class TypeVarKey extends SymbolKey {

    private static Map<Symbol, String> typeParamToDeclaringName = new HashMap<>();
    private String parentKey;

    private TypeVarKey(Symbol symbol) {
        super(NodeTypes.TYPE_VARIABLE.toString(), symbol);
        this.parentKey = typeParamToDeclaringName.get(symbol);

    }

    public TypeVarKey(TypeVariable type, String parentKey) {
        super(NodeTypes.TYPE_VARIABLE.toString(), ((Type.TypeVar) type).tsym);
        this.parentKey = parentKey;
        typeParamToDeclaringName.put(symbol, parentKey);

    }

    @Override
    public String toString() {
        return parentKey + ":" + super.toString();
    }

    public static final String WILDCARD_CLUE = "capture#";

    public static SymbolKey getKeyForTypeVar(TypeVariable t) {
        Symbol symbol = ((Type.TypeVar) t).tsym;
        if (t.toString().contains(WILDCARD_CLUE))
            return new SymbolKey(NodeTypes.TYPE_VARIABLE.toString(), symbol);
        return new TypeVarKey(symbol);

    }
}

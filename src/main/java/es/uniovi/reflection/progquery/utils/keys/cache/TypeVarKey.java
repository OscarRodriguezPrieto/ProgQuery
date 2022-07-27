package es.uniovi.reflection.progquery.utils.keys.cache;

import com.sun.tools.javac.code.Symbol;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;

public class TypeVarKey extends SymbolKey {
    public static int counter = 0;
    private int index;

    public void setIndex() {
        this.index = counter++;
    }

    public TypeVarKey(Symbol symbol) {
        super(NodeTypes.TYPE_VARIABLE.toString(), symbol);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + index + ")";
    }
}

package es.uniovi.reflection.progquery.cache;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.types.ExternalTypeDefKey;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiModuleDefinitionCache<TKEY> extends DefinitionCache<TKEY> {


    private final Map<ExternalTypeDefKey, NodeWrapper> externalDefinitionCache;
    private final Function<TKEY, ExternalTypeDefKey> fromKeyToExternalKey;

    public MultiModuleDefinitionCache(Stream<Pair<NodeWrapper, ExternalTypeDefKey>> externalDefs,
                                      Function<TKEY, ExternalTypeDefKey> keyToExternal) {
        externalDefinitionCache =
                externalDefs.collect(Collectors.toMap(pair -> pair.getSecond(), pair -> pair.getFirst()));
        fromKeyToExternalKey = keyToExternal;
    }


    public static void initExternalCache(String programID, String userID) {
        try (NEO4JManager manager = DatabaseFachade.CURRENT_INSERTION_STRATEGY.getManager()) {
            DefinitionCache.TYPE_CACHE =
                    new MultiModuleDefinitionCache<>(manager.getDeclaredTypeDefsFrom(programID, userID), (Object o) -> {
                        if (o instanceof ClassSymbol) {
                            ClassSymbol cs = (ClassSymbol) o;
                            String fileName = cs.sourcefile == null ? cs.classfile.getName() : cs.sourcefile.getName();
                            System.out.println(fileName + "    " + cs.getSimpleName().toString() + "   " +
                                    cs.getQualifiedName().toString() + "   " + cs.fullname);
                            return new ExternalTypeDefKey(fileName, cs.getSimpleName().toString());

                        }
                        return null;
                    });
            DefinitionCache.METHOD_DEF_CACHE = new DefinitionCache<>();
        }
    }

    public void put(TKEY k, NodeWrapper v) {

        if (externalDefinitionCache.containsKey(fromKeyToExternalKey.apply(k)))
            throw new IllegalArgumentException(
                    "There is already an external definition with this Key=" + fromKeyToExternalKey.apply(k));

        super.put(k, v);
    }

    public NodeWrapper get(TKEY k) {
        ExternalTypeDefKey externalKey = fromKeyToExternalKey.apply(k);
        return externalDefinitionCache.containsKey(externalKey) ? externalDefinitionCache.get(externalKey) :
                super.get(k);
    }

    public boolean containsKey(TKEY k) {
        if (super.containsKey(k))
            return true;
        return externalDefinitionCache.containsKey(fromKeyToExternalKey.apply(k));
    }

    public boolean containsDef(TKEY k) {
        if (super.containsDef(k))
            return true;
        return externalDefinitionCache.containsKey(fromKeyToExternalKey.apply(k));
    }

}

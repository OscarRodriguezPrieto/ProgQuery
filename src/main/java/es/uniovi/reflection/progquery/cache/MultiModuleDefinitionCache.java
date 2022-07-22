package es.uniovi.reflection.progquery.cache;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.types.ExternalNotDefinedTypeKey;
import es.uniovi.reflection.progquery.utils.types.ExternalTypeDefKey;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiModuleDefinitionCache<TKEY> extends DefinitionCache<TKEY> {


    private final Map<ExternalNotDefinedTypeKey, NodeWrapper> externalDefinitionCache;
    private final Function<TKEY, ExternalNotDefinedTypeKey> toExternalKeyForDefined;
    private final Function<TKEY, ExternalNotDefinedTypeKey> toExternalKeyForNonDefined;

    public MultiModuleDefinitionCache(Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> externalDefs,
                                      Stream<Pair<NodeWrapper, ExternalNotDefinedTypeKey>> externalDefsNotDeclared,
                                      Function<TKEY, ExternalNotDefinedTypeKey> toExternalKeyForDefined,
                                      Function<TKEY, ExternalNotDefinedTypeKey> toExternalKeyForNonDefined) {
        externalDefinitionCache =
                externalDefs.collect(Collectors.toMap(pair -> pair.getSecond(), pair -> pair.getFirst()));
        externalDefinitionCache.putAll(externalDefsNotDeclared
                .collect(Collectors.toMap(pair -> pair.getSecond(), pair -> pair.getFirst())));
        this.toExternalKeyForDefined = toExternalKeyForDefined;
        this.toExternalKeyForNonDefined = toExternalKeyForNonDefined;
    }


    public static void initExternalCache(String programID, String userID) {
        try (NEO4JManager manager = DatabaseFachade.CURRENT_INSERTION_STRATEGY.getNewManager()) {
            DefinitionCache.TYPE_CACHE =
                    new MultiModuleDefinitionCache<>(manager.getDeclaredTypeDefsFrom(programID, userID),
                            manager.getNotDeclaredTypesFrom(programID, userID), (Object o) -> {
                        //ESTO SOLO SE VA A LLAMAR PARA PREGUNTAR SI ALGO ESTA EN LA EXTERNAL CACHE
                        if (o instanceof ClassSymbol) {
                            ClassSymbol cs = (ClassSymbol) o;
                            if (cs.sourcefile == null && cs.classfile == null)
                                return new ExternalNotDefinedTypeKey(cs.fullname.toString());
                            String fileName = cs.sourcefile == null ? cs.classfile.getName() : cs.sourcefile.getName();
                            return new ExternalTypeDefKey(fileName, cs.getSimpleName().toString());

                        }
                        return null;
                    }, (Object o) -> {
                        if (o instanceof ClassSymbol)
                            return new ExternalNotDefinedTypeKey(((ClassSymbol) o).fullname.toString());
                        return null;
                    });
            DefinitionCache.METHOD_DEF_CACHE = new DefinitionCache<>();
        }
    }

    @Override
    public void put(TKEY k, NodeWrapper v) {
        ExternalNotDefinedTypeKey lastKeyUsed;
        if (externalDefinitionCache.containsKey(lastKeyUsed = toExternalKeyForNonDefined.apply(k)) ||
                externalDefinitionCache.containsKey(lastKeyUsed = toExternalKeyForDefined.apply(k)))
            throw new IllegalArgumentException("There is already an external definition with this Key=" + lastKeyUsed);

        super.put(k, v);
    }

    @Override
    public NodeWrapper get(TKEY k) {
        ExternalNotDefinedTypeKey notDefinedKey = toExternalKeyForNonDefined.apply(k), definedkey =
                toExternalKeyForDefined.apply(k);

        return externalDefinitionCache.containsKey(notDefinedKey) ? externalDefinitionCache.get(notDefinedKey) :
                externalDefinitionCache.containsKey(definedkey) ? externalDefinitionCache.get(definedkey) :
                        super.get(k);
    }

    @Override
    public boolean containsKey(TKEY k) {
        if (super.containsKey(k))
            return true;
        return externalDefinitionCache.containsKey(toExternalKeyForNonDefined.apply(k)) ||
                externalDefinitionCache.containsKey(toExternalKeyForDefined.apply(k));
    }

    @Override
    public boolean containsDef(TKEY k) {
        if (super.containsDef(k))
            return true;
        return externalDefinitionCache.containsKey(toExternalKeyForDefined.apply(k));
    }

    @Override
    protected NodeWrapper getFromNotDefinedCache(TKEY k) {
        NodeWrapper oldClassNode = super.getFromNotDefinedCache(k);
        return oldClassNode == null ? externalDefinitionCache.get(toExternalKeyForNonDefined.apply(k)) : oldClassNode;
    }


}

package es.uniovi.reflection.progquery.cache;

import com.sun.tools.javac.code.Symbol;
import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.database.relations.CDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.database.relations.TypeRelations;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.types.ExternalTypeDefKey;
import es.uniovi.reflection.progquery.visitors.KeyTypeVisitor;
import es.uniovi.reflection.progquery.visitors.TypeVisitor;
import org.neo4j.graphdb.Direction;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefinitionCache<TKEY> {
    private static final boolean DEBUG = false;
    public static ASTAuxiliarStorage ast;
    public static DefinitionCache<Object> TYPE_CACHE;
    public static DefinitionCache<Symbol> METHOD_DEF_CACHE;


    private final Map<TKEY, NodeWrapper> auxNodeCache = new HashMap<>();
    final Map<TKEY, NodeWrapper> definitionNodeCache = new HashMap<>();
    private final Map<ExternalTypeDefKey, NodeWrapper> externalDefinitionCache;

    public DefinitionCache(Stream<Pair<NodeWrapper, ExternalTypeDefKey>> externalDefs) {
        externalDefinitionCache =
                externalDefs.collect(Collectors.toMap(pair -> pair.getSecond(), pair -> pair.getFirst()));
    }

    public static void initExternalCache(String programID, String userID) {
        try (NEO4JManager manager = DatabaseFachade.CURRENT_INSERTION_STRATEGY.getManager()) {
            DefinitionCache.TYPE_CACHE = new DefinitionCache<>(manager.getDeclaredTypeDefsFrom(programID,userID));
//            DefinitionCache.METHOD_DEF_CACHE = new DefinitionCache<>(retrievePreviousMethods(), null);
        }
    }

    public DefinitionCache() {
        externalDefinitionCache = new HashMap<>();
    }

    public void put(TKEY k, NodeWrapper v) {

        if (DEBUG)
            System.out.println("putting " + k + " " + v);
        // System.out.println("Trying to put ");
        if (auxNodeCache.containsKey(k))
            throw new IllegalArgumentException("Key " + k + " twice ");

        if (!definitionNodeCache.containsKey(k))
            auxNodeCache.put(k, v);
            // System.out.println("PUTTNG n=" + v.getId() + " k=" + k);
        else
            throw new IllegalArgumentException("Key " + k + " already in definition");
    }

    public void putClassDefinition(TKEY classSymbol, NodeWrapper classDec, Set<NodeWrapper> typeDecNodeList,
                                   Set<NodeWrapper> typeDecsUses) {
        NodeWrapper oldClassNode = null;

        if (auxNodeCache.containsKey(classSymbol)) {
            oldClassNode = auxNodeCache.get(classSymbol);
            for (RelationshipWrapper r : oldClassNode.getRelationships(Direction.OUTGOING,
                    //					RelationTypes.DECLARES_METHOD, RelationTypes.DECLARES_CONSTRUCTOR,

                    RelationTypes.DECLARES_FIELD, TypeRelations.IS_SUBTYPE_EXTENDS,
                    TypeRelations.IS_SUBTYPE_IMPLEMENTS))
                r.delete();

            typeDecNodeList.remove(oldClassNode);
            oldClassNode.getRelationships(Direction.OUTGOING, CDGRelationTypes.USES_TYPE_DEF)
                    .forEach(usesTypeDecRel -> typeDecsUses.add(usesTypeDecRel.getEndNode()));
        }

        putDefinition(classSymbol, classDec, oldClassNode);
    }

    public void putDefinition(TKEY k, NodeWrapper v) {
        putDefinition(k, v, auxNodeCache.get(k));
    }

    private void putDefinition(TKEY k, NodeWrapper v, NodeWrapper previousNode) {
        if (DEBUG)
            System.out.println("putting def " + k + " " + v);

        if (previousNode != null) {
            for (RelationshipWrapper r : previousNode.getRelationships(Direction.INCOMING)) {
                r.getStartNode().createRelationshipTo(v, r.getType());
                r.delete();
            }
            for (RelationshipWrapper r : previousNode.getRelationships(Direction.OUTGOING)) {
                v.createRelationshipTo(r.getEndNode(), r.getType());
                r.delete();
            }

            auxNodeCache.remove(k);
            previousNode.delete();
        }

        definitionNodeCache.put(k, v);
    }

    public NodeWrapper get(TKEY k) {
        return definitionNodeCache.containsKey(k) ? definitionNodeCache.get(k) : auxNodeCache.get(k);
    }

    public boolean containsKey(TKEY k) {
        return auxNodeCache.containsKey(k) || definitionNodeCache.containsKey(k);
    }

    public boolean containsDef(TKEY k) {
        return definitionNodeCache.containsKey(k);
    }

    public int totalTypesCached() {
        return auxNodeCache.size();
    }

    public int totalDefsCached() {
        return definitionNodeCache.size();
    }


    public static NodeWrapper getOrCreateType(TypeMirror type, Object key, ASTAuxiliarStorage ast) {
        // System.out.println(type + " inspected in es.uniovi.reflection.progquery.cache");
        // System.out.println("looking for key " + key);
        // System.out.println(DefinitionCache.CLASS_TYPE_CACHE.auxNodeCache.toString());
        // System.out.println(DefinitionCache.CLASS_TYPE_CACHE.auxNodeCache.size());
        if (DefinitionCache.TYPE_CACHE.containsKey(key)) {

            // System.out.println("RECOVERED FROM KEY " + key + "\n\t FROM " +
            // type);
            // DefinitionCache.CLASS_TYPE_CACHE.get(key).getLabels().forEach(System.out::print);
            // System.out.println();
            // System.out.println(type + " already in es.uniovi.reflection.progquery.cache");
            return DefinitionCache.TYPE_CACHE.get(key);
        }
        // System.out.println("CREATING NEW TYPE " + type);
        return createTypeDec(type, key, ast);
    }

    public static NodeWrapper getExistingType(TypeMirror type) {
        if (DefinitionCache.TYPE_CACHE.containsKey(type)) {

            // System.out.println(type + " already in es.uniovi.reflection.progquery.cache");
            return DefinitionCache.TYPE_CACHE.get(type);
        }
        throw new IllegalArgumentException("Not Type dounf for " + type);
    }

    public static NodeWrapper getOrCreateType(TypeMirror type, ASTAuxiliarStorage ast) {

        return getOrCreateType(type, type.accept(new KeyTypeVisitor(), null), ast);
    }

    private static NodeWrapper createTypeDec(TypeMirror type, Object key, ASTAuxiliarStorage ast) {

        return type.accept(new TypeVisitor(ast), key);

    }
}

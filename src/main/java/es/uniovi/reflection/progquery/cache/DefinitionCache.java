package es.uniovi.reflection.progquery.cache;

import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.database.relations.CDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.database.relations.TypeRelations;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.visitors.KeyTypeVisitor;
import es.uniovi.reflection.progquery.visitors.TypeVisitor;
import org.neo4j.graphdb.Direction;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefinitionCache<TKEY> {
    private static final boolean DEBUG = false;
    public static final DefinitionCache<Object> TYPE_CACHE = new DefinitionCache<>();

    public static final DefinitionCache<String> METHOD_DEF_CACHE = new DefinitionCache<>();

    private final Map<TKEY, NodeWrapper> auxNodeCache = new HashMap<>();
    final Map<TKEY, NodeWrapper> definitionNodeCache = new HashMap<>();

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
        // AuxDebugInfo.lastMessage = "BEFORE PUTTING DEFINITION " +
        // classSymbol.toString();

        if (auxNodeCache.containsKey(classSymbol)) {
            oldClassNode = auxNodeCache.get(classSymbol);
            for (RelationshipWrapper r : oldClassNode.getRelationships(Direction.OUTGOING,
                    //					RelationTypes.DECLARES_METHOD, RelationTypes.DECLARES_CONSTRUCTOR,

                    RelationTypes.DECLARES_FIELD, TypeRelations.IS_SUBTYPE_EXTENDS,
                    TypeRelations.IS_SUBTYPE_IMPLEMENTS))
                r.delete();
            // System.out.println("TRYING TO REMOVE n=" + oldClassNode.getId());
            // System.out.println("CONTAINS = " +
            // typeDecNodeList.contains(oldClassNode));
            // System.out.println("INDEX OF = " +
            // typeDecNodeList.indexOf(oldClassNode));
            // System.out.println(typeDecNodeList.get(typeDecNodeList.indexOf(oldClassNode)).getId());
            // System.out.println("TYPE DEC LIST BEFORE
            // "+typeDecNodeList.size());
            // typeDecNodeList.forEach(n -> System.out.print(n.getId() + ", "));
            // System.out.println();
            typeDecNodeList.remove(oldClassNode);
            // System.out.println("TYPE DEC LIST AFTER"+typeDecNodeList.size());
            // typeDecNodeList.forEach(n -> System.out.print(n.getId() + ", "));
            // System.out.println();
            oldClassNode.getRelationships(Direction.OUTGOING, CDGRelationTypes.USES_TYPE_DEF).forEach(usesTypeDecRel ->

                    // LOS PAQUETES NO SON DECLARADOS O NO...SIEMPRE SE CREAN...
                    typeDecsUses.add(usesTypeDecRel.getEndNode()));

        }

        putDefinition(classSymbol, classDec, oldClassNode);
        // AuxDebugInfo.lastMessage = null;
    }

    public void putDefinition(TKEY k, NodeWrapper v) {
        putDefinition(k, v, auxNodeCache.get(k));
    }

    private void putDefinition(TKEY k, NodeWrapper v, NodeWrapper previousNode) {
        if (DEBUG)
            System.out.println("putting def " + k + " " + v);

        if (previousNode != null) {
            // if (DEBUG)
            // System.out.println("Removing " + previousNode.getId() + " from
            // es.uniovi.reflection.progquery.cache");
            // No me deja eliminalo porque todav�a tiene relaciones

            // Habria que pasar las relaciones al nuevo type
            // Igual es m�s eficiente iterar todas y un if??? con direction
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
    /*
     * private final static Map<TypeKind,
     *
     * Pair<NodeTypes, BiConsumer<Type, NodeWrapper>>>
     * fromKindToCreateStrategyXX = initMap();
     *
     * // private final static Map<TypeKind, BiFunction<Type, Symbol, Object>>
     * // fromKindToKey = initKeyStrategyMap();
     *
     * private static <T extends NodeWrapper> Map<TypeKind, Pair<NodeTypes,
     * BiConsumer<Type, NodeWrapper>>> initMap() { // Aqui puedo poner la
     * creaci�n particular de cada uno Tupla NodeType, // Strategy
     * BiConsumer<Type, NodeWrapper> voidF = (type, node) -> {
     * DefinitionCache.CLASS_TYPE_CACHE.put(type, node); }, voidFPrimitive =
     * (type, node) -> { DefinitionCache.CLASS_TYPE_CACHE.put(type.tsym, node);
     * };
     *
     * Map<TypeKind, Pair<NodeTypes, BiConsumer<Type, NodeWrapper>>> map = new
     * HashMap<TypeKind, Pair<NodeTypes, BiConsumer<Type, NodeWrapper>>>();
     * map.put(TypeKind.ARRAY, Pair.create(NodeTypes.ARRAY_TYPE, (type, node) ->
     * { node.createRelationshipTo(DefinitionCache.getOrCreateType(((ArrayType)
     * type).elemtype), RelationTypes.ELEMENT_TYPE);
     * DefinitionCache.CLASS_TYPE_CACHE.put(type, node); }));
     * map.put(TypeKind.BOOLEAN, Pair.create(NodeTypes.PRIMITIVE_TYPE,
     * voidFPrimitive)); map.put(TypeKind.BYTE,
     * Pair.create(NodeTypes.PRIMITIVE_TYPE, voidFPrimitive));
     * map.put(TypeKind.CHAR, Pair.create(NodeTypes.PRIMITIVE_TYPE,
     * voidFPrimitive)); map.put(TypeKind.DOUBLE,
     * Pair.create(NodeTypes.PRIMITIVE_TYPE, voidFPrimitive));
     *
     * map.put(TypeKind.ERROR, Pair.create(NodeTypes.ERROR_TYPE, voidF));
     *
     * map.put(TypeKind.EXECUTABLE, Pair.create(NodeTypes.EXECUTABLE_TYPE,
     * (type, node) -> { throw new IllegalStateException(); }));
     * map.put(TypeKind.FLOAT, Pair.create(NodeTypes.PRIMITIVE_TYPE,
     * voidFPrimitive)); map.put(TypeKind.INT,
     * Pair.create(NodeTypes.PRIMITIVE_TYPE, voidFPrimitive));
     * map.put(TypeKind.INTERSECTION, Pair.create(NodeTypes.INTERSECTION_TYPE,
     * voidF));
     *
     * map.put(TypeKind.LONG, Pair.create(NodeTypes.PRIMITIVE_TYPE,
     * voidFPrimitive)); map.put(TypeKind.NONE, Pair.create(NodeTypes.NONE_TYPE,
     * voidF)); map.put(TypeKind.NULL, Pair.create(NodeTypes.NULL_TYPE, voidF));
     * map.put(TypeKind.PACKAGE, Pair.create(NodeTypes.PACKAGE_TYPE, voidF));
     * map.put(TypeKind.SHORT, Pair.create(NodeTypes.PRIMITIVE_TYPE,
     * voidFPrimitive));
     *
     * // ESTUIDAR EL CASO DEL TYPEVAR DE MOMENTO SIN METER
     * map.put(TypeKind.UNION, Pair.create(NodeTypes.PRIMITIVE_TYPE,
     * voidFPrimitive)); map.put(TypeKind.VOID,
     * Pair.create(NodeTypes.PRIMITIVE_TYPE, voidFPrimitive));
     * map.put(TypeKind.WILDCARD, Pair.create(NodeTypes.WILDCARD, voidF));
     *
     * return map; }
     *
     * private static Object fromTypeToKey(Type type) { // return
     * fromKindToKey.get(t.getKind()).apply(t); return type.isPrimitiveOrVoid()
     * ? type.tsym : type; }
     */

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
        // System.out.println("Creating type key for" + type);
        // System.out.println("KEY WAS:\t" + type.accept(new KeyTypeVisitor(),
        // null));
        return getOrCreateType(type, type.accept(new KeyTypeVisitor(), null), ast);
    }

    public static NodeWrapper createTypeDec(TypeMirror typeSymbol, ASTAuxiliarStorage ast) {
        // System.out.println("Creating type key for" + typeSymbol);
        // System.out.println("KEY WAS:\t" + typeSymbol.accept(new
        // KeyTypeVisitor(), null));
        return createTypeDec(typeSymbol, typeSymbol.accept(new KeyTypeVisitor(), null), ast);
    }

    private static NodeWrapper createTypeDec(TypeMirror type, Object key, ASTAuxiliarStorage ast) {
        // System.out.println("Creating " + typeSymbol);
        // System.out.println("VREATING TYPE SPEC " + type);
        NodeWrapper ret = type.accept(new TypeVisitor(ast), key);

        return ret;
    }
}

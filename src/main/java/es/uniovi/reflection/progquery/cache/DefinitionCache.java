package es.uniovi.reflection.progquery.cache;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.database.relations.CDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.database.relations.TypeRelations;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.utils.keys.cache.TypeKey;
import es.uniovi.reflection.progquery.visitors.KeyTypeVisitor;
import es.uniovi.reflection.progquery.visitors.TypeVisitor;
import org.neo4j.graphdb.Direction;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefinitionCache<TKEY> {
    private static final boolean DEBUG = false;
    public static ASTAuxiliarStorage ast;
    public static DefinitionCache<TypeKey> TYPE_CACHE;
    public static DefinitionCache<Symbol> METHOD_DEF_CACHE;

    final Map<TKEY, NodeWrapper> auxNodeCache = new HashMap<>();
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


    public void putClassDefinition(TypeMirror type, javax.lang.model.type.TypeVisitor<TKEY,Object> visitor, NodeWrapper classDec, Set<NodeWrapper> typeDecNodeList,
                                   Set<NodeWrapper> typeDecsUses) {
        TKEY key=type.accept(visitor,null);
        NodeWrapper oldClassNode = getFromNotDefinedCache(key);
        if (oldClassNode != null) {
            for (RelationshipWrapper r : oldClassNode.getRelationships(Direction.OUTGOING,
                    //					RelationTypes.DECLARES_METHOD, RelationTypes.DECLARES_CONSTRUCTOR,

                    RelationTypes.DECLARES_FIELD, TypeRelations.IS_SUBTYPE_EXTENDS,
                    TypeRelations.IS_SUBTYPE_IMPLEMENTS))
                r.delete();

            typeDecNodeList.remove(oldClassNode);
            oldClassNode.getRelationships(Direction.OUTGOING, CDGRelationTypes.USES_TYPE_DEF)
                    .forEach(usesTypeDecRel -> typeDecsUses.add(usesTypeDecRel.getEndNode()));
        }

        putDefinition(key, classDec, oldClassNode);
    }

    public void putDefinition(TKEY k, NodeWrapper v) {
        putDefinition(k, v, getFromNotDefinedCache(k));
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

    protected NodeWrapper getFromNotDefinedCache(TKEY k) {
        return auxNodeCache.get(k);
    }

    public boolean containsKey(TKEY k) {
        return auxNodeCache.containsKey(k) || definitionNodeCache.containsKey(k);
    }

    public boolean containsDef(TKEY k) {
        return definitionNodeCache.containsKey(k);
    }


    public static NodeWrapper getOrCreateType(TypeMirror type, TypeKey key, ASTAuxiliarStorage ast) {
        if (DefinitionCache.TYPE_CACHE.containsKey(key))
            return DefinitionCache.TYPE_CACHE.get(key);
        return createTypeDec(type, key, ast);
    }

    public static NodeWrapper getExistingType(TypeMirror type) {
        TypeKey key = type.accept(new KeyTypeVisitor(), null);
        if (DefinitionCache.TYPE_CACHE.containsKey(key))
            return DefinitionCache.TYPE_CACHE.get(key);

        throw new IllegalArgumentException("Not Type found for " + type);
    }

    public static NodeWrapper getOrCreateType(TypeMirror type, ASTAuxiliarStorage ast) {

        return getOrCreateType(type, type.accept(new KeyTypeVisitor(), null), ast);
    }

    private static NodeWrapper createTypeDec(TypeMirror type, TypeKey key, ASTAuxiliarStorage ast) {

        return type.accept(new TypeVisitor(ast), key);

    }
}

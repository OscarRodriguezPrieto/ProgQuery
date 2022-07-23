package es.uniovi.reflection.progquery.node_wrappers;

import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.Neo4jDriverLazyInsertion;
import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.database.relations.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Neo4jLazyRetrievedRel extends Neo4jLazyRelationship {
    private Relationship relationship;

    public Neo4jLazyRetrievedRel(NodeWrapper start, NodeWrapper end, Relationship rel) {
        super(start, end,rel.type());
        this.relationship = rel;
        InfoToInsert.INFO_TO_INSERT.addRetrievedRel(this);
    }

    private static Map<String, RelationTypesInterface> allPossibleRelTypes = new HashMap<>();

    static {
        putValuesInMap(RelationTypes.values());
        putValuesInMap(CFGRelationTypes.values());
        putValuesInMap(CDGRelationTypes.values());
        putValuesInMap(CGRelationTypes.values());
        putValuesInMap(PDGRelationTypes.values());
        putValuesInMap(PGRelationTypes.values());
        putValuesInMap(TypeRelations.values());
    }

    private static <T extends RelationTypesInterface> void putValuesInMap(T[] relTypes) {
        allPossibleRelTypes
                .putAll(Arrays.stream(relTypes).collect(Collectors.toMap(value -> value.name(), value -> value)));
    }

    static RelationTypesInterface getRelTypeFromString(String relationTypeName) {
        RelationTypesInterface relType = allPossibleRelTypes.get(relationTypeName);
        if (relType == null)
            throw new IllegalArgumentException(relationTypeName + " is not a valid ProgQuery relation type name.");
        return relType;
    }

    public static RelationshipWrapper newWrappedRelFrom(Neo4jServerRetrievedNode retrievedNode, Node queriedLinkedNode,
                                                        Relationship queriedRel) {
        Neo4jServerRetrievedNode wrappedLinkedNode = InfoToInsert.INFO_TO_INSERT.getOrCreateNode(queriedLinkedNode);
        if (retrievedNode.getId() == queriedRel.startNodeId())
            return retrievedNode.createRetrievedRelationshipTo(wrappedLinkedNode, queriedRel);
        else
            return wrappedLinkedNode.createRetrievedRelationshipTo(retrievedNode, queriedRel);
    }

    @Override
    public boolean hasProperty(String name) {
        return super.hasProperty(name) || relationship.containsKey(name);
    }

    @Override
    public Object getProperty(String name) {
        Object value = super.getProperty(name);
        return value == null ? relationship.get(name) : value;
    }

    @Override
    public RelationTypesInterface getType() {
        return getRelTypeFromString(relationship.type());
    }


    @Override
    public void delete() {
        super.delete();
        final String DELETE_REL_QUERY = "MATCH (n)-[r]->(m) WHERE ID(n)=$id1 AND ID(m)=$id2 DELETE r";
        Map<String, Object> idParam = new HashMap<>();
        idParam.put("id1", getStartNode().getId());
        idParam.put("id2", getEndNode().getId());
        ((Neo4jDriverLazyInsertion) DatabaseFachade.CURRENT_INSERTION_STRATEGY).getManagerForRetrievedNodes()
                .executeQuery(DELETE_REL_QUERY, idParam);
        InfoToInsert.INFO_TO_INSERT.removeRetrievedRel(this);

    }

    @Override
    public String getTypeString() {
        return relationship.type();
    }

    @Override
    public String toString() {
        return NodeUtils.nodeToStringNoRels(getStartNode()) + "--[" + getTypeString()+ "]-->" + NodeUtils.nodeToStringNoRels(getEndNode());
    }

    public long getId() {
        return relationship.id();
    }
}

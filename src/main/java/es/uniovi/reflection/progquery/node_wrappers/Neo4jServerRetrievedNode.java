package es.uniovi.reflection.progquery.node_wrappers;

import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.Neo4jDriverLazyInsertion;
import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.nodes.NodeCategory;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

import java.util.*;
import java.util.stream.Collectors;

public class Neo4jServerRetrievedNode extends Neo4jLazyNode {
    private Node node;
    private boolean relsLoaded = false;

    public Neo4jServerRetrievedNode(Node node) {
        this.node = node;
        InfoToInsert.INFO_TO_INSERT.putIntoRetrievedCache(this);
    }

    private static Map<String, Label> allPossibleLabels = new HashMap<>();

    static {
        putValuesInMap(NodeTypes.values());
        putValuesInMap(NodeCategory.values());
    }

    private static <T extends Label> void putValuesInMap(T[] labels) {
        allPossibleLabels
                .putAll(Arrays.stream(labels).collect(Collectors.toMap(label -> label.name(), label -> label)));
    }

    @Override
    public Long getId() {
        return node.id();
    }

    private void loadRelationships() {
        relsLoaded = true;
        final String GET_RELS_QUERY = "MATCH (n)-[r]-(m) WHERE ID(n)=$id RETURN r,m";
        Map<String, Object> idParam = new HashMap<>();
        idParam.put("id", node.id());
        final int REL_POS = 0, NODE_POS = 1;
        ((Neo4jDriverLazyInsertion) DatabaseFachade.CURRENT_INSERTION_STRATEGY).getManagerForRetrievedNodes()
                .executeQuery(GET_RELS_QUERY, idParam).stream().map(record -> Neo4jLazyRetrievedRel
                .newWrappedRelFrom(this, record.get(NODE_POS).asNode(), record.get(REL_POS).asRelationship()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationshipWrapper> getRelationships() {
        if (!relsLoaded)
            loadRelationships();
        return super.getRelationships();

    }

    RelationshipWrapper createRetrievedRelationshipTo(NodeWrapper end, Relationship r) {
        RelationshipWrapper rel = new Neo4jLazyRetrievedRel(this, end, r);
        storeNewRelInNodes(rel);
        return rel;
    }

    @Override
    public RelationshipWrapper getSingleRelationship(Direction direction, RelationTypesInterface relTypes) {
        if (!relsLoaded)
            loadRelationships();
        return super.getSingleRelationship(direction, relTypes);
    }

    @Override
    public List<RelationshipWrapper> getRelationships(Direction direction, RelationTypesInterface... possibleRelTypes) {
        if (!relsLoaded)
            loadRelationships();
        return super.getRelationships(direction, possibleRelTypes);
    }

    @Override
    public List<RelationshipWrapper> getRelationships(Direction direction) {
        if (!relsLoaded)
            loadRelationships();
        return super.getRelationships(direction);
    }

    @Override
    public boolean hasRelationship(RelationTypesInterface relType, Direction incoming) {
        if (!relsLoaded)
            loadRelationships();
        return super.hasRelationship(relType, incoming);
    }

    @Override
    public Set<Label> getLabels() {
        HashSet<Label> labels = new HashSet<>();
        node.labels().forEach(labelStr -> labels.add(allPossibleLabels.get(labelStr)));
        labels.addAll(super.getLabels());
        return labels;
    }

    @Override
    public boolean hasLabel(Label label) {
        return node.hasLabel(label.name()) || super.hasLabel(label);
    }

    @Override
    public void removeLabel(Label label) {
        if (super.hasLabel(label))
            super.removeLabel(label);
        else {
            final String REMOVE_LABEL_QUERY = "MATCH (n) WHERE ID(n)=$id REMOVE n:" + label.name() + " RETURN n";
            Map<String, Object> idParam = new HashMap<>();
            idParam.put("id", node.id());
            final int NODE_POS = 0;
            this.node = ((Neo4jDriverLazyInsertion) DatabaseFachade.CURRENT_INSERTION_STRATEGY)
                    .getManagerForRetrievedNodes().executeQuery(REMOVE_LABEL_QUERY, idParam).get(0).get(NODE_POS)
                    .asNode();
        }
    }

    @Override
    public void delete() {
        final String DELETE_NODE_QUERY = "MATCH (n) WHERE ID(n)=$id DELETE n";
        Map<String, Object> idParam = new HashMap<>();
        idParam.put("id", node.id());
        ((Neo4jDriverLazyInsertion) DatabaseFachade.CURRENT_INSERTION_STRATEGY).getManagerForRetrievedNodes()
                .executeQuery(DELETE_NODE_QUERY, idParam);
        InfoToInsert.INFO_TO_INSERT.removeFromRetrievedCache(getId());
    }

    @Override
    public void setId(long id) {
        throw new IllegalStateException("The id of a retrieved node cannot be set.");
    }

    public boolean hasProperty(String name) {
        return super.hasProperty(name) || node.containsKey(name);
    }

    public Object getProperty(String name) {
        Object value = super.getProperty(name);
        return value == null ? node.get(name) : value;
    }

   // public Set<Map.Entry<String, Object>> getAllProperties() {
        //JUST USED TO INSERT AT THE END,NOT FOR THE ANALYSIS, ONLY RETURN THE INHERITED MAP, NOT THE RETRIEVED ONE
     //DONT NEED TO OVERRIDE IT

}

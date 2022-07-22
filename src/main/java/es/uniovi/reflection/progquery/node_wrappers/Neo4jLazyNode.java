package es.uniovi.reflection.progquery.node_wrappers;

import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

import java.util.*;

public class Neo4jLazyNode extends AbstractNeo4jLazyServerDriverElement implements NodeWrapper {

    Map<RelationTypesInterface, List<RelationshipWrapper>> incomingRels = new HashMap<>(), outgoingRels =
            new HashMap<>();
    List<RelationshipWrapper> allRels = new ArrayList<>();

    Set<Label> labels = new HashSet<>();
    Long id;

    public Neo4jLazyNode() {
        id = null;
        InfoToInsert.INFO_TO_INSERT.addNewNode(this);
    }

    public Neo4jLazyNode(NodeTypes... labels) {

        this();
        for (NodeTypes label : labels)
            this.labels.add(label);

    }

    public Neo4jLazyNode(NodeTypes label, Object... props) {
        this(props);
        this.labels.add(label);

    }

    public Neo4jLazyNode(Object... props) {
        super(props);
        InfoToInsert.INFO_TO_INSERT.addNewNode(this);
    }

    @Override
    public Long getId() {
        return id;
    }

    protected void storeNewRelInNodes(RelationshipWrapper rel) {
        NodeWrapper end = rel.getEndNode();
        RelationTypesInterface r = rel.getType();
        List<RelationshipWrapper> relList = outgoingRels.get(r);
        if (relList == null)
            outgoingRels.put(r, relList = new ArrayList<>());
        relList.add(rel);
        Neo4jLazyNode castEnd = (Neo4jLazyNode) end;
        relList = castEnd.incomingRels.get(r);
        if (relList == null)
            castEnd.incomingRels.put(r, relList = new ArrayList<>());
        relList.add(rel);

        allRels.add(rel);
        castEnd.allRels.add(rel);
    }

    @Override
    public RelationshipWrapper createRelationshipTo(NodeWrapper end, RelationTypesInterface r) {
        RelationshipWrapper rel = new Neo4jLazyRelationship(this, end, r);
		storeNewRelInNodes(rel);
        return rel;
    }

    @Override
    public List<RelationshipWrapper> getRelationships() {

        return allRels;
    }

    private List<RelationshipWrapper> getRelsFrom(Direction direction, RelationTypesInterface... relTypes) {
        List<RelationshipWrapper> rels = new ArrayList<>();
        Map<RelationTypesInterface, List<RelationshipWrapper>> currentMap =
                direction == Direction.INCOMING ? incomingRels : outgoingRels;
        for (RelationTypesInterface relType : relTypes) {
            List<RelationshipWrapper> relsFound = currentMap.get(relType);
            if (relsFound != null)
                rels.addAll(relsFound);
        }
        return rels;
    }

    @Override
    public RelationshipWrapper getSingleRelationship(Direction direction, RelationTypesInterface relTypes) {
        List<RelationshipWrapper> rels = getRelsFrom(direction, relTypes);
        if (rels.size() > 1)
            throw new IllegalArgumentException("More than one relationship");
        if (rels.size() == 0)
            return null;
        return rels.get(0);
    }

    @Override
    public List<RelationshipWrapper> getRelationships(Direction direction, RelationTypesInterface... relTypes) {

        return getRelsFrom(direction, relTypes);
    }

    @Override
    public boolean hasRelationship(RelationTypesInterface relType, Direction direction) {
        Map<RelationTypesInterface, List<RelationshipWrapper>> currentMap =
                direction == Direction.INCOMING ? incomingRels : outgoingRels;

        return currentMap.get(relType) != null;
    }

    @Override
    public Set<Label> getLabels() {
        return labels;
    }

    @Override
    public boolean hasLabel(Label label) {

        return labels.contains(label);
    }

    @Override
    public void removeLabel(Label label) {
        labels.remove(label);
    }

    @Override
    public void addLabel(Label newLabel) {
        labels.add(newLabel);
    }

    @Override
    public void delete() {
        // Theoretically, a check is needed in order to asses that there are no
        // rels attached to this node
        // We also can implement detach, just to not to have to delete all the
        // rels... i dont know
        InfoToInsert.INFO_TO_INSERT.deleteNode(this);
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public List<RelationshipWrapper> getRelationships(Direction direction) {
        List<RelationshipWrapper> rels = new ArrayList<>();
        Map<RelationTypesInterface, List<RelationshipWrapper>> currentMap =
                direction == Direction.INCOMING ? incomingRels : outgoingRels;
        for (List<RelationshipWrapper> relsOfType : currentMap.values())
            rels.addAll(relsOfType);

        return rels;
    }

    public void removeIncomingRel(RelationshipWrapper rel) {
        allRels.remove(rel);
        incomingRels.get(rel.getType()).remove(rel);
    }

    public void removeOutgoingRel(RelationshipWrapper rel) {
        allRels.remove(rel);
        outgoingRels.get(rel.getType()).remove(rel);

    }

    @Override
    public String toString() {
        return NodeUtils.nodeToString(this);
    }


}
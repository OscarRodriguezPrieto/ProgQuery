package es.uniovi.reflection.progquery.node_wrappers;

import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

public class Neo4jLazyRelationship extends AbstractNeo4jLazyServerDriverElement implements RelationshipWrapper {
    private NodeWrapper start, end;
    private RelationTypesInterface rType;

    public Neo4jLazyRelationship(NodeWrapper start, NodeWrapper end, RelationTypesInterface rType) {
        this.start = start;
        this.end = end;
        this.rType = rType;
        InfoToInsert.INFO_TO_INSERT.addNewRel(this);
    }
    public Neo4jLazyRelationship(NodeWrapper start, NodeWrapper end, String relName) {
        this.start = start;
        this.end = end;
        this.rType =  Neo4jLazyRetrievedRel.getRelTypeFromString(relName);
    }

    @Override
    public RelationTypesInterface getType() {
        return rType;
    }

    @Override
    public NodeWrapper getStartNode() {
        return start;
    }

    @Override
    public NodeWrapper getEndNode() {
        return end;
    }


    @Override
    public void delete() {
        InfoToInsert.INFO_TO_INSERT.deleteRel(this);
        ((Neo4jLazyNode) start).removeOutgoingRel(this);
        ((Neo4jLazyNode) end).removeIncomingRel(this);
    }

    @Override
    public String getTypeString() {
        return rType.toString();
    }

    @Override
    public String toString() {
        return NodeUtils.nodeToStringNoRels(start) + "--[" + rType.name() + "]-->" + NodeUtils.nodeToStringNoRels(end);
    }
}

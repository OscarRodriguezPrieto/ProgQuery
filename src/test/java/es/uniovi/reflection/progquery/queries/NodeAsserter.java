package es.uniovi.reflection.progquery.queries;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import org.junit.Assert;
import org.neo4j.graphdb.Direction;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NodeAsserter {

    public static List<NodeWrapper> assertRel(NodeWrapper node, Direction direction, RelationTypesInterface relType ){
        assertTrue(node.hasRelationship(relType,direction));
        return node.getRelationships(direction,relType).stream().map(rel->direction==Direction.INCOMING?rel.getStartNode():rel.getEndNode()).collect(Collectors.toList());
    }

    public static void assertRel(NodeWrapper start,NodeWrapper end, RelationTypesInterface relType ){
        for(NodeWrapper retrievedNode:  assertRel(start, Direction.OUTGOING, relType))
            if(retrievedNode==end) {
                assertTrue(true);
                return;
            }
        Assert.assertTrue(false);
       }

    public static Neo4jLazyNode assertJustOne(List<Neo4jLazyNode> nodes){
        assertEquals(nodes.size(),1);
        return nodes.get(0);
    }


}

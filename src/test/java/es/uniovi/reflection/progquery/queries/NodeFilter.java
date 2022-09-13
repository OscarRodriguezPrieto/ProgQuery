package es.uniovi.reflection.progquery.queries;

import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.Propertiable;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.List;
import java.util.stream.Collectors;

public class NodeFilter {


    public static List<NodeWrapper> filterByLabel(List<NodeWrapper> nodes,Label label){
        return nodes.stream().filter(n->n.hasLabel(label)).collect(Collectors.toList());
    }
    public static List<NodeWrapper> filterByRel(List<NodeWrapper> nodes, Direction direction, RelationTypesInterface r){
        return nodes.stream().filter(n->n.hasRelationship(r,direction)).collect(Collectors.toList());
    }
    public static List<NodeWrapper> filterByNoRel(List<NodeWrapper> nodes, Direction direction, RelationTypesInterface r){
        return nodes.stream().filter(n->!n.hasRelationship(r,direction)).collect(Collectors.toList());
    }
    public static <T extends Propertiable> List<T> filterByProp(List<T> elements, String propName, String propVal){
        return elements.stream().filter(element->(element.getProperty(propName).toString()).contentEquals(propVal)).collect(Collectors.toList());
    }
    public static <T extends Propertiable> List<T> filterByName(List<T> elements, String name){
        return  filterByProp(elements,"name", name);
    }
    public static <T extends Propertiable> List<T> filterByMemberNameAndLine(List<T> elements, String memberName, int line){
        return  filterByLineNumber( filterByProp(elements,"memberName", memberName), line);

    }

    public static <T extends Propertiable> List<T> filterByNameAndLine(List<T> elements, String name, int line){
        return  filterByLineNumber( filterByProp(elements,"name", name), line);

    }
    public static <T extends Propertiable> List<T> filterByLineNumber(List<T> elements, int line){
        return  filterByProp(elements,"lineNumber", line+"");
    }

}

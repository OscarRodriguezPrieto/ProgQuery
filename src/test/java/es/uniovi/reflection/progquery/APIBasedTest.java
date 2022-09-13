package es.uniovi.reflection.progquery;

import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.queries.NodeFilter;
import org.neo4j.graphdb.Direction;

import java.util.ArrayList;
import java.util.List;

public abstract class APIBasedTest {


    private String srcDirectory, programName, classPath;
     protected List<NodeWrapper> nodeSet;
     protected List<RelationshipWrapper> relSet;

    public APIBasedTest(String srcDirectory, String programName, String classPath){
this.srcDirectory=srcDirectory;
this.programName=programName;
this.classPath=classPath;
    }
    public APIBasedTest(String srcDirectory, String programName){
        this(srcDirectory,programName,"");
    }

//    @Before
    public void prepareTestDatabase(){
        Main.main(new String[]{"-src="+srcDirectory, "-user=test", "-program="+programName, "-neo4j_mode=no", "-classpath="+classPath});
        nodeSet=InfoToInsert.INFO_TO_INSERT.getNodeSet();
        relSet=InfoToInsert.INFO_TO_INSERT.getRelSet();
    }


      public static void main(String[] args){

          Main.main(new String[]{"-src=testPrograms/pdg_test/src", "-user=test", "-program=pdg_test", "-neo4j_mode=no", "-classpath="});
          List<NodeWrapper> nodes=InfoToInsert.INFO_TO_INSERT.getNodeSet();
//        for(NodeWrapper node:
//                NodeFilter.filterByNoRel(
//                        NodeFilter.filterByLabel(nodes, NodeTypes.MEMBER_SELECTION)
//                        , Direction.INCOMING, RelationTypes.METHODINVOCATION_METHOD_SELECT)
//        ) {
//            System.out.println(node.getProperty("lineNumber"));
//            System.out.println(node.getProperty("fieldName"));
//            System.out.println(node.getProperty("memberName"));
//        }
          for(NodeWrapper node:
                  NodeFilter.filterByLabel(nodes,NodeTypes.ATTR_DEF)
          ) {
//              System.out.println(node.getProperty("lineNumber"));
//              System.out.println(node.getProperty("fieldName"));
//              System.out.println(node.getProperty("memberName"));
              System.out.println("ATTR "+node.getProperty("name"));
          }
        System.out.println("HEY");

    }
}

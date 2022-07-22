package es.uniovi.reflection.progquery;

import es.uniovi.reflection.progquery.database.insertion.lazy.InfoToInsert;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.queries.NodeFilter;

import java.util.List;

public abstract class APIBasedTest {


    private String srcDirectory, programName, classPath;
     protected List<Neo4jLazyNode> nodeSet;
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
          List<Neo4jLazyNode> nodes=InfoToInsert.INFO_TO_INSERT.getNodeSet();

          for(Neo4jLazyNode node:
                  NodeFilter.filterByLabel(nodes,NodeTypes.ATTR_DEF)
          ) {
              System.out.println("ATTR "+node.getProperty("name"));
          }
        System.out.println("HEY");

    }
}

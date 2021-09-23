package es.uniovi.reflection.progquery.pdg;

import es.uniovi.reflection.progquery.APIBasedTest;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.PDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.Propertiable;
import es.uniovi.reflection.progquery.queries.NodeAsserter;
import es.uniovi.reflection.progquery.queries.NodeFilter;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;

import java.util.List;

public class PDGUsedByTest extends APIBasedTest {

    private static final String SRC_DIR="testPrograms\\pdg_test\\src", TEST_NAME="pdg_test";
    public PDGUsedByTest() {
        super(SRC_DIR, TEST_NAME);
    }

    @Before
    public void retrieveGraph(){
        super.prepareTestDatabase();
        List<NodeWrapper> nodes=super.nodeSet;
        attributes=NodeFilter.filterByLabel(nodes, NodeTypes.ATTR_DEF);
        identifiers=NodeFilter.filterByLabel(nodes,NodeTypes.IDENTIFIER);
        fieldAccesses=NodeFilter.filterByNoRel(NodeFilter.filterByLabel(nodes, NodeTypes.MEMBER_SELECTION), Direction.INCOMING, RelationTypes.METHODINVOCATION_METHOD_SELECT);
    }
    List<NodeWrapper> attributes, identifiers, fieldAccesses ;
    private void assertPDGRel(String fieldName, int expLine, PDGRelationTypes pdgRel, List<NodeWrapper> expressionNodes,ExpressionFilter expFilter){

        NodeWrapper attr=NodeAsserter.assertJustOne(NodeFilter.filterByName(attributes, fieldName));
        NodeWrapper expr=NodeAsserter.assertJustOne(expFilter.filter(expressionNodes,fieldName,expLine));
        NodeAsserter.assertRel(attr, expr, pdgRel);
    }
private void assertUseInMemberSel(String fieldName, int expLine){
assertPDGRel(fieldName,expLine, PDGRelationTypes.USED_BY,fieldAccesses,NodeFilter::filterByMemberNameAndLine);
}
    private void assertUseInIdent(String fieldName, int expLine){
        assertPDGRel(fieldName,expLine, PDGRelationTypes.USED_BY,identifiers, NodeFilter::filterByNameAndLine);
    }
    @Test
    public void testUsedByExternalFields() {
        //File UsedByRels.java, Method externalFieldsUsage
    final int firstLineInMethod= 22;
    assertUseInMemberSel("out", firstLineInMethod+1);
    assertUseInMemberSel("externalInstanceField",firstLineInMethod+1);
    assertUseInMemberSel("externalStaticField",firstLineInMethod+2);
    assertUseInMemberSel("externalInstanceField",firstLineInMethod+4);
    assertUseInMemberSel("externalStaticField",firstLineInMethod+5);
    assertUseInMemberSel("EMPTY_LIST",firstLineInMethod+9);
        assertUseInMemberSel("externalStaticField",firstLineInMethod+11);
        assertUseInMemberSel("length",firstLineInMethod+12);


    }

    public void testUsedByInternalFields() {

        //File UsedByRels.java, Method internalFieldsUsage
        final int firstLineInMethod= 38;
        assertUseInIdent("internalStaticField", firstLineInMethod+1);
        assertUseInMemberSel("internalStaticField",firstLineInMethod+2);
        assertUseInMemberSel("internalStaticField",firstLineInMethod+4);
        assertUseInMemberSel("internalInstanceField",firstLineInMethod+5);
        assertUseInIdent("internalInstanceField",firstLineInMethod+7);
        assertUseInMemberSel("internalInstanceField",firstLineInMethod+9);
        assertUseInMemberSel("internalStaticField",firstLineInMethod+10);

    }

    public void testUsedByFieldsInSuper() {
        //File UsedByRels.java, Method internalFieldsUsage
        final int firstLineInMethod= 53;
        assertUseInIdent("superStaticField", firstLineInMethod+1);
        assertUseInMemberSel("superStaticField",firstLineInMethod+2);
        assertUseInMemberSel("superStaticField",firstLineInMethod+3);
        assertUseInMemberSel("superStaticField",firstLineInMethod+4);
        assertUseInMemberSel("superStaticField",firstLineInMethod+5);
        assertUseInMemberSel("superStaticField",firstLineInMethod+6);
        assertUseInMemberSel("superStaticField",firstLineInMethod+7);


        assertUseInIdent("inheritedField",firstLineInMethod+9);
        assertUseInMemberSel("inheritedField",firstLineInMethod+11);
        assertUseInMemberSel("inheritedField",firstLineInMethod+13);
        assertUseInMemberSel("inheritedField",firstLineInMethod+16);
        assertUseInMemberSel("inheritedField",firstLineInMethod+17);


    }


private interface ExpressionFilter{
    <T extends Propertiable> List<T> filter(List<T> elements, String propVal, int line);
}
}


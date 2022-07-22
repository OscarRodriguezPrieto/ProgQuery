package es.uniovi.reflection.progquery.database;

import es.uniovi.reflection.progquery.database.manager.EmptyManager;
import es.uniovi.reflection.progquery.database.manager.NEO4JManager;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.Neo4jLazyNode;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class NotPersistentLazyInsertion implements InsertionStrategy {
    @Override
    public NodeWrapper createNode() {
        return new Neo4jLazyNode();
    }


    @Override
    public NodeWrapper createNode(NodeTypes label) {
        return new Neo4jLazyNode(label);
    }

    @Override
    public NodeWrapper createNode(NodeTypes label, Object[] props) {
        return new Neo4jLazyNode(label, props);
    }

    @Override
    public NodeWrapper createNode(Object[] props) {
        return new Neo4jLazyNode(props);
    }

    @Override
    public void startAnalysis() {

    }

    @Override
    public void endAnalysis() {

    }


    @Override
    public NEO4JManager getNewManager() {
        return new EmptyManager();
    }

    @Override
    public void newMultiModuleProject() {

    }

}

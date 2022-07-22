package es.uniovi.reflection.progquery.database;

import es.uniovi.reflection.progquery.database.manager.NEO4JManager;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public interface InsertionStrategy {
	NodeWrapper createNode();
	NodeWrapper createNode(NodeTypes label);

	NodeWrapper createNode(NodeTypes label, Object[] props);

	NodeWrapper createNode(Object[] props);

	void startAnalysis();
	void endAnalysis();

	NEO4JManager getNewManager();

	void newMultiModuleProject();
}

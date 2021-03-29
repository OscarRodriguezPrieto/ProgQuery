package database;

import database.manager.NEO4JManager;

import database.nodes.NodeTypes;
import node_wrappers.NodeWrapper;

public interface InsertionStrategy {
	NodeWrapper createNode();
	NodeWrapper createNode(NodeTypes label);

	NodeWrapper createNode(NodeTypes label, Object[] props);

	NodeWrapper createNode(Object[] props);

	void startAnalysis();
	void endAnalysis();

	NEO4JManager getManager();

}

package database;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.sun.source.tree.Tree;

import cache.nodes.TreeToNodeCache;
import relations.NodeTypes;
import utils.JavacInfo;

public class DatabaseFachade {
	public static void setDB(GraphDatabaseService db) {
		graphDb = db;
	}

	private static GraphDatabaseService graphDb;

	public DatabaseFachade(GraphDatabaseService graphDb) {
		super();
		this.graphDb = graphDb;
	}

	public static Node createSkeletonNode(Tree tree) {

		Node node = graphDb.createNode();
		setMetaInfo(tree, node);
		TreeToNodeCache.putNode(tree, node);

		return node;

	}

	public static Node createSkeletonNode(Tree tree, NodeTypes nodeType) {

		Node node = graphDb.createNode();
		setMetaInfo(tree, node, nodeType);
		TreeToNodeCache.putNode(tree, node);

		return node;

	}

	private static void setMetaInfo(Tree tree, Node node,  NodeTypes nodeType) {

		node.setProperty("nodeType", nodeType);
		setPosition(node, tree);
	}

	private static void setMetaInfo(Tree tree, Node node) {

		node.setProperty("nodeType", tree.getClass().getSimpleName());
		setPosition(node, tree);
	}

	private static void setPosition(Node node, Tree tree) {

		node.setProperty("lineNumber", JavacInfo.getLineNumber(tree));
		node.setProperty("position", JavacInfo.getPosition(tree));
		// node.setProperty("size", JavacInfo.getSize(tree));
	}

	public static Transaction beginTx() {
		return graphDb.beginTx();
	}

	public static Node createNode() {
		return graphDb.createNode();
	}

	public static Node createNode(String nodeType) {
		Node node = graphDb.createNode();
		node.setProperty("nodeType", nodeType);
		return node;
	}
}

package database;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;

import database.nodes.NodeTypes;
import utils.JavacInfo;
import visitors.CFGVisitor;

public class DatabaseFachade {
	private static final String NODE_TYPE_STRING = "nodeType";

	public static String getDBPath() {
		String dbPath = System.getenv("WIGGLE_DB_PATH");
		if (dbPath == null)
			dbPath = "./neo4j/data/wiggle.db";
		return dbPath;
	}

	public static GraphDatabaseService getDB() {
		return new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(getDBPath()))
				.setConfig(GraphDatabaseSettings.node_keys_indexable, "nodeType")
				.setConfig(GraphDatabaseSettings.relationship_keys_indexable, "typeKind")
				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
				.setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true").newGraphDatabase();
	}

	public static void setDB(GraphDatabaseService db) {
		graphDb = db;
	}

	private static GraphDatabaseService graphDb;

	public static Node createSkeletonNode(NodeTypes type) {

		Node node = graphDb.createNode();

		node.setProperty(NODE_TYPE_STRING, type.toString());
		return node;

	}

	public static Node createSkeletonNode(Tree tree) {

		Node node = graphDb.createNode();
		setMetaInfo(tree, node);

		return node;

	}

	public static Node createSkeletonNode(Tree tree, NodeTypes nodeType) {

		Node node = graphDb.createNode();
		setMetaInfo(tree, node, nodeType);

		return node;

	}

	private static void setMetaInfo(Tree tree, Node node, NodeTypes nodeType) {

		node.setProperty(NODE_TYPE_STRING, nodeType.toString());
		setPosition(node, tree);
	}

	private static void setMetaInfo(Tree tree, Node node) {

		node.setProperty(NODE_TYPE_STRING, tree.getClass().getSimpleName());
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
		node.setProperty(NODE_TYPE_STRING, nodeType);
		return node;
	}

	private static void setTypeDecProperties(Node classNode, String simpleName, String fullyQualifiedType,
			boolean declared) {
		classNode.setProperty("simpleName", simpleName);
		classNode.setProperty("fullyQualifiedName", fullyQualifiedType);
		classNode.setProperty("isDeclared", declared);
	}

	public static Node createTypeDecNode(ClassTree classTree, String simpleName, String fullyQualifiedType) {
		Node classNode = DatabaseFachade.createSkeletonNode(classTree, classTree.getKind() == Kind.CLASS
				? NodeTypes.CLASS_DECLARATION
				: classTree.getKind() == Kind.INTERFACE ? NodeTypes.INTERFACE_DECLARATION : NodeTypes.ENUM_DECLARATION);
		setTypeDecProperties(classNode, simpleName, fullyQualifiedType, true);
		return classNode;
	}

	public static Node createTypeDecNode(Symbol s, NodeTypes type) {
		Node classNode = DatabaseFachade.createSkeletonNode(type);
		String[] fullyNameSplit = s.toString().split(".");
		setTypeDecProperties(classNode, fullyNameSplit[fullyNameSplit.length - 1], s.toString(), false);
		return classNode;

	}

}

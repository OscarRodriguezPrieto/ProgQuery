package es.uniovi.reflection.progquery.database;

import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.tree.JCTree;

import es.uniovi.reflection.progquery.database.nodes.NodeCategory;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.WrapperUtils;
import es.uniovi.reflection.progquery.utils.JavacInfo;

public class DatabaseFachade {
	private final InsertionStrategy insertionStrategy;
	public static InsertionStrategy CURRENT_INSERTION_STRATEGY;

	public static DatabaseFachade CURRENT_DB_FACHADE;

	public static void init(InsertionStrategy current) {
		CURRENT_INSERTION_STRATEGY = current;
//		System.out.println(current);
		CURRENT_DB_FACHADE = new DatabaseFachade(current);
		// System.out.println(current);
	}

	public DatabaseFachade(InsertionStrategy insertionStrategy) {
		this.insertionStrategy = insertionStrategy;
	}

	public NodeWrapper createNodeWithoutExplicitTree(NodeTypes type) {
		return createNode(type, new Object[] {});
	}

	private NodeWrapper createNode(NodeTypes type, Object[] properties) {
		NodeWrapper node = insertionStrategy.createNode(type, properties);
		addMultiLabelHypernyms(node, type);
		return node;
	}

	private static void addMultiLabelHypernyms(NodeWrapper node, NodeTypes type) {
		for (NodeCategory nodeCategory : type.hypernyms)
			node.addLabel(nodeCategory);
	}

	private static final int IMPLICIT_POSITION = -1;

	public NodeWrapper createSkeletonNode(Tree tree, NodeTypes nodeType) {
		NodeWrapper node = createNodeWithoutExplicitTree(nodeType);

		node.setProperties(
				((JCTree)tree).pos==-1
						? new Object[] { "lineNumber", IMPLICIT_POSITION, "column", IMPLICIT_POSITION, "position",
								IMPLICIT_POSITION }
						: getPosition(tree));
		return node;
	}


	public NodeWrapper createSkeletonNodeExplicitCats(Tree tree, NodeTypes nodeType,
			NodeCategory... cats) {
		NodeWrapper node = createSkeletonNode(tree, nodeType);
		for (NodeCategory cat : cats)
			node.addLabel(cat);
		return node;
	}


	private static Object[] getPosition(Tree tree) {
		return JavacInfo.getPosition(tree);
	}

	/*
	 * public static Transaction beginTxXX() { return graphDb.beginTx(); }
	 * 
	 * public static Node createNodeXX() { return graphDb.createNode(); }
	 * 
	 * public static Node createNodeXX(NodeTypes nodeType) { Node node =
	 * graphDb.createNode(); node.addLabel(nodeType); return node; }
	 */
	private static Object[] getTypeDecProperties(String simpleName, String fullyQualifiedType, boolean declared) {
		return new Object[] { "simpleName", WrapperUtils.stringToNeo4jQueryString(simpleName), "fullyQualifiedName",
				WrapperUtils.stringToNeo4jQueryString(fullyQualifiedType), "isDeclared", declared };
	}

	private static Object[] getTypeDecProperties(String simpleName, String fullyQualifiedType, boolean declared,
			boolean isFinal) {
		return new Object[] { "simpleName", WrapperUtils.stringToNeo4jQueryString(simpleName), "fullyQualifiedName",
				WrapperUtils.stringToNeo4jQueryString(fullyQualifiedType), "isDeclared", declared, "isFinal", isFinal };
	}

	private static Object[] getTypeDecProperties(ClassSymbol symbol, boolean isDeclared) {
		Set<Modifier> modifiers = Flags.asModifierSet(symbol.flags_field);
		return symbol.isEnum()
				? new Object[] { "simpleName", WrapperUtils.stringToNeo4jQueryString(symbol.getSimpleName().toString()),
						"fullyQualifiedName",
						WrapperUtils.stringToNeo4jQueryString(symbol.getQualifiedName().toString()), "isDeclared",
						isDeclared, "isFinal", true, "isStatic", true, "accessLevel", "public" }
				: symbol.isInterface() ? new Object[] { "simpleName",
						WrapperUtils.stringToNeo4jQueryString(symbol.getSimpleName().toString()), "fullyQualifiedName",
						WrapperUtils.stringToNeo4jQueryString(symbol.getQualifiedName().toString()), "isDeclared",
						isDeclared, "isAbstract", modifiers.contains(Modifier.ABSTRACT), "accessLevel",
						modifiers.contains(Modifier.PUBLIC) ? "public" : "package" }
						: new Object[] { "simpleName",
								WrapperUtils.stringToNeo4jQueryString(symbol.getSimpleName().toString()),
								"fullyQualifiedName",
								WrapperUtils.stringToNeo4jQueryString(symbol.getQualifiedName().toString()),
								"isDeclared", isDeclared, "isAbstract", modifiers.contains(Modifier.ABSTRACT),
								"isStatic", modifiers.contains(Modifier.STATIC), "isFinal",
								modifiers.contains(Modifier.FINAL), "accessLevel",
								modifiers.contains(Modifier.PUBLIC) ? "public"
										: modifiers.contains(Modifier.PRIVATE) ? "private" : "package" };

	}

	public NodeWrapper createTypeDecNode(ClassTree classTree, String simpleName, String fullyQualifiedType) {
		NodeWrapper typeDef = createNode(
				classTree.getKind() == Kind.CLASS ? NodeTypes.CLASS_DEF
						: classTree.getKind() == Kind.INTERFACE ? NodeTypes.INTERFACE_DEF : NodeTypes.ENUM_DEF,
				getTypeDecProperties(simpleName, fullyQualifiedType, true));
		typeDef.setProperties(getPosition(classTree));
		return typeDef;
	}

	public NodeWrapper createNonDeclaredTypeDecNodeExplicitCats(TypeMirror t, NodeTypes type,
			NodeCategory... categories) {

		NodeWrapper res = createNonDeclaredTypeDecNode(t, type);
		for (NodeCategory cat : categories)
			res.addLabel(cat);
		return res;
	}

	public NodeWrapper createNonDeclaredTypeDecNode(TypeMirror t, NodeTypes type) {

		String[] names = t.toString().split("\\.");
		return createNode(type, getTypeDecProperties(names[names.length - 1], t.toString(), false));

	}

	public NodeWrapper createNonDeclaredCLASSTypeDecNode(ClassType c, NodeTypes type) {
		return createNode(type, getTypeDecProperties((ClassSymbol) c.tsym, false));

	}
}

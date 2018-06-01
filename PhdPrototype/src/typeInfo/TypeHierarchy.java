package typeInfo;

import org.neo4j.graphdb.Node;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;

import cache.DefinitionCache;
import database.nodes.NodeTypes;
import database.relations.RelationTypes;

public class TypeHierarchy {

	private static final boolean DEBUG = false;

	public static void visitClass(ClassSymbol classSymbol, Node classNode) {
		Symbol superSymbol = classSymbol.getSuperclass().tsym;
		if (superSymbol != null)
			classNode.createRelationshipTo(DefinitionCache
					.getOrCreateTypeDec((ClassSymbol) classSymbol.getSuperclass().tsym, NodeTypes.CLASS_DECLARATION),
					RelationTypes.IS_SUBTYPE_EXTENDS);

		// implements
		for (Type interfaceType : classSymbol.getInterfaces())
			classNode.createRelationshipTo(DefinitionCache.getOrCreateTypeDec((ClassSymbol) interfaceType.tsym,
					NodeTypes.INTERFACE_DECLARATION), RelationTypes.IS_SUBTYPE_IMPLEMENTS);
	}

	// public static void visitClass(ClassTree classTree, Node classNode) {
	//
	// Tree extendsTree = classTree.getExtendsClause();
	// // extends
	// connectSubType(extendsTree, classNode, RelationTypes.IS_SUBTYPE_EXTENDS);
	//
	// // implements
	// for (Tree implementsTree : classTree.getImplementsClause())
	// connectSubType(implementsTree, classNode,
	// RelationTypes.IS_SUBTYPE_IMPLEMENTS);
	//
	// }
	//
	// private static void connectSubType(Tree superTree, Node baseClassNode,
	// RelationTypes r) {
	// if (DEBUG) {
	// System.out.println("--------NEW CONNECT SUBTYPE--------" + r.toString());
	// System.out.println("BASE CLASS NODE " + baseClassNode);
	// }
	// JCTree jcTree = (JCTree) superTree;
	// if (DEBUG) {
	// System.out.println("SUPER TREE :" + superTree);
	// System.out.println("SUPER TREE cast to JCTREE:" + jcTree);
	// }
	// if (jcTree != null) {
	// Symbol s = TreeInfo.symbol(jcTree);
	// if (DEBUG) {
	// System.out.println("JCTREE SYMBOL:" + s + " " + s.toString().length() + "
	// " + s.getClass());
	// System.out.println(s + " " + s.getClass() + " " + ((ClassSymbol)
	// s).classfile.getKind() + " "
	// + ((ClassSymbol) s).sourcefile.getKind());
	// }
	// baseClassNode.createRelationshipTo(DefinitionCache.getOrCreateTypeDec((ClassSymbol)
	// s), r);
	//
	// }
	// }
}

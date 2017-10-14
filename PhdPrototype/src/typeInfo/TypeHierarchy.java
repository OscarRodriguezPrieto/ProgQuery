package typeInfo;

import org.neo4j.graphdb.Node;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import cache.nodes.DefinitionCache;
import database.DatabaseFachade;
import relations.RelationTypes;

public class TypeHierarchy {

	private static final boolean DEBUG = false;

	public static void visitClass(ClassTree classTree, Node classNode) {

		Tree extendsTree = classTree.getExtendsClause();
		// extends
		connectSubType(extendsTree, classNode, RelationTypes.IS_SUBTYPE_EXTENDS);

		// implements
		for (Tree implementsTree : classTree.getImplementsClause())
			connectSubType(implementsTree, classNode, RelationTypes.IS_SUBTYPE_IMPLEMENTS);

	}

	private static Node getClassType(String typeStr) {
		Node classType = null;
		if (DEBUG)
			System.out.println(
					"CLASS TYPE CACHE CONTAINS TYPESTR :" + DefinitionCache.CLASS_TYPE_CACHE.containsKey(typeStr) + " "
							+ DefinitionCache.CLASS_TYPE_CACHE.totalTypesCached());
		if (DefinitionCache.CLASS_TYPE_CACHE.containsKey(typeStr)) {

			classType = DefinitionCache.CLASS_TYPE_CACHE.get(typeStr);
			if (DEBUG)
				System.out.println("CLASS TYPE CACHED" + classType);
		} else {
			classType = DatabaseFachade.createNode();
			classType.setProperty("nodeType", "ClassType");
			classType.setProperty("fullyQualifiedName", typeStr);
			if (DEBUG)
				System.out.println("NEW CLASS TYPE " + classType);
			DefinitionCache.CLASS_TYPE_CACHE.put(typeStr, classType);
			if (DEBUG)
				System.out.println("CLASS TYPE CACHE CONTAINS TYPESTR (AFTER) :"
						+ DefinitionCache.CLASS_TYPE_CACHE.containsKey(typeStr) + "  "
						+ DefinitionCache.CLASS_TYPE_CACHE.totalTypesCached());

		}
		return classType;
	}

	private static void connectSubType(Tree superTree, Node baseClassNode, RelationTypes r) {
		if (DEBUG) {
			System.out.println("--------NEW CONNECT SUBTYPE--------" + r.toString());
			System.out.println("BASE CLASS NODE " + baseClassNode);
		}
		JCTree jcTree = (JCTree) superTree;
		if (DEBUG) {
			System.out.println("SUPER TREE :" + superTree);
			System.out.println("SUPER TREE cast to JCTREE:" + jcTree);
		}
		if (jcTree != null) {
			Symbol s = TreeInfo.symbol(jcTree);
			if (DEBUG)
				System.out.println("JCTREE SYMBOL:" + s + " " + s.toString().length() + " " + s.getClass());

			String typeStr = s.toString();
			baseClassNode.createRelationshipTo(getClassType(typeStr), r);

		}
	}
}

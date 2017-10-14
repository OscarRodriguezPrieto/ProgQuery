package visitors;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import cache.nodes.DefinitionCache;
import cache.nodes.TreeToNodeCache;
import database.DatabaseFachade;
import relations.RelationTypes;
import utils.Pair;

public class TypeHierarchyVisitor extends TreePathScanner<Object, Pair<Tree, RelationTypes>> {
	private static final boolean DEBUG = false;

	public TypeHierarchyVisitor() {
	}

	private Node getClassType(String typeStr) {
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

	@Override
	public Object visitClass(ClassTree classTree, Pair<Tree, RelationTypes> t) {

		Node baseClassNode = TreeToNodeCache.getNode(classTree);
		Tree extendsTree = classTree.getExtendsClause();
		// extends
		connectSubType(extendsTree, baseClassNode, RelationTypes.IS_SUBTYPE_EXTENDS);

		// implements
		for (Tree implementsTree : classTree.getImplementsClause())
			connectSubType(implementsTree, baseClassNode, RelationTypes.IS_SUBTYPE_IMPLEMENTS);
		return extendsTree;
	}

	private void connectSubType(Tree superTree, Node baseClassNode, RelationTypes r) {
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
			if (DEBUG)
				System.out.println("Creating rel. " + baseClassNode + " " + getClassType(typeStr) + " " + r);
			baseClassNode.createRelationshipTo(getClassType(typeStr), r);

		} else
			throw new IllegalStateException("QUE PASO?");
	}

}

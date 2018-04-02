package typeInfo;

import org.neo4j.graphdb.Node;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;

import cache.DefinitionCache;
import database.nodes.NodeTypes;
import database.relations.RelationTypes;

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
			System.out.println(s + "    " + s.getClass() + "    " + ((ClassSymbol) s).classfile.getKind() + "    "
					+ ((ClassSymbol) s).sourcefile.getKind());
			baseClassNode
					.createRelationshipTo(DefinitionCache.getOrCreateTypeDec(s), r);

		}
	}
}

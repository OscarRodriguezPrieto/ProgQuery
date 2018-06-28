package typeInfo;

import java.util.List;

import org.neo4j.graphdb.Node;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;

import cache.DefinitionCache;
import database.nodes.NodeTypes;
import database.relations.CDGRelationTypes;
import database.relations.RelationTypes;
import visitors.ASTTypesVisitor;

public class TypeHierarchy {

	private static final boolean DEBUG = false;

	public static void addTypeHierarchy(ClassSymbol symbol, Node classNode, List<Node> typeDecs,
			ASTTypesVisitor astVisitor) {
		// System.out.println(symbol);
		Symbol superSymbol = symbol.getSuperclass().tsym;
		// System.out.println("BASE:");
		// System.out.println(superSymbol);
		// System.out.println("INTERFACES:");
		// symbol.getInterfaces().forEach(interfaceType ->
		// System.out.println((ClassSymbol) interfaceType.tsym));
		if (superSymbol != null)
			scanBaseClassSymbol((ClassSymbol) superSymbol, classNode, RelationTypes.IS_SUBTYPE_EXTENDS,
					NodeTypes.CLASS_DECLARATION, typeDecs, astVisitor);

		// implements
		for (Type interfaceType : symbol.getInterfaces())
			scanBaseClassSymbol((ClassSymbol) interfaceType.tsym, classNode, RelationTypes.IS_SUBTYPE_IMPLEMENTS,
					NodeTypes.INTERFACE_DECLARATION, typeDecs, astVisitor);

	}

	private static void scanBaseClassSymbol(ClassSymbol baseSymbol, Node classNode, RelationTypes rel,
			NodeTypes nodeType, List<Node> typeDecs, ASTTypesVisitor astVisitor) {
		Node superTypeClass = DefinitionCache.getOrCreateTypeDec(baseSymbol, nodeType, typeDecs);
		classNode.createRelationshipTo(superTypeClass, rel);
		if (astVisitor == null)
			classNode.createRelationshipTo(superTypeClass, CDGRelationTypes.USES_TYPE_DEC);
		else if (!astVisitor.getTypeDecUses().contains(superTypeClass)) {
			classNode.createRelationshipTo(superTypeClass, CDGRelationTypes.USES_TYPE_DEC);
			astVisitor.getTypeDecUses().add(superTypeClass);
		}
	}
}

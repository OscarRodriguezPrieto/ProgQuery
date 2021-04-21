package es.uniovi.reflection.progquery.typeInfo;

import javax.lang.model.type.TypeKind;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;

import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.cache.DefinitionCache;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.CDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.TypeRelations;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.visitors.ASTTypesVisitor;

public class TypeHierarchy {

	private static final boolean DEBUG = false;

	public static void addTypeHierarchy(ClassSymbol symbol, NodeWrapper classNode, ASTTypesVisitor astVisitor,
			ASTAuxiliarStorage ast) {
		// System.out.println(symbol);
		// System.out.println("BASE:");
		// System.out.println(superSymbol);
		// System.out.println("INTERFACES:");
		// symbol.getInterfaces().forEach(interfaceType ->
		// System.out.println((ClassSymbol) interfaceType.tsym));
		// System.out.println(sym bol + " added to the HIER");
//		System.out.println(symbol + " EXTENDSSSS");
		scanBaseClassSymbol(symbol.getSuperclass(), classNode, TypeRelations.IS_SUBTYPE_EXTENDS, NodeTypes.CLASS_DEF,
				astVisitor, symbol, ast);
//		System.out.println(symbol + " IMPLEMENTSSS");
		// implements
		for (Type interfaceType : symbol.getInterfaces())
			scanBaseClassSymbol(interfaceType, classNode, TypeRelations.IS_SUBTYPE_IMPLEMENTS, NodeTypes.INTERFACE_DEF,
					astVisitor, symbol, ast);

	}

	private static void scanBaseClassSymbol(Type baseType, NodeWrapper classNode, TypeRelations rel, NodeTypes nodeType,
			ASTTypesVisitor astVisitor, ClassSymbol classSymbol, ASTAuxiliarStorage ast) {
		if (baseType.getKind() != TypeKind.NONE) {
//			System.out.println("BASE TYPE " + baseType + " WITH " + rel);
//			System.out.println("EXTENDED FROM " + classSymbol);
			NodeWrapper superTypeClass = DefinitionCache.getOrCreateType(baseType, ast);
			classNode.createRelationshipTo(superTypeClass, rel);
			
			if (astVisitor == null) {
				classNode.createRelationshipTo(superTypeClass, CDGRelationTypes.USES_TYPE_DEF);
				PackageInfo.PACKAGE_INFO.handleNewDependency(classSymbol.packge(), baseType.tsym.packge());
			} else 
				astVisitor.addToTypeDependencies(superTypeClass, baseType.tsym.packge());
			
			// AuxDebugInfo.lastMessage = null;

		}
	}
}

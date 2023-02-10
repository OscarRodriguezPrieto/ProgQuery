package es.uniovi.reflection.progquery.typeInfo;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.cache.DefinitionCache;
import es.uniovi.reflection.progquery.database.relations.CDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.TypeRelations;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.visitors.ASTTypesVisitor;

import javax.lang.model.type.TypeKind;

public class TypeHierarchy {

    private static final boolean DEBUG = false;

    public static void addTypeHierarchy(ClassSymbol symbol, NodeWrapper classNode, ASTTypesVisitor astVisitor,
                                        ASTAuxiliarStorage ast) {
//        System.out.println(symbol);
//        System.out.println("BASE:");
//        System.out.println(symbol.getSuperclass());
//        System.out.println("INTERFACES:");
//        symbol.getInterfaces().forEach(interfaceType -> System.out.println("INTERF: " + interfaceType.tsym));
//        System.out.println(symbol + " added to the HIER");
        //		System.out.println(symbol + " EXTENDSSSS");
        scanBaseClassSymbol(symbol.getSuperclass(), classNode, TypeRelations.IS_SUBTYPE_EXTENDS, astVisitor, symbol,
                ast);
        //		System.out.println(symbol + " IMPLEMENTSSS");
        // implements
        for (Type interfaceType : symbol.getInterfaces())
            scanBaseClassSymbol(interfaceType, classNode, TypeRelations.IS_SUBTYPE_IMPLEMENTS, astVisitor, symbol, ast);

    }

    private static void scanBaseClassSymbol(Type baseType, NodeWrapper classNode, TypeRelations rel,
                                            ASTTypesVisitor astVisitor, ClassSymbol classSymbol,
                                            ASTAuxiliarStorage ast) {
        if (baseType.getKind() != TypeKind.NONE) {
            NodeWrapper superTypeClass = DefinitionCache.getOrCreateType(baseType, ast);
            classNode.createRelationshipTo(superTypeClass, rel);
//            if (classNode.getProperty("fullyQualifiedName").toString()
//                    .contentEquals("com.google.protobuf.AbstractMessageLite.Builder")) {
//                System.out.println("type " + classNode.getProperty("fullyQualifiedName"));
//                System.out.println("through rel: " + rel);
//
//                System.out.println("supertype " + superTypeClass.getProperty("fullyQualifiedName"));
//            }
//            if (superTypeClass.getProperty("fullyQualifiedName").toString()
//                    .contentEquals(classNode.getProperty("fullyQualifiedName").toString()))
//                throw new IllegalStateException("SUBTYPE OF ITSELF! " + classNode.getProperty("fullyQualifiedName"));
            if (astVisitor == null) {
                classNode.createRelationshipTo(superTypeClass, CDGRelationTypes.USES_TYPE_DEF);
                PackageInfo.PACKAGE_INFO.handleNewDependency(classSymbol.packge(), baseType.tsym.packge());
            } else
                astVisitor.addToTypeDependencies(superTypeClass, baseType.tsym.packge());

            // AuxDebugInfo.lastMessage = null;

        }
    }
}

package utils;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCExpression;

import ast.ASTAuxiliarStorage;
import cache.DefinitionCache;
import database.nodes.NodeUtils;
import database.relations.PartialRelation;
import database.relations.RelationTypes;
import database.relations.RelationTypesInterface;
import database.relations.TypeRelations;
import node_wrappers.NodeWrapper;
import utils.dataTransferClasses.Pair;

public class GraphUtils {

	public static <T extends RelationTypesInterface> void connectWithParent(NodeWrapper child,
			Pair<PartialRelation<T>, Object> pair) {
		pair.getFirst().createRelationship(child);
	}

	// This method does not take into account the previous relationship
	public static <T extends RelationTypesInterface> void connectWithParent(NodeWrapper child,
			Pair<PartialRelation<T>, Object> pair, RelationTypes r) {

		pair.getFirst().getStartingNode().createRelationshipTo(child, r);
	}

	// public static TypeMirror attachTyperrrr(Tree tree, NodeWrapper node,
	// TreePath
	// path) {
	//
	// TypeMirror fullyQualifiedType = JavacInfo.getTypeMirror(tree, path);
	//
	// if (fullyQualifiedType != null) {
	// node.setProperty("actualType", fullyQualifiedType.toString());
	//
	// TypeKind typeKind = fullyQualifiedType.getKind();
	// if (typeKind != null)
	// node.setProperty("typeKind", typeKind.toString());
	// }
	// return fullyQualifiedType;
	// }
	//
	// public static TypeMirror attachTyperrr(Tree tree, NodeWrapper node) {
	// TypeMirror fullyQualifiedType = JavacInfo.getTypeMirror(tree);
	//
	// if (fullyQualifiedType != null) {
	// node.setProperty("actualType", fullyQualifiedType.toString());
	//
	// TypeKind typeKind = fullyQualifiedType.getKind();
	// if (typeKind != null)
	// node.setProperty("typeKind", typeKind.toString());
	// }
	//
	// return fullyQualifiedType;
	// }

	// public static void attachTypeDirect(NodeWrapper node, Type type, String
	// actualType, String typeKind) {
	//
	// // OJO AL RETURNTYPE
	// // CACHE GET OR CREATE TYPEDEC -->ACTUALTYPE
	// }
	public static void attachTypeDirect(NodeWrapper node, ExpressionTree exp, ASTAuxiliarStorage ast) {
		Type type = JavacInfo.getTypeDirect(exp);
		// if (type != null) {
		attachType(node, type,ast);
		// } else {
		// System.out.println("exp: " + exp.toString());
		// System.out.println(attachType(exp, node, JavacInfo.getPath(exp)));
		//
		// }
	}
/*
	public static void attachTypeDirectIdent(NodeWrapper node, IdentifierTree exp) {
		Type type = JavacInfo.getTypeDirect(exp);

		if (type.getKind() == TypeKind.EXECUTABLE)
			attachType(node, type);

		else
			// if (type != null) {
			attachType(node, type);
	}

	public static void attachTypeDirectMemberSel(NodeWrapper node, MemberSelectTree exp) {
		Type type = JavacInfo.getTypeDirect(exp);

		if (type.getKind() == TypeKind.EXECUTABLE)
			attachType(node, type);

		else
			// if (type != null) {
			attachType(node, type);
	}*/

	public static void attachTypeDirect(NodeWrapper node, VariableTree varDec, ASTAuxiliarStorage ast) {
		Type type = JavacInfo.getTypeDirect(varDec);
		// if (type != null) {
		// } else
		// System.out.println("varDec: " + varDec.getName());
		attachType(node, type,ast);
	}
/*
	public static void attachType(NodeWrapper node, Type type, int a) {
		attachTypeDirect(node, DefinitionCache.getOrCreateType(type), type.toString(), type.getKind().toString());
	}*/

	public static void attachType(NodeWrapper node, Type type, ASTAuxiliarStorage ast) {
//System.out.println(type);
//System.out.println(NodeUtils.nodeToString(node));
//		attachTypeDirect(node, type, type.toString(), type.getKind().toString(),ast);
	}

	public static void attachTypeDirect(NodeWrapper node, ExpressionTree exp, String actualType, String typeKind, ASTAuxiliarStorage ast) {

		attachTypeDirect(node, JavacInfo.getTypeDirect(exp), actualType, typeKind,ast);
	}

	private static void attachTypeDirect(NodeWrapper node, Type type, String actualType, String typeKind, ASTAuxiliarStorage ast) {
//		System.out.println("Type "+type);
		attachTypeDirect(node, DefinitionCache.getOrCreateType(type,ast), actualType, typeKind);
	}

	private static void attachTypeDirect(NodeWrapper node, NodeWrapper endNode, String actualType, String typeKind) {
		node.setProperty("actualType", actualType);
		node.setProperty("typeKind", typeKind);
		node.createRelationshipTo(endNode, TypeRelations.ITS_TYPE_IS);
	}
}

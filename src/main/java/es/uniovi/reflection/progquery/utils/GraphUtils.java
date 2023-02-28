package es.uniovi.reflection.progquery.utils;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Type;
import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.cache.DefinitionCache;
import es.uniovi.reflection.progquery.database.relations.PartialRelation;
import es.uniovi.reflection.progquery.database.relations.RelationTypesInterface;
import es.uniovi.reflection.progquery.database.relations.TypeRelations;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;

public class GraphUtils {

	public static <T extends RelationTypesInterface> void connectWithParent(NodeWrapper child,
																			Pair<PartialRelation<T>, Object> pair) {
		pair.getFirst().createRelationship(child);
	}

	public static <T extends RelationTypesInterface> void connectWithParent(NodeWrapper child, NodeWrapper parent,
																			T r) {
		parent.createRelationshipTo(child, r);
	}

	// This method does not take into account the previous relationship
	public static <T extends RelationTypesInterface> void connectWithParent(NodeWrapper child,
																			Pair<PartialRelation<T>, Object> pair,
																			T r) {

		pair.getFirst().getStartingNode().createRelationshipTo(child, r);
	}


	public static void attachTypeDirect(NodeWrapper node, ExpressionTree exp, ASTAuxiliarStorage ast) {
		Type type = JavacInfo.getTypeDirect(exp);
		attachType(node, type, ast);
	}


	public static void attachTypeDirect(NodeWrapper node, VariableTree varDec, ASTAuxiliarStorage ast) {
		Type type = JavacInfo.getTypeDirect(varDec);
		// if (type != null) {
		// } else
		// System.out.println("varDec: " + varDec.getName());
		attachType(node, type, ast);
	}
/*
	public static void attachType(NodeWrapper node, Type type, int a) {
		attachTypeDirect(node, DefinitionCache.getOrCreateType(type), type.toString(), type.getKind().toString());
	}*/

	public static NodeWrapper attachType(NodeWrapper node, Type type, ASTAuxiliarStorage ast) {
		//Dinamcally generated classes lacks types of some expressions/methods etc.
		if (type == null)
			type = Type.noType;
		return attachTypeDirect(node, type, type.toString(), type.getKind().toString(), ast);
	}

	public static void attachTypeDirect(NodeWrapper node, ExpressionTree exp, String actualType, String typeKind,
										ASTAuxiliarStorage ast) {

		attachTypeDirect(node, JavacInfo.getTypeDirect(exp), actualType, typeKind, ast);
	}

	private static NodeWrapper attachTypeDirect(NodeWrapper node, Type type, String actualType, String typeKind,
												ASTAuxiliarStorage ast) {
		NodeWrapper typeNode = DefinitionCache.getOrCreateType(type, ast);
		attachTypeDirect(node, typeNode, actualType, typeKind);
		return typeNode;
	}

	private static void attachTypeDirect(NodeWrapper node, NodeWrapper endNode, String actualType, String typeKind) {
		node.setProperty("actualType", actualType);
		node.setProperty("typeKind", typeKind);
		node.createRelationshipTo(endNode, TypeRelations.ITS_TYPE_IS);
	}
}

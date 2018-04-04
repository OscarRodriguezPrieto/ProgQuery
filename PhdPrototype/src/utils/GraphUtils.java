package utils;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.neo4j.graphdb.Node;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

import database.relations.PartialRelation;
import database.relations.RelationTypes;
import database.relations.RelationTypesInterface;

public class GraphUtils {

	public static <T extends RelationTypesInterface> void connectWithParent(Node child,
			Pair<PartialRelation<T>, Object> pair) {
		pair.getFirst().createRelationship(child);
	}

	// This method does not take into account the previous relationship
	public static <T extends RelationTypesInterface> void connectWithParent(Node child,
			Pair<PartialRelation<T>, Object> pair,
			RelationTypes r) {

		pair.getFirst().getStartingNode().createRelationshipTo(child, r);
	}

	public static TypeMirror attachType(Tree tree, Node node, TreePath path) {

		TypeMirror fullyQualifiedType = JavacInfo.getTypeMirror(tree, path);

		if (fullyQualifiedType != null) {
			node.setProperty("actualType", fullyQualifiedType.toString());

			TypeKind typeKind = fullyQualifiedType.getKind();
			if (typeKind != null)
				node.setProperty("typeKind", typeKind.toString());
		}
		return fullyQualifiedType;
	}

	public static TypeMirror attachType(Tree tree, Node node) {
		TypeMirror fullyQualifiedType = JavacInfo.getTypeMirror(tree);

		if (fullyQualifiedType != null) {
			node.setProperty("actualType", fullyQualifiedType.toString());

			TypeKind typeKind = fullyQualifiedType.getKind();
			if (typeKind != null)
				node.setProperty("typeKind", typeKind.toString());
		}

		return fullyQualifiedType;
	}

	public static void attachTypeDirect(Node node, String actualType, String typeKind) {

		node.setProperty("actualType", actualType);
		node.setProperty("typeKind", typeKind);
	}

	public static void attachTypeDirect(Node node, TypeMirror type) {

		node.setProperty("actualType", type.toString());
		node.setProperty("typeKind", type.getKind());
	}

	public static void attachTypeDirect(Node node, ExpressionTree exp) {
		TypeMirror type = JavacInfo.getTypeDirect(exp);
		// if (type != null) {
		node.setProperty("actualType", type.toString());
		node.setProperty("typeKind", type.getKind().toString());
		// } else {
		// System.out.println("exp: " + exp.toString());
		// System.out.println(attachType(exp, node, JavacInfo.getPath(exp)));
		//
		// }
	}

	public static void attachTypeDirect(ExpressionTree exp, Node node) {
		TypeMirror type = JavacInfo.getTypeDirect(exp);
		// if (type != null) {
		node.setProperty("actualType", type.toString());
		node.setProperty("typeKind", type.getKind().toString());
		// } else {
		// System.out.println("exp: " + exp.toString());
		// System.out.println(attachType(exp, node, JavacInfo.getPath(exp)));
		//
		// }
	}

	public static void attachTypeDirect(Node node, VariableTree varDec) {
		TypeMirror type = JavacInfo.getTypeDirect(varDec);
		// if (type != null) {
		node.setProperty("actualType", type.toString());
		node.setProperty("typeKind", type.getKind().toString());
		// } else
		// System.out.println("varDec: " + varDec.getName());
	}
}

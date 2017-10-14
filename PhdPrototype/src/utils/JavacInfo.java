package utils;

import java.util.Currency;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.parboiled.trees.TreeUtils;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

public class JavacInfo {

	private static JavacInfo currentJavacInfo;

	public static void setJavacInfo(JavacInfo javacInfo) {
		currentJavacInfo = javacInfo;
	}

	private final SourcePositions sourcePositions;
	private final Trees trees;
	private final CompilationUnitTree currCompilationUnit;
	private final Types types;

	public JavacInfo(CompilationUnitTree currCompilationUnit, JavacTask task) {
		this.currCompilationUnit = currCompilationUnit;
		this.trees = Trees.instance(task);
		types = task.getTypes();
		this.sourcePositions = trees.getSourcePositions();
	}

	// private CompilationUnitTree currentCompilationUnit;
	// private Trees trees;
	public static long getLineNumber(Tree tree) {

		// map offsets to line numbers in source file
		LineMap lineMap = currentJavacInfo.currCompilationUnit.getLineMap();
		if (lineMap == null)
			return -1;
		// find offset of the specified AST node
		long position = currentJavacInfo.sourcePositions.getStartPosition(currentJavacInfo.currCompilationUnit, tree);
		return lineMap.getLineNumber(position);
	}

	public static long getPosition(Tree tree) {
		return currentJavacInfo.sourcePositions.getStartPosition(currentJavacInfo.currCompilationUnit, tree);
	}

	public static long getSize(Tree tree) {
		return currentJavacInfo.sourcePositions.getEndPosition(currentJavacInfo.currCompilationUnit, tree)
				- currentJavacInfo.sourcePositions.getStartPosition(currentJavacInfo.currCompilationUnit, tree);
	}

	public static TreePath getPath(Tree tree) {
		return TreePath.getPath(currentJavacInfo.currCompilationUnit, tree);
	}

	public static TypeMirror getTypeMirror(Tree tree, TreePath path) {
		return currentJavacInfo.trees.getTypeMirror(path);
	}

	public static TypeMirror getTypeMirror(Tree tree) {

		return currentJavacInfo.trees.getTypeMirror(getPath(tree));
	}

	public static TypeMirror getTypeDirect(ExpressionTree tree) {

		return ((JCExpression) tree).type;
	}

	public static TypeMirror getTypeDirect(VariableTree tree) {

		return ((JCVariableDecl) tree).type;
	}

	public static TypeMirror getTypeDirect(MethodTree tree) {

		return ((JCMethodDecl) tree).type;
	}

	public static Tree getTree(Symbol s) {
		return currentJavacInfo.trees.getTree(s);
	}

}

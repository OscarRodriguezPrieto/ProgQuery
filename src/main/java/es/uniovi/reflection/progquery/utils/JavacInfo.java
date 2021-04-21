package es.uniovi.reflection.progquery.utils;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeInfo;

public class JavacInfo {

	private static JavacInfo currentJavacInfo;

	public static void setJavacInfo(JavacInfo javacInfo) {
		currentJavacInfo = javacInfo;
	}

	private final SourcePositions sourcePositions;
	private final Trees trees;
	private final CompilationUnitTree currCompilationUnit;
	private final Types javaxTypes;
	private final com.sun.tools.javac.code.Types types;
	private final Symtab symTab;

	public JavacInfo(CompilationUnitTree currCompilationUnit, JavacTask task) {
		this.currCompilationUnit = currCompilationUnit;
		this.trees = Trees.instance(task);
		javaxTypes = task.getTypes();
		types = com.sun.tools.javac.code.Types.instance(((com.sun.tools.javac.api.BasicJavacTask) task).getContext());
		this.sourcePositions = trees.getSourcePositions();
		symTab = Symtab.instance(((com.sun.tools.javac.api.BasicJavacTask) task).getContext());
	}

	public static Object[] getPosition(Tree tree) {
		// map offsets to line numbers in source file
		LineMap lineMap = currentJavacInfo.currCompilationUnit.getLineMap();
		if (lineMap == null)
			return new Object[0];
		// find offset of the specified AST node
		long position = currentJavacInfo.sourcePositions.getStartPosition(currentJavacInfo.currCompilationUnit, tree);
		// return lineMap.getLineNumber(position);
		// return
		// currentJavacInfo.sourcePositions.getStartPosition(currentJavacInfo.currCompilationUnit,
		// tree);
		// System.out.println();
		return new Object[] { "lineNumber", lineMap.getLineNumber(position), "column",
				lineMap.getColumnNumber(position), "position", position
				// "endColumn"

				// , lineMap.get(arg0, arg1)(
				// currentJavacInfo.sourcePositions.getEndPosition(currentJavacInfo.currCompilationUnit,
				// tree))
		};

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

	public static Type getTypeDirect(ExpressionTree tree) {

		return ((JCExpression) tree).type;
	}

	public static com.sun.tools.javac.code.Type getTypeDirect(VariableTree tree) {

		return ((JCVariableDecl) tree).type;
	}

	public static TypeMirror getTypeDirect(MethodTree tree) {

		return ((JCMethodDecl) tree).type;
	}

	public static Tree getTree(Symbol s) {
		return currentJavacInfo.trees.getTree(s);
	}

	public static Tree getTreeFromElement(Element e) {
		return currentJavacInfo.trees.getTree(e);
	}

	public static Tree getTreeFromElement(Tree t) {
		return currentJavacInfo.trees.getTree(currentJavacInfo.trees.getElement(getPath(t)));
	}

	public static Scope getScope(Tree t) {
		return currentJavacInfo.trees.getScope(getPath(t));
	}

	public static boolean isSubtype(Type t1, Type t2) {
		return currentJavacInfo.javaxTypes.isSubtype(t1, t2);
	}

	public static boolean isSuperType(Type t1, Type t2) {
		return currentJavacInfo.javaxTypes.isSubtype(t2, t1);
	}

	public static Symtab getSymtab() {
		return currentJavacInfo.symTab;
	}

	public static Symbol getSymbolFromTree(Tree t) {
		return TreeInfo.symbol((JCTree) t);
	}

	public static Type erasure(Type t) {
		return t.tsym.erasure(currentJavacInfo.types);
//		return currentJavacInfo.javaxTypes.erasure(t.ty);
	}
}

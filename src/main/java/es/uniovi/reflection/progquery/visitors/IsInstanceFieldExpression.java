package es.uniovi.reflection.progquery.visitors;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;

//RETURNS THE CLASS SYMBOL OF THE LEFT MOST (VALID- NON CLASS SYMBOL) ID IN THE LEFT PAQRT OF ASSIGNMENT
public class IsInstanceFieldExpression extends TreeScanner<Boolean, Void> {
	public static final IsInstanceFieldExpression GET_ID_VISITOR = new IsInstanceFieldExpression();

	@Override
	public Boolean scan(Tree t, Void v) {
		Boolean res = super.scan(t, v);
		return res == null ? false : res;
	}

	@Override
	public Boolean visitIdentifier(IdentifierTree identTree, Void v) {
		Symbol s = ((JCIdent) identTree).sym;
		return !(s.kind == Kinds.Kind.VAR && ((VarSymbol) s).isLocal());
	}

	@Override
	public Boolean visitMemberSelect(MemberSelectTree memberSel, Void v) {
		Symbol s = ((JCFieldAccess) memberSel).sym;
		return s.kind == Kinds.Kind.VAR && s.isStatic() ? false : scan(memberSel.getExpression(), v);

	}

	@Override
	public Boolean visitArrayAccess(ArrayAccessTree arrayAccess, Void v) {
		return scan(arrayAccess.getExpression(), v);

	}

}

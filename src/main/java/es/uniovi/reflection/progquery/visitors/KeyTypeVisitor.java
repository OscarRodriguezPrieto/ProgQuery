package es.uniovi.reflection.progquery.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.JCPrimitiveType;
import com.sun.tools.javac.code.Type.PackageType;
import com.sun.tools.javac.code.Type.TypeVar;

import es.uniovi.reflection.progquery.utils.types.CompoundTypeKey;
import es.uniovi.reflection.progquery.utils.types.GenericTypeKey;
import es.uniovi.reflection.progquery.utils.types.MethodTypeKey;
import es.uniovi.reflection.progquery.utils.types.WildcardKey;
import es.uniovi.reflection.progquery.utils.JavacInfo;

public class KeyTypeVisitor implements TypeVisitor<Object, Object> {

	@Override
	public Object visit(TypeMirror t) {
		// TODO Auto-generated method stub
		throw new IllegalStateException(t.getClass().toString());
	}

	@Override
	public Object visit(TypeMirror t, Object param) {
		// TODO Auto-generated method stub
		throw new IllegalStateException(t.getClass().toString());
	}

	@Override
	public Object visitArray(ArrayType t, Object param) {
		// System.out.println(t);
		// System.out.println(((Type)t).tsym);
		return t.toString();
	}

	@Override
	public Object visitDeclared(DeclaredType t, Object param) {
		if (t.getTypeArguments().size() > 0)
			return new GenericTypeKey(
					t.getTypeArguments().stream().map(argT -> argT.accept(this, null)).collect(Collectors.toList()),
					((Type) t).tsym);
		return ((Type) t).tsym;
	}

	@Override
	public Object visitError(ErrorType t, Object param) {
		final String key = "%%%%%ERROR_TYPE%%%%%";
		return key;
	}

	@Override
	public Object visitExecutable(ExecutableType t, Object param) {
		// MethodType type = (MethodType) t;
		List<Object> params = new ArrayList<Object>();
		for (TypeMirror pType : t.getParameterTypes())
			params.add(pType.accept(this, null));

		return new MethodTypeKey(params,
				t.getThrownTypes().stream().map(thrownType -> thrownType.accept(this, null))
						.collect(Collectors.toList()),
				t.getReturnType().accept(this, null),
				t.getReceiverType() == null ? null : t.getReceiverType().accept(this, null),
				((Type) t).tsym.isConstructor());
	}

	@Override
	public Object visitIntersection(IntersectionType t, Object param) {

		return new CompoundTypeKey(true,
				t.getBounds().stream().map(otherType -> otherType.accept(this, null)).collect(Collectors.toList()));
	}

	@Override
	public Object visitNoType(NoType t, Object param) {
		// TODO Auto-generated method stub
		if (t.getKind() == TypeKind.VOID) {
			final String voidKey = "%%%%%VOID_TYPE%%%%%";
			return voidKey;
		}
		if (t.getKind() == TypeKind.PACKAGE)
			return ((PackageType) t).tsym;
		throw new IllegalStateException(t.getClass().toString());
	}

	@Override
	public Object visitNull(NullType t, Object param) {
		final String key = "%%%%%NULL_TYPE%%%%%";
		return key;
	}

	@Override
	public Object visitPrimitive(PrimitiveType t, Object param) {
		return ((JCPrimitiveType) t).tsym;
	}

	@Override
	public Object visitTypeVariable(TypeVariable t, Object param) {
		// System.out.println("GENERATING KEY FOR TYPE VARIABLE ");
		// System.out.println(((TypeVar) t).tsym);
		// System.out.println(((TypeVar) t).tsym.getClass());
		return ((TypeVar) t).tsym;
	}

	@Override
	public Object visitUnion(UnionType t, Object param) {
		return new CompoundTypeKey(false, t.getAlternatives().stream().map(otherType -> otherType.accept(this, null))
				.collect(Collectors.toList()));

	}

	@Override
	public Object visitUnknown(TypeMirror t, Object param) {
		final String key = "%%%%%UNKNOWN_TYPE%%%%%";
		return key;
	}

	@Override
	public Object visitWildcard(WildcardType t, Object param) {
		// System.out.println(t);
		// System.out.println(t.getSuperBound());
		// System.out.println(t.getExtendsBound());
		// System.out.println(JavacInfo.getSymtab().botType);
		return new WildcardKey(
				(t.getSuperBound() == null ? JavacInfo.getSymtab().botType : t.getSuperBound()).accept(this, null),
				(t.getExtendsBound() == null ? JavacInfo.getSymtab().objectType : t.getExtendsBound()).accept(this,
						null));
	}

}

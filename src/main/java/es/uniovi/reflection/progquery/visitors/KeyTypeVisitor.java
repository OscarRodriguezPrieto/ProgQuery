package es.uniovi.reflection.progquery.visitors;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.JCPrimitiveType;
import com.sun.tools.javac.code.Type.PackageType;
import com.sun.tools.javac.code.Type.TypeVar;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.utils.JavacInfo;
import es.uniovi.reflection.progquery.utils.keys.cache.*;

import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyTypeVisitor implements TypeVisitor<TypeKey, Object> {

    @Override
    public TypeKey visit(TypeMirror t) {
        // TODO Auto-generated method stub
        throw new IllegalStateException(t.getClass().toString());
    }

    @Override
    public TypeKey visit(TypeMirror t, Object param) {
        // TODO Auto-generated method stub
        throw new IllegalStateException(t.getClass().toString());
    }

    @Override
    public TypeKey visitArray(ArrayType t, Object param) {
        return new StringKey(t.toString(), NodeTypes.ARRAY_TYPE.toString());
    }

    @Override
    public TypeKey visitDeclared(DeclaredType t, Object param) {
        if (t.getTypeArguments().size() > 0)
            return new GenericTypeKey(t.toString(),
                    t.getTypeArguments().stream().map(argT -> argT.accept(this, null)).collect(Collectors.toList()),
                    JavacInfo.erasure((Type) t).accept(this, null));
        return new ClassSymbolKey((Symbol.ClassSymbol) ((Type) t).tsym);
    }

    @Override
    public TypeKey visitError(ErrorType t, Object param) {
        final String key = "%%%%%ERROR_TYPE%%%%%";
        return new StringKey(key, NodeTypes.ERROR_TYPE.toString());
    }

    @Override
    public TypeKey visitExecutable(ExecutableType t, Object param) {
        // MethodType type = (MethodType) t;
        List<TypeKey> params = new ArrayList<>();
        for (TypeMirror pType : t.getParameterTypes())
            params.add(pType.accept(this, null));
        String fullName = t.toString() + " throws " + t.getThrownTypes().stream()
                .reduce("", (s, typeMirror) -> s + "," + typeMirror.toString(), (s1, s2) -> s1 + s2);

        return new MethodTypeKey(fullName, params,
                t.getThrownTypes().stream().map(thrownType -> thrownType.accept(this, null))
                        .collect(Collectors.toList()), t.getReturnType().accept(this, null),
                t.getReceiverType() == null ? null : t.getReceiverType().accept(this, null),
                ((Type) t).tsym.isConstructor());
    }

    @Override
    public TypeKey visitIntersection(IntersectionType t, Object param) {

        return new CompoundTypeKey(t.toString(), true,
                t.getBounds().stream().map(otherType -> otherType.accept(this, null)).collect(Collectors.toList()));
    }

    @Override
    public TypeKey visitNoType(NoType t, Object param) {
        // TODO Auto-generated method stub
        if (t.getKind() == TypeKind.VOID) {
            final String voidKey = "%%%%%VOID_TYPE%%%%%";
            return new StringKey(voidKey, NodeTypes.VOID_TYPE.toString());
        }
        if (t.getKind() == TypeKind.PACKAGE)
            return new SymbolKey(NodeTypes.PACKAGE_TYPE.toString(), ((PackageType) t).tsym);
        throw new IllegalStateException(t.getClass().toString());
    }

    @Override
    public TypeKey visitNull(NullType t, Object param) {
        final String key = "%%%%%NULL_TYPE%%%%%";
        return new StringKey(key, NodeTypes.NULL_TYPE.toString());
    }

    @Override
    public TypeKey visitPrimitive(PrimitiveType t, Object param) {
        return new SymbolKey( NodeTypes.PRIMITIVE_TYPE.toString(),((JCPrimitiveType) t).tsym);
    }

    @Override
    public TypeKey visitTypeVariable(TypeVariable t, Object param) {
        return new SymbolKey( NodeTypes.TYPE_VARIABLE.toString(),((TypeVar) t).tsym);
    }

    @Override
    public TypeKey visitUnion(UnionType t, Object param) {

        String fullName =
                t.getAlternatives().stream().reduce("", (s1, type) -> s1 + "|" + type.toString(), (s1, s2) -> s1 + s2);
        return new CompoundTypeKey(fullName, false,
                t.getAlternatives().stream().map(otherType -> otherType.accept(this, null))
                        .collect(Collectors.toList()));

    }

    @Override
    public TypeKey visitUnknown(TypeMirror t, Object param) {
        final String key = "%%%%%UNKNOWN_TYPE%%%%%";
        return new StringKey(key, NodeTypes.UNKNOWN_TYPE.toString());
    }

    @Override
    public TypeKey visitWildcard(WildcardType t, Object param) {

        return new WildcardKey(
                (t.getSuperBound() == null ? JavacInfo.getSymtab().botType : t.getSuperBound()).accept(this, null),
                (t.getExtendsBound() == null ? JavacInfo.getSymtab().objectType : t.getExtendsBound())
                        .accept(this, null), t.toString());
    }

}

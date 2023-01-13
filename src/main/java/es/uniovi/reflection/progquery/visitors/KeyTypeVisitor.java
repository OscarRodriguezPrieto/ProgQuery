package es.uniovi.reflection.progquery.visitors;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.TypeVar;
import es.uniovi.reflection.progquery.utils.JavacInfo;
import es.uniovi.reflection.progquery.utils.types.TypeKey;
import es.uniovi.reflection.progquery.utils.types.keys.*;

import javax.lang.model.element.ElementKind;
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
        TypeKey of = t.getComponentType().accept(this, null);
        return new ArrayTypeKey(of);
    }

    @Override
    public TypeKey visitDeclared(DeclaredType declaredType, Object param) {
//        System.out.println("DECLARED..." + declaredType);
//        System.out.println("TYPE INSIDE..." + ((Type) declaredType).tsym.type);
//        System.out.println("CONDITION..." + (declaredType.getTypeArguments().stream()
//                .filter(argType -> argType instanceof TypeVar &&
//                        ((TypeVar) argType).tsym.owner.type.equals(declaredType)).count() > 0));

        if (declaredType.getTypeArguments().stream().filter(argType -> !(argType instanceof TypeVar) ||
                !((TypeVar) argType).tsym.owner.type.equals(declaredType)).count() > 0)
            return new ParameterizedTypeKey(
                    declaredType.getTypeArguments().stream().map(argT -> argT.accept(this, null))
                            .collect(Collectors.toList()), ((Type) declaredType).tsym.type.accept(this, null));
        else
            return new TypeDefinitionKey(((Type) declaredType).tsym);
    }

    @Override
    public TypeKey visitError(ErrorType t, Object param) {
        return ErrorTypeKey.ERROR_TYPE_KEY;
    }

    @Override
    public TypeKey visitExecutable(ExecutableType t, Object param) {
        // MethodType type = (MethodType) t;
        List<TypeKey> params = new ArrayList<>();
        for (TypeMirror pType : t.getParameterTypes())
            params.add(pType.accept(this, null));

        return new MethodTypeKey(params, t.getThrownTypes().stream().map(thrownType -> thrownType.accept(this, null))
                .collect(Collectors.toList()),
                t.getTypeVariables().stream().map(typeVar -> typeVar.accept(this, null)).collect(Collectors.toList()),
                t.getReturnType().accept(this, null),
                t.getReceiverType() == null ? null : t.getReceiverType().accept(this, null),
                ((Type) t).tsym.isConstructor());
    }

    @Override
    public TypeKey visitIntersection(IntersectionType t, Object param) {

        return new CompoundTypeKey(true,
                t.getBounds().stream().map(otherType -> otherType.accept(this, null)).collect(Collectors.toList()));
    }

    @Override
    public TypeKey visitNoType(NoType t, Object param) {
        // TODO Auto-generated method stub
        if (t.getKind() == TypeKind.VOID)
            return VoidTypeKey.VOID_TYPE_KEY;

        if (t.getKind() == TypeKind.PACKAGE)
            return new PackageTypeKey(t);
        throw new IllegalStateException(t.getClass().toString());
    }

    @Override
    public TypeKey visitNull(NullType t, Object param) {
        return NullTypeKey.NULL_TYPE_KEY;
    }

    @Override
    public TypeKey visitPrimitive(PrimitiveType t, Object param) {
        return new PrimitiveTypeKey(t);
    }

    @Override
    public TypeKey visitTypeVariable(TypeVariable t, Object param) {
        ElementKind ownerKind = ((TypeVar) t).tsym.owner.getKind();
        return ownerKind == ElementKind.METHOD || ownerKind == ElementKind.CONSTRUCTOR ||
                ownerKind == ElementKind.STATIC_INIT || ownerKind == ElementKind.INSTANCE_INIT ?
                new TypeVariableKey(t) : new TypeVariableKey(t, ((TypeVar) t).tsym.owner.type.accept(this, null));
    }

    @Override
    public TypeKey visitUnion(UnionType t, Object param) {
        return new CompoundTypeKey(false, t.getAlternatives().stream().map(otherType -> otherType.accept(this, null))
                .collect(Collectors.toList()));

    }

    @Override
    public TypeKey visitUnknown(TypeMirror t, Object param) {
        return UnknownTypeKey.UNKNOWN_TYPE_KEY;
    }

    @Override
    public TypeKey visitWildcard(WildcardType t, Object param) {
        // System.out.println(t);
        // System.out.println(t.getSuperBound());
        // System.out.println(t.getExtendsBound());
        // System.out.println(JavacInfo.getSymtab().botType);
        return new WildcardKey(
                (t.getSuperBound() == null ? JavacInfo.getSymtab().botType : t.getSuperBound()).accept(this, null),
                (t.getExtendsBound() == null ? JavacInfo.getSymtab().objectType : t.getExtendsBound())
                        .accept(this, null));
    }

}

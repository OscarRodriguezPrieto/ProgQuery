package es.uniovi.reflection.progquery.visitors;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.cache.DefinitionCache;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.nodes.NodeCategory;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.database.relations.TypeRelations;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.typeInfo.TypeHierarchy;
import es.uniovi.reflection.progquery.utils.JavacInfo;
import es.uniovi.reflection.progquery.utils.MethodNameInfo;
import es.uniovi.reflection.progquery.utils.types.TypeKey;
import es.uniovi.reflection.progquery.utils.types.keys.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.*;
import java.util.List;
import java.util.stream.Collectors;

public class TypeVisitor implements javax.lang.model.type.TypeVisitor<NodeWrapper, TypeKey> {
    private ASTAuxiliarStorage ast;

    // private Set<>
    public TypeVisitor(ASTAuxiliarStorage ast) {
        super();
        this.ast = ast;
    }

    @Override
    public NodeWrapper visit(TypeMirror t) {
        throw new IllegalStateException(t.getClass().toString());
    }

    @Override
    public NodeWrapper visit(TypeMirror t, TypeKey key) {
        // TODO Auto-generated method stub
        throw new IllegalStateException(t.getClass().toString());
    }

    private static Object[] onlyNamesProps(TypeMirror type) {
        return DatabaseFachade.getTypeDecProperties(type.toString().replaceAll("(\\w+\\.)", ""), type.toString());
    }

    private static NodeWrapper createWithProps(NodeTypes nodeType, Object[] props) {
        return DatabaseFachade.CURRENT_DB_FACHADE.createTypeDecNode(nodeType, props);

    }

    private static NodeWrapper createWithSingleName(TypeMirror type, NodeTypes nodeType) {
        return createWithProps(nodeType, DatabaseFachade.getTypeDecProperties(type.toString()));
    }

    private static NodeWrapper createWithOnlyNames(TypeMirror type, NodeTypes nodeType) {
        return createWithProps(nodeType, onlyNamesProps(type));
    }

    @Override
    public NodeWrapper visitArray(ArrayType type, TypeKey key) {
        NodeWrapper node = createWithOnlyNames(type, NodeTypes.ARRAY_TYPE);
        putInCacheAsType(key, node);
        node.createRelationshipTo(
                DefinitionCache.getOrCreateType(type.getComponentType(), ((ArrayTypeKey) key).getTypeOf(), ast),
                RelationTypes.TYPE_PER_ELEMENT);
        return node;
    }

    public static NodeWrapper generatedClassType(ClassSymbol classSymbol, ASTAuxiliarStorage ast) {
        TypeDefinitionKey typeDefKey = new TypeDefinitionKey(classSymbol);
        if (DefinitionCache.TYPE_CACHE.containsKey(typeDefKey))
            return DefinitionCache.TYPE_CACHE.get(typeDefKey);

        NodeWrapper generatedTypeDec =
                DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredCLASSTypeDecNode(classSymbol, NodeTypes.CLASS_DEF);
        //Para la caché usamos el nombre del simbolo, porque no sabemos si el simbolo será el mismo de una ejecución
        // a otra
        putInCache(typeDefKey, generatedTypeDec);
        ast.typeDecNodes.add(generatedTypeDec);
        //De la herecia no sabemos nada, superclass none
        //	TypeHierarchy.addTypeHierarchy((ClassSymbol) ((Type.ClassType) t).tsym, nonDeclaredTypeDec, null, ast);
        return generatedTypeDec;
    }

    // private static final Set<Symbol> nonDeclaredTypeSymbols=new HashSet<>();
    @Override
    public NodeWrapper visitDeclared(DeclaredType t, TypeKey declaredTypeKey) {
        Type type = ((Type) t);
        //        		System.out.println("Visiting type DECLARED " + t+"\n with type args:\n");
        //         t.getTypeArguments().forEach(System.out::println);
        NodeWrapper declaredType;
        TypeRelations typeArgRel = TypeRelations.GENERIC_TYPE_PARAM;
        String typeArgPropertyName = "paramIndex";
        List<TypeKey> typeArgkeys;
        if (declaredTypeKey instanceof ParameterizedTypeKey) {
            //            System.out.println(type.tsym.type);
            //             System.out.println(((ClassType) t).tsym.asType());
            //             System.out.println(type.tsym.erasure_field);
            //             System.out.println(JavacInfo.erasure(type));
            typeArgRel = TypeRelations.TYPE_ARGUMENT;
            typeArgPropertyName = "argumentIndex";
            typeArgkeys = ((ParameterizedTypeKey) declaredTypeKey).getTypeArgs();
            declaredType = createWithOnlyNames(type, NodeTypes.PARAMETERIZED_TYPE);
            putInCacheAsType(declaredTypeKey, declaredType);

            final NodeWrapper genericType = DefinitionCache
                    .getOrCreateType(type.tsym.type, ((ParameterizedTypeKey) declaredTypeKey).getGenericType(), ast);

            declaredType.createRelationshipTo(genericType, TypeRelations.PARAMETERIZES_TYPE);

        } else {

            typeArgkeys = t.getTypeArguments().stream()
                    .map(typeParam -> new TypeVariableKey((TypeVariable) typeParam, declaredTypeKey))
                    .collect(Collectors.toList());
            declaredType =
                    // t instanceof ClassType
                    // ?
                    DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredCLASSTypeDecNode(((ClassType) t),
                            type.isInterface() ? NodeTypes.INTERFACE_DEF :
                                    type.tsym.isEnum() ? NodeTypes.ENUM_DEF : NodeTypes.CLASS_DEF);

            if (t.getTypeArguments().size() > 0)
                declaredType.addLabel(NodeTypes.GENERIC_TYPE);
            putInCache(declaredTypeKey, declaredType);
            ast.typeDecNodes.add(declaredType);

            // Solo a�adimos dependencias de clases no declaradas cuando heredan
            // o
            // implementan de otra, para el futuro se podr�an analizar los class
            // symbols de
            // las signaturas de los m�todos o de los atributos, tendr�amos que
            // hacer en este visit un Set<NodeWrapper> con los typeDecUses para
            // no
            // crear dos veces la misma relaci�n, se podr�a reutilizar el m�todo
            // est�tico de ASTTypesVisitor addToTypeDependencies
            TypeHierarchy.addTypeHierarchy((ClassSymbol) ((Type.ClassType) t).tsym, declaredType, null, ast);
            //			System.out.println("FINISEHD TYPE HIER FOR " + t);
            //			System.out.println("ENCLOSED ELEMENTS");
            (type.tsym).getEnclosedElements().forEach(elementSymbol -> {
                //				System.out.println("ELEMENT " + e);
                //				System.out.println("ELEMENT " + e.getKind());
                if (elementSymbol.getKind() != ElementKind.FIELD) {
                    // System.out.println("TYPE" + e.type);
                    try {
                        if (elementSymbol.getKind() != ElementKind.METHOD &&
                                elementSymbol.getKind() != ElementKind.CONSTRUCTOR
                            //                        && elementSymbol.getKind() != ElementKind.STATIC_INIT &&
                            //                        elementSymbol.getKind() != ElementKind.INSTANCE_INIT
                        )
                            return;

                        MethodNameInfo nameInfo = new MethodNameInfo((MethodSymbol) elementSymbol);

                        if (!DefinitionCache.METHOD_DEF_CACHE.containsKey(nameInfo.getFullyQualifiedName()))
                            if (elementSymbol.getKind() == ElementKind.METHOD)
                                ASTTypesVisitor.createNonDeclaredMethodDuringTypeCreation(nameInfo, declaredType,
                                        type.isInterface(), ast, (MethodSymbol) elementSymbol);
                            else
                                ASTTypesVisitor.getNotDeclaredConstructorDuringTypeCreation(nameInfo, declaredType,
                                        elementSymbol);

                    } catch (com.sun.tools.javac.code.Symbol.CompletionFailure ex) {
                        // System.out.println("CACHED RUNTIME");
                        // e.printStackTrace();
                        // throw new IllegalStateException(ex);
                        System.err.println("Failed to analyze " + elementSymbol.getKind() + " of " + t.toString() +
                                ", due to missing symbols:\n" + ex.toString() + "\n");
                    }

                }
            });
        }

        for (int i = 0; i < t.getTypeArguments().size(); i++)// CON EL FOR
            declaredType.createRelationshipTo(
                    DefinitionCache.getOrCreateType(t.getTypeArguments().get(i), typeArgkeys.get(i), ast), typeArgRel)
                    .setProperty(typeArgPropertyName, i + 1);


        return declaredType;
    }

    @Override
    public NodeWrapper visitError(ErrorType t, TypeKey key) {
        return putInCache(key, createWithSingleName(t, NodeTypes.ERROR_TYPE));

    }

    @Override
    public NodeWrapper visitExecutable(ExecutableType t, TypeKey key) {
        MethodTypeKey mtKey = (MethodTypeKey) key;

        NodeWrapper methodTypeNode = createWithOnlyNames(t, NodeTypes.CALLABLE_TYPE);
        putInCache(key, methodTypeNode);

        methodTypeNode
                .createRelationshipTo(DefinitionCache.getOrCreateType(t.getReturnType(), mtKey.getReturnType(), ast),
                        TypeRelations.RETURN_TYPE);
        int i = 0;
        for (TypeKey pKey : mtKey.getParamTypes()) {
            methodTypeNode
                    .createRelationshipTo(DefinitionCache.getOrCreateType(t.getParameterTypes().get(i), pKey, ast),
                            TypeRelations.PARAM_TYPE).setProperty("paramIndex", ++i);
        }
        // METER UN PUTO FOR PARA PODER REUTILIZAR LAS KEYS, POR FAVOR!!
        for (i = 0; i < t.getThrownTypes().size(); i++)
            methodTypeNode.createRelationshipTo(
                    DefinitionCache.getOrCreateType(t.getThrownTypes().get(i), mtKey.getThrownTypes().get(i), ast),
                    TypeRelations.THROWS_TYPE);

        if (mtKey.getReceiverType() != null)
            // MIRAR SI RECEIVER TYPE ES IGUAL A INSTANCE ARG TYPE PROBAR A
            // SACAR POR PANTALLA COSITAS...
            methodTypeNode.createRelationshipTo(
                    DefinitionCache.getOrCreateType(t.getReceiverType(), mtKey.getReceiverType(), ast),
                    TypeRelations.INSTANCE_ARG_TYPE);
        return methodTypeNode;
    }

    @Override
    public NodeWrapper visitIntersection(IntersectionType t, TypeKey key) {
        NodeWrapper intersType = createWithOnlyNames(t, NodeTypes.INTERSECTION_TYPE);
        putInCacheAsType(key, intersType);

        for (int i = 0; i < t.getBounds().size(); i++)
            intersType.createRelationshipTo(DefinitionCache
                            .getOrCreateType(t.getBounds().get(i), ((CompoundTypeKey) key).getTypes().get(i), ast),
                    TypeRelations.INTERSECTION_OF);

        return intersType;
    }

    @Override
    public NodeWrapper visitNoType(NoType t, TypeKey key) {
        return putInCache(key,
                createWithSingleName(t, t.getKind() == TypeKind.VOID ? NodeTypes.VOID_TYPE : NodeTypes.PACKAGE_TYPE));

    }

    @Override
    public NodeWrapper visitNull(NullType t, TypeKey key) {
        return putInCache(key, createWithSingleName(t, NodeTypes.NULL_TYPE));
    }

    @Override
    public NodeWrapper visitPrimitive(PrimitiveType t, TypeKey key) {
        return putInCacheAsType(key, createWithSingleName(t, NodeTypes.PRIMITIVE_TYPE));
    }

    @Override
    public NodeWrapper visitTypeVariable(TypeVariable t, TypeKey key) {
        NodeWrapper typeVar = createWithProps(NodeTypes.TYPE_VARIABLE,
                DatabaseFachade.getTypeDecProperties(t.toString(), key.toString()));
        putInCache(key, typeVar);

        typeVar.createRelationshipTo(DefinitionCache
                        .getOrCreateType(t.getUpperBound() == null ? JavacInfo.getSymtab().objectType :
                                t.getUpperBound(), ast),
                RelationTypes.UPPER_BOUND_TYPE);
        typeVar.createRelationshipTo(// QUITAR TSYM PARA TIPOS QUE NO TENEMOS
                // GARANTIA DE QUE SEAN DECLARED, la key
                // ya se calcular� en el keyvisitor
                DefinitionCache
                        .getOrCreateType(t.getLowerBound() == null ? JavacInfo.getSymtab().botType : t.getLowerBound(),
                                ast), RelationTypes.LOWER_BOUND_TYPE);
        return typeVar;

    }

    @Override
    public NodeWrapper visitUnion(UnionType t, TypeKey key) {
        NodeWrapper union = createWithProps(NodeTypes.UNION_TYPE, new Object[]{});
        putInCacheAsType(key, union);
        String fullName = "", simpleName = "";
        for (int i = 0; i < t.getAlternatives().size(); i++) {
            NodeWrapper alternative = DefinitionCache
                    .getOrCreateType(t.getAlternatives().get(i), ((CompoundTypeKey) key).getTypes().get(i), ast);
            union.createRelationshipTo(alternative, TypeRelations.UNION_ALTERNATIVE);
            fullName += "|" + alternative.getProperty("fullyQualifiedName");
            simpleName += "|" + alternative.getProperty("simpleName");

        }
        union.setProperty("fullyQualifiedName", fullName.substring(1));
        union.setProperty("simpleName", simpleName.substring(1));
        union.setProperty("resultingType", t.toString());
        return union;
    }

    @Override
    public NodeWrapper visitUnknown(TypeMirror t, TypeKey key) {
        return putInCache(key, createWithSingleName(t, NodeTypes.UNKNOWN_TYPE));
    }

    @Override
    public NodeWrapper visitWildcard(WildcardType t, TypeKey key) {
        // System.out.println("WILCARD KIND " + t.getKind().toString());

        NodeWrapper wildcardNode = createWithOnlyNames(t, NodeTypes.WILDCARD_TYPE);
        putInCacheAsType(key, wildcardNode);
        // wildcardNode.setProperty("typeBoundKind", t.getKind().toString());
        wildcardNode.createRelationshipTo(DefinitionCache
                .getOrCreateType(t.getExtendsBound() == null ? JavacInfo.getSymtab().objectType : t.getExtendsBound(),
                        ((WildcardKey) key).getExtendsBound(), ast), TypeRelations.WILDCARD_EXTENDS_BOUND);
        wildcardNode.createRelationshipTo(DefinitionCache
                .getOrCreateType(t.getSuperBound() == null ? JavacInfo.getSymtab().botType : t.getSuperBound(),
                        ((WildcardKey) key).getSuperBound(), ast), TypeRelations.WILDCARD_SUPER_BOUND);
        return wildcardNode;
    }

    private static NodeWrapper putInCacheAsType(TypeKey key, NodeWrapper typeNode) {
        typeNode.addLabel(NodeCategory.TYPE_NODE);
        DefinitionCache.TYPE_CACHE.put(key, typeNode);
        return typeNode;
    }

    private static NodeWrapper putInCache(TypeKey key, NodeWrapper typeNode) {

        DefinitionCache.TYPE_CACHE.put(key, typeNode);
        return typeNode;
    }
}

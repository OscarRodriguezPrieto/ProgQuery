package es.uniovi.reflection.progquery.visitors;

import com.sun.tools.javac.code.Symbol;
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
import es.uniovi.reflection.progquery.utils.keys.cache.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.*;

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

    @Override
    public NodeWrapper visitArray(ArrayType type, TypeKey key) {
        NodeWrapper node = createNonDeclaredTypeNode(NodeTypes.ARRAY_TYPE, key.toString());
        putInCache(key, node);
        node.createRelationshipTo(DefinitionCache.getOrCreateType(type.getComponentType(), ast),
                RelationTypes.TYPE_PER_ELEMENT);
        return node;
    }

    public static NodeWrapper generatedClassType(ClassSymbol classSymbol, ASTAuxiliarStorage ast) {
        TypeKey key = classSymbol.type.accept(new KeyTypeVisitor(), null);
        if (DefinitionCache.TYPE_CACHE.containsKey(key))
            return DefinitionCache.TYPE_CACHE.get(key);

        NodeWrapper generatedTypeDec =
                DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredCLASSTypeDecNode(classSymbol, NodeTypes.CLASS_DEF);
        putInCache(key, generatedTypeDec);
        ast.typeDecNodes.add(generatedTypeDec);
        //De la herecia no sabemos nada, superclass none
        //	TypeHierarchy.addTypeHierarchy((ClassSymbol) ((Type.ClassType) t).tsym, nonDeclaredTypeDec, null, ast);
        return generatedTypeDec;
    }

    // private static final Set<Symbol> nonDeclaredTypeSymbols=new HashSet<>();
    @Override
    public NodeWrapper visitDeclared(DeclaredType t, TypeKey key) {
        Type type = ((Type) t);
        //		System.out.println("Visiting type DECLARED " + t);
        // t.getTypeArguments().forEach(System.out::println);
        // System.out.println("VISITING DECLARED " + t);
        final NodeWrapper nonDeclaredTypeDec;
        NodeWrapper ret = null;
        // System.out.println(t.getClass());
        // System.out.println(t instanceof ClassType);
        if (t.getTypeArguments().size() > 0) {
            // System.out.println("GENERIC");
            // System.out.println(type.tsym.type);
            // System.out.println(((ClassType) t).tsym.asType());
            //
            // System.out.println(type.tsym.erasure_field);
            //
            // System.out.println(JavacInfo.erasure(type));
            // System.out.println(type.tsym.completer);
            // System.out.println("BASE TYPE " + type.baseType());
            //
            // System.out.println(type.getOriginalType());
            // System.out.println(type.getModelType());

            NodeWrapper genericType = createNonDeclaredTypeNode(NodeTypes.PARAMETERIZED_TYPE, key.toString());
            genericType.addLabel(NodeCategory.TYPE_NODE);
            String rawName = t.toString().split("<")[0];
            genericType.setProperty("rawName", rawName);
            String[] names = rawName.split("\\.");
            genericType.setProperty("simpleName", names[names.length - 1]);
            ret = genericType;
            // putInCache(((GenericTypeKey) key).getParameterizedType(),
            // nonDeclaredTypeDec);
            putInCache(key, genericType);
            // es.uniovi.reflection.progquery.ast.typeDecNodes.add(genericType);
            nonDeclaredTypeDec = DefinitionCache
                    .getOrCreateType(JavacInfo.erasure(type), ((ParameterizedTypeKey) key).getParameterizedType(), ast);

            genericType.createRelationshipTo(nonDeclaredTypeDec, RelationTypes.PARAMETERIZED_TYPE);
            // System.out.println("TYPE ARGUMENTS");
            for (int i = 0; i < t.getTypeArguments().size(); i++)// CON EL FOR
                // PODEMOS
                // REUTILIZAR
                // LAS KEYS
                // DEL
                // GENERIC
                // TYPE
                genericType.createRelationshipTo(DefinitionCache
                        .getOrCreateType(t.getTypeArguments().get(i), ((ParameterizedTypeKey) key).getTypeArgs().get(i),
                                ast), RelationTypes.GENERIC_TYPE_ARGUMENT).setProperty("argumentIndex", i + 1);

        } else {

            nonDeclaredTypeDec =
                    // t instanceof ClassType
                    // ?
                    DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredCLASSTypeDecNode(((ClassType) t),
                            type.isInterface() ? NodeTypes.INTERFACE_DEF :
                                    type.tsym.isEnum() ? NodeTypes.ENUM_DEF : NodeTypes.CLASS_DEF);
            putInCache(key, nonDeclaredTypeDec);
            ret = nonDeclaredTypeDec;
            ast.typeDecNodes.add(nonDeclaredTypeDec);
            if (type.tsym.getTypeParameters().size() > 0) {
                nonDeclaredTypeDec.addLabel(NodeTypes.GENERIC_TYPE);
                type.tsym.getTypeParameters().forEach(typeParamSymbol -> nonDeclaredTypeDec.createRelationshipTo(
                        DefinitionCache.getOrCreateType(typeParamSymbol.type, typeParamSymbol.type
                                .accept(new KeyForNewTypeVarVisitor(key.toString().split("<")[0]), null), ast),
                        TypeRelations.HAS_TYPE_PARAMETER));
            }
            // Solo a�adimos dependencias de clases no declaradas cuando heredan
            // o
            // implementan de otra, para el futuro se podr�an analizar los class
            // symbols de
            // las signaturas de los m�todos o de los atributos, tendr�amos que
            // hacer en este visit un Set<NodeWrapper> con los typeDecUses para
            // no
            // crear dos veces la misma relaci�n, se podr�a reutilizar el m�todo
            // est�tico de ASTTypesVisitor addToTypeDependencies
            TypeHierarchy.addTypeHierarchy((ClassSymbol) ((Type.ClassType) t).tsym, nonDeclaredTypeDec, null, ast);
            //			System.out.println("FINISEHD TYPE HIER FOR " + t);
            //			System.out.println("ENCLOSED ELEMENTS");
            type.tsym.getEnclosedElements().forEach(elementSymbol -> {
                //				System.out.println("ELEMENT " + e);
                //				System.out.println("ELEMENT " + e.getKind());
                if (elementSymbol.getKind() != ElementKind.FIELD) {
                    // System.out.println("TYPE" + e.type);
                    try {
                        if (!DefinitionCache.METHOD_DEF_CACHE.containsKey(elementSymbol)) {
                            //							System.out.println("AFTER CONTAINS KEY");
                            if (elementSymbol.getKind() == ElementKind.METHOD)
                                ASTTypesVisitor.createNonDeclaredMethodDuringTypeCreation(nonDeclaredTypeDec,
                                        type.isInterface(), ast, (MethodSymbol) elementSymbol);
                            else if (elementSymbol.getKind() == ElementKind.CONSTRUCTOR)
                                ASTTypesVisitor
                                        .getNotDeclaredConstructorDuringTypeCreation(nonDeclaredTypeDec, elementSymbol,
                                                ast);
                        }
                    } catch (com.sun.tools.javac.code.Symbol.CompletionFailure ex) {
                        System.err.println("Failed to analyze " + elementSymbol.getKind() + " of " + t.toString() +
                                ", due to missing symbols:\n" + ex.toString() + "\n");
                    }

                }
            });
        }

        // System.out.println("RETURNING declared");
        return ret;
    }

    @Override
    public NodeWrapper visitError(ErrorType t, TypeKey key) {
        NodeWrapper erroNode = createNonDeclaredTypeNode(NodeTypes.ERROR_TYPE, t.toString());
        putInCache(key, erroNode);
        return erroNode;
    }

    @Override
    public NodeWrapper visitExecutable(ExecutableType t, TypeKey key) {
        MethodTypeKey mtKey = (MethodTypeKey) key;
        String fullName = key.toString();


        NodeWrapper methodTypeNode = createNonDeclaredTypeNode(NodeTypes.CALLABLE_TYPE, fullName);
        methodTypeNode
                .setProperty("simpleName", t.getThrownTypes().size() > 0 ? fullName.split(" throws ")[0] : fullName);
        putInCache(key, methodTypeNode);
        Symbol.TypeSymbol methodSymbol =
                t instanceof Type.MethodType ? ((Type.MethodType) t).tsym : ((Type.ForAll) t).tsym;
        if (methodSymbol.getTypeParameters().size() > 0) {
            methodTypeNode.addLabel(NodeTypes.GENERIC_TYPE);
            int i = 0;
            for (Symbol.TypeVariableSymbol typeVarSymbol : methodSymbol.getTypeParameters())
                methodTypeNode.createRelationshipTo(

                       DefinitionCache.getOrCreateType(typeVarSymbol.type,
                                typeVarSymbol.type.accept(new KeyForNewTypeVarVisitor(fullName.split("<")[0]), null),ast),
                        TypeRelations.HAS_TYPE_PARAMETER).setProperty("paramIndex", ++i);
        }

        methodTypeNode
                .createRelationshipTo(DefinitionCache.getOrCreateType(t.getReturnType(), mtKey.getReturnType(), ast),
                        TypeRelations.RETURN_TYPE);


        for (int i = 0; i < t.getParameterTypes().size(); i++) {
            methodTypeNode.createRelationshipTo(
                    DefinitionCache.getOrCreateType(t.getParameterTypes().get(i), mtKey.getParamTypes().get(i), ast),
                    TypeRelations.PARAM_TYPE).setProperty("paramIndex", ++i);
        }
        // METER UN PUTO FOR PARA PODER REUTILIZAR LAS KEYS, POR FAVOR!!
        for (int i = 0; i < t.getThrownTypes().size(); i++)
            methodTypeNode.createRelationshipTo(
                    DefinitionCache.getOrCreateType(t.getThrownTypes().get(i), mtKey.getThrownTypes().get(i), ast),
                    TypeRelations.THROWS_TYPE);

        if (mtKey.getInstanceType() != null)
            // MIRAR SI RECEIVER TYPE ES IGUAL A INSTANCE ARG TYPE PROBAR A
            // SACAR POR PANTALLA COSITAS...
            methodTypeNode.createRelationshipTo(DefinitionCache.getOrCreateType(t.getReceiverType(), ast),
                    TypeRelations.INSTANCE_ARG_TYPE);
        return methodTypeNode;
    }

    @Override
    public NodeWrapper visitIntersection(IntersectionType t, TypeKey key) {
        NodeWrapper intersType = createNonDeclaredTypeNode(NodeTypes.INTERSECTION_TYPE, key.toString());
        putInCache(key, intersType);
        for (int i = 0; i < t.getBounds().size(); i++)
            intersType.createRelationshipTo(DefinitionCache
                            .getOrCreateType(t.getBounds().get(i), ((CompoundTypeKey) key).getTypes().get(i), ast),
                    RelationTypes.INTERSECTION_COMPOSED_OF);

        return intersType;
    }

    @Override
    public NodeWrapper visitNoType(NoType t, TypeKey key) {
        return putInCache(key,
                createNonDeclaredTypeNode(t.getKind() == TypeKind.VOID ? NodeTypes.VOID_TYPE : NodeTypes.PACKAGE_TYPE,
                        t.toString()));

    }

    @Override
    public NodeWrapper visitNull(NullType t, TypeKey key) {
        return putInCache(key, createNonDeclaredTypeNode(NodeTypes.NULL_TYPE, t.toString()));
    }

    @Override
    public NodeWrapper visitPrimitive(PrimitiveType t, TypeKey key) {
        NodeWrapper primitive = createNonDeclaredTypeNode(NodeTypes.PRIMITIVE_TYPE, t.toString());
        primitive.addLabel(NodeCategory.TYPE_NODE);
        return putInCache(key, primitive);
    }

    @Override
    public NodeWrapper visitTypeVariable(TypeVariable t, TypeKey key) {
        NodeWrapper typeVar = createNonDeclaredTypeNode(NodeTypes.TYPE_VARIABLE, key.toString());
        final String WILCARD_NAME = "<captured wildcard>";
        typeVar.setProperty("name", key.toString().contains(TypeVarKey.WILDCARD_CLUE) ? WILCARD_NAME : t.toString());
        putInCache(key, typeVar);
        if (t.getUpperBound() != null)
            typeVar.createRelationshipTo(DefinitionCache.getOrCreateType(t.getUpperBound(), ast),
                    RelationTypes.UPPER_BOUND_TYPE);

        if (t.getLowerBound() != null)
            typeVar.createRelationshipTo(DefinitionCache.getOrCreateType(t.getLowerBound(), ast),
                    RelationTypes.LOWER_BOUND_TYPE);
        return typeVar;

    }

    @Override
    public NodeWrapper visitUnion(UnionType t, TypeKey key) {
        NodeWrapper union = createNonDeclaredTypeNode(NodeTypes.UNION_TYPE, key.toString());
        union.addLabel(NodeCategory.TYPE_NODE);
        union.setProperty("resultingType", t.toString());
        putInCache(key, union);
        for (int i = 0; i < t.getAlternatives().size(); i++)
            union.createRelationshipTo(DefinitionCache
                            .getOrCreateType(t.getAlternatives().get(i), ((CompoundTypeKey) key).getTypes().get(i),
                                    ast),
                    RelationTypes.UNION_TYPE_ALTERNATIVE);

        return union;
    }

    @Override
    public NodeWrapper visitUnknown(TypeMirror t, TypeKey key) {
        return putInCache(key, createNonDeclaredTypeNode(NodeTypes.UNKNOWN_TYPE, t.toString()));
    }

    @Override
    public NodeWrapper visitWildcard(WildcardType t, TypeKey key) {
        // System.out.println("WILCARD KIND " + t.getKind().toString());

        NodeWrapper wildcardNode = createNonDeclaredTypeNode(NodeTypes.WILDCARD_TYPE, key.toString());
        wildcardNode.addLabel(NodeCategory.TYPE_NODE);
        putInCache(key, wildcardNode);

        if (t.getExtendsBound() != null)
            wildcardNode.createRelationshipTo(
                    DefinitionCache.getOrCreateType(t.getExtendsBound(), ((WildcardKey) key).getExtendsBound(), ast),
                    TypeRelations.WILDCARD_EXTENDS_BOUND);

        if (t.getSuperBound() != null)
            wildcardNode.createRelationshipTo(
                    DefinitionCache.getOrCreateType(t.getSuperBound(), ((WildcardKey) key).getSuperBound(), ast),
                    TypeRelations.WILDCARD_SUPER_BOUND);
        return wildcardNode;
    }

    private static NodeWrapper putInCache(TypeKey key, NodeWrapper typeNode) {
        DefinitionCache.TYPE_CACHE.put(key, typeNode);
        return typeNode;
    }

    private static NodeWrapper createNonDeclaredTypeNode(NodeTypes nodetype, String fullyQualifiedName) {

        NodeWrapper typeNode = DatabaseFachade.CURRENT_DB_FACHADE.createNodeWithoutExplicitTree(nodetype);
        typeNode.setProperty("fullyQualifiedName", fullyQualifiedName);
        return typeNode;
    }
}

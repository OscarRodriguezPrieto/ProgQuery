package visitors;

import javax.lang.model.element.ElementKind;
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
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;

import ast.ASTAuxiliarStorage;
import cache.DefinitionCache;
import database.DatabaseFachade;
import database.nodes.NodeCategory;
import database.nodes.NodeTypes;
import database.relations.RelationTypes;
import database.relations.TypeRelations;
import node_wrappers.NodeWrapper;
import typeInfo.TypeHierarchy;
import utils.JavacInfo;
import utils.types.CompoundTypeKey;
import utils.types.GenericTypeKey;
import utils.types.MethodTypeKey;
import utils.types.WildcardKey;

public class TypeVisitor implements javax.lang.model.type.TypeVisitor<NodeWrapper, Object> {
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
	public NodeWrapper visit(TypeMirror t, Object key) {
		// TODO Auto-generated method stub
		throw new IllegalStateException(t.getClass().toString());
	}

	@Override
	public NodeWrapper visitArray(ArrayType type, Object key) {
		NodeWrapper node = DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNodeExplicitCats(type,
				NodeTypes.ARRAY_TYPE, NodeCategory.TYPE_NODE);
		putInCache(key, node);
		node.createRelationshipTo(DefinitionCache.getOrCreateType(type.getComponentType(), ast),
				RelationTypes.TYPE_PER_ELEMENT);
		return node;
	}

	// private static final Set<Symbol> nonDeclaredTypeSymbols=new HashSet<>();
	@Override
	public NodeWrapper visitDeclared(DeclaredType t, Object key) {
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

			NodeWrapper genericType = DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNodeExplicitCats(t,
					NodeTypes.GENERIC_TYPE, NodeCategory.TYPE_NODE);
			ret = genericType;
			// putInCache(((GenericTypeKey) key).getParameterizedType(),
			// nonDeclaredTypeDec);
			putInCache(key, genericType);
			// ast.typeDecNodes.add(genericType);
			nonDeclaredTypeDec = DefinitionCache.getOrCreateType(JavacInfo.erasure(type),
					((GenericTypeKey) key).getParameterizedType(), ast);

			genericType.createRelationshipTo(nonDeclaredTypeDec, RelationTypes.PARAMETERIZED_TYPE);
			// System.out.println("TYPE ARGUMENTS");
			for (int i = 0; i < t.getTypeArguments().size(); i++)// CON EL FOR
																	// PODEMOS
																	// REUTILIZAR
																	// LAS KEYS
																	// DEL
																	// GENERIC
																	// TYPE
				genericType
						.createRelationshipTo(
								DefinitionCache.getOrCreateType(t.getTypeArguments().get(i),
										((GenericTypeKey) key).getTypeArgs().get(i), ast),
								RelationTypes.GENERIC_TYPE_ARGUMENT)
						.setProperty("argumentIndex", i + 1);
			;
		} else {

			// System.out.println("NON_GENERIC");
			// System.out.println(t);
			nonDeclaredTypeDec =
					// t instanceof ClassType
					// ?
					DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredCLASSTypeDecNode(((ClassType) t),
							type.isInterface() ? NodeTypes.INTERFACE_DEF
									: type.tsym.isEnum() ? NodeTypes.ENUM_DEF : NodeTypes.CLASS_DEF)
			// :
			// DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNode(t,
			// type.isInterface()
			// ? NodeTypes.INTERFACE_DEF : type.tsym.isEnum() ?
			// NodeTypes.ENUM_DEF : NodeTypes.CLASS_DEF)
			;
			putInCache(key, nonDeclaredTypeDec);
			ret = nonDeclaredTypeDec;
			ast.typeDecNodes.add(nonDeclaredTypeDec);

			// Solo añadimos dependencias de clases no declaradas cuando heredan
			// o
			// implementan de otra, para el futuro se podrían analizar los class
			// symbols de
			// las signaturas de los métodos o de los atributos, tendríamos que
			// hacer en este visit un Set<NodeWrapper> con los typeDecUses para
			// no
			// crear dos veces la misma relación, se podría reutilizar el método
			// estático de ASTTypesVisitor addToTypeDependencies
			TypeHierarchy.addTypeHierarchy((ClassSymbol) ((Type.ClassType) t).tsym, nonDeclaredTypeDec, null, ast);
//			System.out.println("FINISEHD TYPE HIER FOR " + t);
//			System.out.println("ENCLOSED ELEMENTS");
			((ClassSymbol) type.tsym).getEnclosedElements().forEach(e -> {
//				System.out.println("ELEMENT " + e);
//				System.out.println("ELEMENT " + e.getKind());
				if (e.getKind() != ElementKind.FIELD) {
					// System.out.println("TYPE" + e.type);
					try {
						if (!DefinitionCache.METHOD_TYPE_CACHE.containsKey(e)) {
//							System.out.println("AFTER CONTAINS KEY");
							if (e.getKind() == ElementKind.METHOD)
								ASTTypesVisitor.createNonDeclaredMethodDuringTypeCreation(nonDeclaredTypeDec,
										type.isInterface(), ast, (MethodSymbol) e);
							else if (e.getKind() == ElementKind.CONSTRUCTOR)
								ASTTypesVisitor.getNotDeclaredConstructorDuringTypeCreation(nonDeclaredTypeDec,
										(MethodSymbol) e);
						}
					} catch (com.sun.tools.javac.code.Symbol.CompletionFailure ex) {
						// System.out.println("CACHED RUNTIME");
						// e.printStackTrace();
						// throw new IllegalStateException(ex);
						System.err.println("Failed to analyze " + e.getKind() + " of " + t.toString()
								+ ", due to missing symbols:\n" + ex.toString() + "\n");
					}

				}
			});
		}

		// System.out.println("RETURNING declared");
		return ret;
	}

	@Override
	public NodeWrapper visitError(ErrorType t, Object key) {
		return putInCache(key,
				DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNode(t, NodeTypes.ERROR_TYPE));

	}

	@Override
	public NodeWrapper visitExecutable(ExecutableType t, Object key) {
		MethodTypeKey mtKey = (MethodTypeKey) key;

		NodeWrapper methodTypeNode = DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNode(t,
				NodeTypes.CALLABLE_TYPE);
		putInCache(key, methodTypeNode);

		methodTypeNode.createRelationshipTo(
				DefinitionCache.getOrCreateType(t.getReturnType(), mtKey.getReturnType(), ast),
				TypeRelations.RETURN_TYPE);
		int i = 0;
		for (Object pKey : mtKey.getParamTypes()) {
			methodTypeNode
					.createRelationshipTo(DefinitionCache.getOrCreateType(t.getParameterTypes().get(i), pKey, ast),
							TypeRelations.PARAM_TYPE)
					.setProperty("paramIndex", ++i);
		}
		// METER UN PUTO FOR PARA PODER REUTILIZAR LAS KEYS, POR FAVOR!!
		for (i = 0; i < t.getThrownTypes().size(); i++)
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
	public NodeWrapper visitIntersection(IntersectionType t, Object key) {
		NodeWrapper intersType = DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNodeExplicitCats(t,
				NodeTypes.INTERSECTION_TYPE, NodeCategory.TYPE_NODE);
		putInCache(key, intersType);
		// System.out.println("INTERS TYPE\t" + t);
		// System.out.println("KEY\t" + key);
		// System.out.println("CLASS\t" + key.getClass());

		for (int i = 0; i < t.getBounds().size(); i++)
			intersType.createRelationshipTo(DefinitionCache.getOrCreateType(t.getBounds().get(i),
					((CompoundTypeKey) key).getTypes().get(i), ast), RelationTypes.INTERSECTION_COMPOSED_OF);

		return intersType;
	}

	@Override
	public NodeWrapper visitNoType(NoType t, Object key) {
		return putInCache(key, DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNode(t,
				t.getKind() == TypeKind.VOID ? NodeTypes.VOID_TYPE : NodeTypes.PACKAGE_TYPE));

	}

	@Override
	public NodeWrapper visitNull(NullType t, Object key) {
		return putInCache(key, DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNode(t, NodeTypes.NULL_TYPE));
	}

	@Override
	public NodeWrapper visitPrimitive(PrimitiveType t, Object key) {
		return putInCache(key, DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNodeExplicitCats(t,
				NodeTypes.PRIMITIVE_TYPE, NodeCategory.TYPE_NODE));
	}

	@Override
	public NodeWrapper visitTypeVariable(TypeVariable t, Object key) {
		NodeWrapper typeVar = DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNode(t,
				NodeTypes.TYPE_VARIABLE);
		putInCache(key, typeVar);
		// System.out.println("typevrble");
		// System.out.println(t);
		// System.out.println(t.getUpperBound());
		// System.out.println(t.getLowerBound());

		typeVar.createRelationshipTo(
				DefinitionCache.getOrCreateType(
						t.getUpperBound() == null ? JavacInfo.getSymtab().objectType : t.getUpperBound(), ast),
				RelationTypes.UPPER_BOUND_TYPE);
		typeVar.createRelationshipTo(// QUITAR TSYM PARA TIPOS QUE NO TENEMOS
										// GARANTIA DE QUE SEAN DECLARED, la key
										// ya se calculará en el keyvisitor
				DefinitionCache.getOrCreateType(
						t.getLowerBound() == null ? JavacInfo.getSymtab().botType : t.getLowerBound(), ast),
				RelationTypes.LOWER_BOUND_TYPE);
		return typeVar;

	}

	@Override
	public NodeWrapper visitUnion(UnionType t, Object key) {
		NodeWrapper union = DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNodeExplicitCats(t,
				NodeTypes.UNION_TYPE, NodeCategory.TYPE_NODE);
		putInCache(key, union);
		for (int i = 0; i < t.getAlternatives().size(); i++)
			union.createRelationshipTo(DefinitionCache.getOrCreateType(t.getAlternatives().get(i),
					((CompoundTypeKey) key).getTypes().get(i), ast), RelationTypes.UNION_TYPE_ALTERNATIVE);

		return union;
	}

	@Override
	public NodeWrapper visitUnknown(TypeMirror t, Object key) {
		return putInCache(key,
				DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNode(t, NodeTypes.UNKNOWN_TYPE));
	}

	@Override
	public NodeWrapper visitWildcard(WildcardType t, Object key) {
		// System.out.println("WILCARD KIND " + t.getKind().toString());

		NodeWrapper wildcardNode = DatabaseFachade.CURRENT_DB_FACHADE.createNonDeclaredTypeDecNodeExplicitCats(t,
				NodeTypes.WILDCARD_TYPE, NodeCategory.TYPE_NODE);
		putInCache(key, wildcardNode);
		// wildcardNode.setProperty("typeBoundKind", t.getKind().toString());
		wildcardNode
				.createRelationshipTo(
						DefinitionCache.getOrCreateType(
								t.getExtendsBound() == null ? JavacInfo.getSymtab().objectType : t.getExtendsBound(),
								((WildcardKey) key).getExtendsBound(), ast),
						TypeRelations.WILDCARD_EXTENDS_BOUND);
		wildcardNode.createRelationshipTo(DefinitionCache.getOrCreateType(
				t.getSuperBound() == null ? JavacInfo.getSymtab().botType : t.getSuperBound(),
				((WildcardKey) key).getSuperBound(), ast), TypeRelations.WILDCARD_SUPER_BOUND);
		return wildcardNode;
	}

	private NodeWrapper putInCache(Object key, NodeWrapper typeNode) {
		DefinitionCache.CLASS_TYPE_CACHE.put(key, typeNode);
		return typeNode;
	}
}

package visitors;

import java.util.Map;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.management.relation.RelationType;

import org.neo4j.codegen.MethodDeclaration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import cache.nodes.DefinitionCache;
import cache.nodes.TreeToNodeCache;
import database.DatabaseFachade;
import relations.RelationTypes;
import scala.reflect.internal.Trees.GenericApply;
import sun.tools.tree.TypeExpression;
import typeInfo.TypeHierarchy;
import utils.JavacInfo;
import utils.Pair;

public class OldASTTypesVisitor extends TreePathScanner<Object, Pair<Pair<Tree, Node>, RelationTypes>> {
	private static final boolean DEBUG = false;
	private String fullNamePrecedent = null;
	private Node lastMethodDecVisited = null;

	@Override
	public Object visitCompilationUnit(CompilationUnitTree compilationUnitTree,
			Pair<Pair<Tree, Node>, RelationTypes> pair) {
		String nodeType = "ClassFile(CU)";
		// DEFAULT

		Node compilationUnitNode = null;
		Transaction tx = DatabaseFachade.beginTx();
		try {
			compilationUnitNode = DatabaseFachade.createSkeletonNode(compilationUnitTree, nodeType);
			String fileName = compilationUnitTree.getSourceFile().toUri().toString();
			compilationUnitNode.setProperty("fileName", fileName);
			Tree packageDec = compilationUnitTree.getPackageName();

			System.out.println(packageDec.getClass());
			Pair<Pair<Tree, Node>, RelationTypes> p = Pair
					.create(Pair.create((Tree) compilationUnitTree, compilationUnitNode), RelationTypes.CU_PACKAGE_DEC);
			this.fullNamePrecedent = packageDec.toString();
			scan(compilationUnitTree.getPackageAnnotations(), p);
			scan(packageDec, p);
			scan(compilationUnitTree.getImports(), p);
			scan(compilationUnitTree.getTypeDecls(), p);

			tx.success();
		} finally {
			tx.finish();
			// System.out.println("Visited CU and commited");
		}

		return null;
	}

	private void connectWithParent(Node child, Tree parent, RelationTypes r) {
		Node parentNode = TreeToNodeCache.getNode(parent);
		parentNode.createRelationshipTo(child, r);
	}

	private void connectWithParent(Node child, Node parent, RelationTypes r) {
		parent.createRelationshipTo(child, r);
	}

	private void connectWithParent(Node child, Pair<Pair<Tree, Node>, RelationTypes> pair) {
		pair.getFirst().getSecond().createRelationshipTo(child, pair.getSecond());
	}

	private void connectWithParent(Node child, Pair<Tree, Node> pair, RelationTypes r) {
		pair.getSecond().createRelationshipTo(child, r);
	}

	@Override
	public Object visitClass(ClassTree classTree, Pair<Pair<Tree, Node>, RelationTypes> pair) {
		String previusPrecedent = this.fullNamePrecedent;
		String simpleName = classTree.getSimpleName().toString();
		String fullyQualifiedType = this.fullNamePrecedent + "." + simpleName;
		this.fullNamePrecedent = fullyQualifiedType;

		Node classNode = null;
		// DE momento prescindimos de la cach, estudiarlo

		classNode = DatabaseFachade.createSkeletonNode(classTree, classTree.getKind().toString() + "_DEC");

		// Redundancia simple Name fullyqualifiedName
		classNode.setProperty("simpleName", simpleName);

		classNode.setProperty("fullyQualifiedName", fullyQualifiedType);

		connectWithParent(classNode, pair.getFirst(), RelationTypes.HAS_TYPE_DEC);

		DefinitionCache.CLASS_TYPE_CACHE.putDefinition(fullyQualifiedType.toString(), classNode);

		TypeHierarchy.visitClass(classTree, classNode);
		Pair<Tree, Node> treeNodePair = Pair.create(classTree, classNode);
		scan(classTree.getModifiers(), Pair.create(treeNodePair, RelationTypes.HAS_CLASS_MODIFIERS));
		scan(classTree.getTypeParameters(), Pair.create(treeNodePair, RelationTypes.HAS_CLASS_TYPEPARAMETERS));
		scan(classTree.getExtendsClause(), Pair.create(treeNodePair, RelationTypes.HAS_CLASS_EXTENDS));

		scan(classTree.getImplementsClause(), Pair.create(treeNodePair, RelationTypes.HAS_CLASS_IMPLEMENTS));
		scan(classTree.getMembers(), Pair.create(treeNodePair, RelationTypes.HAS_STATIC_INIT));

		this.fullNamePrecedent = previusPrecedent;
		return null;

	}

	@Override
	public Object visitImport(ImportTree importTree, Pair<Pair<Tree, Node>, RelationTypes> t) {

		Node importNode = DatabaseFachade.createSkeletonNode(importTree, "Import");
		importNode.setProperty("qualifiedIdentifier", importTree.getQualifiedIdentifier().toString());
		importNode.setProperty("isStatic", importTree.isStatic());

		connectWithParent(importNode, t.getFirst(), RelationTypes.IMPORTS);
		// Posteriormente relacionar classfile con classfile o con typedec....
		// En caso de imports así import a.b.* liada jejeje
		return null;
	}

	@Override
	public Object visitAnnotation(AnnotationTree annotationTree, Pair<Pair<Tree, Node>, RelationTypes> t) {

		Node annotationNode = DatabaseFachade.createSkeletonNode(annotationTree, "Annotation");
		connectWithParent(annotationNode, t.getFirst(), RelationTypes.HAS_ANNOTATIONS);

		Pair<Tree, Node> treeNodePair = Pair.create(annotationTree, annotationNode);

		scan(annotationTree.getAnnotationType(), Pair.create(treeNodePair, RelationTypes.HAS_ANNOTATIONS_TYPE));

		// TODO: order
		scan(annotationTree.getArguments(), Pair.create(treeNodePair, RelationTypes.HAS_ANNOTATIONS_ARGUMENTS));
		return null;
	}

	public Object visitMethod(MethodTree methodTree, Pair<Pair<Tree, Node>, RelationTypes> t) {

		Node previousMethodDec = lastMethodDecVisited;
		Node methodNode = DatabaseFachade.createSkeletonNode(methodTree, "Method_DEC");
		lastMethodDecVisited = methodNode;
		Pair<Tree, Node> treeNodePair = Pair.create(methodTree, methodNode);
		String fullyQualifiedName = fullNamePrecedent + ":" + methodTree.getName().toString();
		methodNode.setProperty("name", methodTree.getName().toString());
		methodNode.setProperty("fullyQualifiedName", fullyQualifiedName);
		methodNode.setProperty("isDeclared", true);
		// Me da igual si viene de una declaración de clases o de una clase
		// anónima, siempre será DECLARES_METHOD

		// Aqui metemos la definición en la cache, guardada por el nombre
		// KlassName":"methodName
		DefinitionCache.METHOD_TYPE_CACHE.putDefinition(
				fullyQualifiedName + ":" + JavacInfo.getTypeMirror(methodTree).toString().split("\\)")[0], methodNode);

		connectWithParent(methodNode, t.getFirst(), RelationTypes.DECLARES_METHOD);
		scan(methodTree.getModifiers(), Pair.create(treeNodePair, RelationTypes.HAS_METHODDECL_MODIFIERS));
		scan(methodTree.getReturnType(), Pair.create(treeNodePair, RelationTypes.HAS_METHODDECL_RETURNS));
		scan(methodTree.getTypeParameters(), Pair.create(treeNodePair, RelationTypes.HAS_METHODDECL_TYPEPARAMETERS));
		scan(methodTree.getParameters(), Pair.create(treeNodePair, RelationTypes.HAS_METHODDECL_PARAMETERS));
		scan(methodTree.getThrows(), Pair.create(treeNodePair, RelationTypes.HAS_METHODDECL_THROWS));
		scan(methodTree.getBody(), Pair.create(treeNodePair, RelationTypes.HAS_METHODDECL_BODY));

		lastMethodDecVisited = previousMethodDec;
		return null;

	}

	public Object visitBlock(BlockTree blockTree, Pair<Pair<Tree, Node>, RelationTypes> t) {

		Node blockNode = DatabaseFachade.createSkeletonNode(blockTree, "Block");
		Pair<Pair<Tree, Node>, RelationTypes> n = Pair.create(Pair.create(blockTree, blockNode),
				RelationTypes.ENCLOSES);
		blockNode.setProperty("isStatic", blockTree.isStatic());
		connectWithParent(blockNode, t);

		// TODO: need ordered label
		scan(blockTree.getStatements(), n);

		return null;

	}

	private TypeMirror attachType(Tree tree, Node node, TreePath path) {

		TypeMirror fullyQualifiedType = JavacInfo.getTypeMirror(tree, path);

		if (fullyQualifiedType != null) {
			node.setProperty("actualType", fullyQualifiedType.toString());

			TypeKind typeKind = fullyQualifiedType.getKind();
			if (typeKind != null)
				node.setProperty("typeKind", typeKind.toString());
		}
		return fullyQualifiedType;
	}

	private TypeMirror attachType(Tree tree, Node node) {
		TypeMirror fullyQualifiedType = JavacInfo.getTypeMirror(tree);

		if (fullyQualifiedType != null) {
			node.setProperty("actualType", fullyQualifiedType.toString());

			TypeKind typeKind = fullyQualifiedType.getKind();
			if (typeKind != null)
				node.setProperty("typeKind", typeKind.toString());
		}

		return fullyQualifiedType;
	}

	private void attachType(Node node, String actualType, String typeKind) {

		node.setProperty("actualType", actualType);
		node.setProperty("typeKind", typeKind);
	}

	// private void attachType(Node node, TypeMirror fullyQualifiedType) {
	// if (fullyQualifiedType != null) {
	// node.setProperty("actualType", fullyQualifiedType.toString());
	//
	// TypeKind typeKind = fullyQualifiedType.getKind();
	// if (typeKind != null)
	// node.setProperty("typeKind", typeKind.toString());
	// }
	// }

	@Override
	public Object visitMethodInvocation(MethodInvocationTree methodInvocationTree,
			Pair<Pair<Tree, Node>, RelationTypes> pair) {
		// Realiza dos veces la misma operación getPath?¿?¿?¿---UNA AQUI Y OTRA
		// EN ATTACHTYPE LOOOOL

		TreePath path = JavacInfo.getPath(methodInvocationTree);

		Node methodInvocationNode = DatabaseFachade.createSkeletonNode(methodInvocationTree, "MethodInvocation");
		attachType(methodInvocationTree, methodInvocationNode, path);
		connectWithParent(methodInvocationNode, pair);
		JCMethodInvocation inv = (JCMethodInvocation) methodInvocationTree;
		JCExpression meth = inv.meth;
		

		System.out.println("METHOD TYPE=" + meth.type);

		if (path.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
			// extract the identifier and receiver (methodSelectTree)
			String fullyQualifiedName = null;
			ExpressionTree methodSelectExpr = (JCExpression) methodInvocationTree.getMethodSelect();
			String methodName = null;
			Pair<Tree, Node> treeNodePair = Pair.create(methodInvocationTree, methodInvocationNode);
			pair = Pair.create(treeNodePair, RelationTypes.METHODINVOCATION_METHOD_SELECT);
			// Esto igual es omitible si retornan algo los hijos
			switch (methodSelectExpr.getKind()) {

			case MEMBER_SELECT:
				JCFieldAccess mst = (JCFieldAccess) methodSelectExpr;

				System.out.println("SYMBOLO MEMBERACCES:\t" + mst.sym);
				System.out.println("NAME MEMBERACCES:\t" + mst.name);

				TypeMirror callerClass = JavacInfo.getTypeMirror(mst.getExpression());
				methodName = mst.getIdentifier().toString();
				fullyQualifiedName = callerClass.toString() + ":" + methodName;

				// OJO No tenemos el tipo de la expresión, sino el de la
				// expresión a la que se aplica el acceso a campo
				visitMemberSelect(mst, pair);
				break;

			case IDENTIFIER:
				JCIdent mst2 = (JCIdent) methodSelectExpr;

				System.out.println("SYMBOLO IDENT:\t" + mst2.sym);
				System.out.println("NAME IDENT:\t" + mst2.name);
				methodName = mst2.name.toString();
				fullyQualifiedName = fullNamePrecedent + ":" + methodName;
				visitIdentifier(mst2, pair);
				break;

			// Ahora puede ser otra invocación que retorne una función noooo???
			default:
				throw new IllegalStateException("Invocation que viene de " + methodSelectExpr.getKind());
				// Aqui se debería llamar a scan pa asegurarse de coger tooooo

			}
			String expType = JavacInfo.getTypeMirror(methodSelectExpr).toString();
			String key = fullyQualifiedName + ":" + expType.split("\\)")[0];
			Node decNode;
			if (DefinitionCache.METHOD_TYPE_CACHE.containsKey(key))
				decNode = DefinitionCache.METHOD_TYPE_CACHE.get(key);
			else {
				// Se hacen muchas cosas y es posible que se visite la
				// declaración después
				decNode = DatabaseFachade.createNode();
				decNode.setProperty("nodeType", "Method_DEC");
				decNode.setProperty("isDeclared", false);
				decNode.setProperty("name", methodName);
				decNode.setProperty("fullyQualifiedName", fullyQualifiedName);

				decNode.setProperty("methodType", expType);
				// Aqui se pueden hacer nodos como el de declaracion, declara
				// params declara return throws???¿
				// De momento no, solo usamos el methodType

				DefinitionCache.METHOD_TYPE_CACHE.put(key, decNode);

			}

			lastMethodDecVisited.createRelationshipTo(decNode, RelationTypes.CALLS);

			scan(methodInvocationTree.getTypeArguments(),
					Pair.create(treeNodePair, RelationTypes.METHODINVOCATION_TYPE_ARGUMENTS));
			scan(methodInvocationTree.getArguments(),
					Pair.create(treeNodePair, RelationTypes.METHODINVOCATION_ARGUMENTS));

		} else

		{
			throw new IllegalStateException("A methodInv path leaf node is not a MethodInv");
		}
		return null;
	}

	@Override
	public Void visitMemberSelect(MemberSelectTree memberSelectTree, Pair<Pair<Tree, Node>, RelationTypes> t) {
		Node memberSelect = DatabaseFachade.createSkeletonNode(memberSelectTree);
		memberSelect.setProperty("memberName", memberSelectTree.getIdentifier().toString());

		attachType(memberSelectTree, memberSelect);
		connectWithParent(memberSelect, t);

		Pair<Tree, Node> treeNodePair = Pair.create(memberSelectTree, memberSelect);
		scan(memberSelectTree.getExpression(), Pair.create(treeNodePair, RelationTypes.MEMBER_SELECT_EXPR));

		return null;
	}

	// OJO FALTA REVISAR
	@Override
	public Void visitNewClass(NewClassTree newClassTree, Pair<Pair<Tree, Node>, RelationTypes> pair) {

		Node newClassNode = DatabaseFachade.createSkeletonNode(newClassTree, "NewClass");

		Pair<Tree, Node> treeNodePair = Pair.create(newClassTree, newClassNode);
		// Igual no hace falta el attachType porque la expresión siempre es del
		// tipo de la clase pero paquete + identifier no vale, igual se puede
		// hacer algo con el Symbol s y el getTypeMirror para obtener el nombre
		// completo---->Demomento lo dejo así

		// Aqui sabemos ya que el Kind es DECLARED, falta la cadena
		Symbol s = TreeInfo.symbol((JCTree) newClassTree.getIdentifier());
		if (DEBUG)
			System.out.println("**SYMBOL INFO:\t" + newClassTree.getIdentifier().toString() + " "
					+ newClassTree.getIdentifier().getKind() + " " + s);
		attachType(newClassNode, s.toString(), "DECLARED");
		connectWithParent(newClassNode, pair);
		// Aquí hay que encontrar la declaracion del constructor de la clase,
		// para relacionar el CALLS, y el ¿IS_CALLED?
		scan(newClassTree.getEnclosingExpression(),
				Pair.create(treeNodePair, RelationTypes.NEWCLASS_ENCLOSING_EXPRESSION));
		scan(newClassTree.getIdentifier(), Pair.create(treeNodePair, RelationTypes.NEWCLASS_IDENTIFIER));
		scan(newClassTree.getTypeArguments(), Pair.create(treeNodePair, RelationTypes.NEW_CLASS_TYPE_ARGUMENTS));
		scan(newClassTree.getArguments(), Pair.create(treeNodePair, RelationTypes.NEW_CLASS_ARGUMENTS));
		scan(newClassTree.getClassBody(), Pair.create(treeNodePair, RelationTypes.NEW_CLASS_BODY));

		System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree).constructorType);

		System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree).def);
		System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree).constructor);
		System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree).type);
		System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree));
		System.out.println(newClassNode.getProperty("lineNumber"));

		// String fullyQualifiedName = s + ":" + "<init>";
		if (DEBUG) {
			System.out.println(newClassTree.getEnclosingExpression());
			System.out.println(newClassTree.getIdentifier());
			System.out.println(newClassTree.getClassBody());
			System.out.println(newClassTree.getKind());
		}

		// Si no podemos sacar el methodType de la expresión como con las
		// invocaciones, tendremos que buscar entre los contructores de la clase
		return null;
	}
}

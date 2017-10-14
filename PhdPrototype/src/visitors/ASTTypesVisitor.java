package visitors;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.TypeMirror;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import cache.nodes.DefinitionCache;
import database.DatabaseFachade;
import relations.NodeTypes;
import relations.RelationTypes;
import typeInfo.TypeHierarchy;
import utils.GraphUtils;
import utils.JavacInfo;
import utils.Pair;

public class ASTTypesVisitor extends TreeScanner<Object, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object>> {
	private static final boolean DEBUG = false;
	private String fullNamePrecedent = null;
	private Node lastMethodDecVisited = null;
	private Tree typeDec;
	private boolean first;

	public ASTTypesVisitor(Tree typeDec, boolean first) {
		this.typeDec = typeDec;
		this.first = first;
	}

	private Node getConstructorDecNode(String fullyQualifiedName, String consName) {
		Node constructorDef;
		if (DefinitionCache.METHOD_TYPE_CACHE.containsKey(fullyQualifiedName))
			constructorDef = DefinitionCache.METHOD_TYPE_CACHE.get(fullyQualifiedName);// DefinitionCache.METHOD_TYPE_CACHE.
		else {
			// Se hacen muchas cosas y es posible que se visite la
			// declaración después
			constructorDef = DatabaseFachade.createNode();
			constructorDef.setProperty("nodeType", NodeTypes.CONSTRUCTOR_DEC);
			constructorDef.setProperty("isDeclared", false);
			constructorDef.setProperty("name", "<init>");
			constructorDef.setProperty("fullyQualifiedName", fullyQualifiedName);
			// Aqui se pueden hacer nodos como el de declaracion, declara
			// params declara return throws???¿
			// De momento no, solo usamos el methodType

			DefinitionCache.METHOD_TYPE_CACHE.put(fullyQualifiedName, constructorDef);

		}
		return constructorDef;
	}

	private Node getMethodDecNode(String fullyQualifiedName, String methodName) {
		Node decNode;
		if (DefinitionCache.METHOD_TYPE_CACHE.containsKey(fullyQualifiedName))
			decNode = DefinitionCache.METHOD_TYPE_CACHE.get(fullyQualifiedName);
		else {
			// Se hacen muchas cosas y es posible que se visite la
			// declaración después
			decNode = DatabaseFachade.createNode();
			decNode.setProperty("nodeType", NodeTypes.METHOD_DEC);

			decNode.setProperty("isDeclared", false);
			decNode.setProperty("name", methodName);
			decNode.setProperty("fullyQualifiedName", fullyQualifiedName);
			// Aqui se pueden hacer nodos como el de declaracion, declara
			// params declara return throws???¿
			// De momento no, solo usamos el methodType

			DefinitionCache.METHOD_TYPE_CACHE.put(fullyQualifiedName, decNode);

		}
		return decNode;
	}

	@Override
	public Void visitAnnotatedType(AnnotatedTypeTree annotatedTypeTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node annotatedTypeNode = DatabaseFachade.createSkeletonNode(annotatedTypeTree, NodeTypes.ANNOTATION_TYPE);
		GraphUtils.attachTypeDirect(annotatedTypeTree, annotatedTypeNode);
		GraphUtils.connectWithParent(annotatedTypeNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(annotatedTypeTree, annotatedTypeNode);

		scan(annotatedTypeTree.getAnnotations(), Pair.createPair(treeNodePair, RelationTypes.HAS_ANNOTATIONS));
		scan(annotatedTypeTree.getUnderlyingType(), Pair.createPair(treeNodePair, RelationTypes.UNDERLYING_TYPE));

		return null;
	}

	@Override
	public Object visitAnnotation(AnnotationTree annotationTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node annotationNode = DatabaseFachade.createSkeletonNode(annotationTree, NodeTypes.ANNOTATION);
		GraphUtils.connectWithParent(annotationNode, t, RelationTypes.HAS_ANNOTATIONS);

		Pair<Tree, Node> treeNodePair = Pair.create(annotationTree, annotationNode);

		scan(annotationTree.getAnnotationType(), Pair.createPair(treeNodePair, RelationTypes.HAS_ANNOTATIONS_TYPE));

		// TODO: order
		scan(annotationTree.getArguments(), Pair.createPair(treeNodePair, RelationTypes.HAS_ANNOTATIONS_ARGUMENTS));
		return null;
	}

	@Override
	public Void visitArrayAccess(ArrayAccessTree arrayAccessTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node arrayAccessNode = DatabaseFachade.createSkeletonNode(arrayAccessTree, NodeTypes.ARRAY_ACCESS);
		GraphUtils.attachTypeDirect(arrayAccessNode, arrayAccessTree);
		GraphUtils.connectWithParent(arrayAccessNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(arrayAccessTree, arrayAccessNode);
		scan(arrayAccessTree.getExpression(), Pair.createPair(treeNodePair, RelationTypes.ARRAYACCESS_EXPR));
		scan(arrayAccessTree.getIndex(), Pair.createPair(treeNodePair, RelationTypes.ARRAYACCESS_INDEX));
		return null;
	}

	@Override
	public Void visitArrayType(ArrayTypeTree arrayTypeTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node arrayTypeNode = DatabaseFachade.createSkeletonNode(arrayTypeTree, NodeTypes.ARRAY_TYPE);
		arrayTypeNode.setProperty("elementType", arrayTypeTree.getType().toString());
		GraphUtils.connectWithParent(arrayTypeNode, t);

		Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> n = Pair.createPair(arrayTypeTree, arrayTypeNode,
				RelationTypes.TYPE_PER_ELEMENT);
		scan(arrayTypeTree.getType(), n);

		return null;
	}

	@Override
	public Void visitAssert(AssertTree assertTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node assertNode = DatabaseFachade.createSkeletonNode(assertTree, NodeTypes.ASSERT_STATEMENT);
		// GraphUtils.attachTypeDirect(assertTree, assertNode);
		GraphUtils.connectWithParent(assertNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(assertTree, assertNode);
		scan(assertTree.getCondition(), Pair.createPair(treeNodePair, RelationTypes.ASSERT_CONDITION));
		scan(assertTree.getDetail(), Pair.createPair(treeNodePair, RelationTypes.ASSERT_DETAIL));
		return null;
	}

	@Override
	public Void visitAssignment(AssignmentTree assignmentTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node assignmentNode = DatabaseFachade.createSkeletonNode(assignmentTree, NodeTypes.ASSIGNMENT);
		GraphUtils.connectWithParent(assignmentNode, t);

		GraphUtils.attachTypeDirect(assignmentTree, assignmentNode);
		Pair<Tree, Node> treeNodePair = Pair.create(assignmentTree, assignmentNode);

		scan(assignmentTree.getVariable(), Pair.createPair(treeNodePair, RelationTypes.ASSIGNMENT_LHS));
		scan(assignmentTree.getExpression(), Pair.createPair(treeNodePair, RelationTypes.ASSIGNMENT_RHS));

		return null;

	}

	@Override
	public Void visitBinary(BinaryTree binaryTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node binaryNode = DatabaseFachade.createSkeletonNode(binaryTree, NodeTypes.BINARY_OPERATION);
		binaryNode.setProperty("operator", binaryTree.getKind().toString());
		GraphUtils.attachTypeDirect(binaryTree, binaryNode);
		GraphUtils.connectWithParent(binaryNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(binaryTree, binaryNode);
		scan(binaryTree.getLeftOperand(), Pair.createPair(treeNodePair, RelationTypes.BINOP_LHS));
		scan(binaryTree.getRightOperand(), Pair.createPair(treeNodePair, RelationTypes.BINOP_RHS));
		return null;
	}

	@Override
	public Object visitBlock(BlockTree blockTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		Node prevMethodDeclaration = null;
		Node blockNode = DatabaseFachade.createSkeletonNode(blockTree, NodeTypes.BLOCK);
		blockNode.setProperty("isStatic", blockTree.isStatic());
		boolean isStaticInit = t.getFirst().getSecond() == RelationTypes.HAS_STATIC_INIT;
		if (isStaticInit) {
			prevMethodDeclaration = lastMethodDecVisited;
			lastMethodDecVisited = blockNode;
		}

		GraphUtils.connectWithParent(blockNode, t);
		t = Pair.createPair(blockTree, blockNode, RelationTypes.ENCLOSES);
		scan(blockTree.getStatements(), t);

		if (isStaticInit)
			lastMethodDecVisited = prevMethodDeclaration;

		return null;

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
	public Void visitBreak(BreakTree breakTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node breakNode = DatabaseFachade.createSkeletonNode(breakTree, NodeTypes.BREAK_STATEMENT);
		if (breakTree.getLabel() != null)
			breakNode.setProperty("label", breakTree.getLabel().toString());
		GraphUtils.connectWithParent(breakNode, t);
		return null;
	}

	@Override
	public Void visitCase(CaseTree caseTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node caseNode = DatabaseFachade.createSkeletonNode(caseTree, NodeTypes.CASE_STATEMENT);
		GraphUtils.connectWithParent(caseNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(caseTree, caseNode);
		scan(caseTree.getExpression(), Pair.createPair(treeNodePair, RelationTypes.CASE_EXPR));
		scan(caseTree.getStatements(), Pair.createPair(treeNodePair, RelationTypes.CASE_STATEMENTS));
		return null;
	}

	@Override
	public Void visitCatch(CatchTree catchTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node catchNode = DatabaseFachade.createSkeletonNode(catchTree, NodeTypes.CATCH_BLOCK);
		GraphUtils.connectWithParent(catchNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(catchTree, catchNode);
		scan(catchTree.getParameter(), Pair.createPair(treeNodePair, RelationTypes.CATCH_PARAM));
		scan(catchTree.getBlock(), Pair.createPair(treeNodePair, RelationTypes.CATCH_BLOCK));
		return null;
	}

	@Override

	public Object visitClass(ClassTree classTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> pair) {

		System.out.println("Visitando calse " + classTree.getSimpleName());
		if (DEBUG)
			System.out.println("Miembros de la clase " + classTree.getSimpleName() + "(" + classTree.getClass() + ")");
		String previusPrecedent = this.fullNamePrecedent;
		String simpleName = classTree.getSimpleName().toString();
		String fullyQualifiedType = this.fullNamePrecedent + "." + simpleName;
		this.fullNamePrecedent = fullyQualifiedType;

		Node classNode = null;
		// DE momento prescindimos de la cach, estudiarlo
		Kind k = classTree.getKind();
		classNode = DatabaseFachade.createSkeletonNode(classTree, k == Kind.CLASS ? NodeTypes.CLASS_DECLARATION
				: k == Kind.INTERFACE ? NodeTypes.INTERFACE_DECLARATION : NodeTypes.ENUM_DECLARATION);

		// Redundancia simple Name fullyqualifiedName
		classNode.setProperty("simpleName", simpleName);

		classNode.setProperty("fullyQualifiedName", fullyQualifiedType);

		GraphUtils.connectWithParent(classNode, pair, RelationTypes.HAS_TYPE_DEC);

		DefinitionCache.CLASS_TYPE_CACHE.putDefinition(fullyQualifiedType.toString(), classNode);

		TypeHierarchy.visitClass(classTree, classNode);
		Pair<Tree, Node> treeNodePair = Pair.create(classTree, classNode);
		scan(classTree.getModifiers(), Pair.createPair(treeNodePair, RelationTypes.HAS_CLASS_MODIFIERS));
		scan(classTree.getTypeParameters(), Pair.createPair(treeNodePair, RelationTypes.HAS_CLASS_TYPEPARAMETERS));
		scan(classTree.getExtendsClause(), Pair.createPair(treeNodePair, RelationTypes.HAS_CLASS_EXTENDS));

		scan(classTree.getImplementsClause(), Pair.createPair(treeNodePair, RelationTypes.HAS_CLASS_IMPLEMENTS));

		List<Node> attrs = new ArrayList<Node>(), staticAttrs = new ArrayList<Node>(),
				constructors = new ArrayList<Node>();

		scan(classTree.getMembers(), Pair.createPair(treeNodePair, RelationTypes.HAS_STATIC_INIT,
				Pair.create(Pair.create(attrs, staticAttrs), constructors)));

		for (Node constructor : constructors)
			for (Node instanceAttrInit : attrs) {
				constructor.getSingleRelationship(RelationTypes.HAS_METHODDECL_BODY, Direction.OUTGOING).getEndNode()
						.createRelationshipTo(instanceAttrInit, RelationTypes.ENCLOSES);
				for (Relationship r : instanceAttrInit.getRelationships(RelationTypes.CALLS)) {
					constructor.createRelationshipTo(r.getEndNode(), RelationTypes.CALLS);
					r.delete();
				}
			}
		this.fullNamePrecedent = previusPrecedent;
		return null;

	}

	@Override
	public Object visitCompilationUnit(CompilationUnitTree compilationUnitTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> pair) {
		// DEFAULT

		String fileName = compilationUnitTree.getSourceFile().toUri().toString();
		Tree packageDec = compilationUnitTree.getPackageName();
		if (DEBUG)
			System.out.println(fileName);
		this.fullNamePrecedent = packageDec == null ? "" : packageDec.toString();

		if (first) {

			scan(compilationUnitTree.getPackageAnnotations(), pair);
			// scan(packageDec, p);
			scan(compilationUnitTree.getImports(), pair);
		} // scan(compilationUnitTree.getTypeDecls(), pair);
		scan(typeDec, pair);

		return null;
	}

	@Override
	public Void visitCompoundAssignment(CompoundAssignmentTree compoundAssignmentTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node assignmentNode = DatabaseFachade.createSkeletonNode(compoundAssignmentTree, NodeTypes.COMPOUND_ASSIGNMENT);
		assignmentNode.setProperty("operator", compoundAssignmentTree.getKind().toString());
		GraphUtils.connectWithParent(assignmentNode, t);

		GraphUtils.attachTypeDirect(compoundAssignmentTree, assignmentNode);

		Pair<Tree, Node> treeNodePair = Pair.create(compoundAssignmentTree, assignmentNode);

		scan(compoundAssignmentTree.getVariable(),
				Pair.createPair(treeNodePair, RelationTypes.COMPOUND_ASSIGNMENT_LHS));
		scan(compoundAssignmentTree.getExpression(),
				Pair.createPair(treeNodePair, RelationTypes.COMPOUND_ASSIGNMENT_RHS));
		return null;
	}

	@Override
	public Void visitConditionalExpression(ConditionalExpressionTree conditionalTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node conditionalNode = DatabaseFachade.createSkeletonNode(conditionalTree, NodeTypes.CONDITIONAL_EXPRESSION);
		GraphUtils.attachTypeDirect(conditionalTree, conditionalNode);
		GraphUtils.connectWithParent(conditionalNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(conditionalTree, conditionalNode);
		scan(conditionalTree.getCondition(), Pair.createPair(treeNodePair, RelationTypes.CONDITIONAL_CONDITION));
		scan(conditionalTree.getTrueExpression(), Pair.createPair(treeNodePair, RelationTypes.CONDITIONAL_THEN));
		scan(conditionalTree.getFalseExpression(), Pair.createPair(treeNodePair, RelationTypes.CONDITIONAL_ELSE));
		return null;
	}

	@Override
	public Void visitContinue(ContinueTree continueTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node continueNode = DatabaseFachade.createSkeletonNode(continueTree, NodeTypes.CONTINUE_STATEMENT);
		if (continueTree.getLabel() != null)
			continueNode.setProperty("label", continueTree.getLabel().toString());
		GraphUtils.connectWithParent(continueNode, t);
		return null;
	}

	@Override
	public Void visitDoWhileLoop(DoWhileLoopTree doWhileLoopTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node doWhileLoopNode = DatabaseFachade.createSkeletonNode(doWhileLoopTree);
		GraphUtils.connectWithParent(doWhileLoopNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(doWhileLoopTree, doWhileLoopNode);
		scan(doWhileLoopTree.getStatement(), Pair.createPair(treeNodePair, RelationTypes.ENCLOSES));
		scan(doWhileLoopTree.getCondition(), Pair.createPair(treeNodePair, RelationTypes.DOWHILE_CONDITION));
		return null;
	}

	@Override
	public Void visitEmptyStatement(EmptyStatementTree emptyStatementTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node emptyStatementNode = DatabaseFachade.createSkeletonNode(emptyStatementTree);
		GraphUtils.connectWithParent(emptyStatementNode, t);

		return null;
	}

	@Override
	public Void visitEnhancedForLoop(EnhancedForLoopTree enhancedForLoopTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node enhancedForLoopNode = DatabaseFachade.createSkeletonNode(enhancedForLoopTree, NodeTypes.ENHANCED_FOR);
		GraphUtils.connectWithParent(enhancedForLoopNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(enhancedForLoopTree, enhancedForLoopNode);

		scan(enhancedForLoopTree.getVariable(), Pair.createPair(treeNodePair, RelationTypes.FOREACH_VAR));
		scan(enhancedForLoopTree.getExpression(), Pair.createPair(treeNodePair, RelationTypes.FOREACH_EXPR));
		scan(enhancedForLoopTree.getStatement(), Pair.createPair(treeNodePair, RelationTypes.FOREACH_STATEMENT));

		return null;
	}

	@Override
	public Void visitErroneous(ErroneousTree erroneousTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		Node erroneousNode = DatabaseFachade.createSkeletonNode(erroneousTree, NodeTypes.ERRONEOUS_NODE);
		GraphUtils.attachTypeDirect(erroneousTree, erroneousNode);
		GraphUtils.connectWithParent(erroneousNode, t);
		scan(erroneousTree.getErrorTrees(),
				Pair.createPair(erroneousTree, erroneousNode, RelationTypes.ERRONEOUS_NODE_CAUSED_BY));
		return null;
	}

	@Override
	public Void visitExpressionStatement(ExpressionStatementTree expressionStatementTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node expressionStatementNode = DatabaseFachade.createSkeletonNode(expressionStatementTree);
		GraphUtils.connectWithParent(expressionStatementNode, t);

		scan(expressionStatementTree.getExpression(),
				Pair.createPair(expressionStatementTree, expressionStatementNode, RelationTypes.EXPR_ENCLOSES));

		return null;
	}

	@Override
	public Void visitForLoop(ForLoopTree forLoopTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node forLoopNode = DatabaseFachade.createSkeletonNode(forLoopTree);
		GraphUtils.connectWithParent(forLoopNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(forLoopTree, forLoopNode);

		scan(forLoopTree.getInitializer(), Pair.createPair(treeNodePair, RelationTypes.FORLOOP_INIT));
		scan(forLoopTree.getCondition(), Pair.createPair(treeNodePair, RelationTypes.FORLOOP_CONDITION));
		scan(forLoopTree.getUpdate(), Pair.createPair(treeNodePair, RelationTypes.FORLOOP_UPDATE));
		scan(forLoopTree.getStatement(), Pair.createPair(treeNodePair, RelationTypes.FORLOOP_STATEMENT));

		return null;
	}

	@Override
	public Void visitIdentifier(IdentifierTree identifierTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node identifierNode = DatabaseFachade.createSkeletonNode(identifierTree, NodeTypes.IDENTIFIER);
		identifierNode.setProperty("name", identifierTree.getName().toString());
		GraphUtils.attachTypeDirect(identifierNode, identifierTree);
		GraphUtils.connectWithParent(identifierNode, t);

		return null;
	}

	@Override
	public Void visitIf(IfTree ifTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node ifNode = DatabaseFachade.createSkeletonNode(ifTree, NodeTypes.IF_STATEMENT);
		GraphUtils.connectWithParent(ifNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(ifTree, ifNode);
		scan(ifTree.getCondition(), Pair.createPair(treeNodePair, RelationTypes.IF_CONDITION));
		scan(ifTree.getThenStatement(), Pair.createPair(treeNodePair, RelationTypes.IF_THEN));
		scan(ifTree.getElseStatement(), Pair.createPair(treeNodePair, RelationTypes.IF_ELSE));

		return null;
	}

	@Override
	public Object visitImport(ImportTree importTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node importNode = DatabaseFachade.createSkeletonNode(importTree, NodeTypes.IMPORT);
		importNode.setProperty("qualifiedIdentifier", importTree.getQualifiedIdentifier().toString());
		importNode.setProperty("isStatic", importTree.isStatic());

		GraphUtils.connectWithParent(importNode, t, RelationTypes.IMPORTS);
		// Posteriormente relacionar classfile con classfile o con typedec....
		// En caso de imports así import a.b.* liada jejeje
		return null;
	}

	@Override
	public Void visitInstanceOf(InstanceOfTree instanceOfTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node instanceOfNode = DatabaseFachade.createSkeletonNode(instanceOfTree, NodeTypes.INSTANCE_OF);
		GraphUtils.attachTypeDirect(instanceOfNode, "boolean", "BOOLEAN");
		GraphUtils.connectWithParent(instanceOfNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(instanceOfTree, instanceOfNode);
		scan(instanceOfTree.getExpression(), Pair.createPair(treeNodePair, RelationTypes.INSTANCEOF_EXPR));
		scan(instanceOfTree.getType(), Pair.createPair(treeNodePair, RelationTypes.INSTANCEOF_TYPE));

		return null;
	}

	@Override
	public Void visitIntersectionType(IntersectionTypeTree intersectionTypeTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		Node intersectionTypeNode = DatabaseFachade.createSkeletonNode(intersectionTypeTree,
				NodeTypes.INTERSECTION_TYPE);

		GraphUtils.connectWithParent(intersectionTypeNode, t);

		scan(intersectionTypeTree.getBounds(),
				Pair.createPair(intersectionTypeTree, intersectionTypeNode, RelationTypes.INTERSECTION_COMPOSED_BY));

		return null;
	}

	@Override
	public Object visitLabeledStatement(LabeledStatementTree labeledStatementTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node labeledStatementNode = DatabaseFachade.createSkeletonNode(labeledStatementTree,
				NodeTypes.LABELED_STATEMENT);
		labeledStatementNode.setProperty("name", labeledStatementTree.getLabel().toString());
		GraphUtils.connectWithParent(labeledStatementNode, t);

		return scan(labeledStatementTree.getStatement(),
				Pair.createPair(labeledStatementTree, labeledStatementNode, RelationTypes.LABELED_STATEMENT));
	}

	@Override
	public Void visitLambdaExpression(LambdaExpressionTree lambdaExpressionTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node lambdaExpressionNode = DatabaseFachade.createSkeletonNode(lambdaExpressionTree,
				NodeTypes.LAMBDA_EXPRESSION);
		lambdaExpressionNode.setProperty("bodyKind", lambdaExpressionTree.getBodyKind());
		GraphUtils.connectWithParent(lambdaExpressionNode, t);
		GraphUtils.attachTypeDirect(lambdaExpressionTree, lambdaExpressionNode);
		Pair<Tree, Node> treeNodePair = Pair.create(lambdaExpressionTree, lambdaExpressionNode);
		scan(lambdaExpressionTree.getBody(), Pair.createPair(treeNodePair, RelationTypes.LAMBDA_EXPRESSION_BODY));
		scan(lambdaExpressionTree.getParameters(),
				Pair.createPair(treeNodePair, RelationTypes.LAMBDA_EXPRESSION_PARAMETERS));

		return null;
	}

	@Override
	public Void visitLiteral(LiteralTree literalTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node literalNode = DatabaseFachade.createSkeletonNode(literalTree, NodeTypes.LITERAL);
		literalNode.setProperty("typetag", literalTree.getKind().toString());
		if (literalTree.getValue() != null)
			literalNode.setProperty("value", literalTree.getValue().toString());

		GraphUtils.attachTypeDirect(literalNode, literalTree);
		GraphUtils.connectWithParent(literalNode, t);

		return null;

	}

	@Override
	public Void visitMemberReference(MemberReferenceTree memberReferenceTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node memberReferenceNode = DatabaseFachade.createSkeletonNode(memberReferenceTree, NodeTypes.MEMBER_REFERENCE);
		memberReferenceNode.setProperty("mode", memberReferenceTree.getMode());
		memberReferenceNode.setProperty("name", memberReferenceTree.getName());
		GraphUtils.connectWithParent(memberReferenceNode, t);
		GraphUtils.attachTypeDirect(memberReferenceTree, memberReferenceNode);

		Pair<Tree, Node> treeNodePair = Pair.create(memberReferenceTree, memberReferenceNode);
		scan(memberReferenceTree.getQualifierExpression(),
				Pair.createPair(treeNodePair, RelationTypes.MEMBER_REFERENCE_EXPRESSION));
		scan(memberReferenceTree.getTypeArguments(),
				Pair.createPair(treeNodePair, RelationTypes.MEMBER_REFERENCE_TYPE_ARGUMENTS));

		return null;
	}

	@Override
	public Void visitMemberSelect(MemberSelectTree memberSelectTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		Node memberSelect = DatabaseFachade.createSkeletonNode(memberSelectTree, NodeTypes.MEMBER_ACCESS);
		memberSelect.setProperty("memberName", memberSelectTree.getIdentifier().toString());

		GraphUtils.attachTypeDirect(memberSelect, memberSelectTree);
		GraphUtils.connectWithParent(memberSelect, t);

		scan(memberSelectTree.getExpression(),
				Pair.createPair(memberSelectTree, memberSelect, RelationTypes.MEMBER_SELECT_EXPR));

		return null;
	}

	@Override
	public Object visitMethod(MethodTree methodTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		if (DEBUG)
			System.out.println("Visiting method declaration " + methodTree.getName());
		Node previousMethodDec = lastMethodDecVisited;
		String name = methodTree.getName().toString();
		Node methodNode = DatabaseFachade.createSkeletonNode(methodTree,
				name.contentEquals("<init>") ? NodeTypes.CONSTRUCTOR_DEC : NodeTypes.METHOD_DEC);
		lastMethodDecVisited = methodNode;
		Pair<Tree, Node> treeNodePair = Pair.create(methodTree, methodNode);

		TypeMirror type = JavacInfo.getTypeDirect(methodTree);
		String fullyQualifiedName = "UNKNOWN";
		if (type == null)
			System.out.println("No hay información del tipo del método (directa):" + methodTree.toString());
		else
			fullyQualifiedName = fullNamePrecedent + ":" + methodTree.getName().toString() + ":" + type.toString();
		methodNode.setProperty("name", name);
		methodNode.setProperty("fullyQualifiedName", fullyQualifiedName);
		methodNode.setProperty("isDeclared", true);
		// Me da igual si viene de una declaración de clases o de una clase
		// anónima, siempre será DECLARES_METHOD

		// Aqui metemos la definición en la cache, guardada por el nombre
		// KlassName":"methodName
		if (DEBUG) {
			System.out.println("METHOD DECLARATION :" + methodTree.getClass());

			System.out.println("Symbol:" + ((JCMethodDecl) methodTree).sym.toString());
		}
		DefinitionCache.METHOD_TYPE_CACHE.putDefinition(fullyQualifiedName, methodNode);
		GraphUtils.connectWithParent(methodNode, t, RelationTypes.DECLARES_METHOD);
		scan(methodTree.getModifiers(), Pair.createPair(treeNodePair, RelationTypes.HAS_METHODDECL_MODIFIERS));
		scan(methodTree.getReturnType(), Pair.createPair(treeNodePair, RelationTypes.HAS_METHODDECL_RETURNS));
		scan(methodTree.getTypeParameters(),
				Pair.createPair(treeNodePair, RelationTypes.HAS_METHODDECL_TYPEPARAMETERS));
		scan(methodTree.getParameters(), Pair.createPair(treeNodePair, RelationTypes.HAS_METHODDECL_PARAMETERS));
		scan(methodTree.getThrows(), Pair.createPair(treeNodePair, RelationTypes.HAS_METHODDECL_THROWS));
		scan(methodTree.getBody(), Pair.createPair(treeNodePair, RelationTypes.HAS_METHODDECL_BODY));
		lastMethodDecVisited = previousMethodDec;
		return null;

	}

	@Override
	public Object visitMethodInvocation(MethodInvocationTree methodInvocationTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> pair) {
		// Realiza dos veces la misma operación getPath?¿?¿?¿---UNA AQUI Y OTRA
		// EN ATTACHTYPE LOOOOL

		TreePath path = JavacInfo.getPath(methodInvocationTree);
		Node methodInvocationNode = DatabaseFachade.createSkeletonNode(methodInvocationTree,
				NodeTypes.METHOD_INVOCATION);

		// Habrá que escanear los hijos antes de sacar el tipo del padre
		// directamente no con TypeMirror????---->pos No
		GraphUtils.attachTypeDirect(methodInvocationNode, methodInvocationTree);
		GraphUtils.connectWithParent(methodInvocationNode, pair);

		if (path.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
			// extract the identifier and receiver (methodSelectTree)
			String fullyQualifiedName = null, methodName = null;
			ExpressionTree methodSelectExpr = (JCExpression) methodInvocationTree.getMethodSelect();
			Pair<Tree, Node> treeNodePair = Pair.create(methodInvocationTree, methodInvocationNode);
			pair = Pair.createPair(treeNodePair, RelationTypes.METHODINVOCATION_METHOD_SELECT);
			// Esto igual es omitible si retornan algo los hijos
			boolean isCons = false;
			switch (methodSelectExpr.getKind()) {

			case MEMBER_SELECT:
				JCFieldAccess mst = (JCFieldAccess) methodSelectExpr;
				String methodType = mst.type == null ? "UNKNOWN" : mst.type.toString();

				methodName = mst.sym == null ? "UNKNOWN" : mst.sym.toString();
				TypeMirror type = JavacInfo.getTypeDirect(mst.getExpression());
				fullyQualifiedName = type == null ? "UNKNOWN" : type.toString() + ":" + methodName;
				if (methodType.contentEquals("UNKNOWN") || methodName.contentEquals("UNKNOWN")
						|| fullyQualifiedName.contentEquals("UNKNOWN"))
					System.out.println("Falta información para : " + methodSelectExpr.toString());
				// OJO No tenemos el tipo de la expresión, sino el de la
				// expresión a la que se aplica el acceso a campo
				visitMemberSelect(mst, pair);
				break;

			case IDENTIFIER:
				JCIdent mst2 = (JCIdent) methodSelectExpr;
				methodType = "UNKNOWN()";
				if (mst2.type == null)
					System.out.println("IDENTIFIER WITHOUT TYPE:" + mst2.toString());
				else
					methodType = mst2.type.toString();
				// Esta hay que identificarla con CONS_DEC
				if (isCons = mst2.getName().toString().contentEquals("super") && mst2.sym != null) {
					methodName = mst2.sym.toString();
					fullyQualifiedName = methodName.split("\\(")[0] + ":<init>:" + methodType;
					if (DEBUG) {
						System.out.println(mst2.type.toString());
						System.out.println(mst2.getClass().toString());
					}
				} else {
					fullyQualifiedName = fullNamePrecedent + ":" + mst2.getName().toString() + ":" + methodType;
					methodName = "<init>:(" + methodType.split("\\(")[1];
				}
				if (DEBUG)
					System.out.println(fullyQualifiedName);
				visitIdentifier(mst2, pair);

				break;

			// Ahora puede ser otra invocación que retorne una función noooo???
			default:
				throw new IllegalStateException("Invocation que viene de " + methodSelectExpr.getKind());
				// Aqui se debería llamar a scan pa asegurarse de coger tooooo

			}
			Node decNode = isCons ? getConstructorDecNode(fullyQualifiedName, methodName)
					: getMethodDecNode(fullyQualifiedName, methodName);
			// if (lastMethodDecVisited != null)
			lastMethodDecVisited.createRelationshipTo(methodInvocationNode, RelationTypes.CALLS);
			methodInvocationNode.createRelationshipTo(decNode, RelationTypes.HAS_DEC);
			scan(methodInvocationTree.getTypeArguments(),
					Pair.createPair(treeNodePair, RelationTypes.METHODINVOCATION_TYPE_ARGUMENTS));
			scan(methodInvocationTree.getArguments(),
					Pair.createPair(treeNodePair, RelationTypes.METHODINVOCATION_ARGUMENTS));

		} else

		{
			throw new IllegalStateException("A methodInv path leaf node is not a MethodInv");
		}
		return null;
	}

	@Override
	public Void visitModifiers(ModifiersTree modifiersTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node modifiersNode = DatabaseFachade.createSkeletonNode(modifiersTree, NodeTypes.MODIFIERS);
		modifiersNode.setProperty("flags", modifiersTree.getFlags().toString());
		GraphUtils.connectWithParent(modifiersNode, t);

		Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> n = Pair.createPair(modifiersTree, modifiersNode,
				RelationTypes.ENCLOSES);
		scan(modifiersTree.getAnnotations(), n);

		return null;
	}

	@Override
	public Void visitNewArray(NewArrayTree newArrayTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node newArrayNode = DatabaseFachade.createSkeletonNode(newArrayTree, NodeTypes.NEW_ARRAY);
		GraphUtils.attachTypeDirect(newArrayTree, newArrayNode);
		GraphUtils.connectWithParent(newArrayNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(newArrayTree, newArrayNode);
		scan(newArrayTree.getType(), Pair.createPair(treeNodePair, RelationTypes.NEWARRAY_TYPE));
		scan(newArrayTree.getDimensions(), Pair.createPair(treeNodePair, RelationTypes.NEWARRAY_DIMENSION));

		scan(newArrayTree.getInitializers(), Pair.createPair(treeNodePair, RelationTypes.NEWARRAY_INIT));
		return null;
	}

	// OJO FALTA REVISAR
	@Override
	public Void visitNewClass(NewClassTree newClassTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> pair) {

		Node newClassNode = DatabaseFachade.createSkeletonNode(newClassTree, NodeTypes.NEW_INSTANCE);

		Pair<Tree, Node> treeNodePair = Pair.create(newClassTree, newClassNode);
		// Igual no hace falta el attachType porque la expresión siempre es del
		// tipo de la clase pero paquete + identifier no vale, igual se puede
		// hacer algo con el Symbol s y el getTypeMirror para obtener el nombre
		// completo---->Demomento lo dejo así

		// Aqui sabemos ya que el Kind es DECLARED, falta la cadena
		GraphUtils.attachTypeDirect(newClassNode, newClassTree);
		GraphUtils.connectWithParent(newClassNode, pair);
		// Aquí hay que encontrar la declaracion del constructor de la clase,
		// para relacionar el CALLS, y el ¿IS_CALLED?
		scan(newClassTree.getEnclosingExpression(),
				Pair.createPair(treeNodePair, RelationTypes.NEWCLASS_ENCLOSING_EXPRESSION));
		scan(newClassTree.getIdentifier(), Pair.createPair(treeNodePair, RelationTypes.NEWCLASS_IDENTIFIER));
		scan(newClassTree.getTypeArguments(), Pair.createPair(treeNodePair, RelationTypes.NEW_CLASS_TYPE_ARGUMENTS));
		scan(newClassTree.getArguments(), Pair.createPair(treeNodePair, RelationTypes.NEW_CLASS_ARGUMENTS));
		scan(newClassTree.getClassBody(), Pair.createPair(treeNodePair, RelationTypes.NEW_CLASS_BODY));

		if ((((JCNewClass) newClassTree).constructorType) == null)
			System.out.println("NO CONS TYPE: " + newClassTree.toString());
		else {
			String constName = "<init>:" + (((JCNewClass) newClassTree).constructorType).toString();
			String fullyQualifiedName = newClassTree.getIdentifier().toString() + constName;
			Node constructorDef = getConstructorDecNode(fullyQualifiedName, constName);
			newClassNode.createRelationshipTo(constructorDef, RelationTypes.HAS_DEC);
		} // if (lastMethodDecVisited != null)
		lastMethodDecVisited.createRelationshipTo(newClassNode, RelationTypes.CALLS);

		if (DEBUG) {
			System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree).constructorType);

			System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree).def);
			System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree).constructor);
			System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree).type);
			System.out.println("CONSTRUCTOR TYPE=" + ((JCNewClass) newClassTree));
			System.out.println(newClassNode.getProperty("lineNumber"));
			// String fullyQualifiedName = s + ":" + "<init>";
			System.out.println(newClassTree.getEnclosingExpression());
			System.out.println(newClassTree.getIdentifier());
			System.out.println(newClassTree.getClassBody());
			System.out.println(newClassTree.getKind());
		}

		// Si no podemos sacar el methodType de la expresión como con las
		// invocaciones, tendremos que buscar entre los contructores de la clase
		return null;
	}

	@Override
	public Void visitOther(Tree arg0, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> arg1) {
		throw new IllegalArgumentException(
				"[EXCEPTION] Tree not included in the visitor: " + arg0.getClass() + "\n" + arg0);
	}

	@Override
	public Void visitParameterizedType(ParameterizedTypeTree parameterizedTypeTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node parameterizedNode = DatabaseFachade.createSkeletonNode(parameterizedTypeTree, NodeTypes.PARAMETRIZED_TYPE);
		GraphUtils.connectWithParent(parameterizedNode, t);

		scan(parameterizedTypeTree.getType(),
				Pair.createPair(parameterizedTypeTree, parameterizedNode, RelationTypes.PARAMETERIZEDTYPE_TYPE));

		scan(parameterizedTypeTree.getTypeArguments(), Pair.createPair(parameterizedTypeTree, parameterizedNode,
				RelationTypes.PARAMETERIZEDTYPE_TYPEARGUMENTS));

		return null;
	}

	@Override
	public Void visitParenthesized(ParenthesizedTree parenthesizedTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		// Esto no debería entrar por aquí, porque los paréntesis no deben
		// preservarse, o sí, y soy yo el que los tiene que obviar //Si los dejo
		// puedo detectar fallos de cuando sobran paréntesis, no sé si sale
		// rentable

		Node parenthesizedNode = DatabaseFachade.createSkeletonNode(parenthesizedTree,
				NodeTypes.PARENTHESIZED_EXPRESSION);
		GraphUtils.attachTypeDirect(parenthesizedNode, parenthesizedTree);
		GraphUtils.connectWithParent(parenthesizedNode, t);
		scan(parenthesizedTree.getExpression(),
				Pair.createPair(parenthesizedTree, parenthesizedNode, RelationTypes.EXPR_ENCLOSES));
		return null;
	}

	@Override
	public Void visitPrimitiveType(PrimitiveTypeTree primitiveTypeTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node primitiveTypeNode = DatabaseFachade.createSkeletonNode(primitiveTypeTree, NodeTypes.PRIMITIVE_TYPE);
		primitiveTypeNode.setProperty("primitiveTypeKind", primitiveTypeTree.getPrimitiveTypeKind().toString());
		GraphUtils.connectWithParent(primitiveTypeNode, t);
		return null;
	}

	@Override
	public Void visitReturn(ReturnTree returnTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node returnNode = DatabaseFachade.createSkeletonNode(returnTree, NodeTypes.RETURN_STATEMENT);

		GraphUtils.connectWithParent(returnNode, t);

		scan(returnTree.getExpression(), Pair.createPair(returnTree, returnNode, RelationTypes.RETURN_EXPR));

		return null;
	}

	@Override
	public Void visitSwitch(SwitchTree switchTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node switchNode = DatabaseFachade.createSkeletonNode(switchTree, NodeTypes.SWITCH_STATEMENT);
		GraphUtils.connectWithParent(switchNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(switchTree, switchNode);

		scan(switchTree.getExpression(), Pair.createPair(treeNodePair, RelationTypes.SWITCH_EXPR));
		scan(switchTree.getCases(), Pair.createPair(treeNodePair, RelationTypes.SWITCH_ENCLOSES_CASES));
		return null;
	}

	@Override
	public Void visitSynchronized(SynchronizedTree synchronizedTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node synchronizedNode = DatabaseFachade.createSkeletonNode(synchronizedTree, NodeTypes.SYNCHRONIZED_BLOCK);
		GraphUtils.connectWithParent(synchronizedNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(synchronizedTree, synchronizedNode);
		scan(synchronizedTree.getExpression(), Pair.createPair(treeNodePair, RelationTypes.SYNCHRONIZED_EXPR));
		scan(synchronizedTree.getBlock(), Pair.createPair(treeNodePair, RelationTypes.SYNCHRONIZED_BLOCK));
		return null;
	}

	@Override
	public Void visitThrow(ThrowTree throwTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node throwNode = DatabaseFachade.createSkeletonNode(throwTree, NodeTypes.THROW_STATEMENT);
		GraphUtils.connectWithParent(throwNode, t);

		scan(throwTree.getExpression(), Pair.createPair(throwTree, throwNode, RelationTypes.THROW_EXPR));
		return null;
	}

	@Override
	public Void visitTry(TryTree tryTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node tryNode = DatabaseFachade.createSkeletonNode(tryTree, NodeTypes.TRY_BLOCK);
		GraphUtils.connectWithParent(tryNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(tryTree, tryNode);
		scan(tryTree.getResources(), Pair.createPair(treeNodePair, RelationTypes.TRY_RESOURCES));
		scan(tryTree.getBlock(), Pair.createPair(treeNodePair, RelationTypes.TRY_BLOCK));
		scan(tryTree.getCatches(), Pair.createPair(treeNodePair, RelationTypes.TRY_CATCH));
		scan(tryTree.getFinallyBlock(), Pair.createPair(treeNodePair, RelationTypes.TRY_FINALLY));
		return null;
	}

	@Override
	public Void visitTypeCast(TypeCastTree typeCastTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node typeCastNode = DatabaseFachade.createSkeletonNode(typeCastTree, NodeTypes.TYPE_CAST);
		GraphUtils.attachTypeDirect(typeCastTree, typeCastNode);
		GraphUtils.connectWithParent(typeCastNode, t);

		Pair<Tree, Node> treeNodePair = Pair.create(typeCastTree, typeCastNode);
		scan(typeCastTree.getType(), Pair.createPair(treeNodePair, RelationTypes.CAST_TYPE));
		scan(typeCastTree.getExpression(), Pair.createPair(treeNodePair, RelationTypes.CAST_ENCLOSES));
		return null;
	}

	@Override
	public Void visitTypeParameter(TypeParameterTree typeParameterTree,
			Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node typeParameterNode = DatabaseFachade.createSkeletonNode(typeParameterTree, NodeTypes.TYPE_PARAM);
		typeParameterNode.setProperty("name", typeParameterTree.getName().toString());
		GraphUtils.connectWithParent(typeParameterNode, t);

		scan(typeParameterTree.getBounds(),
				Pair.createPair(typeParameterTree, typeParameterNode, RelationTypes.TYPEPARAMETER_EXTENDS));
		return null;
	}

	@Override
	public Void visitUnary(UnaryTree unaryTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		Node unaryNode = DatabaseFachade.createSkeletonNode(unaryTree, NodeTypes.UNARY_OPERATION);
		GraphUtils.attachTypeDirect(unaryTree, unaryNode);
		unaryNode.setProperty("operator", unaryTree.getKind().toString());
		GraphUtils.connectWithParent(unaryNode, t);

		scan(unaryTree.getExpression(), Pair.createPair(unaryTree, unaryNode, RelationTypes.UNARY_ENCLOSES));

		return null;
	}

	@Override
	public Void visitUnionType(UnionTypeTree unionTypeTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		Node unionTypeNode = DatabaseFachade.createSkeletonNode(unionTypeTree, NodeTypes.UNION_TYPE);
		GraphUtils.connectWithParent(unionTypeNode, t);

		scan(unionTypeTree.getTypeAlternatives(), Pair.createPair(unionTypeTree, unionTypeNode, RelationTypes.UNION));

		return null;

	}

	@Override
	public Object visitVariable(VariableTree variableTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {
		boolean isAttr = t.getFirst().getSecond().equals(RelationTypes.HAS_STATIC_INIT);
		boolean isParam = !isAttr && t.getFirst().getSecond().equals(RelationTypes.HAS_METHODDECL_PARAMETERS);
		// This can be calculated cehcking if the param Object is null or not?
		Node variableNode = DatabaseFachade.createSkeletonNode(variableTree, 
				isAttr ? NodeTypes.ATTR_DEC : isParam ? NodeTypes.PARAMETER_DEC : NodeTypes.VAR_DEC);
		variableNode.setProperty("name", variableTree.getName().toString());
		GraphUtils.attachTypeDirect(variableNode, variableTree);

		if (DEBUG)
			System.out
					.println("VARIABLE:" + variableTree.getName() + "(" + variableNode.getProperty("actualType") + ")");
		@SuppressWarnings("unchecked")
		Pair<Pair<List<Node>, List<Node>>, List<Node>> param = (Pair<Pair<List<Node>, List<Node>>, List<Node>>) t
				.getSecond();
		Pair<Tree, Node> treeNodePair = Pair.create(variableTree, variableNode);

		if (isAttr) {
			// Warning, lineNumber and position should be added depending on the
			// constructor
			GraphUtils.connectWithParent(variableNode, t, RelationTypes.DECLARES_FIELD);
			Node previousNode = lastMethodDecVisited;

			lastMethodDecVisited = DatabaseFachade.createNode("IMPLICIT_INITIALIZATION");
			lastMethodDecVisited.createRelationshipTo(variableNode, RelationTypes.ITS_ATTR_DEC_IS);
			(variableTree.getModifiers().getFlags().toString().contains("static") ? param.getFirst().getSecond()
					: param.getFirst().getFirst()).add(lastMethodDecVisited);
			scan(variableTree.getInitializer(), Pair.createPair(treeNodePair, RelationTypes.HAS_VARIABLEDECL_INIT));

			lastMethodDecVisited = previousNode;

		} else {
			GraphUtils.connectWithParent(variableNode, t);
			scan(variableTree.getInitializer(), Pair.createPair(treeNodePair, RelationTypes.HAS_VARIABLEDECL_INIT));
		}
		scan(variableTree.getModifiers(), Pair.createPair(treeNodePair, RelationTypes.HAS_VARIABLEDECL_MODIFIERS));
		scan(variableTree.getType(), Pair.createPair(treeNodePair, RelationTypes.HAS_VARIABLEDECL_TYPE));

		return null;
	}

	@Override
	public Void visitWhileLoop(WhileLoopTree whileLoopTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node whileLoopNode = DatabaseFachade.createSkeletonNode(whileLoopTree);
		GraphUtils.connectWithParent(whileLoopNode, t);
		Pair<Tree, Node> treeNodePair = Pair.create(whileLoopTree, whileLoopNode);

		scan(whileLoopTree.getCondition(), Pair.createPair(treeNodePair, RelationTypes.WHILE_CONDITION));
		scan(whileLoopTree.getStatement(), Pair.createPair(treeNodePair, RelationTypes.ENCLOSES));
		return null;
	}

	@Override
	public Void visitWildcard(WildcardTree wildcardTree, Pair<Pair<Pair<Tree, Node>, RelationTypes>, Object> t) {

		Node wildcardNode = DatabaseFachade.createSkeletonNode(wildcardTree, NodeTypes.WILDCARD);
		wildcardNode.setProperty("typeBoundKind", wildcardTree.getKind().toString());
		GraphUtils.connectWithParent(wildcardNode, t);

		scan(wildcardTree.getBound(), Pair.createPair(wildcardTree, wildcardNode, RelationTypes.WILDCARD_BOUND));
		return null;
	}

}

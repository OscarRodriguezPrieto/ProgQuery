package visitors;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.TypeMirror;

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
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;

import ast.ASTAuxiliarStorage;
import cache.DefinitionCache;
import database.DatabaseFachade;
import database.nodes.NodeTypes;
import database.relations.PartialRelation;
import database.relations.PartialRelationWithProperties;
import database.relations.RelationTypes;
import typeInfo.TypeHierarchy;
import utils.GraphUtils;
import utils.JavacInfo;
import utils.Pair;

public class ASTTypesVisitor extends TreeScanner<Node, Pair<PartialRelation<RelationTypes>, Object>> {
	private static final boolean DEBUG = false;
	private String fullNamePrecedent = null;
	private Node lastStaticConsVisited = null;
	private Tree typeDec;
	private boolean first;
	private PDGVisitor pdgUtils;
	private ASTAuxiliarStorage ast;

	private List<String> currentMethodInvocations = new ArrayList<String>();

	// Must-May superficial analysis
	private boolean must = true;
	private boolean anyBreak;

	public ASTTypesVisitor(Tree typeDec, boolean first, PDGVisitor pdgUtils, ASTAuxiliarStorage ast) {
		this.typeDec = typeDec;
		this.first = first;
		this.pdgUtils = pdgUtils;
		this.ast = ast;
	}

	private Node addInvocationInStatement(Node statement) {
		ast.addInvocationInStatement(statement, currentMethodInvocations);
		currentMethodInvocations = new ArrayList<String>();
		return statement;
	}

	private Node getConstructorDecNode(Symbol symbol, String fullyQualifiedName) {
		Node constructorDef;
		if (DefinitionCache.METHOD_TYPE_CACHE.containsKey(symbol))
			constructorDef = DefinitionCache.METHOD_TYPE_CACHE.get(symbol);// DefinitionCache.METHOD_TYPE_CACHE.
		else {
			// Se hacen muchas cosas y es posible que se visite la
			// declaración después
			constructorDef = DatabaseFachade.createNode();
			constructorDef.setProperty("nodeType", NodeTypes.CONSTRUCTOR_DEC.toString());
			constructorDef.setProperty("isDeclared", false);
			constructorDef.setProperty("name", "<init>");
			constructorDef.setProperty("fullyQualifiedName", fullyQualifiedName);
			// Aqui se pueden hacer nodos como el de declaracion, declara
			// params declara return throws???¿
			// De momento no, solo usamos el methodType

			DefinitionCache.METHOD_TYPE_CACHE.put(symbol, constructorDef);

		}

		return constructorDef;
	}

	private Node getMethodDecNode(Symbol symbol, String fullyQualifiedName, String methodName) {
		Node decNode;
		if (DefinitionCache.METHOD_TYPE_CACHE.containsKey(symbol))
			decNode = DefinitionCache.METHOD_TYPE_CACHE.get(symbol);
		else {
			// Se hacen muchas cosas y es posible que se visite la
			// declaración después
			decNode = DatabaseFachade.createNode();
			decNode.setProperty("nodeType", NodeTypes.METHOD_DEC.toString());

			decNode.setProperty("isDeclared", false);
			decNode.setProperty("name", methodName);
			decNode.setProperty("fullyQualifiedName", fullyQualifiedName);
			// Aqui se pueden hacer nodos como el de declaracion, declara
			// params declara return throws???¿
			// De momento no, solo usamos el methodType

			DefinitionCache.METHOD_TYPE_CACHE.put(symbol, decNode);

		}
		return decNode;
	}

	@Override
	public Node visitAnnotatedType(AnnotatedTypeTree annotatedTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node annotatedTypeNode = DatabaseFachade.createSkeletonNode(annotatedTypeTree, NodeTypes.ANNOTATION_TYPE);
		GraphUtils.attachTypeDirect(annotatedTypeTree, annotatedTypeNode);
		GraphUtils.connectWithParent(annotatedTypeNode, t);

		scan(annotatedTypeTree.getAnnotations(), Pair.createPair(annotatedTypeNode, RelationTypes.HAS_ANNOTATIONS));
		scan(annotatedTypeTree.getUnderlyingType(), Pair.createPair(annotatedTypeNode, RelationTypes.UNDERLYING_TYPE));

		return null;
	}

	@Override
	public Node visitAnnotation(AnnotationTree annotationTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node annotationNode = DatabaseFachade.createSkeletonNode(annotationTree, NodeTypes.ANNOTATION);
		GraphUtils.connectWithParent(annotationNode, t, RelationTypes.HAS_ANNOTATIONS);

		scan(annotationTree.getAnnotationType(), Pair.createPair(annotationNode, RelationTypes.HAS_ANNOTATIONS_TYPE));

		// TODO: order
		scan(annotationTree.getArguments(), Pair.createPair(annotationNode, RelationTypes.HAS_ANNOTATIONS_ARGUMENTS));

		return null;
	}

	@Override
	public Node visitArrayAccess(ArrayAccessTree arrayAccessTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node arrayAccessNode = DatabaseFachade.createSkeletonNode(arrayAccessTree, NodeTypes.ARRAY_ACCESS);
		GraphUtils.attachTypeDirect(arrayAccessNode, arrayAccessTree);
		GraphUtils.connectWithParent(arrayAccessNode, t);
		scan(arrayAccessTree.getExpression(),
				Pair.createPair(arrayAccessNode, RelationTypes.ARRAYACCESS_EXPR, PDGVisitor.getModifiedArg(t)));
		scan(arrayAccessTree.getIndex(), Pair.createPair(arrayAccessNode, RelationTypes.ARRAYACCESS_INDEX));
		return arrayAccessNode;
	}

	@Override
	public Node visitArrayType(ArrayTypeTree arrayTypeTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node arrayTypeNode = DatabaseFachade.createSkeletonNode(arrayTypeTree, NodeTypes.ARRAY_TYPE);
		arrayTypeNode.setProperty("elementType", arrayTypeTree.getType().toString());
		GraphUtils.connectWithParent(arrayTypeNode, t);

		scan(arrayTypeTree.getType(), Pair.createPair(arrayTypeNode, RelationTypes.TYPE_PER_ELEMENT));

		return null;
	}

	@Override
	public Node visitAssert(AssertTree assertTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node assertNode = DatabaseFachade.createSkeletonNode(assertTree, NodeTypes.ASSERT_STATEMENT);
		GraphUtils.connectWithParent(assertNode, t);
		ExpressionTree condition = assertTree.getCondition();

		ast.putConditionInCfgCache(condition,
				addInvocationInStatement(scan(condition, Pair.createPair(assertNode, RelationTypes.ASSERT_CONDITION))));
		scan(assertTree.getDetail(), Pair.createPair(assertNode, RelationTypes.ASSERT_DETAIL));
		return null;
	}

	@Override
	public Node visitAssignment(AssignmentTree assignmentTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node assignmentNode = DatabaseFachade.createSkeletonNode(assignmentTree, NodeTypes.ASSIGNMENT);
		GraphUtils.connectWithParent(assignmentNode, t);

		assignmentNode.setProperty("mustBeExecuted", must);
		GraphUtils.attachTypeDirect(assignmentTree, assignmentNode);

		Node previousAssignment = pdgUtils.lastAssignment;
		pdgUtils.lastAssignment = assignmentNode;
		scan(assignmentTree.getVariable(),
				Pair.createPair(assignmentNode, RelationTypes.ASSIGNMENT_LHS, PDGVisitor.getLefAssignmentArg(t)));
		pdgUtils.lastAssignment = previousAssignment;

		scan(assignmentTree.getExpression(),
				Pair.createPair(assignmentNode, RelationTypes.ASSIGNMENT_RHS, PDGVisitor.USED));

		return assignmentNode;

	}

	@Override
	public Node visitBinary(BinaryTree binaryTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node binaryNode = DatabaseFachade.createSkeletonNode(binaryTree, NodeTypes.BINARY_OPERATION);
		binaryNode.setProperty("operator", binaryTree.getKind().toString());
		GraphUtils.attachTypeDirect(binaryTree, binaryNode);
		GraphUtils.connectWithParent(binaryNode, t);

		scan(binaryTree.getLeftOperand(), Pair.createPair(binaryNode, RelationTypes.BINOP_LHS));
		scan(binaryTree.getRightOperand(), Pair.createPair(binaryNode, RelationTypes.BINOP_RHS));
		return binaryNode;
	}

	@Override
	public Node visitBlock(BlockTree blockTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node blockNode = DatabaseFachade.createSkeletonNode(blockTree, NodeTypes.BLOCK);
		blockNode.setProperty("isStatic", blockTree.isStatic());
		boolean isStaticInit = t.getFirst().getRelationType() == RelationTypes.HAS_STATIC_INIT;
		if (isStaticInit) {
			pdgUtils.newMethod(lastStaticConsVisited = blockNode);
			ast.newMethodDeclaration();
		}

		GraphUtils.connectWithParent(blockNode, t);

		// Se debe elegir entre filtrar por ENCLOSES relationType o usar todas
		// las relaciones salientes pero crear un nodo STATIC_CONS_DEC que
		// reciba y retorne void o que no reciba ni retorne nada....
		scan(blockTree.getStatements(), Pair.createPair(blockNode, RelationTypes.ENCLOSES));
		if (isStaticInit) {
			pdgUtils.endMethod();
			ast.endMethodDeclaration();
		}

		return blockNode;

	}

	@Override
	public Node visitBreak(BreakTree breakTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		anyBreak = false;
		Node breakNode = DatabaseFachade.createSkeletonNode(breakTree, NodeTypes.BREAK_STATEMENT);
		ast.putCfgNodeInCache(breakTree, breakNode);
		if (breakTree.getLabel() != null)
			breakNode.setProperty("label", breakTree.getLabel().toString());
		GraphUtils.connectWithParent(breakNode, t);

		return null;
	}

	@Override
	public Node visitCase(CaseTree caseTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node caseNode = DatabaseFachade.createSkeletonNode(caseTree, NodeTypes.CASE_STATEMENT);
		GraphUtils.connectWithParent(caseNode, t);
		// Si hay un case default y no hay ningún break en el switch, seguro que
		// pasa
		boolean prev = must;
		must = caseTree.getExpression() == null && prev && !anyBreak;
		ast.putConditionInCfgCache(caseTree.getExpression(),
				scan(caseTree.getExpression(), Pair.createPair(caseNode, RelationTypes.CASE_EXPR)));

		scan(caseTree.getStatements(), Pair.createPair(caseNode, RelationTypes.CASE_STATEMENTS));
		must = prev;
		return null;
	}

	@Override
	public Node visitCatch(CatchTree catchTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node catchNode = DatabaseFachade.createSkeletonNode(catchTree, NodeTypes.CATCH_BLOCK);
		ast.putCfgNodeInCache(catchTree, catchNode);
		GraphUtils.connectWithParent(catchNode, t);

		boolean prev = must;
		must = false;
		scan(catchTree.getParameter(), Pair.createPair(catchNode, RelationTypes.CATCH_PARAM));
		scan(catchTree.getBlock(), Pair.createPair(catchNode, RelationTypes.CATCH_BLOCK));
		must = prev;
		return null;
	}

	@Override

	public Node visitClass(ClassTree classTree, Pair<PartialRelation<RelationTypes>, Object> pair) {
		if (DEBUG) {
			System.out.println("Visitando calse " + classTree.getSimpleName() + "\n" + classTree);
			System.out.println("Miembros de la clase " + classTree.getSimpleName() + "(" + classTree.getClass() + ")");
		}
		String previusPrecedent = this.fullNamePrecedent;
		String simpleName = classTree.getSimpleName().toString();
		String fullyQualifiedType = this.fullNamePrecedent + "." + simpleName;
		this.fullNamePrecedent = fullyQualifiedType;

		Node classNode = DatabaseFachade.createTypeDecNode(classTree, simpleName, fullyQualifiedType);

		pdgUtils.visitClass(classNode);
		GraphUtils.connectWithParent(classNode, pair, RelationTypes.HAS_TYPE_DEC);

		DefinitionCache.CLASS_TYPE_CACHE.putDefinition(((JCClassDecl) classTree).sym, classNode);

		TypeHierarchy.visitClass(classTree, classNode);
		scan(classTree.getModifiers(), Pair.createPair(classNode, RelationTypes.HAS_CLASS_MODIFIERS));
		scan(classTree.getTypeParameters(), Pair.createPair(classNode, RelationTypes.HAS_CLASS_TYPEPARAMETERS));
		scan(classTree.getExtendsClause(), Pair.createPair(classNode, RelationTypes.HAS_CLASS_EXTENDS));

		scan(classTree.getImplementsClause(), Pair.createPair(classNode, RelationTypes.HAS_CLASS_IMPLEMENTS));

		List<Node> attrs = new ArrayList<Node>(), staticAttrs = new ArrayList<Node>(),
				constructors = new ArrayList<Node>();
		Node prevStaticCons = lastStaticConsVisited;
		scan(classTree.getMembers(), Pair.createPair(classNode, RelationTypes.HAS_STATIC_INIT,
				Pair.create(Pair.create(attrs, staticAttrs), constructors)));
		if (DEBUG)
			System.out.println(
					"Attrs found: " + attrs.size() + " S :" + staticAttrs.size() + " C : " + constructors.size());
		for (Node constructor : constructors)
			for (Node instanceAttr : attrs)
				callsFromVarDecToConstructor(instanceAttr, constructor);
		for (Node staticAttr : staticAttrs)
			callsFromVarDecToConstructor(staticAttr, lastStaticConsVisited);

		lastStaticConsVisited = prevStaticCons;
		this.fullNamePrecedent = previusPrecedent;
		pdgUtils.endVisitClass();
		return null;

	}

	private static void callsFromVarDecToConstructor(Node attr, Node constructor) {
		for (Relationship r : attr.getRelationships(RelationTypes.CALLS)) {
			constructor.createRelationshipTo(r.getEndNode(), RelationTypes.CALLS);
			r.delete();
		}
	}

	@Override
	public Node visitCompilationUnit(CompilationUnitTree compilationUnitTree,
			Pair<PartialRelation<RelationTypes>, Object> pair) {
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
	public Node visitCompoundAssignment(CompoundAssignmentTree compoundAssignmentTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node assignmentNode = DatabaseFachade.createSkeletonNode(compoundAssignmentTree, NodeTypes.COMPOUND_ASSIGNMENT);
		assignmentNode.setProperty("operator", compoundAssignmentTree.getKind().toString());
		assignmentNode.setProperty("mustBeExecuted", must);

		GraphUtils.connectWithParent(assignmentNode, t);

		GraphUtils.attachTypeDirect(compoundAssignmentTree, assignmentNode);

		scan(compoundAssignmentTree.getVariable(),
				Pair.createPair(assignmentNode, RelationTypes.COMPOUND_ASSIGNMENT_LHS));
		scan(compoundAssignmentTree.getExpression(),
				Pair.createPair(assignmentNode, RelationTypes.COMPOUND_ASSIGNMENT_RHS));
		return assignmentNode;
	}

	@Override
	public Node visitConditionalExpression(ConditionalExpressionTree conditionalTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node conditionalNode = DatabaseFachade.createSkeletonNode(conditionalTree, NodeTypes.CONDITIONAL_EXPRESSION);
		GraphUtils.attachTypeDirect(conditionalTree, conditionalNode);
		GraphUtils.connectWithParent(conditionalNode, t);

		scan(conditionalTree.getCondition(),
				Pair.createPair(conditionalNode, RelationTypes.CONDITIONAL_EXPR_CONDITION));
		boolean prev = must;
		must = false;
		scan(conditionalTree.getTrueExpression(),
				Pair.createPair(conditionalNode, RelationTypes.CONDITIONAL_EXPR_THEN));
		scan(conditionalTree.getFalseExpression(),
				Pair.createPair(conditionalNode, RelationTypes.CONDITIONAL_EXPR_ELSE));
		must = prev;
		return conditionalNode;
	}

	@Override
	public Node visitContinue(ContinueTree continueTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node continueNode = DatabaseFachade.createSkeletonNode(continueTree, NodeTypes.CONTINUE_STATEMENT);
		ast.putCfgNodeInCache(continueTree, continueNode);
		if (continueTree.getLabel() != null)
			continueNode.setProperty("label", continueTree.getLabel().toString());
		GraphUtils.connectWithParent(continueNode, t);
		return null;
	}

	@Override
	public Node visitDoWhileLoop(DoWhileLoopTree doWhileLoopTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node doWhileLoopNode = DatabaseFachade.createSkeletonNode(doWhileLoopTree, NodeTypes.DO_WHILE_LOOP);
		GraphUtils.connectWithParent(doWhileLoopNode, t);
		scan(doWhileLoopTree.getStatement(), Pair.createPair(doWhileLoopNode, RelationTypes.ENCLOSES));

		ast.putConditionInCfgCache(doWhileLoopTree.getCondition(),
				addInvocationInStatement(scan(doWhileLoopTree.getCondition(),
						Pair.createPair(doWhileLoopNode, RelationTypes.DOWHILE_CONDITION))));
		return null;
	}

	@Override
	public Node visitEmptyStatement(EmptyStatementTree emptyStatementTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node emptyStatementNode = DatabaseFachade.createSkeletonNode(emptyStatementTree, NodeTypes.EMPTY_STATEMENT);
		ast.putCfgNodeInCache(emptyStatementTree, emptyStatementNode);
		GraphUtils.connectWithParent(emptyStatementNode, t);

		return null;
	}

	@Override
	public Node visitEnhancedForLoop(EnhancedForLoopTree enhancedForLoopTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node enhancedForLoopNode = DatabaseFachade.createSkeletonNode(enhancedForLoopTree, NodeTypes.ENHANCED_FOR);
		GraphUtils.connectWithParent(enhancedForLoopNode, t);
		scan(enhancedForLoopTree.getVariable(), Pair.createPair(enhancedForLoopNode, RelationTypes.FOREACH_VAR));
		ast.putConditionInCfgCache(enhancedForLoopTree.getExpression(),
				addInvocationInStatement(scan(enhancedForLoopTree.getExpression(),
						Pair.createPair(enhancedForLoopNode, RelationTypes.FOREACH_EXPR))));
		boolean prev = must;
		must = false;
		scan(enhancedForLoopTree.getStatement(), Pair.createPair(enhancedForLoopNode, RelationTypes.FOREACH_STATEMENT));
		must = prev;
		return null;
	}

	@Override
	public Node visitErroneous(ErroneousTree erroneousTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		Node erroneousNode = DatabaseFachade.createSkeletonNode(erroneousTree, NodeTypes.ERRONEOUS_NODE);
		GraphUtils.attachTypeDirect(erroneousTree, erroneousNode);
		GraphUtils.connectWithParent(erroneousNode, t);
		scan(erroneousTree.getErrorTrees(), Pair.createPair(erroneousNode, RelationTypes.ERRONEOUS_NODE_CAUSED_BY));
		return erroneousNode;
	}

	@Override
	public Node visitExpressionStatement(ExpressionStatementTree expressionStatementTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node expressionStatementNode = DatabaseFachade.createSkeletonNode(expressionStatementTree,
				NodeTypes.EXPRESSION_STATEMENT);
		GraphUtils.connectWithParent(expressionStatementNode, t);

		scan(expressionStatementTree.getExpression(), Pair.createPair(expressionStatementNode,
				RelationTypes.ENCLOSES_EXPR, PDGVisitor.getExprStatementArg(expressionStatementTree)));
		addInvocationInStatement(expressionStatementNode);
		ast.putCfgNodeInCache(expressionStatementTree, expressionStatementNode);

		return null;
	}

	@Override
	public Node visitForLoop(ForLoopTree forLoopTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node forLoopNode = DatabaseFachade.createSkeletonNode(forLoopTree, NodeTypes.FOR_LOOP);
		GraphUtils.connectWithParent(forLoopNode, t);

		scan(forLoopTree.getInitializer(), Pair.createPair(forLoopNode, RelationTypes.FORLOOP_INIT));
		ast.putConditionInCfgCache(forLoopTree.getCondition(),
				scan(forLoopTree.getCondition(), Pair.createPair(forLoopNode, RelationTypes.FORLOOP_CONDITION)));
		boolean prev = must;
		must = false;
		scan(forLoopTree.getStatement(), Pair.createPair(forLoopNode, RelationTypes.FORLOOP_STATEMENT));
		scan(forLoopTree.getUpdate(), Pair.createPair(forLoopNode, RelationTypes.FORLOOP_UPDATE));
		must = prev;

		return null;
	}

	@Override
	public Node visitIdentifier(IdentifierTree identifierTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		if (DEBUG && t.getFirst().getRelationType() == RelationTypes.HAS_METHODDECL_THROWS) {
			System.out.println(identifierTree);
			System.out.println(JavacInfo.getTree(((JCIdent) identifierTree).sym));
			System.out.println(identifierTree.getName().toString());
			System.out.println("TYPE:\n" + JavacInfo.getTypeDirect(identifierTree));
		}
		Node identifierNode = DatabaseFachade.createSkeletonNode(identifierTree, NodeTypes.IDENTIFIER);
		identifierNode.setProperty("name", identifierTree.getName().toString());
		// It can be useful or not, by the moment it is not necessary for coding
		// any rule. so it is commented
		// identifierNode.setProperty("symbol", ((JCIdent)
		// identifierTree).sym.toString());
		GraphUtils.attachTypeDirect(identifierNode, identifierTree);
		GraphUtils.connectWithParent(identifierNode, t);
		pdgUtils.relationOnIdentifier(identifierTree, identifierNode, t);
		return identifierNode;
	}

	@Override
	public Node visitIf(IfTree ifTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node ifNode = DatabaseFachade.createSkeletonNode(ifTree, NodeTypes.IF_STATEMENT);
		GraphUtils.connectWithParent(ifNode, t);
		ast.putConditionInCfgCache(ifTree.getCondition(), addInvocationInStatement(
				scan(ifTree.getCondition(), Pair.createPair(ifNode, RelationTypes.IF_CONDITION))));

		boolean prev = must;
		must = false;
		scan(ifTree.getThenStatement(), Pair.createPair(ifNode, RelationTypes.IF_THEN));
		scan(ifTree.getElseStatement(), Pair.createPair(ifNode, RelationTypes.IF_ELSE));
		must = prev;

		return null;
	}

	@Override
	public Node visitImport(ImportTree importTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node importNode = DatabaseFachade.createSkeletonNode(importTree, NodeTypes.IMPORT);
		importNode.setProperty("qualifiedIdentifier", importTree.getQualifiedIdentifier().toString());
		importNode.setProperty("isStatic", importTree.isStatic());

		GraphUtils.connectWithParent(importNode, t, RelationTypes.IMPORTS);
		// Posteriormente relacionar classfile con classfile o con typedec....
		// En caso de imports así import a.b.* liada jejeje
		return null;
	}

	@Override
	public Node visitInstanceOf(InstanceOfTree instanceOfTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node instanceOfNode = DatabaseFachade.createSkeletonNode(instanceOfTree, NodeTypes.INSTANCE_OF);
		GraphUtils.attachTypeDirect(instanceOfNode, "boolean", "BOOLEAN");
		GraphUtils.connectWithParent(instanceOfNode, t);

		scan(instanceOfTree.getExpression(), Pair.createPair(instanceOfNode, RelationTypes.INSTANCEOF_EXPR));
		scan(instanceOfTree.getType(), Pair.createPair(instanceOfNode, RelationTypes.INSTANCEOF_TYPE));

		return instanceOfNode;
	}

	@Override
	public Node visitIntersectionType(IntersectionTypeTree intersectionTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		Node intersectionTypeNode = DatabaseFachade.createSkeletonNode(intersectionTypeTree,
				NodeTypes.INTERSECTION_TYPE);

		GraphUtils.connectWithParent(intersectionTypeNode, t);

		scan(intersectionTypeTree.getBounds(),
				Pair.createPair(intersectionTypeNode, RelationTypes.INTERSECTION_COMPOSED_BY));

		return null;
	}

	@Override
	public Node visitLabeledStatement(LabeledStatementTree labeledStatementTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node labeledStatementNode = DatabaseFachade.createSkeletonNode(labeledStatementTree,
				NodeTypes.LABELED_STATEMENT);
		// ast.putCfgNodeInCache(this,labeledStatementTree,
		// labeledStatementNode);
		labeledStatementNode.setProperty("name", labeledStatementTree.getLabel().toString());
		GraphUtils.connectWithParent(labeledStatementNode, t);

		scan(labeledStatementTree.getStatement(),
				Pair.createPair(labeledStatementNode, RelationTypes.LABELED_STATEMENT));
		return null;
	}

	@Override
	public Node visitLambdaExpression(LambdaExpressionTree lambdaExpressionTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node lambdaExpressionNode = DatabaseFachade.createSkeletonNode(lambdaExpressionTree,
				NodeTypes.LAMBDA_EXPRESSION);
		lambdaExpressionNode.setProperty("bodyKind", lambdaExpressionTree.getBodyKind());
		GraphUtils.connectWithParent(lambdaExpressionNode, t);
		GraphUtils.attachTypeDirect(lambdaExpressionTree, lambdaExpressionNode);
		scan(lambdaExpressionTree.getBody(),
				Pair.createPair(lambdaExpressionNode, RelationTypes.LAMBDA_EXPRESSION_BODY));
		scan(lambdaExpressionTree.getParameters(),
				Pair.createPair(lambdaExpressionNode, RelationTypes.LAMBDA_EXPRESSION_PARAMETERS));

		return lambdaExpressionNode;
	}

	@Override
	public Node visitLiteral(LiteralTree literalTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node literalNode = DatabaseFachade.createSkeletonNode(literalTree, NodeTypes.LITERAL);
		literalNode.setProperty("typetag", literalTree.getKind().toString());
		if (literalTree.getValue() != null)
			literalNode.setProperty("value", literalTree.getValue().toString());

		GraphUtils.attachTypeDirect(literalNode, literalTree);
		GraphUtils.connectWithParent(literalNode, t);

		return literalNode;

	}

	@Override
	public Node visitMemberReference(MemberReferenceTree memberReferenceTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		Node memberReferenceNode = DatabaseFachade.createSkeletonNode(memberReferenceTree, NodeTypes.MEMBER_REFERENCE);
		memberReferenceNode.setProperty("mode", memberReferenceTree.getMode());
		memberReferenceNode.setProperty("name", memberReferenceTree.getName());
		GraphUtils.connectWithParent(memberReferenceNode, t);
		GraphUtils.attachTypeDirect(memberReferenceTree, memberReferenceNode);

		scan(memberReferenceTree.getQualifierExpression(),
				Pair.createPair(memberReferenceNode, RelationTypes.MEMBER_REFERENCE_EXPRESSION));
		scan(memberReferenceTree.getTypeArguments(),
				Pair.createPair(memberReferenceNode, RelationTypes.MEMBER_REFERENCE_TYPE_ARGUMENTS));

		return memberReferenceNode;
	}

	@Override
	public Node visitMemberSelect(MemberSelectTree memberSelectTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		Node memberSelect = DatabaseFachade.createSkeletonNode(memberSelectTree, NodeTypes.MEMBER_SELECTION);
		memberSelect.setProperty("memberName", memberSelectTree.getIdentifier().toString());

		GraphUtils.attachTypeDirect(memberSelect, memberSelectTree);
		GraphUtils.connectWithParent(memberSelect, t);
		pdgUtils.relationOnAttribute(memberSelectTree, memberSelect, t);

		scan(memberSelectTree.getExpression(),
				Pair.createPair(memberSelect, RelationTypes.MEMBER_SELECT_EXPR, PDGVisitor.getModifiedArg(t)));

		return memberSelect;
	}

	@Override
	public Node visitMethod(MethodTree methodTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		if (DEBUG)
			System.out.println("Visiting method declaration " + methodTree.getName());
		String name = methodTree.getName().toString();

		Node methodNode;
		if (name.contentEquals("<init>")) {
			methodNode = DatabaseFachade.createSkeletonNode(methodTree, NodeTypes.CONSTRUCTOR_DEC);
			((Pair<Pair, List<Node>>) t.getSecond()).getSecond().add(methodNode);
			GraphUtils.connectWithParent(methodNode, t, RelationTypes.DECLARES_CONSTRUCTOR);
		} else {
			methodNode = DatabaseFachade.createSkeletonNode(methodTree, NodeTypes.METHOD_DEC);
			GraphUtils.connectWithParent(methodNode, t, RelationTypes.DECLARES_METHOD);
		}
		scan(methodTree.getModifiers(), Pair.createPair(methodNode, RelationTypes.HAS_METHODDECL_MODIFIERS));
		pdgUtils.newMethod(methodNode);

		TypeMirror type = JavacInfo.getTypeDirect(methodTree);
		String fullyQualifiedName = fullNamePrecedent + ":" + methodTree.getName().toString() + ":" + type.toString();
		methodNode.setProperty("name", name);
		methodNode.setProperty("fullyQualifiedName", fullyQualifiedName);
		methodNode.setProperty("isDeclared", true);
		if (DEBUG) {
			System.out.println("METHOD DECLARATION :" + methodTree.getClass());

			System.out.println("Symbol:" + ((JCMethodDecl) methodTree).sym.toString());
		}
		// DefinitionCache.METHOD_TYPE_CACHE.putDefinition(fullyQualifiedName.split("\\)")[0]
		// + ")", methodNode);
		DefinitionCache.METHOD_TYPE_CACHE.putDefinition(((JCMethodDecl) methodTree).sym, methodNode);

		ast.newMethodDeclaration();
		scan(methodTree.getReturnType(), Pair.createPair(methodNode, RelationTypes.HAS_METHODDECL_RETURNS));
		scan(methodTree.getTypeParameters(), Pair.createPair(methodNode, RelationTypes.HAS_METHODDECL_TYPEPARAMETERS));

		for (int i = 0; i < methodTree.getParameters().size(); i++)
			scan(methodTree.getParameters().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(methodNode,
							RelationTypes.HAS_METHODDECL_PARAMETERS, "paramIndex", i + 1)));

		List<Type> throwsNames = new ArrayList<Type>();
		for (ExpressionTree throwsId : methodTree.getThrows())
			throwsNames.add(JavacInfo.getTypeDirect(throwsId));
		ast.addThrowsInfoToMethod(fullyQualifiedName, throwsNames);

		scan(methodTree.getBody(), Pair.createPair(methodNode, RelationTypes.HAS_METHODDECL_BODY));
		scan(methodTree.getDefaultValue(), Pair.createPair(methodNode, RelationTypes.HAS_DEFAULT_VALUE));
		scan(methodTree.getReceiverParameter(), Pair.createPair(methodNode, RelationTypes.HAS_RECEIVER_PARAMETER));

		ast.addInfo(methodTree, methodNode, pdgUtils.getIdentificationForLeftAssignExprs());
		ast.endMethodDeclaration();
		pdgUtils.endMethod();
		return null;

	}

	@Override
	public Node visitMethodInvocation(MethodInvocationTree methodInvocationTree,
			Pair<PartialRelation<RelationTypes>, Object> pair) {

		TreePath path = JavacInfo.getPath(methodInvocationTree);
		Node methodInvocationNode = DatabaseFachade.createSkeletonNode(methodInvocationTree,
				NodeTypes.METHOD_INVOCATION);
		GraphUtils.attachTypeDirect(methodInvocationNode, methodInvocationTree);
		GraphUtils.connectWithParent(methodInvocationNode, pair);

		if (path.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
			String fullyQualifiedName = null, methodName = null;
			ExpressionTree methodSelectExpr = (JCExpression) methodInvocationTree.getMethodSelect();
			pair = Pair.createPair(methodInvocationNode, RelationTypes.METHODINVOCATION_METHOD_SELECT);
			// Esto igual es omitible si retornan algo los hijos
			boolean isCons = false;
			Symbol s = null;
			switch (methodSelectExpr.getKind()) {

			case MEMBER_SELECT:
				JCFieldAccess mst = (JCFieldAccess) methodSelectExpr;
				String methodType = mst.type.toString();

				methodName = (s = mst.sym).toString();
				TypeMirror type = JavacInfo.getTypeDirect(mst.getExpression());
				fullyQualifiedName = type.toString() + ":" + methodName;
				// if (methodType.contentEquals("UNKNOWN") ||
				// methodName.contentEquals("UNKNOWN")
				// || fullyQualifiedName.contentEquals("UNKNOWN"))
				// System.out.println("Falta información para : " +
				// methodSelectExpr.toString());
				visitMemberSelect(mst, pair);
				break;

			case IDENTIFIER:
				JCIdent mst2 = (JCIdent) methodSelectExpr;
				// methodType = "UNKNOWN()";
				// if (mst2.type == null)
				// System.out.println("IDENTIFIER WITHOUT TYPE:" +
				// mst2.toString());
				// else
				methodType = mst2.type.toString();
				s = mst2.sym;
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
				if (DEBUG) {
					System.out.println(fullyQualifiedName);
					System.out.println("Symbol:\t" + s);
				}
				visitIdentifier(mst2, pair);

				break;
			default:
				throw new IllegalStateException("Invocation que viene de " + methodSelectExpr.getKind());

			}
			currentMethodInvocations.add(fullyQualifiedName);
			Node decNode = isCons ? getConstructorDecNode(s, fullyQualifiedName)
					: getMethodDecNode(s, fullyQualifiedName, methodName);
			Relationship callRelation = pdgUtils.getLastMethodDecVisited().createRelationshipTo(methodInvocationNode,
					RelationTypes.CALLS);
			callRelation.setProperty("mustBeExecuted", must);
			methodInvocationNode.createRelationshipTo(decNode, RelationTypes.HAS_DEC);
			scan(methodInvocationTree.getTypeArguments(),
					Pair.createPair(methodInvocationNode, RelationTypes.METHODINVOCATION_TYPE_ARGUMENTS));
			for (int i = 0; i < methodInvocationTree.getArguments().size(); i++)
				scan(methodInvocationTree.getArguments().get(i),
						Pair.createPair(new PartialRelationWithProperties<RelationTypes>(methodInvocationNode,
								RelationTypes.METHODINVOCATION_ARGUMENTS, "argumentIndex", i + 1)));

		} else

		{
			throw new IllegalStateException("A methodInv path leaf node is not a MethodInv");
		}
		return methodInvocationNode;
	}

	@Override
	public Node visitModifiers(ModifiersTree modifiersTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node modifiersNode = DatabaseFachade.createSkeletonNode(modifiersTree, NodeTypes.MODIFIERS);
		modifiersNode.setProperty("flags", modifiersTree.getFlags().toString());
		GraphUtils.connectWithParent(modifiersNode, t);

		Pair<PartialRelation<RelationTypes>, Object> n = Pair.createPair(modifiersNode, RelationTypes.ENCLOSES);
		scan(modifiersTree.getAnnotations(), n);

		return null;
	}

	@Override
	public Node visitNewArray(NewArrayTree newArrayTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node newArrayNode = DatabaseFachade.createSkeletonNode(newArrayTree, NodeTypes.NEW_ARRAY);
		GraphUtils.attachTypeDirect(newArrayTree, newArrayNode);
		GraphUtils.connectWithParent(newArrayNode, t);

		scan(newArrayTree.getType(), Pair.createPair(newArrayNode, RelationTypes.NEWARRAY_TYPE));
		scan(newArrayTree.getDimensions(), Pair.createPair(newArrayNode, RelationTypes.NEWARRAY_DIMENSION));

		scan(newArrayTree.getInitializers(), Pair.createPair(newArrayNode, RelationTypes.NEWARRAY_INIT));
		return newArrayNode;
	}

	// OJO FALTA REVISAR
	@Override
	public Node visitNewClass(NewClassTree newClassTree, Pair<PartialRelation<RelationTypes>, Object> pair) {

		Node newClassNode = DatabaseFachade.createSkeletonNode(newClassTree, NodeTypes.NEW_INSTANCE);

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
				Pair.createPair(newClassNode, RelationTypes.NEWCLASS_ENCLOSING_EXPRESSION));
		scan(newClassTree.getIdentifier(), Pair.createPair(newClassNode, RelationTypes.NEWCLASS_IDENTIFIER));
		scan(newClassTree.getTypeArguments(), Pair.createPair(newClassNode, RelationTypes.NEW_CLASS_TYPE_ARGUMENTS));

		for (int i = 0; i < newClassTree.getArguments().size(); i++)
			scan(newClassTree.getArguments().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(newClassNode,
							RelationTypes.NEW_CLASS_ARGUMENTS, "argumentIndex", i)));

		scan(newClassTree.getClassBody(), Pair.createPair(newClassNode, RelationTypes.NEW_CLASS_BODY));

		// if ((((JCNewClass) newClassTree).constructorType) == null)
		// System.out.println("NO CONS TYPE: " + newClassTree.toString());
		// else {
		String constName = "<init>:" + (((JCNewClass) newClassTree).constructorType).toString();
		String fullyQualifiedName = newClassTree.getIdentifier().toString() + constName;
		Node constructorDef = getConstructorDecNode(((JCNewClass) newClassTree).constructor, fullyQualifiedName);
		newClassNode.createRelationshipTo(constructorDef, RelationTypes.HAS_DEC);
		// } if (lastMethodDecVisited != null)
		pdgUtils.getLastMethodDecVisited().createRelationshipTo(newClassNode, RelationTypes.CALLS);
		currentMethodInvocations.add(fullyQualifiedName);

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
		return newClassNode;
	}

	@Override
	public Node visitOther(Tree arg0, Pair<PartialRelation<RelationTypes>, Object> t) {
		throw new IllegalArgumentException(
				"[EXCEPTION] Tree not included in the visitor: " + arg0.getClass() + "\n" + arg0);
	}

	@Override
	public Node visitParameterizedType(ParameterizedTypeTree parameterizedTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node parameterizedNode = DatabaseFachade.createSkeletonNode(parameterizedTypeTree, NodeTypes.PARAMETRIZED_TYPE);
		GraphUtils.connectWithParent(parameterizedNode, t);

		scan(parameterizedTypeTree.getType(), Pair.createPair(parameterizedNode, RelationTypes.PARAMETERIZEDTYPE_TYPE));

		scan(parameterizedTypeTree.getTypeArguments(),
				Pair.createPair(parameterizedNode, RelationTypes.PARAMETERIZEDTYPE_TYPEARGUMENTS));

		return null;
	}

	@Override
	public Node visitParenthesized(ParenthesizedTree parenthesizedTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		// Esto no debería entrar por aquí, porque los paréntesis no deben
		// preservarse, o sí, y soy yo el que los tiene que obviar //Si los dejo
		// puedo detectar fallos de cuando sobran paréntesis, no sé si sale
		// rentable

		Node parenthesizedNode = DatabaseFachade.createSkeletonNode(parenthesizedTree,
				NodeTypes.PARENTHESIZED_EXPRESSION);
		GraphUtils.attachTypeDirect(parenthesizedNode, parenthesizedTree);
		GraphUtils.connectWithParent(parenthesizedNode, t);
		scan(parenthesizedTree.getExpression(),
				Pair.createPair(parenthesizedNode, RelationTypes.PARENTHESIZED_ENCLOSES));
		return parenthesizedNode;
	}

	@Override
	public Node visitPrimitiveType(PrimitiveTypeTree primitiveTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node primitiveTypeNode = DatabaseFachade.createSkeletonNode(primitiveTypeTree, NodeTypes.PRIMITIVE_TYPE);
		primitiveTypeNode.setProperty("primitiveTypeKind", primitiveTypeTree.getPrimitiveTypeKind().toString());
		GraphUtils.connectWithParent(primitiveTypeNode, t);
		return null;
	}

	@Override
	public Node visitReturn(ReturnTree returnTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node returnNode = DatabaseFachade.createSkeletonNode(returnTree, NodeTypes.RETURN_STATEMENT);
		ast.putCfgNodeInCache(returnTree, returnNode);
		GraphUtils.connectWithParent(returnNode, t);

		scan(returnTree.getExpression(), Pair.createPair(returnNode, RelationTypes.RETURN_EXPR));
		addInvocationInStatement(returnNode);
		return null;
	}

	@Override
	public Node visitSwitch(SwitchTree switchTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node switchNode = DatabaseFachade.createSkeletonNode(switchTree, NodeTypes.SWITCH_STATEMENT);
		GraphUtils.connectWithParent(switchNode, t);
		ast.putConditionInCfgCache(switchTree.getExpression(), addInvocationInStatement(
				scan(switchTree.getExpression(), Pair.createPair(switchNode, RelationTypes.SWITCH_EXPR))));

		scan(switchTree.getCases(), Pair.createPair(switchNode, RelationTypes.SWITCH_ENCLOSES_CASES));
		return null;
	}

	@Override
	public Node visitSynchronized(SynchronizedTree synchronizedTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node synchronizedNode = DatabaseFachade.createSkeletonNode(synchronizedTree, NodeTypes.SYNCHRONIZED_BLOCK);
		GraphUtils.connectWithParent(synchronizedNode, t);
		scan(synchronizedTree.getExpression(), Pair.createPair(synchronizedNode, RelationTypes.SYNCHRONIZED_EXPR));
		scan(synchronizedTree.getBlock(), Pair.createPair(synchronizedNode, RelationTypes.SYNCHRONIZED_BLOCK));
		return null;
	}

	@Override
	public Node visitThrow(ThrowTree throwTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node throwNode = DatabaseFachade.createSkeletonNode(throwTree, NodeTypes.THROW_STATEMENT);
		ast.putCfgNodeInCache(throwTree, throwNode);
		GraphUtils.connectWithParent(throwNode, t);

		scan(throwTree.getExpression(), Pair.createPair(throwNode, RelationTypes.THROW_EXPR));
		addInvocationInStatement(throwNode);
		return null;
	}

	@Override
	public Node visitTry(TryTree tryTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node tryNode = DatabaseFachade.createSkeletonNode(tryTree, NodeTypes.TRY_BLOCK);
		GraphUtils.connectWithParent(tryNode, t);
		boolean hasCatchingComponent = tryTree.getCatches().size() > 0 || tryTree.getFinallyBlock() != null;
		if (hasCatchingComponent)
			ast.enterInNewTry(tryTree);
		scan(tryTree.getResources(), Pair.createPair(tryNode, RelationTypes.TRY_RESOURCES));
		scan(tryTree.getBlock(), Pair.createPair(tryNode, RelationTypes.TRY_BLOCK));
		if (hasCatchingComponent)
			ast.exitTry();

		scan(tryTree.getCatches(), Pair.createPair(tryNode, RelationTypes.TRY_CATCH));

		ast.putCfgNodeInCache(tryTree, tryNode);

		if (tryTree.getFinallyBlock() != null)
			ast.putCfgNodeInCache(tryTree.getFinallyBlock(),
					DatabaseFachade.createSkeletonNode(NodeTypes.CFG_LAST_STATEMENT_IN_FINALLY));
		scan(tryTree.getFinallyBlock(), Pair.createPair(tryNode, RelationTypes.TRY_FINALLY));

		return null;

	}

	@Override
	public Node visitTypeCast(TypeCastTree typeCastTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node typeCastNode = DatabaseFachade.createSkeletonNode(typeCastTree, NodeTypes.TYPE_CAST);
		GraphUtils.attachTypeDirect(typeCastTree, typeCastNode);
		GraphUtils.connectWithParent(typeCastNode, t);

		scan(typeCastTree.getType(), Pair.createPair(typeCastNode, RelationTypes.CAST_TYPE));
		scan(typeCastTree.getExpression(), Pair.createPair(typeCastNode, RelationTypes.CAST_ENCLOSES));
		return typeCastNode;
	}

	@Override
	public Node visitTypeParameter(TypeParameterTree typeParameterTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		Node typeParameterNode = DatabaseFachade.createSkeletonNode(typeParameterTree, NodeTypes.TYPE_PARAM);
		typeParameterNode.setProperty("name", typeParameterTree.getName().toString());
		GraphUtils.connectWithParent(typeParameterNode, t);

		scan(typeParameterTree.getBounds(), Pair.createPair(typeParameterNode, RelationTypes.TYPEPARAMETER_EXTENDS));
		return null;
	}

	@Override
	public Node visitUnary(UnaryTree unaryTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		Node unaryNode = DatabaseFachade.createSkeletonNode(unaryTree, NodeTypes.UNARY_OPERATION);
		GraphUtils.attachTypeDirect(unaryTree, unaryNode);
		unaryNode.setProperty("operator", unaryTree.getKind().toString());
		GraphUtils.connectWithParent(unaryNode, t);

		scan(unaryTree.getExpression(), Pair.createPair(unaryNode, RelationTypes.UNARY_ENCLOSES));

		return unaryNode;
	}

	@Override
	public Node visitUnionType(UnionTypeTree unionTypeTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		Node unionTypeNode = DatabaseFachade.createSkeletonNode(unionTypeTree, NodeTypes.UNION_TYPE);
		GraphUtils.connectWithParent(unionTypeNode, t);

		scan(unionTypeTree.getTypeAlternatives(), Pair.createPair(unionTypeNode, RelationTypes.UNION));

		return null;

	}

	private void createVarInit(VariableTree varTree, Node varDecNode) {
		if (varTree.getInitializer() != null) {
			Node initNode = DatabaseFachade.createSkeletonNode(varTree, NodeTypes.INITIALIZATION);
			varDecNode.createRelationshipTo(initNode, RelationTypes.HAS_VARIABLEDECL_INIT);
			scan(varTree.getInitializer(), Pair.createPair(initNode, RelationTypes.INITIALIZATION_EXPR));
			PDGVisitor.createVarDecInitRel(varDecNode, initNode);
		}
	}

	@Override
	public Node visitVariable(VariableTree variableTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		boolean isAttr = t.getFirst().getRelationType().equals(RelationTypes.HAS_STATIC_INIT);
		boolean isMethodParam = t.getFirst().getRelationType().equals(RelationTypes.HAS_METHODDECL_PARAMETERS);
		// This can be calculated cehcking if the param Object is null or not?
		Node variableNode = DatabaseFachade.createSkeletonNode(variableTree,
				isAttr ? NodeTypes.ATTR_DEC : isMethodParam ? NodeTypes.PARAMETER_DEC : NodeTypes.VAR_DEC);
		variableNode.setProperty("name", variableTree.getName().toString());
		GraphUtils.attachTypeDirect(variableNode, variableTree);

		if (DEBUG)
			System.out
					.println("VARIABLE:" + variableTree.getName() + "(" + variableNode.getProperty("actualType") + ")");
		scan(variableTree.getModifiers(), Pair.createPair(variableNode, RelationTypes.HAS_VARIABLEDECL_MODIFIERS));

		if (isAttr) {
			// Warning, lineNumber and position should be added depending on the
			// constructor
			GraphUtils.connectWithParent(variableNode, t, RelationTypes.DECLARES_FIELD);

			pdgUtils.newMethod(variableNode);
			Pair<List<Node>, List<Node>> param = ((Pair<Pair<List<Node>, List<Node>>, List<Node>>) t.getSecond())
					.getFirst();
			(variableTree.getModifiers().getFlags().toString().contains("static") ? param.getSecond()
					: param.getFirst()).add(pdgUtils.getLastMethodDecVisited());
			createVarInit(variableTree, variableNode);
			pdgUtils.endMethod();

		} else {
			GraphUtils.connectWithParent(variableNode, t);

			createVarInit(variableTree, variableNode);
		}
		if (!(isAttr || isMethodParam)) {
			ast.putCfgNodeInCache(variableTree, variableNode);
			addInvocationInStatement(variableNode);
		}
		pdgUtils.putDecInCache(variableTree, variableNode);

		scan(variableTree.getType(), Pair.createPair(variableNode, RelationTypes.HAS_VARIABLEDECL_TYPE));

		return null;
	}

	@Override
	public Node visitWhileLoop(WhileLoopTree whileLoopTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node whileLoopNode = DatabaseFachade.createSkeletonNode(whileLoopTree, NodeTypes.WHILE_LOOP);
		GraphUtils.connectWithParent(whileLoopNode, t);
		ast.putConditionInCfgCache(whileLoopTree.getCondition(), addInvocationInStatement(
				scan(whileLoopTree.getCondition(), Pair.createPair(whileLoopNode, RelationTypes.WHILE_CONDITION))));

		boolean prev = must;
		must = false;
		scan(whileLoopTree.getStatement(), Pair.createPair(whileLoopNode, RelationTypes.ENCLOSES));
		must = prev;
		return null;
	}

	@Override
	public Node visitWildcard(WildcardTree wildcardTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		Node wildcardNode = DatabaseFachade.createSkeletonNode(wildcardTree, NodeTypes.WILDCARD);
		wildcardNode.setProperty("typeBoundKind", wildcardTree.getKind().toString());
		GraphUtils.connectWithParent(wildcardNode, t);

		scan(wildcardTree.getBound(), Pair.createPair(wildcardNode, RelationTypes.WILDCARD_BOUND));
		return null;
	}

}

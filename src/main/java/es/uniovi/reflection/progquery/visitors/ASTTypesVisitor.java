package es.uniovi.reflection.progquery.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import org.neo4j.graphdb.Direction;

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
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.cache.DefinitionCache;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.nodes.NodeCategory;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.database.relations.CDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.CFGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.CGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.PartialRelation;
import es.uniovi.reflection.progquery.database.relations.PartialRelationWithProperties;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.typeInfo.PackageInfo;
import es.uniovi.reflection.progquery.typeInfo.TypeHierarchy;
import es.uniovi.reflection.progquery.utils.GraphUtils;
import es.uniovi.reflection.progquery.utils.JavacInfo;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.ASTVisitorResult;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.ClassState;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.MethodState;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.VisitorResultImpl;
import scala.reflect.internal.Symbols;

public class ASTTypesVisitor extends TreeScanner<ASTVisitorResult, Pair<PartialRelation<RelationTypes>, Object>> {

	private static final boolean DEBUG = false;
	private NodeWrapper lastStaticConsVisited = null;
	private ClassTree typeDec;
	private boolean first;
	private PDGProcessing pdgUtils;
	public ASTAuxiliarStorage ast;

	private MethodState methodState = null;
	// TODO TODO LO DEM�S CON EL CLASSSTATE
	private ClassState classState = null;

	private boolean insideConstructor = false;
	private List<MethodSymbol> currentMethodInvocations = new ArrayList<MethodSymbol>();
	private final NodeWrapper currentCU;
	// Must-May superficial analysis
	private boolean must = true, prevMust = true, auxMust = true;

	private boolean anyBreak;
	private Set<NodeWrapper> typeDecUses;
	private ClassSymbol currentTypeDecSymbol;
	private boolean outsideAnnotation = true;
	private boolean isInAccessibleContext = true;
	private Set<Name> gotoLabelsInDoWhile = new HashSet<>();
	private boolean inADoWhile = false, inALambda=false;

	public Set<NodeWrapper> getTypeDecUses() {
		return typeDecUses;
	}

	public ASTTypesVisitor(ClassTree typeDec, boolean first, PDGProcessing pdgUtils, ASTAuxiliarStorage ast,
			NodeWrapper cu) {
		this.typeDec = typeDec;
		this.first = first;
		this.pdgUtils = pdgUtils;
		this.ast = ast;
		this.currentCU = cu;
		// System.out.println(NodeUtils.nodeToString(cu));
	}

	private NodeWrapper addInvocationInStatement(NodeWrapper statement) {
		ast.addInvocationInStatement(statement, currentMethodInvocations);
		currentMethodInvocations = new ArrayList<MethodSymbol>();
		return statement;
	}

	@Override
	public ASTVisitorResult reduce(ASTVisitorResult n1, ASTVisitorResult n2) {
		return n2;
	}

	private NodeWrapper getNotDeclaredConsFromInv(Symbol methodSymbol, String fullyQualifiedName, String completeName) {
		NodeWrapper consDec = getNotDeclaredConstructorDecNode(methodSymbol, fullyQualifiedName, completeName);
		DefinitionCache.getOrCreateType(methodSymbol.owner.type, ast).createRelationshipTo(consDec,
				RelationTypes.DECLARES_CONSTRUCTOR);
		return consDec;
	}

	public static NodeWrapper getNotDeclaredConstructorDuringTypeCreation(NodeWrapper classNode, Symbol s) {

		String methodName = "<init>";
		String completeName = s.owner + ":" + methodName;
		String fullyQualifiedName = completeName + s.type;
		NodeWrapper consDec = getNotDeclaredConstructorDecNode(s, fullyQualifiedName, completeName);
		classNode.createRelationshipTo(consDec, RelationTypes.DECLARES_CONSTRUCTOR);
		return consDec;
	}

	private static NodeWrapper getNotDeclaredConstructorDecNode(Symbol s, String fullyQualifiedName,
			String completeName) {
		NodeWrapper constructorDef = DatabaseFachade.CURRENT_DB_FACHADE
				.createNodeWithoutExplicitTree(NodeTypes.CONSTRUCTOR_DEF);
		constructorDef.setProperty("isDeclared", false);
		constructorDef.setProperty("name", "<init>");
		constructorDef.setProperty("fullyQualifiedName", fullyQualifiedName);
		constructorDef.setProperty("completeName", completeName);

		// ClassSymbol ownerSymbol = (ClassSymbol) s.owner;

		modifierAccessLevelToNode(s.getModifiers(), constructorDef);

		// classNode.createRelationshipTo(constructorDef,
		// RelationTypes.DECLARES_CONSTRUCTOR);
		// Aqui se pueden hacer nodos como el de declaracion, declara
		// params declara return throws???�
		// De momento no, solo usamos el methodType

		DefinitionCache.METHOD_DEF_CACHE.put(s, constructorDef);

		return constructorDef;
	}

	private NodeWrapper getNotDeclaredMethodDecNode(MethodSymbol symbol, String fullyQualifiedName, String methodName,
			String completeName) {

		ClassSymbol ownerSymbol = (ClassSymbol) symbol.owner;

		NodeWrapper methodDec = createNonDeclaredMethodDuringTypeCreation(ownerSymbol.isInterface(), ast, symbol,
				fullyQualifiedName, methodName, completeName);
		// System.out.println(symbol.owner.type + " DECLARES METHOD FROM INV " +
		// methodDec);
		DefinitionCache.getOrCreateType(symbol.owner.type, ast).createRelationshipTo(methodDec,
				RelationTypes.DECLARES_METHOD);
		return methodDec;
	}

	public static NodeWrapper createNonDeclaredMethodDuringTypeCreation(NodeWrapper classNode, boolean isInterface,
			ASTAuxiliarStorage ast, MethodSymbol symbol) {

		String methodName = symbol.name.toString();
		String completeName = symbol.owner + ":" + methodName;
		String fullyQualifiedName = completeName + symbol.type;
		NodeWrapper methodDec = createNonDeclaredMethodDuringTypeCreation(isInterface, ast, symbol, fullyQualifiedName,
				methodName, completeName);
		// System.out.println(classNode + " DECLARES METHOD IN DURING TYPE " +
		// methodDec);
		classNode.createRelationshipTo(methodDec, RelationTypes.DECLARES_METHOD);
		return methodDec;
	}

	private static NodeWrapper createNonDeclaredMethodDuringTypeCreation(boolean isInterface, ASTAuxiliarStorage ast,
			MethodSymbol symbol, String fullyQualifiedName, String methodName, String completeName) {
		NodeWrapper
		// Se hacen muchas cosas y es posible que se visite la
		// declaraci�n despu�s
		methodDecNode = DatabaseFachade.CURRENT_DB_FACHADE.createNodeWithoutExplicitTree(NodeTypes.METHOD_DEF);

		methodDecNode.setProperty("isDeclared", false);
		methodDecNode.setProperty("name", methodName);
		// System.out.println(fullyQualifiedName);
		methodDecNode.setProperty("fullyQualifiedName", fullyQualifiedName);
		methodDecNode.setProperty("completeName", completeName);

		setMethodModifiers(Flags.asModifierSet(symbol.flags()), methodDecNode, isInterface);

		// Aqui se pueden hacer nodos como el de declaracion, declara
		// params declara return throws???�
		// De momento no, solo usamos el methodType

		ast.addAccesibleMethod(symbol, methodDecNode);
		DefinitionCache.METHOD_DEF_CACHE.put(symbol, methodDecNode);
		return methodDecNode;
	}

	@Override
	public ASTVisitorResult visitAnnotatedType(AnnotatedTypeTree annotatedTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper annotatedTypeNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(annotatedTypeTree,
				NodeTypes.ANNOTATED_TYPE);
		attachTypeDirect(annotatedTypeNode, annotatedTypeTree);
		GraphUtils.connectWithParent(annotatedTypeNode, t);

		scan(annotatedTypeTree.getAnnotations(), Pair.createPair(annotatedTypeNode, RelationTypes.HAS_ANNOTATIONS));
		scan(annotatedTypeTree.getUnderlyingType(), Pair.createPair(annotatedTypeNode, RelationTypes.UNDERLYING_TYPE));

		return null;
	}

	@Override
	public ASTVisitorResult visitAnnotation(AnnotationTree annotationTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper annotationNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(annotationTree,
				NodeTypes.ANNOTATION);
		GraphUtils.connectWithParent(annotationNode, t, RelationTypes.HAS_ANNOTATIONS);
		boolean prevInsideAnn = outsideAnnotation;
		outsideAnnotation = false;
		scan(annotationTree.getAnnotationType(), Pair.createPair(annotationNode, RelationTypes.HAS_ANNOTATION_TYPE));

		// TODO: order
		// scan(annotationTree.getArguments(), Pair.createPair(annotationNode,
		// RelationTypes.HAS_ANNOTATIONS_ARGUMENTS));

		for (int i = 0; i < annotationTree.getArguments().size(); i++)
			scan(annotationTree.getArguments().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(annotationNode,
							RelationTypes.HAS_ANNOTATIONS_ARGUMENTS, "argumentIndex", i + 1)));
		outsideAnnotation = prevInsideAnn;

		return null;
	}

	@Override
	public ASTVisitorResult visitArrayAccess(ArrayAccessTree arrayAccessTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper arrayAccessNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(arrayAccessTree,
				NodeTypes.ARRAY_ACCESS);
		attachTypeDirect(arrayAccessNode, arrayAccessTree);
		GraphUtils.connectWithParent(arrayAccessNode, t);
		ASTVisitorResult res = scan(arrayAccessTree.getExpression(), Pair.createPair(arrayAccessNode,
				RelationTypes.ARRAYACCESS_EXPR, PDGProcessing.modifiedToStateModified(t)));
		scan(arrayAccessTree.getIndex(), Pair.createPair(arrayAccessNode, RelationTypes.ARRAYACCESS_INDEX));
		return res;

	}

	@Override
	public ASTVisitorResult visitArrayType(ArrayTypeTree arrayTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
//		System.out.println("ARRAY TYPE:\n"+arrayTypeTree);
//		System.out.println("ARRAY TYPE:\n"+arrayTypeTree.getKind());
//		System.out.println("ARRAY TYPE:\n"+arrayTypeTree.getClass());

		NodeWrapper arrayTypeNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(arrayTypeTree,
				NodeTypes.ARRAY_TYPE, NodeCategory.AST_TYPE, NodeCategory.AST_NODE);
		GraphUtils.connectWithParent(arrayTypeNode, t);
		String fullyName = ((JCArrayTypeTree) arrayTypeTree).type.toString();
		arrayTypeNode.setProperty("fullyQualifiedName", fullyName);
		// System.out.println(fullyName);
		String[] splittedName = fullyName.split(".");
		arrayTypeNode.setProperty("simpleName",
				splittedName.length == 0 ? fullyName : splittedName[splittedName.length - 1]);
		// System.out.println(arrayTypeTree);
		scan(arrayTypeTree.getType(), Pair.createPair(arrayTypeNode, RelationTypes.TYPE_PER_ELEMENT));

		addClassIdentifier(JavacInfo.getTypeMirror(arrayTypeTree.getType()));
		return null;
	}

	@Override
	public ASTVisitorResult visitAssert(AssertTree assertTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper assertNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(assertTree,
				NodeTypes.ASSERT_STATEMENT);
		GraphUtils.connectWithParent(assertNode, t);

		scan(assertTree.getCondition(), Pair.createPair(assertNode, RelationTypes.ASSERT_CONDITION));
		addInvocationInStatement(assertNode);
		methodState.putCfgNodeInCache(assertTree, assertNode);
		scan(assertTree.getDetail(), Pair.createPair(assertNode, RelationTypes.ASSERT_DETAIL));
		return null;
	}

	// private void setLabelForAssignmentXX(NodeWrapper assignNode, boolean
	// isAssign, boolean isInstanceAssign) {
	// // AQUI PA LAS ASIGNACIONES SE PUEDE INCLUIR EL ATRIBUTO
	// // IS_INSTANCE_ASSIG
	// // System.out.println(as);
	// // System.out.println("ISISTANCE\t" + isInstanceAssign);
	// // System.out.println("INSCONS\t" + insideConstructor);
	// if (isInstanceAssign)
	// if (insideConstructor)
	// assignNode.addLabel(NodeTypes.INITIALIZATION);
	// else {
	// // System.out.println(tree);
	// // System.out.println(NodeUtils.nodeToString(parent));
	// // System.out.println(methodState);
	// // System.out.println(methodState.instanceAssigns);
	// methodState.instanceAssigns.put(assignNode, isAssign);
	// }
	// else if (isAssign)
	// assignNode.addLabel(NodeTypes.ASSIGNMENT);
	// // System.out.println(NodeUtils.nodeToString(assignNode));
	//
	// }
	/*
	 * private boolean enclosesCurrentClass(Symbol classSymbol, ClassSymbol current)
	 * { do if (current == classSymbol || (current = current.enclClass()) ==
	 * classSymbol) return true; while (current != null && current !=
	 * current.enclClass()); /* do { System.out.println("CURR:" + current);
	 * 
	 * System.out.println("CURR ENCL:" + current.enclClass());
	 * System.out.println("CURR B:" + (ClassSymbol) current.getSuperclass().tsym);
	 * if (current == classSymbol || (current = (ClassSymbol)
	 * current.getSuperclass().tsym) == classSymbol) return true; } while (current
	 * != null && current != current.enclClass()); return false;
	 * 
	 * boolean isInstanceAssign =
	 * IsInstanceFieldExpression.GET_ID_VISITOR.scan(assignmentTree.getVariable( ),
	 * null); }
	 */

	private NodeWrapper beforeScanAnyAssign(NodeWrapper assignmentNode,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		assignmentNode.setProperty("mustBeExecuted", must);
		NodeWrapper previousAssignment = pdgUtils.lastAssignment;
		// AQUI SE REPITE COMPUTACION; DEBER�A INTEGRARSE EN EL PDGVISITOR, o
		// mejor dicho en este mismo
		pdgUtils.lastAssignment = assignmentNode;

		return previousAssignment;
	}

	private void afterScanAnyAssign(NodeWrapper previousAssignment) {

		pdgUtils.lastAssignment = previousAssignment;
	}

	// 1 METODO COMUN A TODAS LAS ASIGNACIONES
	// comprobar i++ and i+= used and mod y assign i = ... is not used i
	// USaR LA INFO DE LASTASSIGN EN EL METODO ADDASSIGN JEJEJEJEJ
	@Override
	public ASTVisitorResult visitAssignment(AssignmentTree assignmentTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper assignmentNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(assignmentTree,
				NodeTypes.ASSIGNMENT);

		GraphUtils.connectWithParent(assignmentNode, t);
		attachTypeDirect(assignmentNode, assignmentTree);
		if (outsideAnnotation) {
			NodeWrapper previousLastASsignInfo = beforeScanAnyAssign(assignmentNode, t);
			scan(assignmentTree.getVariable(), Pair.createPair(assignmentNode, RelationTypes.ASSIGNMENT_LHS,
					PDGProcessing.getLefAssignmentArg(t)));

			afterScanAnyAssign(previousLastASsignInfo);
			scan(assignmentTree.getExpression(),
					Pair.createPair(assignmentNode, RelationTypes.ASSIGNMENT_RHS, PDGProcessing.USED));
		} else {
			scan(assignmentTree.getVariable(), Pair.createPair(assignmentNode, RelationTypes.ASSIGNMENT_LHS,
					PDGProcessing.getLefAssignmentArg(t)));
			scan(assignmentTree.getExpression(),
					Pair.createPair(assignmentNode, RelationTypes.ASSIGNMENT_RHS, PDGProcessing.USED));
		}

		return null;

	}

	@Override
	public ASTVisitorResult visitBinary(BinaryTree binaryTree, Pair<PartialRelation<RelationTypes>, Object> t) {
//System.out.println(binaryTree);
		NodeWrapper binaryNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(binaryTree,
				NodeTypes.BINARY_OPERATION);
		binaryNode.setProperty("operator", binaryTree.getKind().toString());
		attachTypeDirect(binaryNode, binaryTree);
		GraphUtils.connectWithParent(binaryNode, t);

		scan(binaryTree.getLeftOperand(), Pair.createPair(binaryNode, RelationTypes.BINOP_LHS));
		scan(binaryTree.getRightOperand(), Pair.createPair(binaryNode,

				binaryTree.getKind().toString().contentEquals("OR")
						|| binaryTree.getKind().toString().contentEquals("AND") ? RelationTypes.BINOP_COND_RHS
								: RelationTypes.BINOP_RHS));
		return null;
	}

	private NodeWrapper lastBlockVisited;

	@Override
	public ASTVisitorResult visitBlock(BlockTree blockTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		lastBlockVisited = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(blockTree, NodeTypes.BLOCK);
		lastBlockVisited.setProperty("isStatic", blockTree.isStatic());
		boolean isStaticInit = t.getFirst().getRelationType() == RelationTypes.HAS_STATIC_INIT;
		MethodState prevState = null;
		if (isStaticInit) {
			prevState = methodState;
			methodState = new MethodState(lastStaticConsVisited = lastBlockVisited);
			pdgUtils.visitNewMethod();
			ast.newMethodDeclaration(methodState);
		}

		GraphUtils.connectWithParent(lastBlockVisited, t);

		// Se debe elegir entre filtrar por ENCLOSES relationType o usar todas
		// las relaciones salientes pero crear un nodo STATIC_CONS_DEC que
		// reciba y retorne void o que no reciba ni retorne nada....
		scan(blockTree.getStatements(), Pair.createPair(lastBlockVisited, RelationTypes.ENCLOSES));
		if (isStaticInit) {

			// pdgUtils.endMethod(methodState, classState.currentClassDec);
			methodState = prevState;
			ast.endMethodDeclaration();
		}

		return null;

	}

	@Override
	public ASTVisitorResult visitBreak(BreakTree breakTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		anyBreak = false;
		NodeWrapper breakNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(breakTree,
				NodeTypes.BREAK_STATEMENT);
		methodState.putCfgNodeInCache(breakTree, breakNode);
		must = false;
		if (breakTree.getLabel() != null) {
			breakNode.setProperty("label", breakTree.getLabel().toString());
			if (inADoWhile) {
				gotoLabelsInDoWhile.add(breakTree.getLabel());
				auxMust = prevMust;
				prevMust = false;
			}
		}
		GraphUtils.connectWithParent(breakNode, t);

		return null;
	}

	@Override
	public ASTVisitorResult visitCase(CaseTree caseTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper caseNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(caseTree,
				NodeTypes.CASE_STATEMENT);
		GraphUtils.connectWithParent(caseNode, t);
		// Si hay un case default y no hay ning�n break en el switch, seguro que
		// pasa

		prevMust = must;
		boolean isAUnconditionalDefault = caseTree.getExpressions().isEmpty() && !anyBreak;
		must = prevMust && isAUnconditionalDefault;
		if (!isAUnconditionalDefault)
			pdgUtils.enteringNewBranch();
		scan(caseTree.getExpressions(), Pair.createPair(caseNode, RelationTypes.CASE_EXPR));
		scan(caseTree.getStatements(), Pair.createPair(caseNode, RelationTypes.CASE_STATEMENTS));

		must = prevMust;
		return isAUnconditionalDefault ? null : new VisitorResultImpl(pdgUtils.exitingCurrentBranch());

	}

	@Override
	public ASTVisitorResult visitCatch(CatchTree catchTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper catchNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(catchTree, NodeTypes.CATCH_BLOCK);
		methodState.putCfgNodeInCache(catchTree, catchNode);
		GraphUtils.connectWithParent(catchNode, t);

		prevMust = must;
		must = false;
		pdgUtils.enteringNewBranch();
		scan(catchTree.getParameter(), Pair.createPair(catchNode, RelationTypes.CATCH_PARAM));
		scan(catchTree.getBlock(), Pair.createPair(catchNode, RelationTypes.CATCH_ENCLOSES_BLOCK));
		pdgUtils.exitingCurrentBranch();
		must = prevMust;
		return null;
	}

	@Override

	public ASTVisitorResult visitClass(ClassTree classTree, Pair<PartialRelation<RelationTypes>, Object> pair) {
		if (DEBUG)
			System.out.println("Visitando clase " + classTree.getSimpleName());
//		 System.out.println(" clase " + classTree.getSimpleName() + "(" +
//		 classTree.getClass() + ")");

		// }

//		System.out.println("CURRENT CLASS:\n"+classTree);

		ClassSymbol previousClassSymbol = currentTypeDecSymbol;
		currentTypeDecSymbol = ((JCClassDecl) classTree).sym;
		// Console s = System.console();
		String simpleName = classTree.getSimpleName().toString();

		String fullyQualifiedType = currentTypeDecSymbol.toString();
		if (simpleName.equals("")) {
			// System.out.println(fullyQualifiedType);
			String[] split = fullyQualifiedType.split(fullyQualifiedType.contains(".") ? "\\." : " ");
			simpleName = split[split.length - 1];
			simpleName = simpleName.substring(0, simpleName.length() - 1);
		}

		NodeWrapper classNode = DatabaseFachade.CURRENT_DB_FACHADE.createTypeDecNode(classTree, simpleName,
				fullyQualifiedType);
		classNode.addLabel(NodeCategory.AST_NODE);

		ast.typeDecNodes.add(classNode);

		ClassState previousClassState = classState;
		classState = new ClassState(classNode);

		Set<NodeWrapper> previousTypeDecUses = typeDecUses;
		typeDecUses = new HashSet<NodeWrapper>();

		Symbol outerMostClass = ((JCClassDecl) classTree).sym.outermostClass();

		if (currentTypeDecSymbol != outerMostClass)
			addClassIdentifier(outerMostClass);
		if (!pair.getFirst().getStartingNode().hasLabel(NodeTypes.COMPILATION_UNIT))
			currentCU.createRelationshipTo(classNode, CDGRelationTypes.HAS_INNER_TYPE_DEF);
		// System.out.println("CREATING REL INNER to\n" + classTree);

		GraphUtils.connectWithParent(classNode, pair, RelationTypes.HAS_TYPE_DEF);

		DefinitionCache.TYPE_CACHE.putClassDefinition(currentTypeDecSymbol, classNode, ast.typeDecNodes,
				typeDecUses);

		TypeHierarchy.addTypeHierarchy(currentTypeDecSymbol, classNode, this, ast);
		boolean prevIsInAccesibleContext = isInAccessibleContext;
		if (pair.getFirst().getRelationType() == RelationTypes.NEW_CLASS_BODY) {
			visitAnonymousClassModifiers(classTree.getModifiers(), classNode);

			isInAccessibleContext = false;
		} else {
			scan(classTree.getModifiers(), Pair.createPair(classNode, null));

			isInAccessibleContext = isInAccessibleContext
					&& classNode.getProperty("accessLevel").toString().contentEquals("public");
		}

		// scan(classTree.getTypeParameters(), Pair.createPair(classNode,
		// RelationTypes.HAS_CLASS_TYPEPARAMETERS));
		for (int i = 0; i < classTree.getTypeParameters().size(); i++)
			scan(classTree.getTypeParameters().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(classNode,
							RelationTypes.HAS_CLASS_TYPEPARAMETERS, "paramIndex", i + 1)));
		scan(classTree.getExtendsClause(), Pair.createPair(classNode, RelationTypes.HAS_EXTENDS_CLAUSE));

		scan(classTree.getImplementsClause(), Pair.createPair(classNode, RelationTypes.HAS_IMPLEMENTS_CLAUSE));

		List<NodeWrapper> attrs = new ArrayList<NodeWrapper>(), staticAttrs = new ArrayList<NodeWrapper>(),
				constructors = new ArrayList<NodeWrapper>();
		NodeWrapper prevStaticCons = lastStaticConsVisited;

		scan(classTree.getMembers(), Pair.createPair(classNode, RelationTypes.HAS_STATIC_INIT,
				Pair.create(Pair.create(attrs, staticAttrs), constructors)));

		if (DEBUG)
			System.out.println(
					"Attrs found: " + attrs.size() + " S :" + staticAttrs.size() + " C : " + constructors.size());
		for (NodeWrapper constructor : constructors)
			for (NodeWrapper instanceAttr : attrs)
				callsFromVarDecToConstructor(instanceAttr, constructor);
		// Depending on the java version a static cons is included or not, so if
		// it is null we can create it, but now are not
		if (lastStaticConsVisited != null)
			for (NodeWrapper staticAttr : staticAttrs)
				callsFromVarDecToConstructor(staticAttr, lastStaticConsVisited);
		// this.isInAInnerClass = previousIsInner;
		lastStaticConsVisited = prevStaticCons;
		// this.fullNamePrecedent = previusPrecedent;
		// pdgUtils.endVisitClass();
		classState = previousClassState;
		typeDecUses = previousTypeDecUses;
		currentTypeDecSymbol = previousClassSymbol;
		return null;

	}

	private static void callsFromVarDecToConstructor(NodeWrapper attr, NodeWrapper constructor) {
		for (RelationshipWrapper r : attr.getRelationships(Direction.OUTGOING, CGRelationTypes.CALLS)) {
			RelationshipWrapper callRelation = constructor.createRelationshipTo(r.getEndNode(), CGRelationTypes.CALLS);
			callRelation.setProperty("mustBeExecuted", r.getProperty("mustBeExecuted"));
			r.delete();
		}
	}

	@Override
	public ASTVisitorResult visitCompilationUnit(CompilationUnitTree compilationUnitTree,
			Pair<PartialRelation<RelationTypes>, Object> pair) {
		// DEFAULT

		// String fileName =
		// compilationUnitTree.getSourceFile().getName().toString();
//		 System.out.println("CU:\n" +
//		 compilationUnitTree.getSourceFile().getName().toString());
//		 System.out.println(compilationUnitTree);


		// if (DEBUG)
		// System.out.println(fileName);
		if (first) {
			currentCU.setProperty("packageName", ((JCCompilationUnit) compilationUnitTree).packge.toString());
			scan(compilationUnitTree.getPackageAnnotations(), pair);
			// scan(packageDec, p);
			scan(compilationUnitTree.getImports(), pair);
//			System.out.println(compilationUnitTree);
		} // scan(compilationUnitTree.getTypeDecls(), pair);
		if (compilationUnitTree.getTypeDecls().size() == 0)
			return null;

		scan(typeDec, pair);

		return null;
	}

	@Override
	public ASTVisitorResult visitCompoundAssignment(CompoundAssignmentTree compoundAssignmentTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		NodeWrapper assignmentNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(compoundAssignmentTree,
				NodeTypes.COMPOUND_ASSIGNMENT);
		assignmentNode.setProperty("operator", compoundAssignmentTree.getKind().toString());

		GraphUtils.connectWithParent(assignmentNode, t);
		attachTypeDirect(assignmentNode, compoundAssignmentTree);
		NodeWrapper lasAssignInfo = beforeScanAnyAssign(assignmentNode, t);

		// THIS CANNOT BE THE SAME AS VISITAS I mean
		// PDGVisitor.getLefAssignmentArg(t), I need USED_AND_MOD
		scan(compoundAssignmentTree.getVariable(), Pair.createPair(assignmentNode,
				RelationTypes.COMPOUND_ASSIGNMENT_LHS, PDGProcessing.getLefAssignmentArg(t)));
		afterScanAnyAssign(lasAssignInfo);
		scan(compoundAssignmentTree.getExpression(),
				Pair.createPair(assignmentNode, RelationTypes.COMPOUND_ASSIGNMENT_RHS, PDGProcessing.USED));
		return null;
	}

	@Override
	public ASTVisitorResult visitConditionalExpression(ConditionalExpressionTree conditionalTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper conditionalExprNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(conditionalTree,
				NodeTypes.CONDITIONAL_EXPRESSION);
		attachTypeDirect(conditionalExprNode, conditionalTree);
		GraphUtils.connectWithParent(conditionalExprNode, t);

		scan(conditionalTree.getCondition(),
				Pair.createPair(conditionalExprNode, RelationTypes.CONDITIONAL_EXPR_CONDITION));
		prevMust = must;
		must = false;
		pdgUtils.enteringNewBranch();
		scan(conditionalTree.getTrueExpression(),
				Pair.createPair(conditionalExprNode, RelationTypes.CONDITIONAL_EXPR_THEN));
		Set<NodeWrapper> paramsThen = pdgUtils.exitingCurrentBranch();
		pdgUtils.enteringNewBranch();
		scan(conditionalTree.getFalseExpression(),
				Pair.createPair(conditionalExprNode, RelationTypes.CONDITIONAL_EXPR_ELSE));
		must = prevMust;
		pdgUtils.merge(paramsThen, pdgUtils.exitingCurrentBranch());
		return null;
	}

	@Override
	public ASTVisitorResult visitContinue(ContinueTree continueTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper continueNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(continueTree,
				NodeTypes.CONTINUE_STATEMENT);
		methodState.putCfgNodeInCache(continueTree, continueNode);
		if (continueTree.getLabel() != null) {
			continueNode.setProperty("label", continueTree.getLabel().toString());
			if (inADoWhile) {
				gotoLabelsInDoWhile.add(continueTree.getLabel());
				auxMust = prevMust;
				prevMust = false;
			}
		}
		must = false;
		GraphUtils.connectWithParent(continueNode, t);
		return null;
	}

	@Override
	public ASTVisitorResult visitDoWhileLoop(DoWhileLoopTree doWhileLoopTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper doWhileLoopNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(doWhileLoopTree,
				NodeTypes.DO_WHILE_LOOP);
		GraphUtils.connectWithParent(doWhileLoopNode, t);
		boolean prevInWh = inADoWhile, prevMust = must;
		inADoWhile = true;
//		pdgUtils.enteringNewBranch();

		scan(doWhileLoopTree.getStatement(), Pair.createPair(doWhileLoopNode, RelationTypes.DO_WHILE_STATEMENT));
		scan(doWhileLoopTree.getCondition(), Pair.createPair(doWhileLoopNode, RelationTypes.DO_WHILE_CONDITION));
		addInvocationInStatement(doWhileLoopNode);
		inADoWhile = prevInWh;
		if (t.getSecond() != null && gotoLabelsInDoWhile.size() > 0) {
			gotoLabelsInDoWhile.remove(t.getSecond());
			if (gotoLabelsInDoWhile.size() == 0) {
				prevMust = auxMust;
			}
		}
//		if (!must)
//			pdgUtils.exitingCurrentBranch();
		must = prevMust;
		methodState.putCfgNodeInCache(doWhileLoopTree, doWhileLoopNode);
		return null;
	}

	@Override
	public ASTVisitorResult visitEmptyStatement(EmptyStatementTree emptyStatementTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper emptyStatementNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(emptyStatementTree,
				NodeTypes.EMPTY_STATEMENT);
		methodState.putCfgNodeInCache(emptyStatementTree, emptyStatementNode);
		GraphUtils.connectWithParent(emptyStatementNode, t);

		return null;
	}

	@Override
	public ASTVisitorResult visitEnhancedForLoop(EnhancedForLoopTree enhancedForLoopTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		NodeWrapper enhancedForLoopNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(enhancedForLoopTree,
				NodeTypes.FOR_EACH_LOOP);
		GraphUtils.connectWithParent(enhancedForLoopNode, t);
		scan(enhancedForLoopTree.getVariable(), Pair.createPair(enhancedForLoopNode, RelationTypes.FOREACH_VAR));
		scan(enhancedForLoopTree.getExpression(), Pair.createPair(enhancedForLoopNode, RelationTypes.FOREACH_EXPR));
		addInvocationInStatement(enhancedForLoopNode);
		methodState.putCfgNodeInCache(enhancedForLoopTree, enhancedForLoopNode);
		prevMust = must;
		must = false;
		pdgUtils.enteringNewBranch();
		scan(enhancedForLoopTree.getStatement(), Pair.createPair(enhancedForLoopNode, RelationTypes.FOREACH_STATEMENT));
		pdgUtils.exitingCurrentBranch();
		must = prevMust;
		return null;
	}

	@Override
	public ASTVisitorResult visitErroneous(ErroneousTree erroneousTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		NodeWrapper erroneousNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(erroneousTree,
				NodeTypes.ERRONEOUS_NODE);
		attachTypeDirect(erroneousNode, erroneousTree);
		GraphUtils.connectWithParent(erroneousNode, t);
		scan(erroneousTree.getErrorTrees(), Pair.createPair(erroneousNode, RelationTypes.ERRONEOUS_NODE_CAUSED_BY));
		return null;
	}

	@Override
	public ASTVisitorResult visitExpressionStatement(ExpressionStatementTree expressionStatementTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper expressionStatementNode = DatabaseFachade.CURRENT_DB_FACHADE
				.createSkeletonNode(expressionStatementTree, NodeTypes.EXPRESSION_STATEMENT);
		GraphUtils.connectWithParent(expressionStatementNode, t);

		scan(expressionStatementTree.getExpression(), Pair.createPair(expressionStatementNode,
				RelationTypes.ENCLOSES_EXPR, PDGProcessing.getExprStatementArg(expressionStatementTree)));
		addInvocationInStatement(expressionStatementNode);
		// System.out.println("PUTTING \n:");
		// System.out.println(expressionStatementTree);
		// System.out.println(expressionStatementNode);

		methodState.putCfgNodeInCache(expressionStatementTree, expressionStatementNode);

		return null;
	}

	@Override
	public ASTVisitorResult visitForLoop(ForLoopTree forLoopTree, Pair<PartialRelation<RelationTypes>, Object> t) {
//		System.out.println(forLoopTree);

		NodeWrapper forLoopNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(forLoopTree,
				NodeTypes.FOR_LOOP);
		GraphUtils.connectWithParent(forLoopNode, t);

		scan(forLoopTree.getInitializer(), Pair.createPair(forLoopNode, RelationTypes.FORLOOP_INIT));
		scan(forLoopTree.getCondition(), Pair.createPair(forLoopNode, RelationTypes.FORLOOP_CONDITION));
		addInvocationInStatement(forLoopNode);
		methodState.putCfgNodeInCache(forLoopTree, forLoopNode);
		prevMust = must;
		must = false;

		pdgUtils.enteringNewBranch();
		scan(forLoopTree.getStatement(), Pair.createPair(forLoopNode, RelationTypes.FORLOOP_STATEMENT));
		scan(forLoopTree.getUpdate(), Pair.createPair(forLoopNode, RelationTypes.FORLOOP_UPDATE));

		pdgUtils.exitingCurrentBranch();
		must = prevMust;

		return null;
	}

	static int foo;

	@Override
	public ASTVisitorResult visitIdentifier(IdentifierTree identifierTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		// System.out.println(identifierTree);
		NodeWrapper identifierNode;
		ElementKind idKind = ((JCIdent) identifierTree).sym.getKind();
		if (idKind == ElementKind.PACKAGE)
			identifierNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(identifierTree,
					NodeTypes.IDENTIFIER);
		else if (idKind == ElementKind.CLASS || idKind == ElementKind.ENUM || idKind == ElementKind.INTERFACE
				|| idKind == ElementKind.ANNOTATION_TYPE || idKind == ElementKind.TYPE_PARAMETER)
			identifierNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(identifierTree,
					NodeTypes.IDENTIFIER, NodeCategory.AST_TYPE);
		else
			identifierNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(identifierTree,
					NodeTypes.IDENTIFIER, NodeCategory.LVALUE, NodeCategory.EXPRESSION);

		// identifierNode.setProperty();
		// It can be useful or not, by the moment it is not necessary for
		// coding
		// any rule. so it is commented
		identifierNode.setProperty("name", identifierTree.getName().toString());
		attachTypeDirect(identifierNode, identifierTree);
		GraphUtils.connectWithParent(identifierNode, t);
		if (outsideAnnotation)
			return new VisitorResultImpl(pdgUtils.relationOnIdentifier(identifierTree, identifierNode, t,
					classState.currentClassDec, methodState));
		else
			return null;
	}

	@Override
	public ASTVisitorResult visitIf(IfTree ifTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper ifNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(ifTree, NodeTypes.IF_STATEMENT);
		GraphUtils.connectWithParent(ifNode, t);
		scan(ifTree.getCondition(), Pair.createPair(ifNode, RelationTypes.IF_CONDITION));
		addInvocationInStatement(ifNode);
		methodState.putCfgNodeInCache(ifTree, ifNode);

		prevMust = must;
		must = false;

		pdgUtils.enteringNewBranch();
		scan(ifTree.getThenStatement(), Pair.createPair(ifNode, RelationTypes.IF_THEN));
		Set<NodeWrapper> paramsThen = pdgUtils.exitingCurrentBranch();
		scan(ifTree.getElseStatement(), Pair.createPair(ifNode, RelationTypes.IF_ELSE));

		pdgUtils.merge(paramsThen, pdgUtils.exitingCurrentBranch());
		must = prevMust;

		return null;
	}

	@Override
	public ASTVisitorResult visitImport(ImportTree importTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper importNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(importTree, NodeTypes.IMPORT);
		importNode.setProperty("qualifiedIdentifier", importTree.getQualifiedIdentifier().toString());
		importNode.setProperty("isStatic", importTree.isStatic());

		GraphUtils.connectWithParent(importNode, t, RelationTypes.IMPORTS);
		// Posteriormente relacionar classfile con classfile o con typedec....
		// En caso de imports as� import a.b.* liada jejeje
		return null;
	}

	@Override
	public ASTVisitorResult visitInstanceOf(InstanceOfTree instanceOfTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
//		System.out.println(instanceOfTree);
		NodeWrapper instanceOfNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(instanceOfTree,
				NodeTypes.INSTANCE_OF);
		GraphUtils.attachTypeDirect(instanceOfNode, instanceOfTree, "boolean", "BOOLEAN", ast);
		GraphUtils.connectWithParent(instanceOfNode, t);
//		System.out.println( instanceOfTree.getType().);
		addClassIdentifier(JavacInfo.getTypeMirror(instanceOfTree.getType()));
		scan(instanceOfTree.getExpression(), Pair.createPair(instanceOfNode, RelationTypes.INSTANCE_OF_EXPR));
		scan(instanceOfTree.getType(), Pair.createPair(instanceOfNode, RelationTypes.INSTANCE_OF_TYPE));

		return null;
	}

	@Override
	public ASTVisitorResult visitIntersectionType(IntersectionTypeTree intersectionTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		// System.out.println(intersectionTypeTree);
		NodeWrapper intersectionTypeNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(
				intersectionTypeTree, NodeTypes.INTERSECTION_TYPE, NodeCategory.AST_TYPE, NodeCategory.AST_NODE);

		GraphUtils.connectWithParent(intersectionTypeNode, t);

		scan(intersectionTypeTree.getBounds(),
				Pair.createPair(intersectionTypeNode, RelationTypes.INTERSECTION_COMPOSED_OF));

		return null;
	}

	@Override
	public ASTVisitorResult visitLabeledStatement(LabeledStatementTree labeledStatementTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper labeledStatementNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(labeledStatementTree,
				NodeTypes.LABELED_STATEMENT);
		// methodState.putCfgNodeInCache(this,labeledStatementTree,
		// labeledStatementNode);
		labeledStatementNode.setProperty("name", labeledStatementTree.getLabel().toString());
		GraphUtils.connectWithParent(labeledStatementNode, t);
		methodState.putCfgNodeInCache(labeledStatementTree, labeledStatementNode);
		scan(labeledStatementTree.getStatement(), Pair.createPair(labeledStatementNode,
				RelationTypes.LABELED_STMT_ENCLOSES, labeledStatementTree.getLabel()));
		return null;
	}

	@Override
	public ASTVisitorResult visitLambdaExpression(LambdaExpressionTree lambdaExpressionTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper lambdaExpressionNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(lambdaExpressionTree,
				NodeTypes.LAMBDA_EXPRESSION);
		lambdaExpressionNode.setProperty("bodyKind", lambdaExpressionTree.getBodyKind().toString());
		GraphUtils.connectWithParent(lambdaExpressionNode, t);
		attachTypeDirect(lambdaExpressionNode, lambdaExpressionTree);

		MethodState prevState = methodState;
		methodState=new MethodState(lambdaExpressionNode);
		pdgUtils.visitNewMethod();
		ast.newMethodDeclaration(methodState);

		boolean prevInside = insideConstructor;
		insideConstructor = false;
		boolean prevIsInAccesibleCtxt = isInAccessibleContext;
		isInAccessibleContext = false;
		inALambda=true;
		scan(lambdaExpressionTree.getBody(),
				Pair.createPair(lambdaExpressionNode, RelationTypes.LAMBDA_EXPRESSION_BODY));
		//DEBERIAMOS PROCESAR PRIMERO LOS PARAMETROS??
		for (int i = 0; i < lambdaExpressionTree.getParameters().size(); i++)
			scan(lambdaExpressionTree.getParameters().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(lambdaExpressionNode,
							RelationTypes.LAMBDA_EXPRESSION_PARAMETERS, "paramIndex", i + 1)));
		// scan(lambdaExpressionTree.getParameters(),
		// Pair.createPair(lambdaExpressionNode,
		// RelationTypes.LAMBDA_EXPRESSION_PARAMETERS));
		inALambda=false;
		//lambda expressions do not have this ref!
//		pdgUtils.setThisRefOfInstanceMethod(methodState, classState.currentClassDec);
//		This line is to do posterior processing like PDG intraprocedural
//		ast.addInfo(methodTree, methodNode, methodState);

		//Currently, we do not support control flow analysis of lambdas
//			CFGVisitor.doCFGAnalysis(methodNode, methodTree, methodState.cfgNodeCache,
//					ast.getTrysToExceptionalPartialRelations(methodState.invocationsInStatements),
//					methodState.finallyCache);
		insideConstructor = prevInside;
		isInAccessibleContext = prevIsInAccesibleCtxt;
		must = true;
		methodState = prevState;
		ast.endMethodDeclaration();

		return null;
	}

	@Override
	public ASTVisitorResult visitLiteral(LiteralTree literalTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper literalNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(literalTree, NodeTypes.LITERAL);
		literalNode.setProperty("typetag", literalTree.getKind().toString());
		if (literalTree.getValue() != null)
			literalNode.setProperty("value", literalTree.getValue().toString());

		attachTypeDirect(literalNode, literalTree);
		GraphUtils.connectWithParent(literalNode, t);

		return null;

	}

	@Override
	public ASTVisitorResult visitMemberReference(MemberReferenceTree memberReferenceTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		NodeWrapper memberReferenceNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(memberReferenceTree,
				NodeTypes.MEMBER_REFERENCE);
		memberReferenceNode.setProperty("mode", memberReferenceTree.getMode().name());
		memberReferenceNode.setProperty("name", memberReferenceTree.getName().toString());
		GraphUtils.connectWithParent(memberReferenceNode, t);
		attachTypeDirect(memberReferenceNode, memberReferenceTree);

		scan(memberReferenceTree.getQualifierExpression(),
				Pair.createPair(memberReferenceNode, RelationTypes.MEMBER_REFERENCE_EXPRESSION));

		// scan(memberReferenceTree.getTypeArguments(),
		// Pair.createPair(memberReferenceNode,
		// RelationTypes.MEMBER_REFERENCE_TYPE_ARGUMENTS));
		if (memberReferenceTree.getTypeArguments() != null)
			for (int i = 0; i < memberReferenceTree.getTypeArguments().size(); i++)
				scan(memberReferenceTree.getTypeArguments().get(i),
						Pair.createPair(new PartialRelationWithProperties<RelationTypes>(memberReferenceNode,
								RelationTypes.MEMBER_REFERENCE_TYPE_ARGUMENTS, "argumentIndex", i + 1)));
		return null;
	}

	private void attachTypeDirect(NodeWrapper exprNode, ExpressionTree exprTree) {
		GraphUtils.attachTypeDirect(exprNode, exprTree, ast);
	}

	@Override
	public ASTVisitorResult visitMemberSelect(MemberSelectTree memberSelectTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		// System.out.println(memberSelectTree);
		// System.out.println(memberSelectTree);

		// NodeWrapper memberSelect =
		// DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(memberSelectTree,
		// NodeTypes.MEMBER_SELECTION);
		NodeWrapper memberSelectNode;
		Symbol memberSymbol = ((JCFieldAccess) memberSelectTree).sym;
		ElementKind idKind = memberSymbol.getKind();
		if (idKind == ElementKind.PACKAGE)
			memberSelectNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(memberSelectTree,
					NodeTypes.MEMBER_SELECTION);
		else if (idKind == ElementKind.CLASS || idKind == ElementKind.ENUM || idKind == ElementKind.INTERFACE
				|| idKind == ElementKind.ANNOTATION_TYPE || idKind == ElementKind.TYPE_PARAMETER)
			memberSelectNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(memberSelectTree,
					NodeTypes.MEMBER_SELECTION, NodeCategory.AST_TYPE);
		else
			memberSelectNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(memberSelectTree,
					NodeTypes.MEMBER_SELECTION, NodeCategory.LVALUE, NodeCategory.EXPRESSION);
		// System.out.println(NodeUtils.nodeToString(memberSelect));
		memberSelectNode.setProperty("memberName", memberSelectTree.getIdentifier().toString());
		// System.out.println(((JCFieldAccess) memberSelectTree).sym);
		// System.out.println(((JCFieldAccess) memberSelectTree).sym.type ==
		// ((JCFieldAccess) memberSelectTree).type);
		// System.out.println(((JCFieldAccess)
		// memberSelectTree).sym.getClass());
		// if (((JCFieldAccess) memberSelectTree).sym instanceof MethodSymbol)
		// System.out.println(((MethodSymbol) ((JCFieldAccess)
		// memberSelectTree).sym).owner);

		attachTypeDirect(memberSelectNode, memberSelectTree);
		GraphUtils.connectWithParent(memberSelectNode, t);

		// Symbol innerSymbol =
		// JavacInfo.getSymbolFromTree(memberSelectTree.getExpression());

		if (idKind == ElementKind.CLASS || idKind == ElementKind.INTERFACE || idKind == ElementKind.ENUM)
			addClassIdentifier(memberSymbol);

		ASTVisitorResult memberSelResult = scan(memberSelectTree.getExpression(), Pair.createPair(memberSelectNode,
				RelationTypes.MEMBER_SELECT_EXPR, PDGProcessing.modifiedToStateModified(t)));
		if (outsideAnnotation) {
			boolean isInstance = memberSelResult != null && !memberSymbol.isStatic() && memberSelResult.isInstance();
			pdgUtils.relationOnFieldAccess(memberSelectTree, memberSelectNode, t, methodState,
					classState.currentClassDec, isInstance);
			memberSelResult = new VisitorResultImpl(isInstance);

		}
		return memberSelResult;
	}

	private void setMethodModifiersAndAnnotations(Set<Modifier> modifiers, NodeWrapper methodNode, boolean isInterface,
			List<? extends AnnotationTree> annotations) {
		scan(annotations, Pair.createPair(methodNode, RelationTypes.HAS_ANNOTATIONS));
		setMethodModifiers(modifiers, methodNode, isInterface);
	}

	private static void setMethodModifiers(Set<Modifier> modifiers, NodeWrapper methodNode, boolean isInterface) {
		boolean isAbstract = false;
		if (isInterface) {
			boolean isStatic;
			methodNode.setProperty("isStatic", isStatic = modifiers.contains(Modifier.STATIC));
			methodNode.setProperty("isAbstract", isAbstract = !(isStatic || modifiers.contains(Modifier.DEFAULT)));
			if (isAbstract)
				methodNode.setProperty("isStrictfp", false);
			else
				checkStrictfpMod(modifiers, methodNode);
			methodNode.setProperty("isNative", false);
			methodNode.setProperty("isSynchronized", false);
			methodNode.setProperty("isFinal", false);
			methodNode.setProperty("accessLevel", "public");
		} else {
			methodNode.setProperty("isAbstract", isAbstract = modifiers.contains(Modifier.ABSTRACT));
			if (isAbstract) {
				methodNode.setProperty("isFinal", false);
				methodNode.setProperty("isSynchronized", false);
				methodNode.setProperty("isStatic", false);
				methodNode.setProperty("isNative", false);
				methodNode.setProperty("isStrictfp", false);
				modifierAccessLevelToNodeExceptPrivate(modifiers, methodNode);
			} else {
				checkStaticMod(modifiers, methodNode);
				checkFinalMod(modifiers, methodNode);
				checkSynchroMod(modifiers, methodNode);
				checkNativeMod(modifiers, methodNode);
				checkStrictfpMod(modifiers, methodNode);
				modifierAccessLevelToNode(modifiers, methodNode);
			}
		}
	}

	// EL EFECTO DE ESTO EN VISIT METHOD ES SOLO PARA A�ADIR EL ARCO DEPENDS,
	// PORUQE EL IDENTIFICADOR YA SE A�ADE AL SER VISITADO con scan
	// (methodTree.getReturnType() o scan(methodTree.getThrowws...), son s�lo
	// para el USES LO MISMO PAL NEW CLASS LO MISMO PAL VARIABLE NO EST� CLARO
	// PAL METHOD_INV

	private void addClassIdentifier(TypeMirror typeMirror) {
		if (typeMirror instanceof ClassType)
			addClassIdentifier(((ClassType) typeMirror).tsym);

	}

	private void addClassIdentifier(Symbol symbol) {

		NodeWrapper newTypeDec = DefinitionCache.getOrCreateType(symbol.type, ast);
		addToTypeDependencies(newTypeDec, symbol.packge());
	}

	private void addExistingClassIdentifier(Symbol symbol) {

		NodeWrapper newTypeDec = DefinitionCache.getExistingType(symbol.type);
		addToTypeDependencies(newTypeDec, symbol.packge());

	}

	public void addToTypeDependencies(NodeWrapper newTypeDec, Symbol newPackageSymbol) {
		addToTypeDependencies(classState.currentClassDec, newTypeDec, newPackageSymbol, typeDecUses,
				PackageInfo.PACKAGE_INFO.currentPackage);
		// if (!typeDecUses.contains(newTypeDec)) {
		// PackageInfo.PACKAGE_INFO.handleNewDependency(newPackageSymbol);
		// classState.currentClassDec.createRelationshipTo(newTypeDec,
		// CDGRelationTypes.USES_TYPE_DEF);
		// // AuxDebugInfo.lastMessage = null;
		// typeDecUses.add(newTypeDec);
		// }
	}

	public static void addToTypeDependencies(NodeWrapper currentClass, NodeWrapper newTypeDec, Symbol newPackageSymbol,
			Set<NodeWrapper> typeDecUses, Symbol dependentPackage) {
		// if (trace)
		// { System.out.println(!typeDecUses.contains(newTypeDec));
		// System.out.println(newTypeDec.getProperty("fullyQualifiedName"));
		// System.out.println(
		// classState.currentClassDec.getProperty("fullyQualifiedName"));
		// }
		if (!typeDecUses.contains(newTypeDec) && !currentClass.equals(newTypeDec)) {
			PackageInfo.PACKAGE_INFO.handleNewDependency(dependentPackage, newPackageSymbol);
			currentClass.createRelationshipTo(newTypeDec, CDGRelationTypes.USES_TYPE_DEF);
			// AuxDebugInfo.lastMessage = null;
			typeDecUses.add(newTypeDec);
		}
	}

	@Override
	public ASTVisitorResult visitMethod(MethodTree methodTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		if (DEBUG) {
			System.out.println("\tVisiting method declaration " + methodTree.getName());
		System.out.println(((JCMethodDecl)methodTree).getReceiverParameter());
		System.out.println(((JCMethodDecl)methodTree).completesNormally);
		System.out.println(((JCMethodDecl)methodTree).defaultValue);
		System.out.println(((JCMethodDecl)methodTree).sym.isVarArgs());
			System.out.println(methodTree);
		}

//		System.out.println(((JCMethodDecl)methodTree).);


		MethodSymbol methodSymbol = ((JCMethodDecl) methodTree).sym;

		// System.out.println(methodSymbol.isConstructor());
		String name = methodTree.getName().toString(), completeName = methodSymbol.owner + ":" + name,
				fullyQualifiedName = completeName + methodSymbol.type;


//		 System.out.println("METHOD:\t"+fullyQualifiedName);
		NodeWrapper methodNode;

		boolean prev = false;
		RelationTypes rel;
		if (methodSymbol.isConstructor()) {
			// System.out.println(methodTree);
			prev = insideConstructor;
			insideConstructor = true;
			methodNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(methodTree, NodeTypes.CONSTRUCTOR_DEF);

			((Pair<Pair, List<NodeWrapper>>) t.getSecond()).getSecond().add(methodNode);
			rel = RelationTypes.DECLARES_CONSTRUCTOR;

		} else {
			methodNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(methodTree, NodeTypes.METHOD_DEF);
			// System.out.println(t.getFirst().getStartingNode() + " DECLARES
			// METHOD IN VISIT " + methodTree);
			rel = RelationTypes.DECLARES_METHOD;
		}

		if (DefinitionCache.METHOD_DEF_CACHE.containsKey(methodSymbol)) {
			ast.deleteAccesibleMethod(methodSymbol);
			// For methods that are invoked in this class, after the removal of the
			// non-declared edges of the class and before the visit of the method
			DefinitionCache.METHOD_DEF_CACHE.putDefinition(methodSymbol, methodNode);

			if (!methodNode.hasRelationship(rel, Direction.INCOMING))
				GraphUtils.connectWithParent(methodNode, t, rel);
		} else {
			DefinitionCache.METHOD_DEF_CACHE.putDefinition(methodSymbol, methodNode);
			GraphUtils.connectWithParent(methodNode, t, rel);
		}


		setMethodModifiersAndAnnotations(methodTree.getModifiers().getFlags(), methodNode,
				t.getFirst().getStartingNode().hasLabel(NodeTypes.INTERFACE_DEF),
				methodTree.getModifiers().getAnnotations());
		String accessLevel = methodNode.getProperty("accessLevel").toString();

//		System.out.println(NodeUtils.nodeToString(classState.currentClassDec));

		if (!methodSymbol.isConstructor() && isInAccessibleContext
				&& (accessLevel.contentEquals("public") || accessLevel.contentEquals("protected")
						&& !(Boolean) classState.currentClassDec.getProperty("isFinal")))

			ast.addAccesibleMethod(methodSymbol, methodNode);

		// System.out.println(isInAccessibleContext);
		// System.out.println(!methodSymbol.isConstructor());
		// System.out.println((accessLevel.contentEquals("public") ||
		// accessLevel.contentEquals("protected")
		// && !(Boolean) classState.currentClassDec.getProperty("isFinal")));

		boolean prevIsInAccesibleCtxt = isInAccessibleContext;
		isInAccessibleContext = false;
		MethodState prevState = methodState;
		must = true;
		methodState = new MethodState(methodNode);
//		System.out.println("NEW STATE for \n"+methodTree);
		pdgUtils.visitNewMethod();
		ast.newMethodDeclaration(methodState);
		methodNode.setProperty("name", name);
		methodNode.setProperty("fullyQualifiedName", fullyQualifiedName);
		methodNode.setProperty("completeName", completeName);
		methodNode.setProperty("isDeclared", true);
		methodNode.setProperty("isVarArgs",methodSymbol.isVarArgs());

		if (DEBUG) {
			System.out.println("METHOD DECLARATION :" + methodTree.getClass());

			System.out.println("Symbol:" + ((JCMethodDecl) methodTree).sym.toString());
		}

		scan(methodTree.getReturnType(), Pair.createPair(methodNode, RelationTypes.CALLABLE_RETURN_TYPE));

		// methodNode.createRelationshipTo(DefinitionCache.getOrCreateType(((Tree)
		// methodTree.getReturnType()).),
		// RelationTypes.RETURN_TYPE);
		GraphUtils.attachType(methodNode, ((JCMethodDecl) methodTree).type, ast);

		// EL EFECTO DE ESTO ES SOLO PARA A�ADIR EL ARCO DEPENDS, PORUQE EL
		// IDENTIFICADOR YA SE A�ADE AL SER VISITADO
		if (!methodSymbol.isConstructor())
			addClassIdentifier(((JCTree) methodTree.getReturnType()).type);

		// scan(methodTree.getTypeParameters(), Pair.createPair(methodNode,
		// RelationTypes.CALLABLE_HAS_TYPEPARAMETERS));
		for (int i = 0; i < methodTree.getTypeParameters().size(); i++)
			scan(methodTree.getTypeParameters().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(methodNode,
							RelationTypes.CALLABLE_HAS_TYPEPARAMETERS, "paramIndex", i + 1)));
		int nParams=0;
		for (nParams = 0; nParams < methodTree.getParameters().size(); nParams++)
			scan(methodTree.getParameters().get(nParams),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(methodNode,
							RelationTypes.CALLABLE_HAS_PARAMETER, "paramIndex", nParams + 1)));

		methodTree.getThrows().forEach((throwsTree) -> {
			TypeMirror type = ((JCExpression) throwsTree).type;
			addClassIdentifier(type);
			scan(throwsTree, Pair.createPair(methodNode, RelationTypes.CALLABLE_HAS_THROWS));
		});

//		System.out.println("Visiting body:\n"+methodTree.getBody());
		scan(methodTree.getBody(), Pair.createPair(methodNode, RelationTypes.CALLABLE_HAS_BODY));
		scan(methodTree.getDefaultValue(), Pair.createPair(methodNode, RelationTypes.HAS_DEFAULT_VALUE));
		scan(methodTree.getReceiverParameter(), Pair.createPair(methodNode, RelationTypes.HAS_RECEIVER_PARAMETER));

		pdgUtils.setThisRefOfInstanceMethod(methodState, classState.currentClassDec);
		ast.addInfo(methodTree, methodNode, methodState, methodSymbol.isVarArgs()?nParams:ASTAuxiliarStorage.NO_VARG_ARG);

//		System.out.println("Initiating cfg for:\n"+methodTree);
//		System.out.println("... AND BODY "+methodTree.getBody());
		if (methodTree.getBody() != null)
			CFGVisitor.doCFGAnalysis(methodNode, methodTree, methodState.cfgNodeCache,
					ast.getTrysToExceptionalPartialRelations(methodState.invocationsInStatements),
					methodState.finallyCache);
		insideConstructor = prev;
		isInAccessibleContext = prevIsInAccesibleCtxt;
		must = true;
		methodState = prevState;
		ast.endMethodDeclaration();
//		if(fullyQualifiedName.contains("com.intuit.karate.core.ScenarioEngine:executeFunction"))
//			throw new IllegalArgumentException("STOOOP");
		return null;

	}

	@Override
	public ASTVisitorResult visitMethodInvocation(MethodInvocationTree methodInvocationTree,
			Pair<PartialRelation<RelationTypes>, Object> pair) {
//		System.out.println(methodInvocationTree);
		NodeWrapper methodInvocationNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(methodInvocationTree,
				NodeTypes.METHOD_INVOCATION);
//		if(t!=null)
		attachTypeDirect(methodInvocationNode, methodInvocationTree);
//		else
//			GraphUtils.attachType(methodInvocationNode, ((JCExpression)methodInvocationTree)., es.uniovi.reflection.progquery.ast);

		GraphUtils.connectWithParent(methodInvocationNode, pair);

		MethodSymbol methodSymbol =
//				((JCMethodInvocation)methodInvocationTree).
//				
				(MethodSymbol) JavacInfo.getSymbolFromTree(methodInvocationTree.getMethodSelect());
		String methodName = null, completeName = null, fullyQualifiedName = null;
		if (methodInvocationTree.getMethodSelect() instanceof IdentifierTree)
			addClassIdentifier(methodSymbol.owner);
		boolean isInCache = DefinitionCache.METHOD_DEF_CACHE.containsKey(methodSymbol);
		if (!isInCache) {
			methodName = methodSymbol.name.toString();
			completeName = methodSymbol.owner + ":" + methodName;
			fullyQualifiedName = completeName + methodSymbol.type;
		}
		pair = Pair.createPair(methodInvocationNode, RelationTypes.METHODINVOCATION_METHOD_SELECT);
		if (methodSymbol.getThrownTypes().size() > 0)
			currentMethodInvocations.add(methodSymbol);

		NodeWrapper decNode = isInCache ? (NodeWrapper) DefinitionCache.METHOD_DEF_CACHE.get(methodSymbol)
				: methodSymbol.isConstructor()
						? getNotDeclaredConsFromInv(methodSymbol, fullyQualifiedName, completeName)
						: getNotDeclaredMethodDecNode(methodSymbol, fullyQualifiedName, methodName, completeName);
//LATER USED FOR INTERPROCEDURAL PROCESSING
		if(!inALambda) {
			RelationshipWrapper callRelation = methodState.lastMethodDecVisited.createRelationshipTo(methodInvocationNode,
					CGRelationTypes.CALLS);
			callRelation.setProperty("mustBeExecuted", must);
		}
		//////

		methodInvocationNode.createRelationshipTo(decNode, CGRelationTypes.HAS_DEF);
		methodInvocationNode.createRelationshipTo(decNode, CGRelationTypes.REFERS_TO);
		pdgUtils.addParamsPrevModifiedForInv(methodInvocationNode, methodState);
		ast.checkIfTrustableInvocation(methodInvocationTree, methodSymbol, methodInvocationNode);
		scan(methodInvocationTree.getMethodSelect(),
				Pair.createPair(methodInvocationNode, RelationTypes.METHODINVOCATION_METHOD_SELECT));
		// scan(methodInvocationTree.getTypeArguments(),
		// Pair.createPair(methodInvocationNode,
		// RelationTypes.METHODINVOCATION_TYPE_ARGUMENTS));
		for (int i = 0; i < methodInvocationTree.getTypeArguments().size(); i++)
			scan(methodInvocationTree.getTypeArguments().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(methodInvocationNode,
							RelationTypes.METHODINVOCATION_TYPE_ARGUMENTS, "argumentIndex", i + 1)));
		for (int i = 0; i < methodInvocationTree.getArguments().size(); i++)
			scan(methodInvocationTree.getArguments().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(methodInvocationNode,
							RelationTypes.METHODINVOCATION_ARGUMENTS, "argumentIndex", i + 1)));

		return null;
	}

	public static void modifierAccessLevelToNodeForClasses(Set<Modifier> modifiers, NodeWrapper modNode) {
		modNode.setProperty("accessLevel", modifiers.contains(Modifier.PUBLIC) ? "public"
				: modifiers.contains(Modifier.PRIVATE) ? "private" : "package");

	}

	public static void modifierAccessLevelToNode(Set<Modifier> modifiers, NodeWrapper modNode) {
		modNode.setProperty("accessLevel",
				modifiers.contains(Modifier.PUBLIC) ? "public"
						: modifiers.contains(Modifier.PROTECTED) ? "protected"
								: modifiers.contains(Modifier.PRIVATE) ? "private" : "package");

	}

	public static void modifierAccessLevelToNodeExceptPrivate(Set<Modifier> modifiers, NodeWrapper modNode) {
		modNode.setProperty("accessLevel", modifiers.contains(Modifier.PUBLIC) ? "public"
				: modifiers.contains(Modifier.PROTECTED) ? "protected" : "package");

	}

	public static void modifierAccessLevelLimitedToNode(Set<Modifier> modifiers, NodeWrapper modNode) {
		modNode.setProperty("accessLevel", modifiers.contains(Modifier.PUBLIC) ? "public" : "package");

	}

	public static void checkFinalMod(Set<Modifier> modifiers, NodeWrapper node) {
		node.setProperty("isFinal", modifiers.contains(Modifier.FINAL));

	}

	public static void checkStaticMod(Set<Modifier> modifiers, NodeWrapper node) {
		node.setProperty("isStatic", modifiers.contains(Modifier.STATIC));

	}

	public static void checkVolatileMod(Set<Modifier> modifiers, NodeWrapper node) {
		node.setProperty("isVolatile", modifiers.contains(Modifier.VOLATILE));

	}

	public static void checkTransientMod(Set<Modifier> modifiers, NodeWrapper node) {
		node.setProperty("isTransient", modifiers.contains(Modifier.TRANSIENT));
	}

	public static void checkAbstractMod(Set<Modifier> modifiers, NodeWrapper node) {
		node.setProperty("isAbstract", modifiers.contains(Modifier.ABSTRACT));
	}

	public static void checkSynchroMod(Set<Modifier> modifiers, NodeWrapper node) {
		node.setProperty("isSynchronized", modifiers.contains(Modifier.SYNCHRONIZED));
	}

	public static void checkNativeMod(Set<Modifier> modifiers, NodeWrapper node) {
		node.setProperty("isNative", modifiers.contains(Modifier.NATIVE));
	}

	public static void checkStrictfpMod(Set<Modifier> modifiers, NodeWrapper node) {
		node.setProperty("isStrictfp", modifiers.contains(Modifier.STRICTFP));
	}

	private void visitAnonymousClassModifiers(ModifiersTree modifiersTree, NodeWrapper classNode) {

		Pair<PartialRelation<RelationTypes>, Object> n = Pair.createPair(classNode, RelationTypes.HAS_ANNOTATIONS);
		scan(modifiersTree.getAnnotations(), n);
		classNode.setProperty("isStatic", false);
		classNode.setProperty("isAbstract", false);
		classNode.setProperty("isFinal", false);
		classNode.setProperty("accessLevel", "private");
	}

	public static void checkAttrDecModifiers(Set<Modifier> modifiers, NodeWrapper node) {
		checkStaticMod(modifiers, node);
		checkFinalMod(modifiers, node);
		checkVolatileMod(modifiers, node);
		checkTransientMod(modifiers, node);
		modifierAccessLevelToNode(modifiers, node);
	}

	@Override
	public ASTVisitorResult visitModifiers(ModifiersTree modifiersTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		NodeWrapper parent = t.getFirst().getStartingNode();
		Set<Modifier> modifiers = modifiersTree.getFlags();
		// Pair<PartialRelation<RelationTypes>, Object> n = ;
		scan(modifiersTree.getAnnotations(), Pair.createPair(parent, RelationTypes.HAS_ANNOTATIONS));
		if (parent.hasLabel(NodeTypes.LOCAL_VAR_DEF) || parent.hasLabel(NodeTypes.PARAMETER_DEF))
			checkFinalMod(modifiers, parent);
		else if (parent.hasLabel(NodeTypes.ATTR_DEF)) {

			checkAttrDecModifiers(modifiers, parent);

		} else if (parent.hasLabel(NodeTypes.CONSTRUCTOR_DEF)) {
			// checkAbstractMod(modifiers, parent);
			// checkFinalMod(modifiers, parent);
			// checkSynchroMod(modifiers, parent);
			// checkNativeMod(modifiers, parent);
			// checkStrictfpMod(modifiers, parent);
			modifierAccessLevelToNode(modifiers, parent);
		} else if (parent.hasLabel(NodeTypes.CLASS_DEF)) {
			checkStaticMod(modifiers, parent);
			checkAbstractMod(modifiers, parent);
			checkFinalMod(modifiers, parent);
			modifierAccessLevelToNodeForClasses(modifiers, parent);
		} else if (parent.hasLabel(NodeTypes.INTERFACE_DEF)) {
			checkAbstractMod(modifiers, parent);
			modifierAccessLevelLimitedToNode(modifiers, parent);
			parent.setProperty("isFinal", false);
			parent.setProperty("isStatic", false);

		} else if (parent.hasLabel(NodeTypes.ENUM_ELEMENT)) {
			parent.setProperty("isStatic", true);
			parent.setProperty("isFinal", true);
			parent.setProperty("accessLevel", "public");

		} else if (parent.hasLabel(NodeTypes.ENUM_DEF)) {
			modifierAccessLevelLimitedToNode(modifiers, parent);
			parent.setProperty("isFinal", true);
			parent.setProperty("isStatic", false);
		} else
			throw new IllegalStateException(
					"Label with modifiers no checked.\n" + NodeUtils.nodeToString(t.getFirst().getStartingNode()));

		return null;

	}

	@Override
	public ASTVisitorResult visitNewArray(NewArrayTree newArrayTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper newArrayNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(newArrayTree,
				NodeTypes.NEW_ARRAY);
		attachTypeDirect(newArrayNode, newArrayTree);
		GraphUtils.connectWithParent(newArrayNode, t);

		scan(newArrayTree.getType(), Pair.createPair(newArrayNode, RelationTypes.NEW_ARRAY_TYPE));
		scan(newArrayTree.getDimensions(), Pair.createPair(newArrayNode, RelationTypes.NEW_ARRAY_DIMENSION));
		scan(newArrayTree.getInitializers(), Pair.createPair(newArrayNode, RelationTypes.NEW_ARRAY_INIT));
		return null;
	}

	// OJO FALTA REVISAR
	@Override
	public ASTVisitorResult visitNewClass(NewClassTree newClassTree,
			Pair<PartialRelation<RelationTypes>, Object> pair) {
//		System.out.println(newClassTree);
		NodeWrapper newClassNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(newClassTree,
				NodeTypes.NEW_INSTANCE);
		Type type = JavacInfo.getTypeDirect(newClassTree.getIdentifier());
		addClassIdentifier(type);
		// Igual no hace falta el attachType porque la expresi�n siempre es del
		// tipo de la clase pero paquete + identifier no vale, igual se puede
		// hacer algo con el Symbol s y el getTypeMirror para obtener el nombre
		// completo---->Demomento lo dejo as�

		// Aqui sabemos ya que el Kind es DECLARED, falta la cadena
		// System.out.println("attributing"+newClassTree+" with "+type);
		GraphUtils.attachType(newClassNode, type, ast);
		GraphUtils.connectWithParent(newClassNode, pair);
		// Aqu� hay que encontrar la declaracion del constructor de la clase,
		// para relacionar el CALLS, y el �IS_CALLED?
		pdgUtils.addParamsPrevModifiedForInv(newClassNode, methodState);
		scan(newClassTree.getEnclosingExpression(),
				Pair.createPair(newClassNode, RelationTypes.NEWCLASS_ENCLOSING_EXPRESSION));
		scan(newClassTree.getIdentifier(), Pair.createPair(newClassNode, RelationTypes.NEWCLASS_IDENTIFIER));
		// scan(newClassTree.getTypeArguments(), Pair.createPair(newClassNode,
		// RelationTypes.NEW_CLASS_TYPE_ARGUMENTS));
		for (int i = 0; i < newClassTree.getTypeArguments().size(); i++)
			scan(newClassTree.getTypeArguments().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(newClassNode,
							RelationTypes.NEW_CLASS_TYPE_ARGUMENTS, "argumentIndex", i + 1)));
		for (int i = 0; i < newClassTree.getArguments().size(); i++)
			scan(newClassTree.getArguments().get(i),
					Pair.createPair(new PartialRelationWithProperties<RelationTypes>(newClassNode,
							RelationTypes.NEW_CLASS_ARGUMENTS, "argumentIndex", i + 1)));

		scan(newClassTree.getClassBody(), Pair.createPair(newClassNode, RelationTypes.NEW_CLASS_BODY));

		// if ((((JCNewClass) newClassTree).constructorType) == null)
		// System.out.println("NO CONS TYPE: " + newClassTree.toString());
		// else {
//		System.out.println(newClassTree);
//		System.out.println(((JCNewClass) newClassTree).constructor);
//
//		System.out.println(((JCNewClass) newClassTree).constructor.getClass());
Symbol newClassConstructor=((JCNewClass) newClassTree).constructor;
//if(newClassConstructor instanceof ClassSymbol)
//{
//	System.out.println(newClassConstructor);
//	System.out.println(newClassConstructor.getClass());
//	System.out.println(((ClassSymbol)newClassConstructor).isConstructor());
//	System.out.println(((ClassSymbol)newClassConstructor).type);
//	System.out.println(((ClassSymbol)newClassConstructor).owner);
//	System.out.println(((ClassSymbol)newClassConstructor.owner).
//			);

//}
		MethodSymbol consSymbol = (MethodSymbol) newClassConstructor;

		NodeWrapper constructorDef = DefinitionCache.METHOD_DEF_CACHE.get(consSymbol);
		if (constructorDef == null) {
			String consType = consSymbol.type.toString();
			String completeName = consSymbol.owner.toString() + ":<init>";
			constructorDef = getNotDeclaredConsFromInv(consSymbol,
					completeName + consType.substring(0, consType.length() - 4), completeName);
		}
		// Redundancia justificada para las consultas
		newClassNode.createRelationshipTo(constructorDef, CGRelationTypes.HAS_DEF);
		newClassNode.createRelationshipTo(constructorDef, CGRelationTypes.REFERS_TO);

		//Later used for interprocedural processing
		if(!inALambda) {
			RelationshipWrapper callRelation = methodState.lastMethodDecVisited.createRelationshipTo(newClassNode,
					CGRelationTypes.CALLS);
			callRelation.setProperty("mustBeExecuted", must);
		}

		if (consSymbol.getThrownTypes().size() > 0)
			currentMethodInvocations.add(consSymbol);

		// Si no podemos sacar el methodType de la expresi�n como con las
		// invocaciones, tendremos que buscar entre los contructores de la clase
		return null;
	}

	@Override
	public ASTVisitorResult visitOther(Tree arg0, Pair<PartialRelation<RelationTypes>, Object> t) {
		throw new IllegalArgumentException(
				"[EXCEPTION] Tree not included in the visitor: " + arg0.getClass() + "\n" + arg0);
	}

	@Override
	public ASTVisitorResult visitParameterizedType(ParameterizedTypeTree parameterizedTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
//		System.out.println("PARAMETERIZED TYPE:\t" + parameterizedTypeTree);
		// System.out.println(parameterizedTypeTree);
		NodeWrapper parameterizedNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(
				parameterizedTypeTree, NodeTypes.GENERIC_TYPE, NodeCategory.AST_TYPE, NodeCategory.AST_NODE);
		GraphUtils.connectWithParent(parameterizedNode, t);

//		System.out.println("PARAMETERIZED . GETTYPE " + parameterizedTypeTree.getType());
//		System.out.println("PARAMETERIZED . GETTYPE CLASS " + parameterizedTypeTree.getType().getClass());
//		System.out.println(((JCTree)parameterizedTypeTree.getType()).type);
//		System.out.println(((JCTree)parameterizedTypeTree.getType()).type.getClass());

		scan(parameterizedTypeTree.getType(), Pair.createPair(parameterizedNode, RelationTypes.PARAMETERIZED_TYPE));
//		addClassIdentifier(JavacInfo.getTypeMirror(parameterizedTypeTree.getType()));
		addClassIdentifier(((JCTree) parameterizedTypeTree.getType()).type);
		for (int i = 0; i < parameterizedTypeTree.getTypeArguments().size(); i++) {
			Tree typeArg = parameterizedTypeTree.getTypeArguments().get(i);
			addClassIdentifier(((JCTree) typeArg).type);
			scan(typeArg, Pair.createPair(new PartialRelationWithProperties<RelationTypes>(parameterizedNode,
					RelationTypes.GENERIC_TYPE_ARGUMENT, "argumentIndex", i + 1)));
		}

		// TODO INSTEAD of L<> to denote it is parameterized, use the visitor to
		// L<T<U>,D>--> changes return null by types--> NO HACE FALTA EL TIPO DE
		// JAVA YA LO TIENE

		parameterizedNode.setProperty("actualType",
				((JCTypeApply) parameterizedTypeTree).type.tsym.getQualifiedName() + "<>");
		return null;
	}

	@Override
	public ASTVisitorResult visitParenthesized(ParenthesizedTree parenthesizedTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		// Esto no deber�a entrar por aqu�, porque los par�ntesis no deben
		// preservarse, o s�, y soy yo el que los tiene que obviar //Si los dejo
		// puedo detectar fallos de cuando sobran par�ntesis, no s� si sale
		// rentable

		// NodeWrapper parenthesizedNode=
		// DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(parenthesizedTree,
		// NodeTypes.PARENTHESIZED_EXPRESSION);
		// attachTypeDirect(parenthesizedNode, parenthesizedTree);
		// GraphUtils.connectWithParent(parenthesizedNode, t);
		return scan(parenthesizedTree.getExpression(), t);

	}

	@Override
	public ASTVisitorResult visitPrimitiveType(PrimitiveTypeTree primitiveTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
//		System.err.println("PRINTIIING PRIMITIVE TYPE\n" + primitiveTypeTree);
//		System.out.println(		primitiveTypeTree.getKind());
//		System.out.println(		primitiveTypeTree.getPrimitiveTypeKind());
//		System.out.println(		((JCPrimitiveTypeTree) primitiveTypeTree).pos);
//		System.out.println(		((JCPrimitiveTypeTree) primitiveTypeTree).getStartPosition());
//		System.out.println(		((JCPrimitiveTypeTree) primitiveTypeTree).isPoly());
//		System.out.println(		((JCPrimitiveTypeTree) primitiveTypeTree).isStandalone());
//		System.out.println(		((JCPrimitiveTypeTree) primitiveTypeTree).getStartPosition());
		NodeWrapper primitiveTypeNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(
				primitiveTypeTree, NodeTypes.PRIMITIVE_TYPE, NodeCategory.AST_TYPE, NodeCategory.AST_NODE);
		// primitiveTypeNode.setProperty("primitiveTypeKind",
		// primitiveTypeTree.getPrimitiveTypeKind().toString());

		primitiveTypeNode.setProperty("fullyQualifiedName", primitiveTypeTree.toString());
		primitiveTypeNode.setProperty("simpleName", primitiveTypeTree.toString());

		GraphUtils.connectWithParent(primitiveTypeNode, t);
		return null;
	}

	@Override
	public ASTVisitorResult visitReturn(ReturnTree returnTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper returnNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(returnTree,
				NodeTypes.RETURN_STATEMENT);
		methodState.putCfgNodeInCache(returnTree, returnNode);
//		System.out.println(returnTree +" PUT IN CACHE \n hashcode "+returnTree.hashCode());
		GraphUtils.connectWithParent(returnNode, t);
		int hash = returnTree.hashCode();
		scan(returnTree.getExpression(), Pair.createPair(returnNode, RelationTypes.RETURN_EXPR));
		if (returnTree.hashCode() != hash)
			throw new IllegalStateException();
		must = false;
		addInvocationInStatement(returnNode);
		return null;
	}

	@Override
	public ASTVisitorResult visitSwitch(SwitchTree switchTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		NodeWrapper switchNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(switchTree,
				NodeTypes.SWITCH_STATEMENT);
		GraphUtils.connectWithParent(switchNode, t);
		scan(switchTree.getExpression(), Pair.createPair(switchNode, RelationTypes.SWITCH_EXPR));
		addInvocationInStatement(switchNode);
		methodState.putCfgNodeInCache(switchTree, switchNode);
		if (switchTree.getCases().size() > 0) {
			ASTVisitorResult caseResult = visitCase(switchTree.getCases().get(0),
					Pair.createPair(switchNode, RelationTypes.SWITCH_ENCLOSES_CASE));
			Set<NodeWrapper> paramsModifiedInAllCases = caseResult == null ? new HashSet<NodeWrapper>()
					: caseResult.paramsPreviouslyModifiedForSwitch();
			boolean unconditionalFound = caseResult == null;
			for (int i = 1; i < switchTree.getCases().size(); i++) {
				caseResult = scan(switchTree.getCases().get(i),
						Pair.createPair(switchNode, RelationTypes.SWITCH_ENCLOSES_CASE));
				if (caseResult != null)
					paramsModifiedInAllCases.retainAll(caseResult.paramsPreviouslyModifiedForSwitch());
				else
					unconditionalFound = true;
			}
			if (!unconditionalFound
					&& switchTree.getCases().get(switchTree.getCases().size() - 1).getExpression() == null)
				pdgUtils.unionWithCurrent(paramsModifiedInAllCases);

		}
		return null;
	}

	@Override
	public ASTVisitorResult visitSynchronized(SynchronizedTree synchronizedTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		NodeWrapper synchronizedNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(synchronizedTree,
				NodeTypes.SYNCHRONIZED_BLOCK);
		GraphUtils.connectWithParent(synchronizedNode, t);
		methodState.putCfgNodeInCache(synchronizedTree, synchronizedNode);
		scan(synchronizedTree.getExpression(), Pair.createPair(synchronizedNode, RelationTypes.SYNCHRONIZED_EXPR));
		addInvocationInStatement(synchronizedNode);
		scan(synchronizedTree.getBlock(), Pair.createPair(synchronizedNode, RelationTypes.SYNCHRONIZED_ENCLOSES_BLOCK));
		return null;
	}

	@Override
	public ASTVisitorResult visitThrow(ThrowTree throwTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper throwNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(throwTree,
				NodeTypes.THROW_STATEMENT);
		methodState.putCfgNodeInCache(throwTree, throwNode);
		GraphUtils.connectWithParent(throwNode, t);

		scan(throwTree.getExpression(), Pair.createPair(throwNode, RelationTypes.THROW_EXPR));
		addInvocationInStatement(throwNode);
		return null;
	}

	@Override
	public ASTVisitorResult visitTry(TryTree tryTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper tryNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(tryTree, NodeTypes.TRY_STATEMENT);
		GraphUtils.connectWithParent(tryNode, t);
		boolean hasCatchingComponent = tryTree.getCatches().size() > 0 || tryTree.getFinallyBlock() != null;
		if (hasCatchingComponent)
			ast.enterInNewTry(tryTree, methodState);
		scan(tryTree.getResources(), Pair.createPair(tryNode, RelationTypes.TRY_RESOURCES));
		scan(tryTree.getBlock(), Pair.createPair(tryNode, RelationTypes.TRY_BLOCK));
		if (hasCatchingComponent)
			ast.exitTry();
		scan(tryTree.getCatches(), Pair.createPair(tryNode, RelationTypes.TRY_CATCH));

		methodState.putCfgNodeInCache(tryTree, tryNode);
		scan(tryTree.getFinallyBlock(), Pair.createPair(tryNode, RelationTypes.TRY_FINALLY));
		NodeWrapper finallyNode = lastBlockVisited;

		if (tryTree.getFinallyBlock() != null) {
			finallyNode.removeLabel(NodeTypes.BLOCK);
			finallyNode.addLabel(NodeTypes.FINALLY_BLOCK);
			NodeWrapper lastStmtInFinally = DatabaseFachade.CURRENT_DB_FACHADE
					.createNodeWithoutExplicitTree(NodeTypes.CFG_LAST_STATEMENT_IN_FINALLY);
			methodState.putFinallyInCache(tryTree.getFinallyBlock(), finallyNode, lastStmtInFinally);
			finallyNode.createRelationshipTo(lastStmtInFinally, CFGRelationTypes.CFG_FINALLY_TO_LAST_STMT);
		}

		return null;

	}

	@Override
	public ASTVisitorResult visitTypeCast(TypeCastTree typeCastTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper typeCastNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(typeCastTree,
				NodeTypes.TYPE_CAST);
		attachTypeDirect(typeCastNode, typeCastTree);
		GraphUtils.connectWithParent(typeCastNode, t);

		scan(typeCastTree.getType(), Pair.createPair(typeCastNode, RelationTypes.CAST_TYPE));
		scan(typeCastTree.getExpression(), Pair.createPair(typeCastNode, RelationTypes.CAST_ENCLOSES));

		addClassIdentifier(JavacInfo.getTypeMirror(typeCastTree.getType()));
		return null;
	}

	@Override
	public ASTVisitorResult visitTypeParameter(TypeParameterTree typeParameterTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		// System.out.println(NodeUtils.nodeToString(t.getFirst().getStartingNode()));
		// System.out.println(typeParameterTree);
		// System.out.println("TYPE PARAM " + typeParameterTree.toString());
		// System.out.println(typeParameterTree.getBounds().size());
		// System.out.println(typeParameterTree.getBounds().get(0));
		NodeWrapper typeParameterNode = DatabaseFachade.CURRENT_DB_FACHADE
				.createSkeletonNodeExplicitCats(typeParameterTree, NodeTypes.TYPE_PARAM, NodeCategory.AST_NODE);
		typeParameterNode.setProperty("name", typeParameterTree.getName().toString());
		GraphUtils.connectWithParent(typeParameterNode, t);
		scan(typeParameterTree.getAnnotations(), Pair.createPair(typeParameterNode, RelationTypes.HAS_ANNOTATIONS));
		scan(typeParameterTree.getBounds(), Pair.createPair(typeParameterNode, RelationTypes.TYPEPARAMETER_EXTENDS));

		return null;
	}

	@Override
	public ASTVisitorResult visitUnary(UnaryTree unaryTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper unaryNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(unaryTree,
				NodeTypes.UNARY_OPERATION);
		unaryNode.setProperty("operator", unaryTree.getKind().toString());

		boolean impliesModification = unaryTree.getKind() == Kind.POSTFIX_INCREMENT
				|| unaryTree.getKind() == Kind.POSTFIX_DECREMENT || unaryTree.getKind() == Kind.PREFIX_INCREMENT
				|| unaryTree.getKind() == Kind.PREFIX_DECREMENT;

		GraphUtils.connectWithParent(unaryNode, t);
		attachTypeDirect(unaryNode, unaryTree);

		if (impliesModification) {
			NodeWrapper lastAssignInfo = beforeScanAnyAssign(unaryNode, t);

			scan(unaryTree.getExpression(),
					Pair.createPair(unaryNode, RelationTypes.UNARY_ENCLOSES, PDGProcessing.getLefAssignmentArg(t)));
			afterScanAnyAssign(lastAssignInfo);

		} else
			scan(unaryTree.getExpression(), Pair.createPair(unaryNode, RelationTypes.UNARY_ENCLOSES));

		return null;
	}

	@Override
	public ASTVisitorResult visitUnionType(UnionTypeTree unionTypeTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		NodeWrapper unionTypeNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(unionTypeTree,
				NodeTypes.UNION_TYPE, NodeCategory.AST_TYPE, NodeCategory.AST_NODE);
		GraphUtils.connectWithParent(unionTypeNode, t);
		// System.out.println(unionTypeTree);
		// System.out.println(unionTypeTree.getTypeAlternatives().size());
		// System.out.println(unionTypeTree.getTypeAlternatives().get(unionTypeTree.getTypeAlternatives().size()
		// - 1));

		scan(unionTypeTree.getTypeAlternatives(), Pair.createPair(unionTypeNode, RelationTypes.UNION_TYPE_ALTERNATIVE));
		// IGUAL SOBRA... porque lo tengo a null pa to los tipos y funciona
		return null;

	}

	private void createVarInit(VariableTree varTree, NodeWrapper varDecNode, boolean isAttr, boolean isStatic) {
		if (varTree.getInitializer() != null) {
			NodeWrapper initNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(varTree,
					NodeTypes.INITIALIZATION);
			RelationshipWrapper r = varDecNode.createRelationshipTo(initNode, RelationTypes.HAS_VARIABLEDECL_INIT);
			if (isAttr)
				r.setProperty("isOwnAccess", true);
			scan(varTree.getInitializer(), Pair.createPair(initNode, RelationTypes.INITIALIZATION_EXPR));
			PDGProcessing.createVarDecInitRel(classState.currentClassDec, initNode, isAttr, isStatic);
		}
	}

	@Override
	public ASTVisitorResult visitVariable(VariableTree variableTree, Pair<PartialRelation<RelationTypes>, Object> t) {
		/*
		 * 
		 * 1� buscar los scans en este metodo u otros que llame, init o algo as� 2�
		 * asegurarse de que el tipo no se visita 3� visitarlo (con el de tipos) y usar
		 * el retorno de NodeWrapper tipo parametrizered type 4� en vez de las dos
		 * llamadas es VAR_DEC - itsTYPEis-> tipo <-USES_TYPE-current
		 */
//		System.out.println("VARIABLE!!!");
//		System.out.println(variableTree);

		boolean isAttr = t.getFirst().getRelationType().equals(RelationTypes.HAS_STATIC_INIT);
		boolean isMethodParam = t.getFirst().getRelationType().equals(RelationTypes.CALLABLE_HAS_PARAMETER)
				|| t.getFirst().getRelationType().equals(RelationTypes.LAMBDA_EXPRESSION_PARAMETERS);
		// This can be calculated cehcking if the param Object is null or not?
		boolean isEnum = false;
		NodeWrapper variableNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(variableTree,
				isAttr ? (isEnum = variableTree.toString().contains("/*")) ? NodeTypes.ENUM_ELEMENT : NodeTypes.ATTR_DEF
						: isMethodParam ? NodeTypes.PARAMETER_DEF : NodeTypes.LOCAL_VAR_DEF);
		variableNode.setProperty("name", variableTree.getName().toString());

		/*
		 * TODO variableTree.getType() instead of ((JCVariableDecl) variableTree).type
		 */
		Type type = ((JCVariableDecl) variableTree).type;
		if (type == null) {
			type = ((JCVariableDecl) variableTree).sym.type;
			System.out.println("TYPE WAS NULLL!");
		}
		// System.out.println("Attributing var " + variableTree);
		GraphUtils.attachType(variableNode, type, ast);
		addClassIdentifier(type);
		if (DEBUG)
			System.out
					.println("VARIABLE:" + variableTree.getName() + "(" + variableNode.getProperty("actualType") + ")");
		scan(variableTree.getModifiers(), Pair.createPair(variableNode, null));

		Symbol s = ((JCVariableDecl) variableTree).sym;

		MethodState previousState = methodState;
		if (isAttr) {
			// Warning, lineNumber and position should be added depending on the
			// constructor
			variableNode.setProperty("isDeclared", true);
			GraphUtils.connectWithParent(variableNode, t,
					isEnum ? RelationTypes.HAS_ENUM_ELEMENT : RelationTypes.DECLARES_FIELD);

			methodState = new MethodState(variableNode);
			Pair<List<NodeWrapper>, List<NodeWrapper>> param = ((Pair<Pair<List<NodeWrapper>, List<NodeWrapper>>, List<NodeWrapper>>) t
					.getSecond()).getFirst();
			(s.isStatic() ? param.getSecond() : param.getFirst()).add(methodState.lastMethodDecVisited);
			// pdgUtils.endMethod(methodState, classState.currentClassDec);

		} else
			GraphUtils.connectWithParent(variableNode, t);

		createVarInit(variableTree, variableNode, isAttr, s.isStatic());

		if (!(isMethodParam || isAttr)) {
			methodState.putCfgNodeInCache(variableTree, variableNode);
			addInvocationInStatement(variableNode);
			// System.out.println("AST VISIT VARIABLE registered in " +
			// es.uniovi.reflection.progquery.ast.cfgNodeCache.hashCode());
			// System.out.println(variableTree);
		}
		// System.out.println("VISITING VARIABLE " + variableTree);
		pdgUtils.putDecInCache(s, variableNode);

		scan(variableTree.getType(), Pair.createPair(variableNode, RelationTypes.HAS_VARIABLEDECL_TYPE));
		if (isAttr) {
			// Warning, lineNumber and position should be added depending on the
			// constructor
			methodState = previousState;
		}
		return null;
	}

	@Override
	public ASTVisitorResult visitWhileLoop(WhileLoopTree whileLoopTree,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper whileLoopNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNode(whileLoopTree,
				NodeTypes.WHILE_LOOP);
		GraphUtils.connectWithParent(whileLoopNode, t);

		scan(whileLoopTree.getCondition(), Pair.createPair(whileLoopNode, RelationTypes.WHILE_CONDITION));
		addInvocationInStatement(whileLoopNode);
		methodState.putCfgNodeInCache(whileLoopTree, whileLoopNode);

		prevMust = must;
		must = false;

		pdgUtils.enteringNewBranch();
		scan(whileLoopTree.getStatement(), Pair.createPair(whileLoopNode, RelationTypes.WHILE_STATEMENT));
		pdgUtils.exitingCurrentBranch();
		must = prevMust;
		return null;
	}

	@Override
	public ASTVisitorResult visitWildcard(WildcardTree wildcardTree, Pair<PartialRelation<RelationTypes>, Object> t) {

		NodeWrapper wildcardNode = DatabaseFachade.CURRENT_DB_FACHADE.createSkeletonNodeExplicitCats(wildcardTree,
				NodeTypes.WILDCARD_TYPE, NodeCategory.AST_TYPE, NodeCategory.AST_NODE);
		wildcardNode.setProperty("typeBoundKind", wildcardTree.getKind().toString());
		GraphUtils.connectWithParent(wildcardNode, t);
		scan(wildcardTree.getBound(), Pair.createPair(wildcardNode, RelationTypes.WILDCARD_BOUND));
		return null;
	}

}

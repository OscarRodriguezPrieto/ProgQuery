package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Type;

import cache.SimpleTreeNodeCache;
import database.nodes.NodeTypes;
import database.relations.CFGRelationTypes;
import database.relations.CGRelationTypes;
import database.relations.PartialRelation;
import database.relations.PartialRelationWithProperties;
import database.relations.RelationTypes;
import mig.DynamicMethodCallAnalysis;
import pdg.GetDeclarationFromExpression;
import pdg.InterproceduralPDG;
import utils.Pair;

public class ASTAuxiliarStorage {
	private static final String OBJECT_CLASS_NAME = "java.lang.Object";

	// public static List<EnhancedForLoopTree> enhancedForLoopList = new
	// ArrayList<EnhancedForLoopTree>();
	// public static Map<MethodTree, List<StatementTree>>
	// nestedConstructorsToBlocks = new HashMap<MethodTree,
	// List<StatementTree>>();
	// public static List<Pair<AssertTree, Node>> assertList = new
	// ArrayList<Pair<AssertTree, Node>>();

	private SimpleTreeNodeCache<Tree> cfgNodeCache;
	private SimpleTreeNodeCache<Tree> previousCfgNodeCache;

	private Map<TryTree, List<Pair<Node, List<MethodSymbol>>>> invocationsInStatements;
	private Stack<List<Pair<Node, List<MethodSymbol>>>> lastInvocationInStatementLists = new Stack<List<Pair<Node, List<MethodSymbol>>>>();
	private Map<TryTree, List<Pair<Node, List<MethodSymbol>>>> previousInvStatements;

	// private final Map<String, List<Type>> methodNamesToExceptionThrowsTypes =
	// new HashMap<String, List<Type>>();
	private List<MethodInfo> methodInfo = new ArrayList<MethodInfo>();
	public final List<Node> typeDecNodes = new ArrayList<Node>();

	// public void addThrowsInfoToMethod(String methodName, List<Type>
	// exceptionTypes) {
	// methodNamesToExceptionThrowsTypes.put(methodName, exceptionTypes);
	// }

	public List<MethodInfo> getMethodsInfo() {
		return methodInfo;
	}

	public SimpleTreeNodeCache<Tree> getCfgNodeCache() {
		return cfgNodeCache;
	}

	public void putCfgNodeInCache(Tree t, Node n) {
		cfgNodeCache.put(t, n);
	}

	public void addInfo(MethodTree methodTree, Node methodNode, Map<Node, Node> identificationForLeftAssignExprs) {

		methodInfo.add(new MethodInfo(methodTree, methodNode, identificationForLeftAssignExprs));
	}

	public void putConditionInCfgCache(ExpressionTree tree, Node n) {
		if (tree instanceof ParenthesizedTree)
			n = n.getSingleRelationship(RelationTypes.PARENTHESIZED_ENCLOSES, Direction.OUTGOING).getEndNode();
		if (tree != null)
			cfgNodeCache.put(tree, n);
	}

	public void enterInNewTry(TryTree tryTree) {
		lastInvocationInStatementLists.push(new ArrayList<Pair<Node, List<MethodSymbol>>>());
		invocationsInStatements.put(tryTree, lastInvocationInStatementLists.peek());
	}

	public void exitTry() {
		lastInvocationInStatementLists.pop();
	}

	public void addInvocationInStatement(Node statement, List<MethodSymbol> methodNames) {
		lastInvocationInStatementLists.peek().add(Pair.create(statement, methodNames));
	}

	public void newMethodDeclaration() {
		previousInvStatements = invocationsInStatements;
		invocationsInStatements = new HashMap<TryTree, List<Pair<Node, List<MethodSymbol>>>>();
		enterInNewTry(null);
		previousCfgNodeCache = cfgNodeCache;
		cfgNodeCache = new SimpleTreeNodeCache<Tree>();
	}

	public void endMethodDeclaration() {
		invocationsInStatements = previousInvStatements;
		cfgNodeCache = previousCfgNodeCache;
		exitTry();
	}

	public Map<TryTree, Map<Type, List<PartialRelation<CFGRelationTypes>>>> getTrysToExceptionalPartialRelations() {
		Map<TryTree, Map<Type, List<PartialRelation<CFGRelationTypes>>>> throwsTypesInStatementsGrouped = new HashMap<TryTree, Map<Type, List<PartialRelation<CFGRelationTypes>>>>();
		for (Entry<TryTree, List<Pair<Node, List<MethodSymbol>>>> entry : invocationsInStatements.entrySet()) {
			Map<Type, List<PartialRelation<CFGRelationTypes>>> typesToRelations = new HashMap<Type, List<PartialRelation<CFGRelationTypes>>>();
			for (Pair<Node, List<MethodSymbol>> invocationsInStatement : entry.getValue()) {
				for (MethodSymbol methodSymbol : invocationsInStatement.getSecond())
					for (Type excType : methodSymbol.getThrownTypes()) {
						if (!typesToRelations.containsKey(excType))
							typesToRelations.put(excType, new ArrayList<PartialRelation<CFGRelationTypes>>());
						typesToRelations.get(excType)
								.add(new PartialRelationWithProperties<CFGRelationTypes>(
										invocationsInStatement.getFirst(), CFGRelationTypes.MAY_THROW,
										Pair.create("methodName", methodSymbol.getQualifiedName().toString()),
										Pair.create("exceptionType", excType.toString())));
					}

			}

			throwsTypesInStatementsGrouped.put(entry.getKey(), typesToRelations);
		}
		return throwsTypesInStatementsGrouped;
	}

	public void doInterproceduralPDGAnalysis(Set<Node> methodsMutateThisAndParams,
			Map<Node, Set<Node>> paramsMutatedInMethods, Map<Node, Set<Node>> paramsMayMutateInMethods,
			Map<Node, Node> thisRefsOfMethods) {

		// TODO Return analysis

		Map<Node, Iterable<Relationship>> methodDecToCalls = new HashMap<Node, Iterable<Relationship>>();
		// Get Declarations Analysis
		GetDeclarationFromExpression getDecs = new GetDeclarationFromExpression(thisRefsOfMethods);
		methodInfo.forEach(mInfo -> {
			getDecs.setIdentificationForLeftAssignIdents(mInfo.identificationForLeftAssignExprs);
			Iterable<Relationship> callRels = mInfo.methodNode.getRelationships(CGRelationTypes.CALLS,
					Direction.OUTGOING);
			methodDecToCalls.put(mInfo.methodNode, callRels);
			for (Relationship callRel : callRels)
				if (callRel.getEndNode().hasLabel(NodeTypes.NEW_INSTANCE))
					getDecs.scanNewClass(callRel.getEndNode());
				else
					getDecs.scanMethodInvocation(callRel.getEndNode());
		});

		// Interprocedural analysis
		InterproceduralPDG pdgAnalysis = new InterproceduralPDG(methodsMutateThisAndParams, paramsMutatedInMethods,
				paramsMayMutateInMethods, thisRefsOfMethods, methodDecToCalls, getDecs.getInvocationsMayModifyVars());
		methodInfo.forEach(mInfo -> pdgAnalysis.doInterproceduralPDGAnalysis(mInfo.methodNode));

	}

	// Aquí tenemos dos opciones, bucle o traversal
	public void doDynamicMethodCallAnalysis() {
		DynamicMethodCallAnalysis dynMethodCallAnalysis = new DynamicMethodCallAnalysis();
		for (Node typeDec : typeDecNodes)
			dynMethodCallAnalysis.dynamicMethodCallAnalysis(typeDec);
	}
}

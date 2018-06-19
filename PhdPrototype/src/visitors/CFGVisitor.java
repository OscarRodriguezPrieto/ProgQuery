package visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Name;

import org.neo4j.graphdb.Node;

import com.sun.source.tree.AssertTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Type;

import cache.SimpleTreeNodeCache;
import database.DatabaseFachade;
import database.nodes.NodeTypes;
import database.relations.CFGRelationTypes;
import database.relations.PartialRelation;
import database.relations.PartialRelationWithProperties;
import database.relations.SimplePartialRelation;
import utils.JavacInfo;
import utils.MutablePair;
import utils.Pair;

public class CFGVisitor extends
		TreeScanner<List<PartialRelation<CFGRelationTypes>>, Pair<Name, List<PartialRelation<CFGRelationTypes>>>> {
	private SimpleTreeNodeCache<Tree> CFGCache;
	private Map<Tree, Pair<Node, Node>> finallyCache;

	private Map<TryTree, Map<Type, List<PartialRelation<CFGRelationTypes>>>> throwsTypesInStatements;

	private static Pair<Name, List<PartialRelation<CFGRelationTypes>>> getEmptyListPair(Name n) {
		return Pair.create(n, new ArrayList<PartialRelation<CFGRelationTypes>>());
	}

	private static Pair<Name, List<PartialRelation<CFGRelationTypes>>> getEmptyPair() {
		return Pair.create(null, new ArrayList<>());
	}

	private static Pair<Name, List<PartialRelation<CFGRelationTypes>>> getNoNamePair(
			List<PartialRelation<CFGRelationTypes>> l) {
		return Pair.create(null, l);
	}

	private static Pair<Name, List<PartialRelation<CFGRelationTypes>>> getPair(Name n,
			List<PartialRelation<CFGRelationTypes>> l) {
		return Pair.create(n, l);
	}

	static Pair<Name, List<PartialRelation<CFGRelationTypes>>> getNoNamePair(Node n, CFGRelationTypes rel) {
		List<PartialRelation<CFGRelationTypes>> l = new ArrayList<PartialRelation<CFGRelationTypes>>();
		l.add(new SimplePartialRelation<CFGRelationTypes>(n, rel));
		return Pair.create(null, l);
	}

	static Pair<Name, List<PartialRelation<CFGRelationTypes>>> getNoNamePair(PartialRelation<CFGRelationTypes> rel) {
		List<PartialRelation<CFGRelationTypes>> l = new ArrayList<PartialRelation<CFGRelationTypes>>();
		l.add(rel);
		return Pair.create(null, l);
	}

	private static Pair<Name, List<PartialRelation<CFGRelationTypes>>> getPair(Name name, Node n,
			CFGRelationTypes rel) {
		List<PartialRelation<CFGRelationTypes>> list = new ArrayList<PartialRelation<CFGRelationTypes>>();
		list.add(new SimplePartialRelation<CFGRelationTypes>(n, rel));
		return Pair.create(name, list);
	}

	private static List<PartialRelation<CFGRelationTypes>> getPairList(Node n, CFGRelationTypes rel) {
		List<PartialRelation<CFGRelationTypes>> l = new ArrayList<PartialRelation<CFGRelationTypes>>();
		l.add(new SimplePartialRelation<CFGRelationTypes>(n, rel));
		return l;
	}

	static void linkLasts(List<PartialRelation<CFGRelationTypes>> l, Node n) {
		for (PartialRelation<CFGRelationTypes> rel : l)
			rel.createRelationship(n);
	}

	// private PartialRelation<CFGRelationTypes> last;
	// TODO Al terminar borrar exitThrowing en caso de que no se haya usado, es
	// decir si no
	// tiene relaciones con nada-- eso s�lo si meto throwing para cualquier
	// expression cosa que no creo que haga
	private Node lastStatementNode, lastLoopStatement, exceptionalMethodEnding;
	private List<PartialRelation<CFGRelationTypes>> currentLoopLasts;
	private Map<Name, List<PartialRelation<CFGRelationTypes>>> loopLastsMap = new HashMap<Name, List<PartialRelation<CFGRelationTypes>>>();
	private int currentLoopTryIndex;
	private Map<Name, Integer> loopTryIndexes = new HashMap<Name, Integer>();
	private Map<Name, Node> nodesToContinue = new HashMap<Name, Node>();
	private List<MutablePair<TryTree, Boolean>> trys = new ArrayList<MutablePair<TryTree, Boolean>>();

	public CFGVisitor(Node lastStatementNode, Node exceptionalEnd, SimpleTreeNodeCache<Tree> CFGCache,
			Map<TryTree, Map<Type, List<PartialRelation<CFGRelationTypes>>>> throwsTypesInStatements,
			Map<Tree, Pair<Node, Node>> finallyCache) {
		this.finallyCache = finallyCache;
		this.CFGCache = CFGCache;
		this.lastStatementNode = lastStatementNode;
		exceptionalMethodEnding = exceptionalEnd;
		this.throwsTypesInStatements = throwsTypesInStatements;
		linkThrowing(throwsTypesInStatements.get(null));
	}

	private void linkThrowing(Map<Type, List<PartialRelation<CFGRelationTypes>>> typesToRelations) {
		boolean ended = false;
		outFor: for (int i = trys.size() - 1; i >= 0 && !ended; i--) {
			if (trys.get(i).getSecond()) {
				TryTree tryTree = trys.get(i).getFirst();
				for (CatchTree catchTree : tryTree.getCatches()) {
					Type catchType = JavacInfo.getTypeDirect(catchTree.getParameter());
					Iterator<Entry<Type, List<PartialRelation<CFGRelationTypes>>>> iterator = typesToRelations
							.entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<Type, List<PartialRelation<CFGRelationTypes>>> typeToRelations = iterator.next();
						boolean inconditionalCatch = JavacInfo.isSubtype(typeToRelations.getKey(), catchType);
						if (inconditionalCatch || JavacInfo.isSubtype(catchType, typeToRelations.getKey())) {
							if (inconditionalCatch)
								typeToRelations.getValue()
										.forEach(r -> r.createRelationship(CFGCache.get(catchTree.getParameter())));
							else
								typeToRelations.getValue().forEach(r -> r.createRelationship(CFGCache.get(catchTree)));
							// typesToRelations.remove(typeToRelations.getKey());
							iterator.remove();
							if (ended = typesToRelations.size() == 0)
								break outFor;
						}
					}
				}
				if (finallyCheck(typesToRelations.values().stream()
						.reduce(new ArrayList<PartialRelation<CFGRelationTypes>>(), (pr1, pr2) -> {
							pr1.addAll(pr2);
							return pr1;
						}), tryTree.getFinallyBlock()))
					ended = true;
			}
		}
		if (!ended)
			typesToRelations.values().stream()
					.reduce(new ArrayList<PartialRelation<CFGRelationTypes>>(), (pr1, pr2) -> {
						pr1.addAll(pr2);
						return pr1;
					}).forEach(r -> r.createRelationship(exceptionalMethodEnding));
	}

	private boolean finallyCheck(List<PartialRelation<CFGRelationTypes>> futureRels, BlockTree finallyBlock) {
		if (finallyBlock != null) {
			addRelationInFinallyStarting(finallyBlock, futureRels);
			return true;
		}
		return false;
	}

	private boolean finallyCheck(PartialRelation<CFGRelationTypes> futureRel, BlockTree finallyBlock) {
		if (finallyBlock != null) {
			addRelationInFinallyStarting(finallyBlock, futureRel);
			return true;
		}
		return false;
	}

	// private boolean isCaseInTheSameSwitch(Node previousCase, Node caseExpr) {
	// for (Relationship r : previousCase.getRelationships())
	// System.out.println(r.getType());
	// if (!previousCase.hasRelationship(RelationTypes.CASE_EXPR,
	// Direction.INCOMING))
	// return false;
	// previousCase =
	// previousCase.getSingleRelationship(RelationTypes.CASE_EXPR,
	// Direction.INCOMING).getStartNode();
	// caseExpr = caseExpr.getSingleRelationship(RelationTypes.CASE_EXPR,
	// Direction.INCOMING).getStartNode();
	// return
	// previousCase.getSingleRelationship(RelationTypes.SWITCH_ENCLOSES_CASES,
	// Direction.INCOMING)
	// .getStartNode().equals(caseExpr
	// .getSingleRelationship(RelationTypes.SWITCH_ENCLOSES_CASES,
	// Direction.INCOMING).getStartNode());
	// }

	private void linkThrowing(Node n, CFGRelationTypes rel, Type throwType) {
		linkThrowing(new SimplePartialRelation<CFGRelationTypes>(n, rel), throwType);
	}

	private void linkThrowing(PartialRelation<CFGRelationTypes> futureRel, Type throwType) {
		boolean ended = false;
		outFor: for (int i = trys.size() - 1; i >= 0 && !ended; i--) {
			TryTree tryTree = trys.get(i).getFirst();
			if (trys.get(i).getSecond()) {
				for (CatchTree catchTree : tryTree.getCatches()) {
					Type catchType = JavacInfo.getTypeDirect(catchTree.getParameter());
					boolean inconditionalCatch = throwType != null && JavacInfo.isSubtype(throwType, catchType);
					if (throwType == null || inconditionalCatch || JavacInfo.isSubtype(catchType, throwType)) {
						if (inconditionalCatch)
							futureRel.createRelationship(CFGCache.get(catchTree.getParameter()));
						else
							futureRel.createRelationship(CFGCache.get(catchTree));
						ended = true;
						break outFor;
					}
				}
				ended = finallyCheck(futureRel, tryTree.getFinallyBlock());
			} else {
				ended = finallyCheck(futureRel, trys.get(i).getFirst().getFinallyBlock());
			}
		}
		if (!ended)
			futureRel.createRelationship(exceptionalMethodEnding);
	}

	private void addRelationInFinallyStarting(BlockTree finallyBlock, Node n, CFGRelationTypes rel) {
		addRelationInFinallyStarting(finallyBlock, new SimplePartialRelation<CFGRelationTypes>(n, rel));
	}

	private void addRelationInFinallyStarting(BlockTree finallyBlock,
			List<PartialRelation<CFGRelationTypes>> futureRels) {
		futureRels.forEach(partialRel -> partialRel.createRelationship(finallyCache.get(finallyBlock).getFirst()));
	}

	private void addRelationInFinallyStarting(BlockTree finallyBlock, PartialRelation<CFGRelationTypes> futureRel) {
		futureRel.createRelationship(finallyCache.get(finallyBlock).getFirst());
	}

	private void linkBreaksToFinallies(Node node, Name label, boolean isBreak) {
		CFGRelationTypes rel = isBreak ? CFGRelationTypes.AFTER_FINALLY_PREVIOUS_BREAK
				: CFGRelationTypes.AFTER_FINALLY_PREVIOUS_CONTINUE;
		boolean hasLabel = label != null;
		System.out.println(label);
		int limitIndex = hasLabel ? loopTryIndexes.get(label) : currentLoopTryIndex;
		int i = trys.size() - 1;
		BlockTree lastFinally = null;
		for (; i >= limitIndex && lastFinally == null; i--) {
			BlockTree finallyBlock = trys.get(i).getFirst().getFinallyBlock();
			if (finallyBlock != null)
				addRelationInFinallyStarting(lastFinally = finallyBlock, node, CFGRelationTypes.CFG_NEXT_STATEMENT);

		}
		for (; i >= limitIndex; i--) {
			BlockTree finallyBlock = trys.get(i).getFirst().getFinallyBlock();
			if (finallyBlock != null) {
				if (hasLabel)
					addRelationInFinallyStarting(finallyBlock, new PartialRelationWithProperties<CFGRelationTypes>(
							finallyCache.get(lastFinally).getSecond(), rel, "label", label.toString()));
				else
					addRelationInFinallyStarting(finallyBlock, finallyCache.get(lastFinally).getSecond(), rel);
				lastFinally = finallyBlock;
			}
		}

		if (isBreak)
			if (hasLabel)
				loopLastsMap.get(label).add(lastFinally != null
						? new PartialRelationWithProperties<CFGRelationTypes>(finallyCache.get(lastFinally).getSecond(),
								rel, "label", label.toString())
						: new SimplePartialRelation<CFGRelationTypes>(node, CFGRelationTypes.CFG_NEXT_STATEMENT));
			else

				currentLoopLasts.add(lastFinally != null
						? new SimplePartialRelation<CFGRelationTypes>(finallyCache.get(lastFinally).getSecond(), rel)
						: new SimplePartialRelation<CFGRelationTypes>(node, CFGRelationTypes.CFG_NEXT_STATEMENT));
		else if (lastFinally == null)
			node.createRelationshipTo(hasLabel ? nodesToContinue.get(label) : lastLoopStatement,
					CFGRelationTypes.CFG_NEXT_STATEMENT);
		else {
			if (hasLabel)
				finallyCache.get(lastFinally).getSecond().createRelationshipTo(nodesToContinue.get(label), rel)
						.setProperty("label", label.toString());
			else
				finallyCache.get(lastFinally).getSecond().createRelationshipTo(lastLoopStatement, rel);
		}
	}

	private List<PartialRelation<CFGRelationTypes>> nextStatement(StatementTree t,
			List<PartialRelation<CFGRelationTypes>> lasts) {
		Node n = CFGCache.get(t);
		// if (n == null) {
		// // System.err.println("OJO:\n" + t);
		// return lasts;
		// }

		linkLasts(lasts, n);

		return getPairList(n, CFGRelationTypes.CFG_NEXT_STATEMENT);
	}

	private List<PartialRelation<CFGRelationTypes>> nextStatement(Node n,
			List<PartialRelation<CFGRelationTypes>> lasts) {
		linkLasts(lasts, n);
		return getPairList(n, CFGRelationTypes.CFG_NEXT_STATEMENT);
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitAssert(AssertTree assertTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		Node n = CFGCache.get(assertTree);

		linkLasts(lasts.getSecond(), n);
		linkThrowing(n, CFGRelationTypes.UNCAUGHT_EXCEPTION, JavacInfo.getSymtab().assertionErrorType);
		return getPairList(n, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE);
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitBlock(BlockTree blockTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		List<PartialRelation<CFGRelationTypes>> newLasts = new ArrayList<PartialRelation<CFGRelationTypes>>();
		if (lasts.getFirst() != null) {
			loopLastsMap.put(lasts.getFirst(), newLasts);
			loopTryIndexes.put(lasts.getFirst(), trys.size());
		}
		for (StatementTree st : blockTree.getStatements())
			lasts = getNoNamePair(scan(st, lasts));

		if (lasts.getFirst() != null) {
			loopLastsMap.remove(lasts.getFirst());
			loopTryIndexes.remove(lasts.getFirst());
		}
		lasts.getSecond().addAll(newLasts);
		return lasts.getSecond();
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitBreak(BreakTree breakTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		Node breakNode = CFGCache.get(breakTree);
		linkLasts(lasts.getSecond(), breakNode);
		linkBreaksToFinallies(breakNode, breakTree.getLabel(), true);
		return new ArrayList<PartialRelation<CFGRelationTypes>>();
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitCase(CaseTree caseTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		List<PartialRelation<CFGRelationTypes>> newLasts = lasts.getSecond();
		for (StatementTree st : caseTree.getStatements())
			newLasts = scan(st, getNoNamePair(newLasts));
		return newLasts;
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitCatch(CatchTree catchTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		Node catchNode = CFGCache.get(catchTree);
		linkLasts(lasts.getSecond(), catchNode);
		return scan(catchTree.getBlock(), getNoNamePair(
				scan(catchTree.getParameter(), getNoNamePair(catchNode, CFGRelationTypes.CAUGHT_EXCEPTION))));
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitContinue(ContinueTree continueTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		Node continueNode = CFGCache.get(continueTree);
		linkLasts(lasts.getSecond(), continueNode);

		linkBreaksToFinallies(continueNode, continueTree.getLabel(), false);

		return new ArrayList<PartialRelation<CFGRelationTypes>>();
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitEmptyStatement(EmptyStatementTree tree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> arg) {
		return nextStatement(tree, arg.getSecond());

	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitForLoop(ForLoopTree forLoopTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		Node forNode = CFGCache.get(forLoopTree);

		List<PartialRelation<CFGRelationTypes>> previousLasts = lasts.getSecond();
		for (StatementTree t : forLoopTree.getInitializer())
			previousLasts = nextStatement(t, previousLasts);

		linkLasts(previousLasts, forNode);

		Node previousLoop = lastLoopStatement;
		previousLasts = currentLoopLasts;
		List<PartialRelation<CFGRelationTypes>> newLasts;

		currentLoopLasts = newLasts = getPairList(forNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE);

		int previousTryIndex = currentLoopTryIndex;
		currentLoopTryIndex = trys.size();
		if (lasts.getFirst() != null) {
			nodesToContinue.put(lasts.getFirst(), forNode);
			loopLastsMap.put(lasts.getFirst(), newLasts);
			loopTryIndexes.put(lasts.getFirst(), trys.size());
		}
		lastLoopStatement = forNode;

		List<PartialRelation<CFGRelationTypes>> statementLasts = scan(forLoopTree.getStatement(),
				getNoNamePair(forNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE));
		for (ExpressionStatementTree update : forLoopTree.getUpdate())
			statementLasts = nextStatement(update, statementLasts);
		linkLasts(statementLasts, forNode);

		lastLoopStatement = previousLoop;
		currentLoopLasts = previousLasts;
		currentLoopTryIndex = previousTryIndex;
		if (lasts.getFirst() != null) {
			nodesToContinue.remove(lasts.getFirst());
			loopLastsMap.remove(lasts.getFirst());
			loopTryIndexes.remove(lasts.getFirst());
		}

		return newLasts;
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitEnhancedForLoop(EnhancedForLoopTree enhancedForLoopTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {

		Node forEachNode = CFGCache.get(enhancedForLoopTree),
				localVarNode = CFGCache.get(enhancedForLoopTree.getVariable());
		linkLasts(lasts.getSecond(), forEachNode);
		Node previousLoop = lastLoopStatement;
		List<PartialRelation<CFGRelationTypes>> previousLasts = currentLoopLasts, newLasts;
		forEachNode.createRelationshipTo(localVarNode, CFGRelationTypes.CFG_FOR_EACH_HAS_NEXT);
		currentLoopLasts = newLasts = getPairList(forEachNode, CFGRelationTypes.CFG_FOR_EACH_NO_MORE_ELEMENTS);
		int previousTryIndex = currentLoopTryIndex;
		currentLoopTryIndex = trys.size();
		if (lasts.getFirst() != null) {
			nodesToContinue.put(lasts.getFirst(), forEachNode);
			loopLastsMap.put(lasts.getFirst(), newLasts);
			loopTryIndexes.put(lasts.getFirst(), trys.size());
		}

		lastLoopStatement = forEachNode;
		linkLasts(scan(enhancedForLoopTree.getStatement(),
				getNoNamePair(localVarNode, CFGRelationTypes.CFG_NEXT_STATEMENT)), forEachNode);
		lastLoopStatement = previousLoop;
		currentLoopLasts = previousLasts;
		currentLoopTryIndex = previousTryIndex;
		if (lasts.getFirst() != null) {
			nodesToContinue.remove(lasts.getFirst());
			loopLastsMap.remove(lasts.getFirst());
			loopTryIndexes.remove(lasts.getFirst());
		}

		return newLasts;
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitExpressionStatement(ExpressionStatementTree tree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> arg) {
		return nextStatement(tree, arg.getSecond());

	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitIf(IfTree ifTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		Node ifNode = CFGCache.get(ifTree);

		linkLasts(lasts.getSecond(), ifNode);
		List<PartialRelation<CFGRelationTypes>> breakLasts = null;
		if (lasts.getFirst() != null) {
			loopTryIndexes.put(lasts.getFirst(), trys.size());
			loopLastsMap.put(lasts.getFirst(), breakLasts = new ArrayList<PartialRelation<CFGRelationTypes>>());
		}
		List<PartialRelation<CFGRelationTypes>> newLasts = scan(ifTree.getThenStatement(),
				getNoNamePair(ifNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE));

		if (ifTree.getElseStatement() != null)
			newLasts.addAll(scan(ifTree.getElseStatement(),
					getNoNamePair(ifNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE)));
		else
			newLasts.add(
					new SimplePartialRelation<CFGRelationTypes>(ifNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE));

		if (lasts.getFirst() != null) {
			loopTryIndexes.remove(lasts.getFirst());
			loopLastsMap.remove(lasts.getFirst());
			newLasts.addAll(breakLasts);
		}
		return newLasts;
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitDoWhileLoop(DoWhileLoopTree doWhileLoopTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		Node doWhileNode = CFGCache.get(doWhileLoopTree);
		Node previousLoop = lastLoopStatement;
		int previousTryIndex = currentLoopTryIndex;
		currentLoopTryIndex = trys.size();
		List<PartialRelation<CFGRelationTypes>> previousLasts = currentLoopLasts, newLasts;
		currentLoopLasts = newLasts = getPairList(doWhileNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE);
		if (lasts.getFirst() != null) {
			loopTryIndexes.put(lasts.getFirst(), currentLoopTryIndex);
			nodesToContinue.put(lasts.getFirst(), doWhileNode);
			loopLastsMap.put(lasts.getFirst(), newLasts);
		}

		lastLoopStatement = doWhileNode;
		lasts.getSecond().add(
				new SimplePartialRelation<CFGRelationTypes>(doWhileNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE));
		linkLasts(scan(doWhileLoopTree.getStatement(), lasts), doWhileNode);

		lastLoopStatement = previousLoop;
		currentLoopLasts = previousLasts;
		currentLoopTryIndex = previousTryIndex;
		if (lasts.getFirst() != null) {
			loopTryIndexes.remove(lasts.getFirst());
			nodesToContinue.remove(lasts.getFirst());
			loopLastsMap.remove(lasts.getFirst());
		}
		return newLasts;
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitLabeledStatement(LabeledStatementTree labeledStatementTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {

		Node labelStatNode = CFGCache.get(labeledStatementTree);
		linkLasts(lasts.getSecond(), labelStatNode);

		return scan(labeledStatementTree.getStatement(),
				getPair(labeledStatementTree.getLabel(), labelStatNode, CFGRelationTypes.CFG_NEXT_STATEMENT));
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitReturn(ReturnTree tree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> arg) {
		PartialRelation<CFGRelationTypes> retPair = nextStatement(tree, arg.getSecond()).get(0);
		retPair.createRelationship(lastStatementNode);
		return new ArrayList<PartialRelation<CFGRelationTypes>>();
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitSwitch(SwitchTree switchTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {

		Node switchNode = CFGCache.get(switchTree);
		linkLasts(lasts.getSecond(), switchNode);
		lasts.getSecond().clear();

		List<PartialRelation<CFGRelationTypes>> previousLasts = currentLoopLasts;
		currentLoopLasts = lasts.getSecond();
		int previousTryIndex = currentLoopTryIndex;
		currentLoopTryIndex = trys.size();
		if (lasts.getFirst() != null) {
			loopLastsMap.put(lasts.getFirst(), lasts.getSecond());
			loopTryIndexes.put(lasts.getFirst(), currentLoopTryIndex);
		}
		int i = 0;
		List<PartialRelation<CFGRelationTypes>> newLasts = new ArrayList<PartialRelation<CFGRelationTypes>>();
		for (; i < switchTree.getCases().size() && switchTree.getCases().get(i).getExpression() != null; i++) {
			CaseTree caseTree = switchTree.getCases().get(i);
			newLasts.add(new PartialRelationWithProperties<CFGRelationTypes>(switchNode,
					CFGRelationTypes.SWITCH_CASE_IS_EQUAL_TO, Pair.create("value", caseTree.toString()),
					Pair.create("caseIndex", i)));
			newLasts = scan(caseTree, getNoNamePair(newLasts));
		}
		if (i < switchTree.getCases().size()) {
			newLasts.add(new PartialRelationWithProperties<CFGRelationTypes>(switchNode,
					CFGRelationTypes.SWITCH_DEFAULT_CASE, Pair.create("caseIndex", i)));
			lasts.getSecond().addAll(scan(switchTree.getCases().get(i), getNoNamePair(newLasts)));
		} else
			lasts.getSecond().addAll(newLasts);

		currentLoopLasts = previousLasts;
		currentLoopTryIndex = previousTryIndex;
		if (lasts.getFirst() != null) {
			loopLastsMap.remove(lasts.getFirst());
			loopTryIndexes.remove(lasts.getFirst());
		}
		return lasts.getSecond();
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitThrow(ThrowTree tree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {

		Node n = CFGCache.get(tree);

		linkLasts(lasts.getSecond(), n);
		linkThrowing(n, CFGRelationTypes.UNCAUGHT_EXCEPTION, JavacInfo.getTypeDirect(tree.getExpression()));
		return new ArrayList<PartialRelation<CFGRelationTypes>>();
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitTry(TryTree tryTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {

		List<PartialRelation<CFGRelationTypes>> newLasts = new ArrayList<PartialRelation<CFGRelationTypes>>();
		if (lasts.getFirst() != null) {
			loopTryIndexes.put(lasts.getFirst(), trys.size());
			loopLastsMap.put(lasts.getFirst(), newLasts);
		}
		MutablePair<TryTree, Boolean> tryPair = null;
		if (tryTree.getCatches().size() > 0 || tryTree.getFinallyBlock() != null) {
			trys.add(tryPair = MutablePair.create(tryTree, true));
			linkThrowing(throwsTypesInStatements.get(tryTree));
		}
		Node tryNode = CFGCache.get(tryTree);
		List<PartialRelation<CFGRelationTypes>> previousLasts = nextStatement(tryNode, lasts.getSecond());
		for (Tree t : tryTree.getResources())
			previousLasts = nextStatement((StatementTree) t, previousLasts);

		// Lo pasamos sin etiqueta para no tener que a�adirlo a los catch ni al
		// finally
		newLasts.addAll(scan(tryTree.getBlock(), getNoNamePair(previousLasts)));

		if (tryTree.getCatches().size() == 0 && tryTree.getFinallyBlock() == null) {
			linkThrowing(tryNode, CFGRelationTypes.UNCAUGHT_EXCEPTION, null);
			return newLasts;
		}
		tryPair.setSecond(false);

		Node blockThrowingUncaught = tryNode;

		for (int i = 0; i < tryTree.getCatches().size(); i++) {
			newLasts.addAll(scan(tryTree.getCatches().get(i),
					getNoNamePair(blockThrowingUncaught, CFGRelationTypes.UNCAUGHT_EXCEPTION)));
			blockThrowingUncaught = CFGCache.get(tryTree.getCatches().get(i));
		}

		trys.remove(tryPair);

		if (tryTree.getFinallyBlock() == null) {
			linkThrowing(blockThrowingUncaught, CFGRelationTypes.UNCAUGHT_EXCEPTION, null);
			return newLasts;
		}

		newLasts.add(new SimplePartialRelation<CFGRelationTypes>(blockThrowingUncaught,
				CFGRelationTypes.UNCAUGHT_EXCEPTION));
		// List<PartialRelation<CFGRelationTypes>> finallyRels =
		// finallyRelationsToDo.get(tryTree.getFinallyBlock());
		// if (finallyRels != null)
		// newLasts.addAll(finallyRels);

		Pair<Node, Node> finallyStartAndEnd = finallyCache.get(tryTree.getFinallyBlock());
		linkThrowing(finallyStartAndEnd.getSecond(), CFGRelationTypes.UNCAUGHT_EXCEPTION, null);

		List<PartialRelation<CFGRelationTypes>> breakRelsForNextStat = new ArrayList<PartialRelation<CFGRelationTypes>>();
		for (PartialRelation<CFGRelationTypes> rel : newLasts)
			if (rel.getStartingNode().equals(finallyStartAndEnd.getSecond()))
				breakRelsForNextStat.add(rel);
			else
				rel.createRelationship(finallyStartAndEnd.getFirst());

		if (tryTree.getFinallyBlock().getStatements().size() == 0) {
			linkLasts(getPairList(finallyStartAndEnd.getFirst(), CFGRelationTypes.CFG_NEXT_STATEMENT),
					finallyStartAndEnd.getSecond());
			if (lasts.getFirst() != null) {
				loopLastsMap.remove(lasts.getFirst());
				loopTryIndexes.remove(lasts.getFirst());
			}
			breakRelsForNextStat.addAll(getPairList(finallyStartAndEnd.getSecond(), CFGRelationTypes.NO_EXCEPTION));
			return breakRelsForNextStat;
		} else {
			List<PartialRelation<CFGRelationTypes>> finallyBreaks = new ArrayList<PartialRelation<CFGRelationTypes>>();
			if (lasts.getFirst() != null) {
				loopLastsMap.put(lasts.getFirst(), finallyBreaks);
				loopTryIndexes.put(lasts.getFirst(), trys.size());
			}

			linkLasts(
					scan(tryTree.getFinallyBlock(),
							getNoNamePair(finallyStartAndEnd.getFirst(), CFGRelationTypes.CFG_NEXT_STATEMENT)),
					finallyStartAndEnd.getSecond());
			if (lasts.getFirst() != null) {
				loopLastsMap.remove(lasts.getFirst());
				loopTryIndexes.remove(lasts.getFirst());
			}
			finallyBreaks.add(new SimplePartialRelation<CFGRelationTypes>(finallyStartAndEnd.getSecond(),
					CFGRelationTypes.NO_EXCEPTION));
			finallyBreaks.addAll(breakRelsForNextStat);
			return finallyBreaks;
		}
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitVariable(VariableTree variableTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {

		return nextStatement(variableTree, lasts.getSecond());
	}

	@Override
	public List<PartialRelation<CFGRelationTypes>> visitWhileLoop(WhileLoopTree whileLoopTree,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		Node whileNode = CFGCache.get(whileLoopTree);
		// Se puede hacer retornando en el caso de los bucles la condicion
		// tambien

		linkLasts(lasts.getSecond(), whileNode);
		Node previousLoop = lastLoopStatement;
		int previousTryIndex = currentLoopTryIndex;
		currentLoopTryIndex = trys.size();
		List<PartialRelation<CFGRelationTypes>> previousLasts = currentLoopLasts, newLasts;
		currentLoopLasts = newLasts = getPairList(whileNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_FALSE);
		if (lasts.getFirst() != null) {
			loopTryIndexes.put(lasts.getFirst(), currentLoopTryIndex);
			nodesToContinue.put(lasts.getFirst(), whileNode);
			loopLastsMap.put(lasts.getFirst(), newLasts);
		}

		lastLoopStatement = whileNode;

		// MUCHO OJO AQU�
		linkLasts(scan(whileLoopTree.getStatement(),
				getNoNamePair(whileNode, CFGRelationTypes.CFG_NEXT_STATEMENT_IF_TRUE)), whileNode);
		lastLoopStatement = previousLoop;
		currentLoopLasts = previousLasts;
		currentLoopTryIndex = previousTryIndex;
		if (lasts.getFirst() != null) {
			loopTryIndexes.remove(lasts.getFirst());
			nodesToContinue.remove(lasts.getFirst());
			loopLastsMap.remove(lasts.getFirst());
		}
		return newLasts;

	}

	public List<PartialRelation<CFGRelationTypes>> scanStatements(List<StatementTree> statements,
			Pair<Name, List<PartialRelation<CFGRelationTypes>>> lasts) {
		List<PartialRelation<CFGRelationTypes>> newLasts = new ArrayList<PartialRelation<CFGRelationTypes>>();

		for (StatementTree st : statements)
			lasts = getNoNamePair(scan(st, lasts));

		lasts.getSecond().addAll(newLasts);
		return lasts.getSecond();
	}

	public static void doCFGAnalysis(Node methodNode, MethodTree tree, SimpleTreeNodeCache<Tree> cfgCache,
			Map<TryTree, Map<Type, List<PartialRelation<CFGRelationTypes>>>> invocationsInStatements,
			Map<Tree, Pair<Node, Node>> finallyCache) {
		Node lastStatementNode = DatabaseFachade.createSkeletonNode(NodeTypes.CFG_METHOD_END),
				entryStatement = DatabaseFachade.createSkeletonNode(NodeTypes.CFG_METHOD_ENTRY),
				exceptionalEnd = DatabaseFachade.createSkeletonNode(NodeTypes.CFG_EXCEPTIONAL_END);
		methodNode.createRelationshipTo(entryStatement, CFGRelationTypes.CFG_ENTRY);

		CFGVisitor.linkLasts(
				new CFGVisitor(lastStatementNode, exceptionalEnd, cfgCache, invocationsInStatements, finallyCache).scan(
						tree.getBody(), CFGVisitor.getNoNamePair(entryStatement, CFGRelationTypes.CFG_NEXT_STATEMENT)),
				lastStatementNode);
		exceptionalEnd.createRelationshipTo(methodNode, CFGRelationTypes.CFG_END_OF);
		lastStatementNode.createRelationshipTo(methodNode, CFGRelationTypes.CFG_END_OF);
	}

}

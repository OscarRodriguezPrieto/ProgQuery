package visitors;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import cache.DefinitionCache;
import database.DatabaseFachade;
import database.nodes.NodeTypes;
import database.relations.PDGRelationTypes;
import database.relations.PartialRelation;
import database.relations.RelationTypes;
import utils.JavacInfo;
import utils.Pair;

public class PDGVisitor {
	public static final PDGRelationTypes[] USED = new PDGRelationTypes[] { PDGRelationTypes.USED_BY },
			MODIFIED = new PDGRelationTypes[] { PDGRelationTypes.MODIFIED_BY },
			STATE_MODIFIED = new PDGRelationTypes[] { PDGRelationTypes.STATE_MODIFIED_BY },
			USED_AND_MOD = new PDGRelationTypes[] { PDGRelationTypes.USED_BY, PDGRelationTypes.MODIFIED_BY },
			USED_AND_STATE_MOD = new PDGRelationTypes[] { PDGRelationTypes.USED_BY,
					PDGRelationTypes.STATE_MODIFIED_BY };
	private static final Map<PDGRelationTypes[], PDGRelationTypes[]> toModify = getMapToModify();

	private Map<Symbol, Node> definitionTable = new HashMap<Symbol, Node>();
	private Map<Symbol, List<Consumer<Node>>> toDo = new HashMap<Symbol, List<Consumer<Node>>>();

	public Node lastAssignment;
	private Node currentClassDec, previousClassDec;
	private List<Node> attrsInClassDec, previousAttrsInClassDec;

	private Node lastMethodDecVisited = null, previousMethodDec;

	private List<Node> paramsInMethodDec = null, previousParams;

	private Map<Node, List<Pair<Node, PDGRelationTypes>>> paramsToOrderedAssignments,
			previousParamsToOrderedAssignments;
	private PDGRelationTypes thisRelationsOnThisMethod, previousThisRelations;

	private Map<Node, Set<Node>> paramsMutatedInMethods = new HashMap<Node, Set<Node>>(),
			paramsMayMutateInMethods = new HashMap<Node, Set<Node>>();

	private Set<Node> methodsMutateThisAndParams = new HashSet<Node>();

	private Map<Node, Node> thisRefsOfMethods = new HashMap<Node, Node>();

	private Map<Node, Node> identificationForLeftAssignExprs, previousIdentification;

	public Map<Node, Set<Node>> getParamsMayMutateInMethods() {
		return paramsMayMutateInMethods;
	}

	public Map<Node, Node> getIdentificationForLeftAssignExprs() {
		return identificationForLeftAssignExprs;
	}

	public Map<Node, Node> getThisRefsOfMethods() {
		return thisRefsOfMethods;
	}

	public Map<Node, Set<Node>> getParamsMutatedInMethods() {
		return paramsMutatedInMethods;
	}

	public Set<Node> getMethodsMutateThisAndParams() {
		return methodsMutateThisAndParams;
	}

	public Node getLastMethodDecVisited() {

		return lastMethodDecVisited;
	}

	public void newMethod(Node methodDec) {
		previousMethodDec = lastMethodDecVisited;
		lastMethodDecVisited = methodDec;
		previousParams = paramsInMethodDec;
		previousParamsToOrderedAssignments = paramsToOrderedAssignments;
		paramsToOrderedAssignments = new HashMap<Node, List<Pair<Node, PDGRelationTypes>>>();
		previousThisRelations = thisRelationsOnThisMethod;
		thisRelationsOnThisMethod = null;
		previousIdentification = identificationForLeftAssignExprs;
		identificationForLeftAssignExprs = new HashMap<Node, Node>();
	}

	private PDGRelationTypes isTheParamStateModified(List<Pair<Node, PDGRelationTypes>> modifRelations) {
		PDGRelationTypes paramRelationInMethod = null;
		boolean mayBeReassigned = false;
		for (Pair<Node, PDGRelationTypes> pair : modifRelations)
			if (pair.getSecond() == PDGRelationTypes.MODIFIED_BY)
				if ((boolean) pair.getFirst().getProperty("mustBeExecuted"))
					return paramRelationInMethod;
				else
					mayBeReassigned = true;
			else if (mayBeReassigned)
				return PDGRelationTypes.STATE_MAY_BE_MODIFIED;
			else if ((boolean) pair.getFirst().getProperty("mustBeExecuted"))
				return PDGRelationTypes.STATE_MODIFIED_BY;
			else
				paramRelationInMethod = PDGRelationTypes.STATE_MAY_BE_MODIFIED;
		return paramRelationInMethod;
	}

	static boolean mustBeThisParamMutated(Node param, PDGRelationTypes rel, Set<Node> paramsMutated,
			Set<Node> paramsMayMutate) {
		if (rel == PDGRelationTypes.STATE_MODIFIED_BY) {
			paramsMutated.add(param);
			return true;
		}
		paramsMayMutate.add(param);
		return false;
	}

	public void endMethod() {

		boolean methodMutateThisAndParams = true;
		paramsInMethodDec = new ArrayList<Node>();
		lastMethodDecVisited.getRelationships(RelationTypes.HAS_METHODDECL_PARAMETERS, Direction.OUTGOING)
				.forEach(rel -> paramsInMethodDec.add(rel.getEndNode()));
		Set<Node> paramsMutated = new HashSet<Node>(), paramsMayMutate = new HashSet<Node>();
		paramsMutatedInMethods.put(lastMethodDecVisited, paramsMutated);
		paramsMayMutateInMethods.put(lastMethodDecVisited, paramsMayMutate);
		for (Node param : paramsInMethodDec)
			if (param.getProperty("typeKind").toString().contentEquals("DECLARED")) {
				if (paramsToOrderedAssignments.containsKey(param)) {
					PDGRelationTypes rel = isTheParamStateModified(paramsToOrderedAssignments.get(param));
					if (rel != null) {
						param.createRelationshipTo(lastMethodDecVisited, rel);
						methodMutateThisAndParams = methodMutateThisAndParams
								&& mustBeThisParamMutated(param, rel, paramsMutated, paramsMayMutate);
					} else
						methodMutateThisAndParams = false;
				} else
					methodMutateThisAndParams = false;
			}
		// With this line we create a this ref node per class, regardless of it
		// has relations or not. The other option is including this declaration
		// inside the ifs

		if (!lastMethodDecVisited.getProperty("nodeType").toString().contentEquals("ATTR_DEC")) {
			boolean isStatic = lastMethodDecVisited
					.getSingleRelationship(RelationTypes.HAS_METHODDECL_MODIFIERS, Direction.OUTGOING).getEndNode()
					.getProperty("flags").toString().contains("static");
			Relationship thisRel = getOrCreateThisNode(currentClassDec);
			if (!isStatic)
				thisRefsOfMethods.put(lastMethodDecVisited, thisRel.getEndNode());
			if (thisRelationsOnThisMethod != null) {

				thisRel.getEndNode().createRelationshipTo(lastMethodDecVisited, thisRelationsOnThisMethod);
				methodMutateThisAndParams = methodMutateThisAndParams && mustBeThisParamMutated(thisRel.getEndNode(),
						thisRelationsOnThisMethod, paramsMutated, paramsMayMutate);
			} else if (isStatic)
				paramsMutated.add(thisRel.getEndNode());

			if (methodMutateThisAndParams) {
				methodsMutateThisAndParams.add(lastMethodDecVisited);
				paramsMutatedInMethods.remove(lastMethodDecVisited);
				paramsMayMutateInMethods.remove(lastMethodDecVisited);
			} else {
				if (paramsMutated.size() == 0)
					paramsMutatedInMethods.remove(lastMethodDecVisited);
				if (paramsMayMutate.size() == 0)
					paramsMayMutateInMethods.remove(lastMethodDecVisited);

			}
		}
		lastMethodDecVisited = previousMethodDec;
		paramsInMethodDec = previousParams;
		paramsToOrderedAssignments = previousParamsToOrderedAssignments;
		thisRelationsOnThisMethod = previousThisRelations;
	}

	public static PDGRelationTypes[] getExprStatementArg(ExpressionStatementTree expStatementTree) {
		return expStatementTree.getExpression().getKind() == Kind.ASSIGNMENT ? MODIFIED : null;
	}

	private static Map<PDGRelationTypes[], PDGRelationTypes[]> getMapToModify() {
		Map<PDGRelationTypes[], PDGRelationTypes[]> map = new HashMap<PDGRelationTypes[], PDGRelationTypes[]>();
		map.put(MODIFIED, STATE_MODIFIED);
		map.put(USED_AND_MOD, USED_AND_STATE_MOD);
		map.put(STATE_MODIFIED, STATE_MODIFIED);
		map.put(USED, USED);
		map.put(USED_AND_STATE_MOD, USED_AND_STATE_MOD);
		map.put(null, null);
		return map;
	}

	public void putDecInCache(VariableTree variableTree, Node n) {
		Symbol s = ((JCVariableDecl) variableTree).sym;
		// System.out.println();
		// System.out.println("ADDED " + variableTree);
		//
		// System.out.println("TODO:");
		// if (toDo.get(s) == null)
		// System.out.println("NONE");
		// else
		// toDo.get(s).forEach(p -> System.out.println(p.getSecond()));
		if (toDo.containsKey(s)) {
			for (Consumer<Node> consumer : toDo.get(s))
				consumer.accept(n);
			toDo.remove(s);
		}
		definitionTable.put(s, n);
	}

	public static Object getLefAssignmentArg(Pair<PartialRelation<RelationTypes>, Object> t) {

		return t.getSecond() == MODIFIED ? MODIFIED : USED_AND_MOD;
	}

	public static Object getModifiedArg(Pair<PartialRelation<RelationTypes>, Object> t) {
		return toModify.get(t.getSecond());
	}

	@FunctionalInterface
	private interface IdToStateModThis {
		public Node apply(Boolean hasNoDec, Node nodeDec, Boolean isNotThis, Symbol s);
	}

	private void addAssignmentToThisMethod(Node paramDec, PDGRelationTypes relType, Node assign) {

		if (relType == PDGRelationTypes.USED_BY)
			return;
		String nodeType = paramDec.getProperty("nodeType").toString();

		// With a strategy pattern I can skip this comprobation after register a
		// state change on this
		if (nodeType.contentEquals("THIS_REF") && thisRelationsOnThisMethod != PDGRelationTypes.STATE_MODIFIED_BY) {
			thisRelationsOnThisMethod = (boolean) assign.getProperty("mustBeExecuted")
					? PDGRelationTypes.STATE_MODIFIED_BY : PDGRelationTypes.STATE_MAY_BE_MODIFIED;
			return;
		}
		if (!nodeType.contentEquals(NodeTypes.PARAMETER_DEC.toString()))
			return;
		if (!paramsToOrderedAssignments.containsKey(paramDec))
			paramsToOrderedAssignments.put(paramDec, new ArrayList<Pair<Node, PDGRelationTypes>>());
		paramsToOrderedAssignments.get(paramDec).add(Pair.create(assign, relType));
	}

	void addRels(Symbol s, Node node, Object ASTVisitorParam, Supplier<Node> classNodeSupplier,
			IdToStateModThis idToStateModThis) {
		List<Consumer<Node>> list = null;
		Node decNode = definitionTable.get(s);
		boolean hasNoDec = decNode == null, isThis = s.name.contentEquals("this");
		Node implicitThisNode = idToStateModThis.apply(hasNoDec, decNode, !isThis, s);
		if (hasNoDec)
			if (isThis)
				definitionTable.put(s, decNode = getOrCreateThisNode(classNodeSupplier.get()).getEndNode());
			else {
				list = toDo.get(s);
				if (list == null) {
					list = new ArrayList<Consumer<Node>>();
					toDo.put(s, list);
				}
			}
		if (ASTVisitorParam == null)
			addRelWithoutIdentification(list, decNode, node, PDGRelationTypes.USED_BY);
		else if (implicitThisNode == null)
			for (PDGRelationTypes pdgRel : (PDGRelationTypes[]) ASTVisitorParam)
				addRel(list, decNode, node, pdgRel);
		else

			for (PDGRelationTypes pdgRel : (PDGRelationTypes[]) ASTVisitorParam) {
				addRel(list, decNode, node, pdgRel);
				if (pdgRel != PDGRelationTypes.USED_BY)
					addRelWithoutIdentification(list, implicitThisNode, lastAssignment,
							PDGRelationTypes.STATE_MODIFIED_BY);
			}
	}

	private void addRel(List<Consumer<Node>> list, Node dec, Node concrete, PDGRelationTypes rel) {

		Node end;
		if (rel == PDGRelationTypes.USED_BY)
			end = concrete;
		else {
			end = lastAssignment;
			if (concrete.getProperty("nodeType").toString().contentEquals("IDENTIFIER"))
				if (dec == null)
					list.add(decNode -> identificationForLeftAssignExprs.put(concrete, decNode));
				else
					identificationForLeftAssignExprs.put(concrete, dec);
		}
		addRelWithoutIdentification(list, dec, end, rel);

	}

	private void addRelWithoutIdentification(List<Consumer<Node>> list, Node start, Node end, PDGRelationTypes rel) {

		if (list == null)
			addAssignmentToThisMethod(createRel(start, end, rel), rel, end);
		else
			addRelToDo(list, end, rel);

	}

	private Node createRel(Node start, Node end, PDGRelationTypes rel) {
		start.createRelationshipTo(end, rel);
		return start;
	}

	private void addRelToDo(List<Consumer<Node>> list, Node end, PDGRelationTypes rel) {
		list.add(decNode -> decNode.createRelationshipTo(end, rel));
	}

	public static void createVarDecInitRel(Node varDecNode, Node varDecInit) {
		varDecNode.createRelationshipTo(varDecInit, PDGRelationTypes.MODIFIED_BY);
	}

	public void relationOnIdentifier(IdentifierTree identifierTree, Node identifierNode,
			Pair<PartialRelation<RelationTypes>, Object> t) {

		// System.out.println();
		// System.out.println("ID TREE:\t" + identifierTree);
		// System.out.println("ID SYMBOL:\t" + ((JCIdent) identifierTree).sym);
		// System.out.println("ID SYMBOL:\t" + ((JCIdent)
		// identifierTree).sym.getClass());
		// if (((JCIdent) identifierTree).sym instanceof ClassSymbol) {
		// System.out.println(
		// "ID SYMBOL SI INTERFACE:" + ((ClassSymbol) ((JCIdent)
		// identifierTree).sym).type.getClass());
		// System.out.println(
		// "ID SYMBOL SI INTERFACE:" + ((ClassSymbol) ((JCIdent)
		// identifierTree).sym).type.isInterface());
		//
		// System.out.println("ID SYMBOL IS INT:" + ((ClassSymbol) ((JCIdent)
		// identifierTree).sym).isInterface());
		//
		// System.out.println("ID SYMBOL IS ENUM:" + ((ClassSymbol) ((JCIdent)
		// identifierTree).sym).isEnum());
		// }
		// System.out.println("SYMBOL HASH:\t" + ((JCIdent)
		// identifierTree).sym.hashCode());
		addRels(((JCIdent) identifierTree).sym, identifierNode, t.getSecond(), () -> currentClassDec,
				(Boolean hasNoDec, Node nodeDec, Boolean isNotThis, Symbol s) -> {
					if (Modifier.isStatic((int) s.flags_field))
						return null;
					if (hasNoDec) {
						if (isNotThis && !(s instanceof ClassSymbol)) {
							// Parameters and locals always have a declaration,
							// because the declaration is visited always before
							// the
							// use of the variable
							// Other options are this and ClassSymbol as
							// outer-left
							// ID in a multiple member selection
							return getOrCreateThisNode(currentClassDec).getEndNode();
						}

					} else if (nodeDec.getProperty("nodeType").toString().contentEquals("ATTR_DEC"))
						return getOrCreateThisNode(currentClassDec).getEndNode();
					return null;

				});
	}

	private Relationship getThisRel(Node classDecNode) {
		return classDecNode.getSingleRelationship(RelationTypes.HAS_THIS_REFERENCE, Direction.OUTGOING);
	}

	private Relationship getOrCreateThisNode(Node classDecNode) {
		Relationship r = getThisRel(classDecNode);
		if (r != null)
			return r;
		return classDecNode.createRelationshipTo(DatabaseFachade.createSkeletonNode(NodeTypes.THIS_REF),
				RelationTypes.HAS_THIS_REFERENCE);

	}

	public void relationOnAttribute(MemberSelectTree memberSelectTree, Node memberSelectNode,
			Pair<PartialRelation<RelationTypes>, Object> t) {
		// This takes into account the case of Class.this inside of a inner
		// class
		addRels(((JCFieldAccess) memberSelectTree).sym, memberSelectNode, t.getSecond(),
				() -> DefinitionCache.getOrCreateTypeDec(JavacInfo.getSymbolFromTree(memberSelectTree.getExpression())),
				(Boolean hasDec, Node nodeDec, Boolean isNotThis, Symbol s) -> null);
	}

	public void endVisitClass() {
		currentClassDec = previousClassDec;
		attrsInClassDec = previousAttrsInClassDec;
	}

	public void visitClass(Node classNode) {
		previousClassDec = currentClassDec;
		currentClassDec = classNode;
		previousAttrsInClassDec = attrsInClassDec;
		attrsInClassDec = new ArrayList<Node>();
		classNode.getRelationships(Direction.OUTGOING, RelationTypes.DECLARES_FIELD)
				.forEach(rel -> attrsInClassDec.add(rel.getEndNode()));

	}
}

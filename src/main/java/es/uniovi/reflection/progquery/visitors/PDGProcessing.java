package es.uniovi.reflection.progquery.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set; 
import java.util.function.Consumer;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import es.uniovi.reflection.progquery.utils.dataTransferClasses.MethodState;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import org.neo4j.graphdb.Direction;

import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;

import es.uniovi.reflection.progquery.ast.ASTAuxiliarStorage;
import es.uniovi.reflection.progquery.cache.DefinitionCache;
import es.uniovi.reflection.progquery.database.DatabaseFachade;
import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.PDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.PartialRelation;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.utils.GraphUtils;

public class PDGProcessing {
	public static final PDGRelationTypes[] USED = new PDGRelationTypes[] { PDGRelationTypes.USED_BY },
			MODIFIED = new PDGRelationTypes[] { PDGRelationTypes.MODIFIED_BY },
			STATE_MODIFIED = new PDGRelationTypes[] { PDGRelationTypes.STATE_MODIFIED_BY },
			USED_AND_MOD = new PDGRelationTypes[] { PDGRelationTypes.USED_BY, PDGRelationTypes.MODIFIED_BY },
			USED_AND_STATE_MOD = new PDGRelationTypes[] { PDGRelationTypes.USED_BY,
					PDGRelationTypes.STATE_MODIFIED_BY };
	private static final Map<PDGRelationTypes[], PDGRelationTypes[]> toModify = getMapToModify();

	private Map<Symbol, NodeWrapper> definitionTable = new HashMap<Symbol, NodeWrapper>();
	private Map<Symbol, List<Consumer<NodeWrapper>>> toDo = new HashMap<Symbol, List<Consumer<NodeWrapper>>>();

	public NodeWrapper lastAssignment;
	/*
	 * private Map<Node, Set<NodeWrapper>> paramsMutatedInMethods = new
	 * HashMap<Node, Set<NodeWrapper>>(), paramsMayMutateInMethods = new
	 * HashMap<Node, Set<NodeWrapper>>();
	 */
	public boolean isLastAssginmentInstanceAssign = false;

	private Set<NodeWrapper> parametersPreviouslyModified, auxParamsModified, parametersMaybePrevioslyModified;

	public void visitNewMethod() {
		parametersPreviouslyModified = new HashSet<>();
		parametersMaybePrevioslyModified = new HashSet<>();
	}

	public void enteringNewBranch() {
		auxParamsModified = parametersPreviouslyModified;
		parametersPreviouslyModified = new HashSet<>(parametersPreviouslyModified);
	}

	public void copyParamsToMaybe() {
		for (NodeWrapper parameterDec : parametersPreviouslyModified)
			parametersMaybePrevioslyModified.add(parameterDec);
	}

	public Set<NodeWrapper> exitingCurrentBranch() {
		copyParamsToMaybe();
		Set<NodeWrapper> ret = parametersPreviouslyModified;
		parametersPreviouslyModified = auxParamsModified;
		return ret;
	}

	public void merge(Set<NodeWrapper> paramsOne, Set<NodeWrapper> paramsTwo) {
		// System.out.println("MERGE PARAMS TWO");
		// for (NodeWrapper n : paramsTwo)
		// System.out.println(NodeUtils.nodeToString(n));
		parametersPreviouslyModified = paramsOne;
		parametersPreviouslyModified.retainAll(paramsTwo);
		// System.out.println("AFTER MERGE");

		// for (NodeWrapper n : parametersPreviouslyModified)
		// System.out.println(NodeUtils.nodeToString(n));
	}

	public void mergeParamsWithCurrent(Set<NodeWrapper> otherParams) {
		parametersPreviouslyModified.retainAll(otherParams);
	}

	public void unionWithCurrent(Set<NodeWrapper> otherParams) {
		parametersPreviouslyModified.addAll(otherParams);
	}

	public void setThisRefOfInstanceMethod(MethodState methodState, NodeWrapper currentClassDec) {

		// AL LLEGAR AL END SE HACEN LAS RELACIONES....
		NodeWrapper method = methodState.lastMethodDecVisited;
		if (!method.hasLabel(NodeTypes.ATTR_DEF)) {
			boolean isStatic = (boolean) method.getProperty("isStatic");
			if (!isStatic && methodState.thisNode == null)
				methodState.thisNode = getOrCreateThisNode(currentClassDec).getEndNode();
		}

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

	public void putDecInCache(Symbol s, NodeWrapper n) {
		// This method is only called from visitVariable to register any var in
		// the defTable
		// and check if any field was added without definition
		// System.out.println("PUTTING " + s + " IN CACHE");
		if (toDo.containsKey(s)) {
			for (Consumer<NodeWrapper> consumer : toDo.get(s))
				// System.out.println("EXECUTING A CONSUMER");
				consumer.accept(n);

			toDo.remove(s);
		}
		definitionTable.put(s, n);
	}

	public static Object getLefAssignmentArg(Pair<PartialRelation<RelationTypes>, Object> t) {
		// System.out.println("ARG:\n" + t.getSecond());
		return t.getSecond() == MODIFIED ? MODIFIED : USED_AND_MOD;
	}

	public static Object modifiedToStateModified(Pair<PartialRelation<RelationTypes>, Object> t) {
		return toModify.get(t.getSecond());
	}

	@FunctionalInterface
	private interface IdToStateModThis {
		public NodeWrapper apply(Boolean hasNoDec, NodeWrapper nodeDec, Boolean isNotThis, Symbol s);
	}

	/*
	 * private void addAssignmentToThisMethod(NodeWrapper paramDec, PDGRelationTypes
	 * relType, NodeWrapper assign, MethodState methodState, boolean
	 * isInstanceAssign) { System.out.println(
	 * "\nANALYSING ASSIGN FOR METHOD AND THIS REL " +
	 * methodState.lastMethodDecVisited.getProperty("name") +
	 * methodState.lastMethodDecVisited.getProperty("actualType") + " " +
	 * methodState.lastMethodDecVisited.getProperty("lineNumber"));
	 * System.out.println("CURRENT THIS REL " +
	 * methodState.thisRelationsOnThisMethod); System.out.println("ASSIGNMENT " +
	 * assign.getId() + " " + assign.getProperty("lineNumber"));
	 * System.out.println("RELTYPE:\t" + relType); System.out.println("IS_THIS:\t" +
	 * paramDec.hasLabel(NodeTypes.THIS_REF)); System.out.println("PARAM DEC:\n" +
	 * NodeUtils.nodeToString(paramDec));
	 * 
	 * if (relType == PDGRelationTypes.USED_BY) return;
	 * 
	 * // With a strategy pattern I can skip this comprobation after register a //
	 * state change on this if (paramDec.hasLabel(NodeTypes.THIS_REF) &&
	 * methodState.thisRelationsOnThisMethod != PDGRelationTypes.STATE_MODIFIED_BY)
	 * { System.out.println("CHANGING CURRENT " + ((boolean)
	 * assign.getProperty("mustBeExecuted") ? PDGRelationTypes.STATE_MODIFIED_BY :
	 * PDGRelationTypes.STATE_MAY_BE_MODIFIED));
	 * methodState.thisRelationsOnThisMethod = (boolean)
	 * assign.getProperty("mustBeExecuted") ? PDGRelationTypes.STATE_MODIFIED_BY :
	 * PDGRelationTypes.STATE_MAY_BE_MODIFIED; return; }
	 * 
	 * if (!paramDec.hasLabel(NodeTypes.PARAMETER_DEC)) return; if
	 * (!methodState.paramsToOrderedAssignments.containsKey(paramDec))
	 * methodState.paramsToOrderedAssignments.put(paramDec, new ArrayList<Pair<Node,
	 * PDGRelationTypes>>());
	 * methodState.paramsToOrderedAssignments.get(paramDec).add(Pair.create( assign,
	 * relType)); }
	 */

	private void addRels(Symbol s, NodeWrapper node, Object ASTVisitorParam, NodeWrapper currentClassDec,
			boolean isIdent, MethodState methodState, NodeWrapper decNode, boolean isThis, boolean isInstance,
			Tree tree) {
		List<Consumer<NodeWrapper>> list = null;
		// System.out.println("ADD RELS " + s);
		// System.out.println("SYMBOL:\t" + s);
		// System.out.println(!isThis);
		// System.out.println(hasNoDec);
		// System.out.println(NodeUtils.nodeToString(decNode));
		// if (ASTVisitorParam != null)
		// for (PDGRelationTypes pdgRel : (PDGRelationTypes[]) ASTVisitorParam)
		// System.out.println(pdgRel);
		if (decNode == null)
			if (isThis)
				definitionTable.put(s, decNode = getOrCreateThisNode(currentClassDec).getEndNode());
			else {

				// System.out.println("Symbol " + s + " without dec and ...");
				// System.out.println(tree);
				// System.out.println(s.getKind());
				// System.out.println(s.getClass());

				list = toDo.get(s);
				if (list == null) {
					list = new ArrayList<Consumer<NodeWrapper>>();
					toDo.put(s, list);
				}
			}
		boolean isAttr = decNode == null || decNode.hasLabel(NodeTypes.ATTR_DEF)
		// Si en lugar de pasar isAttrOrThis, pasaramos isTHis, isAttr, nos
		// ahorrabamos preguntar en leftAnalysis si dec is attr_DEC o
		// THIS_DEC-----> No, porque si dec es null hay que esperar---> No, se
		// sabe que es field y no this
		;
		boolean isStatic = s.isStatic();
		if (ASTVisitorParam == null)
			// {
			// System.out.println("DEC:\n" + NodeUtils.nodeToString(decNode));
			// System.out.println(s + " NULLLL");
			addRelWithoutAnalysis(list, decNode, node, PDGRelationTypes.USED_BY, methodState, isAttr || isThis,
					isInstance, isStatic);
		// }
		else
			// {
			// System.out.println("DEC:\n" + NodeUtils.nodeToString(decNode));
			for (PDGRelationTypes pdgRel : (PDGRelationTypes[]) ASTVisitorParam)
				// {
				// System.out.println(s + " " + pdgRel);
				addUnknownRel(list, decNode, node, pdgRel, methodState, isIdent, currentClassDec, isAttr, isThis,
						isInstance, isStatic);

		// }
		// }
		/*
		 * else
		 * 
		 * for (PDGRelationTypes pdgRel : (PDGRelationTypes[]) ASTVisitorParam) {
		 * addRel(list, decNode, node, pdgRel, methodState); if (pdgRel !=
		 * PDGRelationTypes.USED_BY) { // SI LA RELACI�N NO ES USED BY---> HAY QUE
		 * A�ADIR PRIMERO // LA RELACI�N STATE_MOD_BY (SOLO LAS the this implicito, //
		 * para no duplicar) y la de // metodo, sacar de donde co�o esta super
		 * acoplada.... // preguntando lo mismo..., // donde se est� a�adiendo la otra??
		 * la de A a; // a.a=2; en el es.uniovi.reflection.progquery.ast visitor if (lastAssignmentInfo.isInstanceField
		 * && !node.hasLabel(NodeTypes.THIS_REF)) addRelWithoutIdentification(list,
		 * implicitThisNode, lastAssignmentInfo, PDGRelationTypes.STATE_MODIFIED_BY,
		 * methodState); } }
		 */
	}

	private void addUnknownRel(List<Consumer<NodeWrapper>> list, NodeWrapper dec, NodeWrapper concrete,
			PDGRelationTypes rel, MethodState methodState, boolean isIdent, NodeWrapper currentClassDec, boolean isAttr,
			boolean isThis, boolean isInstance, boolean isStatic) {
		// System.out.println("DEC:\n" + NodeUtils.nodeToString(dec));
		// System.out.println("CONCRETE:\n" + NodeUtils.nodeToString(concrete));
		// System.out.println(rel);
		// System.out.println(list);
		if (rel == PDGRelationTypes.USED_BY)
			addRelWithoutAnalysis(list, dec, concrete, rel, methodState, isAttr || isThis, isInstance, isStatic);
		else
			addNotUseRelWithAnalysis(lastAssignment, dec, rel, isIdent, methodState, list, currentClassDec, isAttr,
					isThis, isInstance, isStatic);

	}

	public static void addNewPDGRelationFromParamToMethod(boolean mustBeExecuted, PDGRelationTypes currentRel,
			Consumer<PDGRelationTypes> putNewRel) {
		PDGRelationTypes decAndMethodRel = mustBeExecuted ? PDGRelationTypes.STATE_MODIFIED_BY
				: PDGRelationTypes.STATE_MAY_BE_MODIFIED_BY;
		if (currentRel == null)
			putNewRel.accept(decAndMethodRel);
		else if (currentRel == PDGRelationTypes.STATE_MAY_BE_MODIFIED_BY
				&& decAndMethodRel == PDGRelationTypes.STATE_MODIFIED_BY)
			putNewRel.accept(PDGRelationTypes.STATE_MODIFIED_BY);
	}

	private static void addNewPDGRelationFromThisToMethod(NodeWrapper assignment, NodeWrapper thisNode,
			MethodState methodState) {

		addNewPDGRelationFromParamToMethod((Boolean) assignment.getProperty("mustBeExecuted"),
				methodState.thisRelationsOnThisMethod, (newRel) -> {
					methodState.thisRelationsOnThisMethod = newRel;
					methodState.thisNode = thisNode;
				});
	}

	private static void addNewPDGRelationFromParamToMethod(NodeWrapper assignment, NodeWrapper paramDec,
			MethodState methodState, boolean notMustAlwaysFalse) {
		// System.out.println("Adding FALSE rel param to method " +
		// NodeUtils.getNameFromDec(paramDec));
		addNewPDGRelationFromParamToMethod(false, methodState.paramsToPDGRelations.get(paramDec),
				(newRel) -> methodState.paramsToPDGRelations.put(paramDec, newRel));
	}

	private static void addNewPDGRelationFromParamToMethod(NodeWrapper assignment, NodeWrapper paramDec,
			MethodState methodState) {

		// System.out.println("Adding rel param to method " +
		// NodeUtils.getNameFromDec(paramDec));
		addNewPDGRelationFromParamToMethod((Boolean) assignment.getProperty("mustBeExecuted"),
				methodState.paramsToPDGRelations.get(paramDec),
				(newRel) -> methodState.paramsToPDGRelations.put(paramDec, newRel));
	}

	private boolean mutationAnalysis(NodeWrapper concrete, NodeWrapper dec, PDGRelationTypes rel, boolean isIdent,
			MethodState methodState, NodeWrapper currentClassDec, boolean isOwnAccess, boolean isAttr, boolean isThis,
			boolean isStatic) {

		if (isIdent) {
			if (isAttr) {
				if (!isStatic) {
					NodeWrapper implicitThis = getOrCreateThisNode(currentClassDec).getEndNode();
					createRel(implicitThis, concrete, PDGRelationTypes.STATE_MODIFIED_BY, true, true, isStatic);
					addNewPDGRelationFromThisToMethod(concrete, implicitThis, methodState);
				}
				return true;
			} else if (isThis) {
				addNewPDGRelationFromThisToMethod(concrete, getOrCreateThisNode(currentClassDec).getEndNode(),
						methodState);
				return true;
			}

			else if (isNormalParameter(methodState,dec)  ) {
				if (rel == PDGRelationTypes.STATE_MODIFIED_BY) {
					// System.out.println("ANALIZING STATE MOD BY of PARAM
					// \n" + NodeUtils.nodeToString(dec));
					// System.out.println(parametersPreviouslyModified.contains(dec));
					// System.out.println(parametersMaybePrevioslyModified.contains(dec));
					if (!parametersPreviouslyModified.contains(dec)) { 
						if (parametersMaybePrevioslyModified.contains(dec))
							addNewPDGRelationFromParamToMethod(concrete, dec, methodState, false);
						else
							addNewPDGRelationFromParamToMethod(concrete, dec, methodState);
					}
				} else if (rel == PDGRelationTypes.MODIFIED_BY)
					parametersPreviouslyModified.add(dec);
			}
			// }
		}
		return false;
	}

	private boolean isNormalParameter(MethodState methodState, NodeWrapper dec) {
		if(dec.hasLabel(NodeTypes.PARAMETER_DEF)) {
			RelationshipWrapper paramRel = dec
					.getRelationships(Direction.INCOMING, RelationTypes.CALLABLE_HAS_PARAMETER,RelationTypes.LAMBDA_EXPRESSION_PARAMETERS).get(0);
//		if(dec.getProperty("name").toString().contentEquals("onClose")) {
//			System.out.println("ON CLOSE " + (paramRel.getStartNode() == methodState.lastMethodDecVisited));
//			System.out.println("ON CLOSE " + (paramRel.getStartNode().getProperty("fullyQualifiedName")));
//		}
			//ASI SE DEBERIA CONTROLAR QUE NO HAY LAMBDA NI PARAMETROS FINAL EN CLASES ANONIMAS
			return paramRel.getStartNode()==methodState.lastMethodDecVisited;
		}
		return false;
	}

	private void addNotUseRelWithAnalysis(NodeWrapper concrete, NodeWrapper dec, PDGRelationTypes rel, boolean isIdent,
			MethodState currentMethodState, List<Consumer<NodeWrapper>> toDoListForSymbol, NodeWrapper currentClassDec,
			boolean isAttr, boolean isThis, boolean isInstanceRel, boolean isStatic) {

		if (dec == null)

			toDoListForSymbol.add(decNode -> createRelsAndMutationAnalysis(concrete, decNode, rel, isIdent,
					currentMethodState, toDoListForSymbol, currentClassDec, isAttr, isThis, isInstanceRel, isStatic));
		else
			createRelsAndMutationAnalysis(concrete, dec, rel, isIdent, currentMethodState, toDoListForSymbol,
					currentClassDec, isAttr, isThis, isInstanceRel, isStatic);
	}

	private void createRelsAndMutationAnalysis(NodeWrapper concrete, NodeWrapper dec, PDGRelationTypes rel,
			boolean isIdent, MethodState currentMethodState, List<Consumer<NodeWrapper>> toDoListForSymbol,
			NodeWrapper currentClassDec, boolean isAttr, boolean isThis, boolean isOwnAccess, boolean isStatic) {
		mutationAnalysis(concrete, dec, rel, isIdent, currentMethodState, currentClassDec, isOwnAccess, isAttr, isThis,
				isStatic);
		createRel(dec, concrete, rel, isAttr || isThis, isOwnAccess, isStatic);
		currentMethodState.identificationForLeftAssignExprs.put(concrete, dec);
	}

	private void addRelWithoutAnalysis(List<Consumer<NodeWrapper>> list, NodeWrapper start, NodeWrapper end,
			PDGRelationTypes rel, MethodState methodState, boolean isAttrOrThis, boolean isOwnAccess,
			boolean isStatic) {
		if (list == null)
			createRel(start, end, rel, isAttrOrThis, isOwnAccess, isStatic);
		else
			futureCreateRelInToDoList(list, end, rel, methodState, isAttrOrThis, isOwnAccess, isStatic);
	}

	private static NodeWrapper createRel(NodeWrapper start, NodeWrapper end, PDGRelationTypes rel,
			boolean isAttrDecOrThis, boolean isInstanceRel, boolean isStatic) {
		//
		// System.out.println("START:\n" + NodeUtils.nodeToString(start));
		// System.out.println("END:\n" + NodeUtils.nodeToString(end));
		// System.out.println("REL:\n" + rel);

		RelationshipWrapper relationship = start.createRelationshipTo(end, rel);
		if (isAttrDecOrThis && !isStatic)
			relationship.setProperty("isOwnAccess", isInstanceRel);
		return start;
	}

	private void futureCreateRelInToDoList(List<Consumer<NodeWrapper>> list, NodeWrapper end, PDGRelationTypes rel,
			MethodState methodState, boolean isAttrDecOrThis, boolean isOwnAccess, boolean isStatic) {

		list.add(decNode -> createRel(decNode, end, rel, isAttrDecOrThis, isOwnAccess, isStatic));
	}

	public static void createVarDecInitRel(NodeWrapper currentClassDec, NodeWrapper varDecInit, boolean isAttr,
			boolean isStatic) {

		if (isAttr && !isStatic)
			createRel(getOrCreateThisNode(currentClassDec).getEndNode(), varDecInit, PDGRelationTypes.STATE_MODIFIED_BY,
					true, true, false);
		// crear la relacion THIS - state modified by -> attr initialization
	}

	public boolean relationOnIdentifier(IdentifierTree identifierTree, NodeWrapper identifierNode,
			Pair<PartialRelation<RelationTypes>, Object> t, NodeWrapper currentClassDec, MethodState methodState) {
		Symbol identSymbol = ((JCIdent) identifierTree).sym;

		if (identSymbol.getKind() == ElementKind.METHOD || identSymbol.getKind() == ElementKind.CONSTRUCTOR
				|| identSymbol.getKind() == ElementKind.TYPE_PARAMETER)
			return false;
		if (identSymbol.getKind() == ElementKind.CLASS || identSymbol.getKind() == ElementKind.INTERFACE
				|| identSymbol.getKind() == ElementKind.ENUM || identSymbol.getKind() == ElementKind.PACKAGE
				|| identSymbol.getKind() == ElementKind.ANNOTATION_TYPE)
			return true;
		NodeWrapper decNode = definitionTable.get(identSymbol);
		boolean isThis = identSymbol.name.contentEquals("this") || identSymbol.name.contentEquals("super"), isInstance =

				(decNode == null || decNode.hasLabel(NodeTypes.ATTR_DEF) || decNode.hasLabel(NodeTypes.THIS_REF))
						&& !identSymbol.isStatic();

		// System.out.println(identifierTree);
		// System.out.println(identifierNode.getProperty("lineNumber"));
		addRels(identSymbol, identifierNode, t.getSecond(), currentClassDec, true, methodState, decNode, isThis,
				isInstance, identifierTree);
		return isInstance;
		// SI Es MOD Y TAL A�ADIR y no es this implicito
		// PERO LA DE LOS METODOS ES CON CUALQUIERA-depende, SON DISTINTAS

		// THIS-SM-> METHOD SOLO AQUI EN IDENT
		// O PARAM-SM ->METHOD IGUAL
		// O ATTR - SM ->METHOD PARA CUALQUIERA, Y CON PROPERTY A VER SI ES DE
		// INSTANCIA
	}

	private static RelationshipWrapper getThisRel(NodeWrapper classDecNode) {
		// System.out.println(classDecNode);
		return classDecNode.getSingleRelationship(Direction.OUTGOING, PDGRelationTypes.HAS_THIS_REFERENCE);
	}

	private static RelationshipWrapper getOrCreateThisNode(NodeWrapper classDecNode) {
		RelationshipWrapper r = getThisRel(classDecNode);
		if (r != null)
			return r;
		return classDecNode.createRelationshipTo(
				DatabaseFachade.CURRENT_DB_FACHADE.createNodeWithoutExplicitTree(NodeTypes.THIS_REF),
				PDGRelationTypes.HAS_THIS_REFERENCE);

	}

	public void createNotDeclaredAttrRels(ASTAuxiliarStorage ast) {
		for (Entry<Symbol, List<Consumer<NodeWrapper>>> entry : toDo.entrySet()) {
//			System.out.println(entry.getKey())<
			VarSymbol symbol = (VarSymbol) entry.getKey();
			if (symbol.name.contentEquals("class"))
				continue;
			NodeWrapper fieldDec = createNotDeclaredAttr(symbol, ast);
			entry.getValue().forEach(decConsumer -> decConsumer.accept(fieldDec));

		}
		toDo.clear();
	}

	private NodeWrapper createNotDeclaredAttr(VarSymbol s, ASTAuxiliarStorage ast) {
		NodeWrapper
		// Se hacen muchas cosas y es posible que se visite la
		// declaraci�n despu�s
		decNode = DatabaseFachade.CURRENT_DB_FACHADE.createNodeWithoutExplicitTree(NodeTypes.ATTR_DEF);

		decNode.setProperty("isDeclared", false);
		decNode.setProperty("name", s.name.toString());
		Set<Modifier> modifiers = Flags.asModifierSet(s.flags_field);
		ASTTypesVisitor.checkAttrDecModifiers(modifiers, decNode);
		GraphUtils.attachType(decNode, s.type, ast);
		DefinitionCache.getOrCreateType(s.owner.type, ast).createRelationshipTo(decNode, RelationTypes.DECLARES_FIELD);
		return decNode;

	}

	public void relationOnFieldAccess(MemberSelectTree memberSelectTree, NodeWrapper memberSelectNode,
			Pair<PartialRelation<RelationTypes>, Object> t, MethodState methodState, NodeWrapper currentClassDec,
			boolean isInstance) {
//		System.out.println("FIELD ACCESS :\n" + memberSelectTree + "IS_INSTANCE:" + isInstance);
		// This takes into account the case of Class.this inside of a inner
		// class
		// If you add C.this.attr, definition for C identifier is never found
		// due to classes defs are not registered here
		Symbol symbol = ((JCFieldAccess) memberSelectTree).sym;
		if (symbol.getKind() == ElementKind.CLASS  || symbol.getKind() == ElementKind.ANNOTATION_TYPE || symbol.getKind() == ElementKind.INTERFACE
				|| symbol.getKind() == ElementKind.ENUM || symbol.getKind() == ElementKind.METHOD
				|| symbol.getKind() == ElementKind.CONSTRUCTOR || symbol.getKind() == ElementKind.PACKAGE)
			return;
		NodeWrapper decNode = definitionTable.get(symbol);
		// System.out.println(memberSelectTree);
		// System.out.println(memberSelectNode.getProperty("lineNumber"));

		addRels(symbol, memberSelectNode, t.getSecond(), currentClassDec,

				false, methodState, decNode, memberSelectTree.getIdentifier().contentEquals("this")
						|| memberSelectTree.getIdentifier().contentEquals("super"),
				isInstance, memberSelectTree);
	}

	public void addParamsPrevModifiedForInv(NodeWrapper methodInvocationNode, MethodState methodState) {
		if (parametersPreviouslyModified == null)
			return;
		if (parametersPreviouslyModified.size() > 0) {
			methodState.callsToParamsPreviouslyModified.put(methodInvocationNode,
					new HashSet<>(parametersPreviouslyModified));
			// for(NodeWrapper n:parametersPreviouslyModified)
			// System.out.println(NodeUtils.nodeToString(n));
		}
		if (parametersMaybePrevioslyModified.size() > 0) {
			methodState.callsToParamsMaybePreviouslyModified.put(methodInvocationNode,
					new HashSet<>(parametersMaybePrevioslyModified));
			// for(NodeWrapper n:parametersMaybePrevioslyModified)
			// System.out.println(NodeUtils.nodeToString(n));
			//
		}
	}

}

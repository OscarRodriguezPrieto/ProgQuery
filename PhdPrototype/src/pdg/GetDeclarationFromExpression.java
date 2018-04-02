package pdg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import database.nodes.NodeUtils;
import database.relations.PDGRelationTypes;
import database.relations.RelationTypes;
import utils.Pair;

public class GetDeclarationFromExpression {

	private Map<Node, Node> thisRefsOfMethods;
	private Map<Node, Node> identificationForLeftAssignIdents;

	// Map<MethodInvocation,List<Vardec,Boolean,Integer>>
	private Map<Node, Map<Integer, List<Pair<Node, Boolean>>>> invocationsMayModifyVars = new HashMap<Node, Map<Integer, List<Pair<Node, Boolean>>>>();

	public Map<Node, Map<Integer, List<Pair<Node, Boolean>>>> getInvocationsMayModifyVars() {
		return invocationsMayModifyVars;
	}

	public void setIdentificationForLeftAssignIdents(Map<Node, Node> identificationForLeftAssignIdents) {
		this.identificationForLeftAssignIdents = identificationForLeftAssignIdents;
	}

	public GetDeclarationFromExpression(Map<Node, Node> thisRefsOfMethods) {
		this.thisRefsOfMethods = thisRefsOfMethods;
	}

	private List<Pair<Node, Boolean>> scan(Node n) {
		switch (n.getProperty("nodeType").toString()) {

		case "METHOD_INVOCATION":
			return scanMethodInvocation(n);
		case "CONDITIONAL_EXPRESSION":
			return scanConditionalExpression(n);
		case "IDENTIFIER":
			return scanIdentifier(n);
		case "MEMBER_SELECTION":
			return scanAPart(n, RelationTypes.MEMBER_SELECT_EXPR);
		case "ARRAY_ACCESS":
			return scanAPart(n, RelationTypes.ARRAYACCESS_EXPR);
		case "ASSIGNMENT":
			return scanAPart(n, RelationTypes.ASSIGNMENT_LHS);
		case "TYPE_CAST":
			return scanAPart(n, RelationTypes.CAST_ENCLOSES);

		default:
			return new ArrayList<Pair<Node, Boolean>>();

		}
	}

	private List<Pair<Node, Boolean>> scanIdentifier(Node identifier) {
		Node identDeclaration = identificationForLeftAssignIdents.get(identifier);
		if (identDeclaration == null && identifier.hasRelationship(PDGRelationTypes.USED_BY, Direction.INCOMING))
			identDeclaration = identifier.getSingleRelationship(PDGRelationTypes.USED_BY, Direction.INCOMING)
					.getStartNode();
		List<Pair<Node, Boolean>> ret = new ArrayList<Pair<Node, Boolean>>();
		// If there is a declaration stored (in the map or in the rels) ret this
		// Param, Local or this dec
		if (identDeclaration != null) {
			ret.add(Pair.create(identDeclaration, true));
			return ret;
		}

		Relationship methodInvocationRelIfExists = identifier
				.getSingleRelationship(RelationTypes.METHODINVOCATION_METHOD_SELECT, Direction.INCOMING);
		if (methodInvocationRelIfExists == null)
			return ret;

		identDeclaration = methodInvocationRelIfExists.getStartNode()
				.getSingleRelationship(RelationTypes.HAS_DEC, Direction.OUTGOING).getEndNode();
		Node thisRef = thisRefsOfMethods.get(identDeclaration);
		// If thisRef is null, then it must be a static method
		if (thisRef == null)
			return ret;
		ret.add(Pair.create(thisRef, true));
		return ret;
	}

	private List<Pair<Node, Boolean>> scanConditionalExpression(Node conditionalExpr) {
		List<Pair<Node, Boolean>> decsOfTheInvocation = new ArrayList<Pair<Node, Boolean>>();
		decsOfTheInvocation.addAll(convertMustToMay(scan(conditionalExpr
				.getSingleRelationship(RelationTypes.CONDITIONAL_EXPR_CONDITION, Direction.OUTGOING).getEndNode())));
		decsOfTheInvocation.addAll(convertMustToMay(scan(conditionalExpr
				.getSingleRelationship(RelationTypes.CONDITIONAL_EXPR_CONDITION, Direction.OUTGOING).getEndNode())));
		return null;
	}

	private List<Pair<Node, Boolean>> scanAPart(Node memberSelection, RelationTypes r) {
		return scan(memberSelection.getSingleRelationship(r, Direction.OUTGOING).getEndNode());
	}

	private List<Pair<Node, Boolean>> convertMustToMay(List<Pair<Node, Boolean>> previous) {

		return previous.stream().map(previousPair -> Pair.create(previousPair.getFirst(), false))
				.collect(Collectors.toList());
	}

	private List<Pair<Node, Boolean>> getDecsForAReturn(Relationship possibleRet,
			Map<Integer, List<Pair<Node, Boolean>>> varDecsInArguments, boolean singleReturn) {
		List<Pair<Node, Boolean>> ret;
		if (possibleRet.getEndNode().getProperty("nodeType").toString().contentEquals("THIS_REF")) {
			ret = varDecsInArguments.get(0);
			return singleReturn ? ret : convertMustToMay(ret);
		} else {
			// Parameter or part of parameter
			ret = varDecsInArguments.get((int) possibleRet.getProperty("paramIndex"));
			return singleReturn ? ret : convertMustToMay(ret);
		}
	}

	public List<Pair<Node, Boolean>> scanMethodInvocation(Node methodInvocation) {
		System.out.println("ANALYSING METHOD_ INVOCATION:\n" + NodeUtils.nodeToString(methodInvocation));

		Map<Integer, List<Pair<Node, Boolean>>> varDecsInArguments = new HashMap<Integer, List<Pair<Node, Boolean>>>();
		varDecsInArguments.put(0, scan(methodInvocation
				.getSingleRelationship(RelationTypes.METHODINVOCATION_METHOD_SELECT, Direction.OUTGOING).getEndNode()));
		System.out.println("METHOD_SELECT:\n" + NodeUtils.nodeToString(methodInvocation
				.getSingleRelationship(RelationTypes.METHODINVOCATION_METHOD_SELECT, Direction.OUTGOING).getEndNode()));

		for (Relationship argumentRel : methodInvocation.getRelationships(RelationTypes.METHODINVOCATION_ARGUMENTS,
				Direction.OUTGOING)) {
			System.out.println("Argument " + (int) argumentRel.getProperty("argumentIndex") + ":\n"
					+ NodeUtils.nodeToString(argumentRel.getEndNode()));
			varDecsInArguments.put((int) argumentRel.getProperty("argumentIndex"), scan(argumentRel.getEndNode()));
		}
		// Aquí faltan cosas pa sacar la declaracion
		Node methodDeclaration = methodInvocation.getSingleRelationship(RelationTypes.HAS_DEC, Direction.OUTGOING)
				.getEndNode();
		List<Pair<Node, Boolean>> decsOfTheInvocation = new ArrayList<Pair<Node, Boolean>>();
		for (Relationship rel : methodDeclaration.getRelationships(Direction.OUTGOING, PDGRelationTypes.RETURNS,
				PDGRelationTypes.RETURNS_A_PART_OF)) {
			return getDecsForAReturn(rel, varDecsInArguments, true);
		}
		for (Relationship rel : methodDeclaration.getRelationships(Direction.OUTGOING, PDGRelationTypes.MAY_RETURN,
				PDGRelationTypes.MAY_RETURN_A_PART_OF))
			decsOfTheInvocation.addAll(getDecsForAReturn(rel, varDecsInArguments, false));

		System.out.println("PUTTING METHOD_ INVOCATION:\n" + NodeUtils.nodeToString(methodInvocation));
		for (Entry<Integer, List<Pair<Node, Boolean>>> entry : varDecsInArguments.entrySet()) {
			System.out.println("For integer " + entry.getKey() + "  :");
			for (Pair<Node, Boolean> pair : entry.getValue())
				System.out.println(NodeUtils.nodeToString(pair.getFirst()) + "\n" + pair.getSecond());
		}
		invocationsMayModifyVars.put(methodInvocation, varDecsInArguments);

		// Falta recorrer a la derecha y añadir cache porque vamos a visitar
		// todos los calls
		return decsOfTheInvocation;
	}

	public List<Pair<Node, Boolean>> scanNewClass(Node newClass) {
		// TODO
		return new ArrayList<Pair<Node, Boolean>>();
	}
}

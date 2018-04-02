package pdg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import database.nodes.NodeUtils;
import database.relations.PDGRelationTypes;
import database.relations.RelationTypes;
import utils.Pair;

public class InterproceduralPDG {
	private Set<Node> methodsMutateThisAndParams;
	private Map<Node, Set<Node>> paramsMutatedInMethods, paramsMayMutateInMethods;
	private Map<Node, Node> thisRefsOfMethods;

	private Map<Node, Iterable<Relationship>> methodDecToCalls;
	private Map<Node, Map<Integer, List<Pair<Node, Boolean>>>> invocationsMayModifyVars = new HashMap<Node, Map<Integer, List<Pair<Node, Boolean>>>>();

	private Set<Node> methodDecsAnalyzed = new HashSet<Node>();

	public InterproceduralPDG(Set<Node> methodsMutateThisAndParams, Map<Node, Set<Node>> paramsMutatedInMethods,
			Map<Node, Set<Node>> paramsMayMutateInMethods, Map<Node, Node> thisRefsOfMethods,
			Map<Node, Iterable<Relationship>> methodDecToCalls,
			Map<Node, Map<Integer, List<Pair<Node, Boolean>>>> invocationsMayModifyVars) {
		this.methodsMutateThisAndParams = methodsMutateThisAndParams;
		this.paramsMutatedInMethods = paramsMutatedInMethods;
		this.paramsMayMutateInMethods = paramsMayMutateInMethods;
		this.thisRefsOfMethods = thisRefsOfMethods;
		this.methodDecToCalls = methodDecToCalls;
		this.invocationsMayModifyVars = invocationsMayModifyVars;
	}

	public void doInterproceduralPDGAnalysis(Node methodDec) {
		methodDecsAnalyzed.add(methodDec);
		System.out.println("ANALYSING METHOD DEC\n" + NodeUtils.nodeToString(methodDec));
		if (methodDecToCalls.containsKey(methodDec))
			// Aquí pueden llegar, sin estar en el map, el constructor por
			// defecto, que no llama a
			// nadie, sólo a super, se puede omitir, y cualquier método no
			// declarado en el proyecto hashCode lenght println...., de los que
			// no podemos sacar esa información
			for (Relationship callRel : methodDecToCalls.get(methodDec)) {

			Node invocationDec = callRel.getEndNode().getSingleRelationship(RelationTypes.HAS_DEC, Direction.OUTGOING).getEndNode();
			if (!methodDecsAnalyzed.contains(invocationDec))
			doInterproceduralPDGAnalysis(invocationDec);
			System.out.println("INVOCATION DEC:\n" + NodeUtils.nodeToString(invocationDec));

			for (Relationship invocationDecModifStateRel : invocationDec.getRelationships(Direction.INCOMING, PDGRelationTypes.STATE_MODIFIED_BY, PDGRelationTypes.STATE_MAY_BE_MODIFIED)) {
			if (invocationDecModifStateRel.getStartNode().getProperty("nodeType").toString().contentEquals("THIS_REF"))
			createRelationsIfNeededForArgumentNumber(0, callRel, invocationDecModifStateRel, methodDec);
			else
			createRelationsIfNeededForArgumentNumber((Integer) invocationDecModifStateRel.getStartNode().getSingleRelationship(RelationTypes.HAS_METHODDECL_PARAMETERS, Direction.INCOMING).getProperty("paramIndex"), callRel, invocationDecModifStateRel, methodDec);
			}
			}

	}

	private void createRelationsIfNeededForArgumentNumber(int argNumber, Relationship callRel,
			Relationship invocationDecModifStateRel, Node methodDec) {
		System.out.println("VARS MAY BE MODIFIED BY :" + NodeUtils.nodeToString(callRel.getEndNode()) + ":\n");
		for (Pair<Node, Boolean> varMayOrMustBeModified : invocationsMayModifyVars.get(callRel.getEndNode())
				.get(argNumber)) {
			System.out.println("VAR FOUND:\n" + NodeUtils.nodeToString(varMayOrMustBeModified.getFirst()));
			System.out.println(varMayOrMustBeModified.getSecond() + "\n");
			boolean isMay = !varMayOrMustBeModified.getSecond()
					|| invocationDecModifStateRel.getType().toString().contains("MAY");
			varMayOrMustBeModified.getFirst().createRelationshipTo(callRel.getEndNode(),
					isMay ? PDGRelationTypes.STATE_MAY_BE_MODIFIED : PDGRelationTypes.STATE_MODIFIED_BY);
			String nodeType = varMayOrMustBeModified.getFirst().getProperty("nodeType").toString();
			if (nodeType.contentEquals("THIS_REF") || nodeType.contentEquals("PARAMETER_DEC"))
				varMayOrMustBeModified.getFirst().createRelationshipTo(methodDec,
						isMay || !(Boolean) callRel.getProperty("mustBeExecuted")
								? PDGRelationTypes.STATE_MAY_BE_MODIFIED : PDGRelationTypes.STATE_MODIFIED_BY);
		}
	}
}

package pdg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import database.nodes.NodeTypes;
import database.relations.CGRelationTypes;
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
		// System.out.println("ANALYSING METHOD DEC\n" +
		// NodeUtils.nodeToString(methodDec));
		if (methodDecToCalls.containsKey(methodDec))
			// Aquí pueden llegar, sin estar en el map, el constructor por
			// defecto, que no llama a
			// nadie, sólo a super, se puede omitir, y cualquier método no
			// declarado en el proyecto hashCode lenght println...., de los que
			// no podemos sacar esa información
			for (Relationship callRel : methodDecToCalls.get(methodDec)) {

			// Node invocationDec =
			// callRel.getEndNode().getSingleRelationship(CGRelationTypes.HAS_DEC,
			// Direction.OUTGOING).getEndNode();
			Iterable<Relationship> possibleDecs = callRel.getEndNode().getRelationships(Direction.OUTGOING, CGRelationTypes.REFER_TO, CGRelationTypes.MAY_REFER_TO);

			possibleDecs.forEach(possibleDec -> {
				if (!methodDecsAnalyzed.contains(possibleDec.getEndNode()))
					doInterproceduralPDGAnalysis(possibleDec.getEndNode());
			});
			// System.out.println("INVOCATION DEC:\n" +
			// NodeUtils.nodeToString(invocationDec));

			// Si pueden ser referidos por el mismo, misma signatura
			// Mapa argNumber, tipoRel=May/must->ocurrences
			Map<Pair<Integer, Boolean>, Integer> possibleRelsToOcurr = new HashMap<Pair<Integer, Boolean>, Integer>();
			int possibleDecsSize = 0;
			for (Relationship invocationReferringDec : possibleDecs) {
			for (Relationship invocationDecModifStateRel : invocationReferringDec.getEndNode().getRelationships(Direction.INCOMING, PDGRelationTypes.STATE_MODIFIED_BY, PDGRelationTypes.STATE_MAY_BE_MODIFIED)) {
			Pair<Integer, Boolean> pair = Pair.create(invocationDecModifStateRel.getStartNode().hasLabel(NodeTypes.THIS_REF) ? 0 : (Integer) invocationDecModifStateRel.getStartNode().getSingleRelationship(RelationTypes.HAS_METHODDECL_PARAMETERS, Direction.INCOMING).getProperty("paramIndex"), !invocationDecModifStateRel.getType().name().contains("MAY"));
			Integer val = possibleRelsToOcurr.get(pair);
			if (val == null)
			possibleRelsToOcurr.put(pair, val = 0);
			possibleRelsToOcurr.put(pair, val + 1);

			}
			possibleDecsSize++;
			}
			for (Entry<Pair<Integer, Boolean>, Integer> possibleRel : possibleRelsToOcurr.entrySet())
			// for (Relationship invocationDecModifStateRel :
			// invocationDec.getRelationships(Direction.INCOMING,
			// PDGRelationTypes.STATE_MODIFIED_BY,
			// PDGRelationTypes.STATE_MAY_BE_MODIFIED)) {

			createRelationsIfNeededForArgumentNumber(possibleRel.getKey().getFirst(), callRel, possibleRel.getValue() == possibleDecsSize ? possibleRel.getKey().getSecond() : false, methodDec);
			}
	}

	/*
	 * En lugar de no retornar nada, hay que retornar las relaciones... s-r->e
	 * Luego para todos los MAY_REFER_TO si hay una relación s-r->e para todas
	 * entonces el tipo es r si sólo hay en algunas s-r->e , el tipo es MAY_
	 */
	private void createRelationsIfNeededForArgumentNumber(int argNumber, Relationship callRel, boolean must,
			Node methodDec) {
		// System.out.println("VARS MAY BE MODIFIED BY :" +
		// NodeUtils.nodeToString(callRel.getEndNode()) + ":\n");
		if (invocationsMayModifyVars.get(callRel.getEndNode()) != null)
			for (Pair<Node, Boolean> varMayOrMustBeModified : invocationsMayModifyVars.get(callRel.getEndNode())
					.get(argNumber)) {
				// System.out.println("VAR FOUND:\n" +
				// NodeUtils.nodeToString(varMayOrMustBeModified.getFirst()));
				// System.out.println(varMayOrMustBeModified.getSecond() +
				// "\n");
				boolean isMay = !varMayOrMustBeModified.getSecond() || !must;
				varMayOrMustBeModified.getFirst().createRelationshipTo(callRel.getEndNode(),
						isMay ? PDGRelationTypes.STATE_MAY_BE_MODIFIED : PDGRelationTypes.STATE_MODIFIED_BY);
				if (varMayOrMustBeModified.getFirst().hasLabel(NodeTypes.THIS_REF)
						|| varMayOrMustBeModified.getFirst().hasLabel(NodeTypes.PARAMETER_DEC))
					varMayOrMustBeModified.getFirst().createRelationshipTo(methodDec,
							isMay || !(Boolean) callRel.getProperty("mustBeExecuted")
									? PDGRelationTypes.STATE_MAY_BE_MODIFIED : PDGRelationTypes.STATE_MODIFIED_BY);
			}
	}
}

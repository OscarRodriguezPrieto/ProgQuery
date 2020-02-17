package pdg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import database.nodes.NodeTypes;
import database.querys.cypherWrapper.EdgeDirection;
import database.relations.CGRelationTypes;
import database.relations.PDGRelationTypes;
import database.relations.RelationTypes;
import node_wrappers.NodeWrapper;
import node_wrappers.RelationshipWrapper;
import pdg.GetDeclarationFromExpression.IsInstance;
import utils.dataTransferClasses.MethodInfo;
import utils.dataTransferClasses.Pair;
import visitors.PDGProcessing;

public class InterproceduralPDG {

	private Map<NodeWrapper, Iterable<RelationshipWrapper>> methodDecToCalls;
	private Map<NodeWrapper, Map<Integer, List<PDGMutatedDecInfoInMethod>>> invocationsMayModifyVars = new HashMap<>();

	private Set<NodeWrapper> methodDecsAnalyzed = new HashSet<NodeWrapper>();
	private Map<NodeWrapper, MethodInfo> fromMethodDecNodeToInfo;

	private Map<NodeWrapper, Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes>> methodToanyDecToInvocationPDGrel = new HashMap<>();

	public InterproceduralPDG(Map<NodeWrapper, Iterable<RelationshipWrapper>> methodDecToCalls,
			Map<NodeWrapper, Map<Integer, List<PDGMutatedDecInfoInMethod>>> invocationsMayModifyVars,

			Map<NodeWrapper, MethodInfo> fromMethodDecNodeToInfo) {
		this.methodDecToCalls = methodDecToCalls;
		this.invocationsMayModifyVars = invocationsMayModifyVars;
		this.fromMethodDecNodeToInfo = fromMethodDecNodeToInfo;
	}

	public void doInterproceduralPDGAnalysis(MethodInfo methodInfo) {
		if (methodInfo == null || methodDecsAnalyzed.contains(methodInfo.methodNode))
			return;
		NodeWrapper methodDec = methodInfo.methodNode;
		methodDecsAnalyzed.add(methodDec);
		// Para analizar
		if (methodDecToCalls.containsKey(methodDec))
			// Aquí pueden llegar, sin estar en el map, el constructor por
			// defecto, que no llama a
			// nadie, sólo a super, se puede omitir, y cualquier método no
			// declarado en el proyecto hashCode lenght println...., de los que
			// no podemos sacar esa información

			for (RelationshipWrapper callRel : methodDecToCalls.get(methodDec)) {

				Iterable<RelationshipWrapper> possibleDecs = callRel.getEndNode().getRelationships(
						EdgeDirection.OUTGOING, CGRelationTypes.REFERS_TO, CGRelationTypes.MAY_REFER_TO);

				possibleDecs.forEach(possibleDec -> {
					if (!methodDecsAnalyzed.contains(possibleDec.getEndNode()))
						doInterproceduralPDGAnalysis(fromMethodDecNodeToInfo.get(possibleDec.getEndNode()));
				});

				// Si pueden ser referidos por el mismo, misma signatura
				// Mapa argNumber, tipoRel=May/must->ocurrences
				Map<Pair<Integer, Boolean>, Integer> possibleRelsToOcurr = new HashMap<Pair<Integer, Boolean>, Integer>();
				int possibleDecsSize = 0;
				for (RelationshipWrapper invocationReferringDec : possibleDecs) {
					// System.out.println("THERE IS A REL FOR " + desc);
					// System.out.println(NodeUtils.nodeToString(invocationReferringDec.getEndNode()));
					MethodInfo calledMethodInfo = fromMethodDecNodeToInfo.get(invocationReferringDec.getEndNode());
					if (calledMethodInfo != null)
						for (Entry<NodeWrapper, PDGRelationTypes> paramEntry : calledMethodInfo.paramsToPDGRelations
								.entrySet()
						
						) {

							// System.out.println("THERE IS STAT REL !!");
							Pair<Integer, Boolean> pair = Pair.create(
									paramEntry.getKey().hasLabel(NodeTypes.THIS_REF) ? 0
											: (Integer) paramEntry.getKey()
													.getSingleRelationship(EdgeDirection.INCOMING,
															RelationTypes.CALLABLE_HAS_PARAMETER)
													.getProperty("paramIndex"),

									// !(invocationReferringDec.getType().name().contains("MAY")
									// ||
									paramEntry.getValue() == PDGRelationTypes.STATE_MODIFIED_BY)
							// )
							;
							Integer val = possibleRelsToOcurr.get(pair);
							if (val == null)
								val = 0;
							possibleRelsToOcurr.put(pair, val + 1);
						}
					possibleDecsSize++;
				}
				for (Entry<Pair<Integer, Boolean>, Integer> possibleRel : possibleRelsToOcurr.entrySet())
					

					createRelationsIfNeededForArgumentNumber(possibleRel.getKey().getFirst(), callRel,
							possibleRel.getValue() == possibleDecsSize ? possibleRel.getKey().getSecond() : false,
							methodInfo);
			}
		createStoredDecToInvRels(methodInfo);
	}

	private void createStoredDecToInvRels(MethodInfo methodInfo) {
		// System.out.println("CREATING INV RELS FOR METHOD " +
		// methodInfo.methodNode.getProperty("name"));
		Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> decToInvPDGRelsInThisMethod = methodToanyDecToInvocationPDGrel
				.get(methodInfo.methodNode);
		if (decToInvPDGRelsInThisMethod != null)
			for (Entry<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> decToInvPDGRel : decToInvPDGRelsInThisMethod
					.entrySet()) {
				RelationshipWrapper rel = decToInvPDGRel.getKey().getFirst().createRelationshipTo(
						decToInvPDGRel.getKey().getSecond().getFirst(), decToInvPDGRel.getValue());
				// System.out.println("REL CREATED BETWEEN \n" +
				// NodeUtils.nodeToString(decToInvPDGRel.getKey().getFirst())
				// + "\n" +
				// NodeUtils.nodeToString(decToInvPDGRel.getKey().getSecond().getFirst())
				// + "\n"
				// + decToInvPDGRel.getValue());
				if (decToInvPDGRel.getKey().getFirst().hasLabel(NodeTypes.ATTR_DEF)) {
					rel.setProperty("isOwnAccess", decToInvPDGRel.getKey().getSecond().getSecond());
					if (decToInvPDGRel.getKey().getSecond().getSecond())
						methodInfo.addRelTodecToInstanceInvRels(rel);
				} else if (decToInvPDGRel.getKey().getFirst().hasLabel(NodeTypes.THIS_REF)) {
					methodInfo.addRelTodecToInstanceInvRels(rel);
					rel.setProperty("isOwnAccess", true);
				}
			}
	}

	/*
	 * En lugar de no retornar nada, hay que retornar las relaciones... s-r->e
	 * Luego para todos los MAY_REFER_TO si hay una relación s-r->e para todas
	 * entonces el tipo es r si sólo hay en algunas s-r->e , el tipo es MAY_
	 */
	private void createRelationsIfNeededForArgumentNumber(int argNumber, RelationshipWrapper callRel, boolean must,
			MethodInfo methodInfo) {
		// System.out.println("VARS MAY BE MODIFIED BY :" +
		// NodeUtils.nodeToString(callRel.getEndNode()) + ":\n");

		// System.out.println("REACHING CALL REL " + callRel.getType() + "REL
		// AND ARGNUMBER=" + argNumber + ":\nDEC "
		// + callRel.getStartNode().getProperty("name") + "\n" +
		// NodeUtils.nodeToString(callRel.getEndNode()));
		Map<NodeWrapper, PDGRelationTypes> paramRelsOnMethod = methodInfo.paramsToPDGRelations;
		Map<Integer, List<PDGMutatedDecInfoInMethod>> invocationModifyVarsInfo = invocationsMayModifyVars
				.get(callRel.getEndNode());
		if (invocationModifyVarsInfo != null) {
			// System.out.println("There is info!");
			List<PDGMutatedDecInfoInMethod> invocationModifyThisVarInfo = invocationModifyVarsInfo.get(argNumber);
			for (PDGMutatedDecInfoInMethod varMayOrMustBeModified : invocationModifyThisVarInfo) {

				boolean isMay = varMayOrMustBeModified.isMay || !must;
				// System.out.println("DEC:\n" +
				// NodeUtils.nodeToString(varMayOrMustBeModified.dec));
				addNewPDGRelFromAnyDecToInv(!isMay, varMayOrMustBeModified.dec, callRel.getEndNode(),
						varMayOrMustBeModified.dec.hasLabel(NodeTypes.THIS_REF)
								|| varMayOrMustBeModified.isOuterMostImplicitThisOrP != IsInstance.NO,
						methodInfo);
				// System.out.println("CREATING A STATE_MAY_MOD " + isMay + "
				// REL:\n"
				// + NodeUtils.nodeToString(varMayOrMustBeModified.getFirst()) +
				// "\n"
				// + NodeUtils.nodeToStrin
				Set<NodeWrapper> paramsSet = methodInfo.callsToParamsPreviouslyModified.get(callRel.getEndNode());
				if (varMayOrMustBeModified.dec.hasLabel(NodeTypes.PARAMETER_DEF)
						&& (paramsSet == null || !paramsSet.contains(varMayOrMustBeModified.dec)))

					addNewPDGRelFromParamToMethod(
							((paramsSet = methodInfo.callsToParamsMaybePreviouslyModified
									.get(callRel.getEndNode())) == null
									|| !paramsSet.contains(varMayOrMustBeModified.dec)) && !isMay
									&& (Boolean) callRel.getProperty("mustBeExecuted"),
							paramRelsOnMethod, varMayOrMustBeModified.dec);
				else if (varMayOrMustBeModified.dec.hasLabel(NodeTypes.ATTR_DEF)
						&& varMayOrMustBeModified.isOuterMostImplicitThisOrP != IsInstance.NO) {
					addNewPDGRelFromParamToMethod(
							!(isMay || IsInstance.MAYBE == varMayOrMustBeModified.isOuterMostImplicitThisOrP)
									&& (Boolean) callRel.getProperty("mustBeExecuted"),
							paramRelsOnMethod, methodInfo.thisNodeIfNotStatic);
					// methodInfo.thisNodeIfNotStatic.createRelationshipTo(callRel.getEndNode(),
					// isMay || IsOuterMostLeftImplicitThisOrParam.MAYBE ==
					// varMayOrMustBeModified.isOuterMostImplicitThisOrP
					// ? PDGRelationTypes.STATE_MAY_BE_MODIFIED :
					// PDGRelationTypes.STATE_MODIFIED_BY);
					// IMPLICIT THIS TO INVOCATION
					addNewPDGRelFromAnyDecToInv(!isMay, methodInfo.thisNodeIfNotStatic, callRel.getEndNode(), true,
							methodInfo);

				}

				// TODAVÍA NO
				// varMayOrMustBeModified.getFirst().createRelationshipTo(methodInfo.methodNodeWrapper,
				// isMay || !(Boolean) callRel.getProperty("mustBeExecuted")
				// ? PDGRelationTypes.STATE_MAY_BE_MODIFIED :
				// PDGRelationTypes.STATE_MODIFIED_BY);
			}
		}
	}

	private Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> getRelMapForMethod(
			MethodInfo methodInfo) {

		Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> relMapForMethod = methodToanyDecToInvocationPDGrel
				.get(methodInfo.methodNode);
		if (relMapForMethod == null)
			methodToanyDecToInvocationPDGrel.put(methodInfo.methodNode, relMapForMethod = new HashMap<>());
		return relMapForMethod;
	}

	private void addNewPDGRelFromAnyDecToInv(boolean isMust, NodeWrapper dec, NodeWrapper call, boolean isInstance,
			MethodInfo methodInfo) {
		// System.out
		// .println("ADDING NEW INVV REL \n" + NodeUtils.nodeToString(dec) +
		// "\n" + NodeUtils.nodeToString(call)
		// + " \n ISMUST " + isMust + "\t IS_INSTANCE " + isInstance);
		Pair<NodeWrapper, Pair<NodeWrapper, Boolean>> decToInvKey = Pair.create(dec, Pair.create(call, isInstance));
		Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> relMapForMethod = getRelMapForMethod(
				methodInfo);
		PDGProcessing.addNewPDGRelationFromParamToMethod(isMust, relMapForMethod.get(decToInvKey), (newRel) -> {
			// SI TIENE OTRA REL SE DEBE BORRAR, ÚNICO CASO
			// STATE_MAY_MOD y new STATE_MOD
			relMapForMethod.put(decToInvKey, newRel);
		});
	}

	private void addNewPDGRelFromParamToMethod(boolean isMust, Map<NodeWrapper, PDGRelationTypes> paramRelsOnMethod,
			NodeWrapper dec) {
		PDGProcessing.addNewPDGRelationFromParamToMethod(isMust, paramRelsOnMethod.get(dec), (newRel) -> {
			// SI TIENE OTRA REL SE DEBE BORRAR, ÚNICO CASO
			// STATE_MAY_MOD y new STATE_MOD
			paramRelsOnMethod.put(dec, newRel);
		});
	}

}

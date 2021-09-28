package es.uniovi.reflection.progquery.pdg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import es.uniovi.reflection.progquery.utils.dataTransferClasses.MethodInfo;
import org.neo4j.graphdb.Direction;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.database.relations.CGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.PDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.pdg.GetDeclarationFromExpression.IsInstance;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.visitors.PDGProcessing;

public class InterproceduralPDG {

	private Map<NodeWrapper, Iterable<RelationshipWrapper>> methodDecToCalls;
	private Map<NodeWrapper, Map<Integer, List<PDGMutatedDecInfoInMethod>>> invocationsMayModifyVars;

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

//		System.out.println(
//				"INTERPROCEDURAL FLOW TRAVERSING :\n" + methodInfo.methodNode.getProperty("fullyQualifiedName"));

		NodeWrapper methodDec = methodInfo.methodNode;
		methodDecsAnalyzed.add(methodDec);
		// SE RECUPERAN LOS CALLS DE CADA M�TODO
		if (methodDecToCalls.containsKey(methodDec))
			// Aqu� pueden llegar, sin estar en el map, el constructor por
			// defecto, que no llama a
			// nadie, s�lo a super, se puede omitir, y cualquier m�todo no
			// declarado en el proyecto hashCode lenght println...., de los que
			// no podemos sacar esa informaci�n

			for (RelationshipWrapper callRel : methodDecToCalls.get(methodDec)) {

				Iterable<RelationshipWrapper> possibleMethodDecsForCalls = callRel.getEndNode().getRelationships(
						Direction.OUTGOING, CGRelationTypes.REFERS_TO, CGRelationTypes.MAY_REFER_TO);

				possibleMethodDecsForCalls.forEach(possibleMethodDecForCall -> {
					if (!methodDecsAnalyzed.contains(possibleMethodDecForCall.getEndNode())) {
						doInterproceduralPDGAnalysis(
								fromMethodDecNodeToInfo.get(possibleMethodDecForCall.getEndNode()));
					}
				});

//				System.out.println(
//						"CONTINUING INTERPROCEDURAL FLOW TRAVERSING :\n" + methodInfo.methodNode.getProperty("fullyQualifiedName"));
				// Si pueden ser referidos por el mismo, misma signatura
				// Mapa argNumber, tipoRel=May/must->ocurrences
				Map<Pair<Integer, Boolean>, Integer> possibleRelsToOcurr = new HashMap<Pair<Integer, Boolean>, Integer>();
				int possibleDecsSize = 0;

				// ITERAMOS SOBRE LAS POSIBLES DEFINICIONES DE LOS M�TODOS LLAMADOS
				for (RelationshipWrapper invocationReferringDec : possibleMethodDecsForCalls) {
					MethodInfo calledMethodInfo = fromMethodDecNodeToInfo.get(invocationReferringDec.getEndNode());
//					if (calledMethodInfo != null)
//					{
//						System.out.println(	"calledMethodInfo.paramsToPDGRelations");
//						System.out.println(	calledMethodInfo.methodNode.getProperty("fullyQualifiedName"));
//
//						System.out.println(	calledMethodInfo.paramsToPDGRelations);
//					}


					if (calledMethodInfo != null)
						// ITERAMOS SOBRE CADA UNO DE LOS PAR�METROS (INCLUIDOS THIS -0-) cuyo estado se
						// modifica (o puede) durante la ejecuci�n del m�todo llamado
						for (Entry<NodeWrapper, PDGRelationTypes> paramMutatedInCalledMethodDec : calledMethodInfo.paramsToPDGRelations
								.entrySet()

						) {

							// System.out.println("THERE IS STAT REL !!");
					//		try {
								// TODO FIX SE NECESITA SABER SI TIENE SENTIDO O NO PASAR LAS
								// LAMBDA_EXPRESSIONES
								Pair<Integer, Boolean> mutatedParamIndexAndMust = Pair
										.create(paramMutatedInCalledMethodDec.getKey().hasLabel(NodeTypes.THIS_REF) ? 0
												: (Integer) paramMutatedInCalledMethodDec.getKey()
														.getRelationships(Direction.INCOMING,
																RelationTypes.CALLABLE_HAS_PARAMETER
																,RelationTypes.LAMBDA_EXPRESSION_PARAMETERS
														)
														.get(0)
														.getProperty("paramIndex"),
												paramMutatedInCalledMethodDec
														.getValue() == PDGRelationTypes.STATE_MODIFIED_BY);

								Integer val = possibleRelsToOcurr.get(mutatedParamIndexAndMust);
								if (val == null)
									val = 0;
								possibleRelsToOcurr.put(mutatedParamIndexAndMust, val + 1);
//							} catch (NullPointerException n) {
//								System.out.println(NodeUtils.nodeToString(paramMutatedInCalledMethodDec.getKey()));
//								throw n;
//							}
							// !(invocationReferringDec.getType().name().contains("MAY")
							// ||
							// )
						}
					possibleDecsSize++;
				}
//				System.out.println("POSSIBLE RELS FOR " + methodInfo.methodNode.getProperty("fullyQualifiedName"));
//				System.out.println(possibleRelsToOcurr);
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

//		System.out.println(
//				"CREATE STORE DEC TO INV RELS IN METHOD " + methodInfo.methodNode.getProperty("fullyQualifiedName"));
		if (decToInvPDGRelsInThisMethod != null)
			for (Entry<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> decToInvPDGRel : decToInvPDGRelsInThisMethod
					.entrySet()) {
//				System.out.println(
//						"DEC NODE TO LINK TO INVS " + NodeUtils.nodeToString(decToInvPDGRel.getKey().getFirst()));
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
	 * En lugar de no retornar nada, hay que retornar las relaciones... s-r->e Luego
	 * para todos los MAY_REFER_TO si hay una relaci�n s-r->e para todas entonces el
	 * tipo es r si s�lo hay en algunas s-r->e , el tipo es MAY_
	 */
	private void createRelationsIfNeededForArgumentNumber(int argNumber, RelationshipWrapper callRel, boolean must,
			MethodInfo methodInfo) {
//		System.out.println("PROCESSING INVOCATION for arg " + argNumber + " in line "
//				+ callRel.getEndNode().getProperty("lineNumber") + ", method "
//				+ methodInfo.methodNode.getProperty("fullyQualifiedName"));

		Map<NodeWrapper, PDGRelationTypes> paramRelsOnMethod = methodInfo.paramsToPDGRelations;
		// Saco la ifnormacion de las declaraciones que pueden mutar en la invocacion
		// (al ser referenciadas directa o indirectamente por los argumentos de la
		// invocaci�n : invocationModifyVarsInfo asocia el index de cada arg (el objeto
		// es el 0) con su PDGMutatedDecInfoInMethod (isMay, isInstance, decNode)
		Map<Integer, List<PDGMutatedDecInfoInMethod>> invocationModifyVarsInfo = invocationsMayModifyVars
				.get(callRel.getEndNode());
		if (invocationModifyVarsInfo != null) {
			// System.out.println("There is info!");
			List<PDGMutatedDecInfoInMethod> invocationModifyThisVarInfo = invocationModifyVarsInfo.get(argNumber);
//			System.out.println("VARS MUTATED IN THIS INV/ARG");
//			System.out.println(invocationModifyVarsInfo);
			for (PDGMutatedDecInfoInMethod varMayOrMustBeModified : invocationModifyThisVarInfo) {
				boolean isMay = varMayOrMustBeModified.isMay || !must;
				// System.out.println("DEC:\n" +
				// NodeUtils.nodeToString(varMayOrMustBeModified.dec));
//				if (varMayOrMustBeModified.dec == null)
//					System.out.println("DEC NULL IN PARAM");
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
				if (varMayOrMustBeModified.dec.hasLabel(NodeTypes.PARAMETER_DEF) && varMayOrMustBeModified.dec.getRelationships(Direction.INCOMING, RelationTypes.CALLABLE_HAS_PARAMETER,RelationTypes.LAMBDA_EXPRESSION_PARAMETERS).get(0).getStartNode()==methodInfo.methodNode
						&& (paramsSet == null || !paramsSet.contains(varMayOrMustBeModified.dec)))

					addNewPDGRelFromParamToMethod(
							((paramsSet = methodInfo.callsToParamsMaybePreviouslyModified
									.get(callRel.getEndNode())) == null
									|| !paramsSet.contains(varMayOrMustBeModified.dec)) && !isMay
									&& (Boolean) callRel.getProperty("mustBeExecuted"),
							paramRelsOnMethod, varMayOrMustBeModified.dec);
				else if (varMayOrMustBeModified.dec.hasLabel(NodeTypes.ATTR_DEF)
						&& varMayOrMustBeModified.isOuterMostImplicitThisOrP != IsInstance.NO
						&& methodInfo.thisNodeIfNotStatic != null) {
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

				// TODAV�A NO
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
//		if (dec == null) {
//			System.out.println("FOUND NULL DEC IN METHOD " + methodInfo.methodNode.getProperty("fullyQualifiedName")
//					+ "  LINKED TO CALL:");
//			System.out.println(NodeUtils.nodeToString(call));
////			throw new IllegalStateException();
//
//		}
		Pair<NodeWrapper, Pair<NodeWrapper, Boolean>> decToInvKey = Pair.create(dec, Pair.create(call, isInstance));

		Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> relMapForMethod = getRelMapForMethod(
				methodInfo);

		PDGProcessing.addNewPDGRelationFromParamToMethod(isMust, relMapForMethod.get(decToInvKey), (newRel) -> {
			// SI TIENE OTRA REL SE DEBE BORRAR, �NICO CASO
			// STATE_MAY_MOD y new STATE_MOD
			relMapForMethod.put(decToInvKey, newRel);
		});
	}

	private void addNewPDGRelFromParamToMethod(boolean isMust, Map<NodeWrapper, PDGRelationTypes> paramRelsOnMethod,
			NodeWrapper dec) {
		PDGProcessing.addNewPDGRelationFromParamToMethod(isMust, paramRelsOnMethod.get(dec), (newRel) -> {
			// SI TIENE OTRA REL SE DEBE BORRAR, �NICO CASO
			// STATE_MAY_MOD y new STATE_MOD
			paramRelsOnMethod.put(dec, newRel);
		});
	}

}

package es.uniovi.reflection.progquery.pdg;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.CGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.PDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;
import es.uniovi.reflection.progquery.pdg.GetDeclarationFromExpression.IsInstance;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.MethodInfo;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import es.uniovi.reflection.progquery.visitors.PDGProcessing;
import org.neo4j.graphdb.Direction;

import java.util.*;
import java.util.Map.Entry;

public class InterproceduralPDG {

    private Map<NodeWrapper, Iterable<RelationshipWrapper>> methodDecToCalls;
    private Map<NodeWrapper, Map<Integer, List<PDGMutatedDecInfoInMethod>>> invocationsMayModifyVars;

    private Set<NodeWrapper> methodDecsAnalyzed = new HashSet<NodeWrapper>();
    private Map<NodeWrapper, MethodInfo> fromMethodDecNodeToInfo;

    private Map<NodeWrapper, Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes>>
            methodToanyDecToInvocationPDGrel = new HashMap<>();

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
        //				"INTERPROCEDURAL FLOW TRAVERSING :\n" + methodInfo.methodNode.getProperty
        //				("fullyQualifiedName"));

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

                Iterable<RelationshipWrapper> possibleMethodDecsForCalls = callRel.getEndNode()
                        .getRelationships(Direction.OUTGOING, CGRelationTypes.REFERS_TO, CGRelationTypes.MAY_REFER_TO);

                possibleMethodDecsForCalls.forEach(possibleMethodDecForCall -> {
                    if (!methodDecsAnalyzed.contains(possibleMethodDecForCall.getEndNode())) {
                        doInterproceduralPDGAnalysis(
                                fromMethodDecNodeToInfo.get(possibleMethodDecForCall.getEndNode()));
                    }
                });

                //				System.out.println(
                //						"CONTINUING INTERPROCEDURAL FLOW TRAVERSING :\n" + methodInfo.methodNode
                //						.getProperty("fullyQualifiedName"));
                // Si pueden ser referidos por el mismo, misma signatura
                // Mapa argNumber, tipoRel=May/must->ocurrences
                Map<MutatedParamInCallInfo, Integer> possibleRelsToOcurr =
                        new HashMap<MutatedParamInCallInfo, Integer>();
                int possibleDecsSize = 0;

                // ITERAMOS SOBRE LAS POSIBLES DEFINICIONES DE LOS M�TODOS LLAMADOS
                for (RelationshipWrapper invocationReferringDec : possibleMethodDecsForCalls) {
                    MethodInfo calledMethodInfo = fromMethodDecNodeToInfo.get(invocationReferringDec.getEndNode());


                    if (calledMethodInfo != null)
                        // ITERAMOS SOBRE CADA UNO DE LOS PAR�METROS (INCLUIDOS THIS -0-) cuyo estado se
                        // modifica (o puede) durante la ejecuci�n del m�todo llamado
                        for (Entry<NodeWrapper, PDGRelationTypes> paramMutatedInCalledMethodDec :
                                calledMethodInfo.paramsToPDGRelations
                                .entrySet()

                        ) {

                            // System.out.println("THERE IS STAT REL !!");
                            //		try {
                            // TODO FIX SE NECESITA SABER SI TIENE SENTIDO O NO PASAR LAS
                            // LAMBDA_EXPRESSIONES
                            int paramIndex = paramMutatedInCalledMethodDec.getKey().hasLabel(NodeTypes.THIS_REF) ? 0 :
                                    (Integer) paramMutatedInCalledMethodDec.getKey()
                                            .getRelationships(Direction.INCOMING, RelationTypes.CALLABLE_HAS_PARAMETER,
                                                    RelationTypes.LAMBDA_EXPRESSION_PARAMETERS).get(0)
                                            .getProperty("paramIndex");
                            MutatedParamInCallInfo mutatedParamIndexAndMust = new MutatedParamInCallInfo(paramIndex,
                                    paramMutatedInCalledMethodDec.getValue() == PDGRelationTypes.STATE_MODIFIED_BY,
                                    calledMethodInfo.varArgParamIndex == paramIndex);

                            Integer val = possibleRelsToOcurr.get(mutatedParamIndexAndMust);
                            if (val == null)
                                val = 0;
                            possibleRelsToOcurr.put(mutatedParamIndexAndMust, val + 1);
                            //							} catch (NullPointerException n) {
                            //								System.out.println(NodeUtils.nodeToString
                            //								(paramMutatedInCalledMethodDec.getKey()));
                            //								throw n;
                            //							}
                            // !(invocationReferringDec.getType().name().contains("MAY")
                            // ||
                            // )
                        }
                    possibleDecsSize++;
                }
                //				System.out.println("POSSIBLE RELS FOR " + methodInfo.methodNode.getProperty
                //				("fullyQualifiedName"));
                //				System.out.println(possibleRelsToOcurr);
                for (Entry<MutatedParamInCallInfo, Integer> possibleRel : possibleRelsToOcurr.entrySet())

                    createRelationsIfNeededForArgumentNumber(possibleRel.getKey().getArgNumber(), callRel,
                            possibleRel.getValue() == possibleDecsSize ? possibleRel.getKey().isMay() : false,
                            methodInfo, possibleRel.getKey().isVargArgs());
            }
        createStoredDecToInvRels(methodInfo);
    }

    private void createStoredDecToInvRels(MethodInfo methodInfo) {
        // System.out.println("CREATING INV RELS FOR METHOD " +
        // methodInfo.methodNode.getProperty("name"));
        Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> decToInvPDGRelsInThisMethod =
                methodToanyDecToInvocationPDGrel.get(methodInfo.methodNode);

        //		System.out.println(
        //				"CREATE STORE DEC TO INV RELS IN METHOD " + methodInfo.methodNode.getProperty
        //				("fullyQualifiedName"));
        if (decToInvPDGRelsInThisMethod != null)
            for (Entry<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> decToInvPDGRel :
                    decToInvPDGRelsInThisMethod
                    .entrySet()) {
                //				System.out.println(
                //						"DEC NODE TO LINK TO INVS " + NodeUtils.nodeToString(decToInvPDGRel.getKey()
                //						.getFirst()));
                RelationshipWrapper rel = decToInvPDGRel.getKey().getFirst()
                        .createRelationshipTo(decToInvPDGRel.getKey().getSecond().getFirst(),
                                decToInvPDGRel.getValue());
                // System.out.println("REL CREATED BETWEEN \n" +
                // NodeUtils.nodeToString(decToInvPDGRel.getKey().getFirst())
                // + "\n" +
                // NodeUtils.nodeToString(decToInvPDGRel.getKey().getSecond().getFirst())
                // + "\n"
                // + decToInvPDGRel.getValue());
                if (decToInvPDGRel.getKey().getFirst().hasLabel(NodeTypes.ATTR_DEF)) {
                    rel.setProperty("isOwnAccess", decToInvPDGRel.getKey().getSecond().getSecond());
                } else if (decToInvPDGRel.getKey().getFirst().hasLabel(NodeTypes.THIS_REF))
                    rel.setProperty("isOwnAccess", true);

            }
    }

    /*
     * En lugar de no retornar nada, hay que retornar las relaciones... s-r->e Luego
     * para todos los MAY_REFER_TO si hay una relaci�n s-r->e para todas entonces el
     * tipo es r si s�lo hay en algunas s-r->e , el tipo es MAY_
     */
    private void createRelationsIfNeededForArgumentNumber(int argNumber, RelationshipWrapper callRel, boolean must,
                                                          MethodInfo methodInfo, boolean isVargArgsParam) {
        //System.out.println("PROCESSING INVOCATION for arg " + argNumber + " in line "
        //		+ callRel.getEndNode().getProperty("lineNumber") + ", method "
        //		+ methodInfo.methodNode.getProperty("fullyQualifiedName"));


        // Saco la ifnormacion de las declaraciones que pueden mutar en la invocacion
        // (al ser referenciadas directa o indirectamente por los argumentos de la
        // invocaci�n : invocationModifyVarsInfo asocia el index de cada arg (el objeto
        // es el 0) con su PDGMutatedDecInfoInMethod (isMay, isInstance, decNode)
        Map<Integer, List<PDGMutatedDecInfoInMethod>> invocationModifyVarsInfo =
                invocationsMayModifyVars.get(callRel.getEndNode());
        if (invocationModifyVarsInfo != null) {
            // System.out.println("There is info!");
            List<PDGMutatedDecInfoInMethod> invocationModifyThisVarInfo = invocationModifyVarsInfo.get(argNumber);
            if (isVargArgsParam) {
                if (invocationModifyThisVarInfo == null)
                    return;
                else {
                    for (int anyArgIndex : invocationModifyVarsInfo.keySet())
                        if (anyArgIndex >= argNumber)
                            addPDGRelsForVarsInArg(methodInfo, invocationModifyVarsInfo.get(argNumber), callRel, false);
                }
            } else {
                addPDGRelsForVarsInArg(methodInfo, invocationModifyThisVarInfo, callRel, must);
            }

            //	System.out.println("VARS MUTATED IN THIS INV/ARG");
            //	System.out.println(invocationModifyVarsInfo);

        }
    }

    private void addPDGRelsForVarsInArg(MethodInfo methodInfo,
                                        List<PDGMutatedDecInfoInMethod> invocationModifyThisVarInfo,
                                        RelationshipWrapper callRel, boolean must) {
        for (PDGMutatedDecInfoInMethod varMayOrMustBeModified : invocationModifyThisVarInfo) {
            boolean isMay = varMayOrMustBeModified.isMay || !must;
            addNewPDGRelFromAnyDecToInv(!isMay, varMayOrMustBeModified.dec, callRel.getEndNode(),
                    varMayOrMustBeModified.dec.hasLabel(NodeTypes.THIS_REF) ||
                            varMayOrMustBeModified.isOuterMostImplicitThisOrP != IsInstance.NO, methodInfo);

            Map<NodeWrapper, PDGRelationTypes> paramRelsOnMethod = methodInfo.paramsToPDGRelations;
            Set<NodeWrapper> paramsSet = methodInfo.callsToParamsPreviouslyModified.get(callRel.getEndNode());
            if (varMayOrMustBeModified.dec.hasLabel(NodeTypes.PARAMETER_DEF) && varMayOrMustBeModified.dec
                    .getRelationships(Direction.INCOMING, RelationTypes.CALLABLE_HAS_PARAMETER,
                            RelationTypes.LAMBDA_EXPRESSION_PARAMETERS).get(0).getStartNode() ==
                    methodInfo.methodNode && (paramsSet == null || !paramsSet.contains(varMayOrMustBeModified.dec)))

                addNewPDGRelFromParamToMethod(
                        ((paramsSet = methodInfo.callsToParamsMaybePreviouslyModified.get(callRel.getEndNode())) ==
                                null || !paramsSet.contains(varMayOrMustBeModified.dec)) && !isMay &&
                                (Boolean) callRel.getProperty("mustBeExecuted"), paramRelsOnMethod,
                        varMayOrMustBeModified.dec);
            else if (varMayOrMustBeModified.dec.hasLabel(NodeTypes.ATTR_DEF) &&
                    varMayOrMustBeModified.isOuterMostImplicitThisOrP != IsInstance.NO &&
                    methodInfo.thisNodeIfNotStatic != null) {
                addNewPDGRelFromParamToMethod(
                        !(isMay || IsInstance.MAYBE == varMayOrMustBeModified.isOuterMostImplicitThisOrP) &&
                                (Boolean) callRel.getProperty("mustBeExecuted"), paramRelsOnMethod,
                        methodInfo.thisNodeIfNotStatic);

                addNewPDGRelFromAnyDecToInv(!isMay, methodInfo.thisNodeIfNotStatic, callRel.getEndNode(), true,
                        methodInfo);
            } else if (varMayOrMustBeModified.dec.hasLabel(NodeTypes.THIS_REF))
                addNewPDGRelFromParamToMethod(!isMay && (Boolean) callRel.getProperty("mustBeExecuted"),
                        paramRelsOnMethod, methodInfo.thisNodeIfNotStatic);
            // IMPLICIT THIS TO INVOCATION

        }
    }


    private Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> getRelMapForMethod(
            MethodInfo methodInfo) {

        Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> relMapForMethod =
                methodToanyDecToInvocationPDGrel.get(methodInfo.methodNode);
        if (relMapForMethod == null)
            methodToanyDecToInvocationPDGrel.put(methodInfo.methodNode, relMapForMethod = new HashMap<>());
        return relMapForMethod;
    }

    private void addNewPDGRelFromAnyDecToInv(boolean isMust, NodeWrapper dec, NodeWrapper call, boolean isInstance,
                                             MethodInfo methodInfo) {

        Pair<NodeWrapper, Pair<NodeWrapper, Boolean>> decToInvKey = Pair.create(dec, Pair.create(call, isInstance))
                //				, decToMethodKey = Pair.create(dec, Pair.create(methodInfo.methodNode, isInstance))
                ;

        Map<Pair<NodeWrapper, Pair<NodeWrapper, Boolean>>, PDGRelationTypes> relMapForMethod =
                getRelMapForMethod(methodInfo);

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

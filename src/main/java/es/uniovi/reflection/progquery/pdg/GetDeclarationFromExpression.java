package es.uniovi.reflection.progquery.pdg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.uniovi.reflection.progquery.database.nodes.NodeUtils;
import es.uniovi.reflection.progquery.database.relations.CGRelationTypes;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.MethodInfo;
import es.uniovi.reflection.progquery.utils.dataTransferClasses.Pair;
import org.neo4j.graphdb.Direction;

import es.uniovi.reflection.progquery.database.nodes.NodeTypes;
import es.uniovi.reflection.progquery.database.relations.PDGRelationTypes;
import es.uniovi.reflection.progquery.database.relations.RelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;
import es.uniovi.reflection.progquery.node_wrappers.RelationshipWrapper;

public class GetDeclarationFromExpression {
	static enum IsInstance {
		YES, MAYBE, NO

	}

	// PODEMOS JUBILAR ESTO, PORQUE THIS SE PUEDE PASAR COMO APRAMETRODEL
	// ANALISIS SEGUN EL METODO; ESTA ALMACENADO EN EL METODO
	private NodeWrapper currentThisRef;
	private Map<NodeWrapper, NodeWrapper> identificationForLeftAssignIdents;

	// Map<MethodInvocation,Map<Integer, Vardec X IsInstance(Boolean)>
	private Map<NodeWrapper, Map<Integer, List<PDGMutatedDecInfoInMethod>>> invocationsMayModifyVars = new HashMap<>();

	public Map<NodeWrapper, Map<Integer, List<PDGMutatedDecInfoInMethod>>> getInvocationsMayModifyVars() {
		return invocationsMayModifyVars;
	}

	public void setInfoForMethod(MethodInfo methodInfo) {
		this.identificationForLeftAssignIdents = methodInfo.identificationForLeftAssignExprs;
		this.currentThisRef = methodInfo.thisNodeIfNotStatic;

	}

	public GetDeclarationFromExpression() {
	}

	private Pair<List<PDGMutatedDecInfoInMethod>, Boolean> scan(NodeWrapper n) {

		return n.hasLabel(NodeTypes.IDENTIFIER) ? scanIdentifier(n)
				: n.hasLabel(NodeTypes.MEMBER_SELECTION) ? scanMemberSel(n)
						: n.hasLabel(NodeTypes.METHOD_INVOCATION) ? scanMethodInvocation(n)
								: n.hasLabel(NodeTypes.ASSIGNMENT) ? scanAPart(n, RelationTypes.ASSIGNMENT_LHS)
										: n.hasLabel(NodeTypes.ARRAY_ACCESS)
												? scanAPart(n, RelationTypes.ARRAYACCESS_EXPR)
												: n.hasLabel(NodeTypes.TYPE_CAST)
														? scanAPart(n, RelationTypes.CAST_ENCLOSES)
														: n.hasLabel(NodeTypes.CONDITIONAL_EXPRESSION)
																? scanConditionalExpression(n)
																: unknownScan(n);

	}

	private Pair<List<PDGMutatedDecInfoInMethod>, Boolean> unknownScan(NodeWrapper n) {
		// System.out.println("LABEL OF UNKNOWN SCAN\t" +
		// n.getLabels().iterator().next() + "");
		// NEW CLASS FOR EXAMPLE new A().a=2; (a=new A()).a=2
		// BINARY OPERATION AND LITERAL
		return Pair.create(new ArrayList<>(), true);
	}

	private NodeWrapper getDecFromExp(NodeWrapper identOrMemberSel
	// , boolean isIdent
	) {
		// System.out.println("IDENT_OR_MEMBERSEL :\n" +
		// NodeUtils.nodeToString(identOrMemberSel));
		NodeWrapper decNode = identificationForLeftAssignIdents.get(identOrMemberSel);
		if (decNode == null)
			if (identOrMemberSel.hasRelationship(PDGRelationTypes.USED_BY, Direction.INCOMING))
				// System.out.println("NOT PREV IDENT");
				// System.out.println(
				// identOrMemberSel.hasRelationship(PDGRelationTypes.USED_BY,
				// Direction.INCOMING));
				// if
				// (identOrMemberSel.hasRelationship(PDGRelationTypes.USED_BY,
				// Direction.INCOMING))
				// System.out.println("DEC:\n" + NodeUtils.nodeToString(
				// identOrMemberSel.getSingleRelationship(PDGRelationTypes.USED_BY,
				// Direction.INCOMING)
				// .getStartNode()));
				return identOrMemberSel.getSingleRelationship(Direction.INCOMING, PDGRelationTypes.USED_BY)
						.getStartNode();
			else {
				// System.out.println("INV CANDIDATE");
				RelationshipWrapper methodInvocationRelIfExists = identOrMemberSel
						.getSingleRelationship(Direction.INCOMING, RelationTypes.METHODINVOCATION_METHOD_SELECT);
				if (methodInvocationRelIfExists == null || identOrMemberSel.hasLabel(NodeTypes.MEMBER_SELECTION)) {

					// SI es el nombre de una clase (tipicamente campo estatico
					// de clase) retornamos null
					return null;
				}

				return currentThisRef;
				// If thisRef is null, then it must be a static method

			}

		return decNode;

	}

	private Pair<List<PDGMutatedDecInfoInMethod>, Boolean> scanMemberSel(NodeWrapper memberSel) {
		// LA DECLARACI�N QUE PRIMERO SE A�ADE ES LA DEL OUTER MOST LEFT , AS�
		// QUE decToTheLEft.get(0) nos lo va a dar
		// System.out.println(NodeUtils.nodeToString(memberSel));
		Pair<List<PDGMutatedDecInfoInMethod>, Boolean> defsToTheLeft = scanAPart(memberSel,
				RelationTypes.MEMBER_SELECT_EXPR);
		NodeWrapper dec = getDecFromExp(memberSel);
		if (dec != null)
			defsToTheLeft.getFirst()
					.add(new PDGMutatedDecInfoInMethod(defsToTheLeft.getSecond(),
							defsToTheLeft.getFirst().size() > 0
									? defsToTheLeft.getFirst().get(0).isOuterMostImplicitThisOrP
									: IsInstance.NO
							// SI No hay, entonces es que se encontr� una clase (static
							// member access), luego no es de instancia
							, dec));

		// MISMO ISMAY, IS_INSTANCE QUE SU HIJO, PERO HAY QUE A�ADIRLE LA
		// DECLARACI�N A LA LISTA
		return defsToTheLeft;
	}

	public Pair<List<PDGMutatedDecInfoInMethod>, Boolean> scanIdentifier(NodeWrapper identifier) {

		// RETORNAR LA DEC, Y PENSAR LO DE LA INFO DE ISISTANCE PARA CUALQUIER
		// IDENT O MEMBER

		// If there is a declaration stored (in the map or in the rels) ret this
		// Param, Local or this dec
		// QUEDA PENDIENTE LOS THIS SUPER COMO LLAMADAS A CONSTRUCTORES... a
		// saber...
		// System.out.println("SCANNING IDENT \n" +
		// NodeUtils.nodeToString(identifier));
		NodeWrapper dec = getDecFromExp(identifier);
		// System.out.println(dec);
//		if(identifier.getProperty("name").toString().contentEquals("super"))
//		{
//			System.out.println("SUPER DEC");
//			System.out.println(dec);
//		}if(identifier.getProperty("name").toString().contentEquals("this"))
//		{
//			System.out.println("THIS DEC");
//			System.out.println(dec);
//		}

		if (dec != null) {
			List<PDGMutatedDecInfoInMethod> identInfo = new ArrayList<>();
			// System.out.println("FOUND DEC\n" + NodeUtils.nodeToString(dec));

			identInfo.add(new PDGMutatedDecInfoInMethod(false,
					dec.hasLabel(NodeTypes.ATTR_DEF) && !(Boolean) dec.getProperty("isStatic") ||
					// instance method
							dec.hasLabel(NodeTypes.THIS_REF)

									? IsInstance.YES
									: IsInstance.NO,
					dec));
			// System.out.println("RETURNING " +
			// identInfo.get(0).isOuterMostImplicitThisOrP);
			return Pair.create(identInfo, false);
		} else
			return Pair.create(new ArrayList<>(), false);
	}

	private IsInstance getCompoundIsInstance(IsInstance i1, IsInstance i2) {
		if (i1 == i2)
			return i1;
		// NO Y SI S�LO OCURREN AS�
		return IsInstance.MAYBE;

	}

	public Pair<List<PDGMutatedDecInfoInMethod>, Boolean> scanConditionalExpression(NodeWrapper conditionalExpr) {
		List<PDGMutatedDecInfoInMethod> ret = new ArrayList<>();
		Pair<List<PDGMutatedDecInfoInMethod>, Boolean> retThen = scan(conditionalExpr
				.getSingleRelationship(Direction.OUTGOING, RelationTypes.CONDITIONAL_EXPR_THEN).getEndNode()),
				retElse = scan(conditionalExpr
						.getSingleRelationship(Direction.OUTGOING, RelationTypes.CONDITIONAL_EXPR_ELSE).getEndNode());
		ret.addAll(convertMustToMay(retElse));
		ret.addAll(convertMustToMay(retThen));

		// EL RETURN ES NO, pero hay que alterar todas las anteriores, se hace
		// arriba
		return Pair.create(ret, false);
	}

	public Pair<List<PDGMutatedDecInfoInMethod>, Boolean> scanAPart(NodeWrapper memberSelection, RelationTypes r) {
		// System.out.println("NODE:\n" +
		// NodeUtils.nodeToString(memberSelection));
		// System.out.println(r);
		return scan(memberSelection.getSingleRelationship(Direction.OUTGOING, r).getEndNode());
	}

	private List<PDGMutatedDecInfoInMethod> convertMustToMay(Pair<List<PDGMutatedDecInfoInMethod>, Boolean> previous) {
		return previous.getFirst().stream()
				.map(previousPdgInfo -> new PDGMutatedDecInfoInMethod(true,
						/*
						 * previousPdgInfo.isOuterMostImplicitThisOrP ==
						 * IsOuterMostLeftImplicitThisOrParam.YES ?
						 * IsOuterMostLeftImplicitThisOrParam.MAYBE :
						 */previousPdgInfo.isOuterMostImplicitThisOrP, previousPdgInfo.dec))
				.collect(Collectors.toList());
	}
	/*
	 * private Pair<List<PDGMutatedDecInfoInMethod>, IsInstanceExpression>
	 * getDecsForAReturn(Relationship possibleRet, Map<Integer,
	 * Pair<Pair<List<Node>, IsInstanceExpression>, Boolean>> varDecsInArguments,
	 * boolean singleReturn) { Pair<List<PDGMutatedDecInfoInMethod>,
	 * IsInstanceExpression> ret; if
	 * (possibleRet.getEndNode().getProperty("nodeType").toString().
	 * contentEquals("THIS_REF")) { ret = varDecsInArguments.get(0); return
	 * singleReturn ? ret : convertMustToMay(ret); } else { // Parameter or part of
	 * parameter ret = varDecsInArguments.get((int)
	 * possibleRet.getProperty("paramIndex")); return singleReturn ? ret :
	 * convertMustToMay(ret); } }
	 */

	public Pair<List<PDGMutatedDecInfoInMethod>, Boolean> scanMethodInvocation(NodeWrapper methodInvocation) {

		Map<Integer, List<PDGMutatedDecInfoInMethod>> varDecsInArguments = new HashMap<>();
		Pair<List<PDGMutatedDecInfoInMethod>, Boolean> thisArgRet;
		NodeWrapper calleeMethodNode = methodInvocation.getSingleRelationship( Direction.OUTGOING, CGRelationTypes.HAS_DEF).getEndNode();

//		if(callee.getProperty("isStatic")== null)
//			System.out.println(methodInvocation.getSingleRelationship( Direction.OUTGOING, CGRelationTypes.HAS_DEF).getEndNode());

			//CONSTRUCTOR CALLS LIKE THIS() SUPER(), OR NON STATIC CALLS REQUIRE LEFT PROCESSING (ARG 0) STATIC CALLS DOES NOT AND HAVE AN EMPTY LIST INSTED
			thisArgRet		 = calleeMethodNode.hasLabel(NodeTypes.CONSTRUCTOR_DEF) || !(Boolean)calleeMethodNode.getProperty("isStatic")?
					scan(methodInvocation
					.getSingleRelationship(Direction.OUTGOING, RelationTypes.METHODINVOCATION_METHOD_SELECT).getEndNode())
					:
					Pair.create(new ArrayList<>(),false);
			varDecsInArguments.put(0, thisArgRet.getFirst());


		for (RelationshipWrapper argumentRel : methodInvocation.getRelationships(Direction.OUTGOING,
				RelationTypes.METHODINVOCATION_ARGUMENTS))
			// System.out.println("Argument " + (int)
			// argumentRel.getProperty("argumentIndex") + ":\n"
			// + NodeUtils.nodeToString(argumentRel.getEndNode()));
			varDecsInArguments.put((int) argumentRel.getProperty("argumentIndex"),
					scan(argumentRel.getEndNode()).getFirst());

		// Aqu� faltan cosas pa sacar la declaracion
		/*
		 * NodeWrapper methodDeclaration =
		 * methodInvocation.getSingleRelationship(CGRelationTypes.HAS_DEC,
		 * Direction.OUTGOING) .getEndNode(); List<Pair<NodeWrapper, Boolean>>
		 * decsOfTheInvocation = new ArrayList<Pair<NodeWrapper, Boolean>>(); for
		 * (Relationship rel : methodDeclaration.getRelationships(Direction.OUTGOING,
		 * PDGRelationTypes.RETURNS, PDGRelationTypes.RETURNS_A_PART_OF)) { return
		 * getDecsForAReturn(rel, varDecsInArguments, true); } for (Relationship rel :
		 * methodDeclaration.getRelationships(Direction.OUTGOING,
		 * PDGRelationTypes.MAY_RETURN, PDGRelationTypes.MAY_RETURN_A_PART_OF))
		 * decsOfTheInvocation.addAll(getDecsForAReturn(rel, varDecsInArguments,
		 * false));
		 */
		// System.out.println("SCAN:\nMETHOD_INV:\n" +
		// NodeUtils.nodeToString(methodInvocation));
		// varDecsInArguments.forEach((e, v) -> {
		// System.out.println("INDEX " + e);
		// v.forEach(p -> System.out.println(p.getSecond() + "\n" +
		// NodeUtils.nodeToString(p.getFirst())));
		// });
		invocationsMayModifyVars.put(methodInvocation, varDecsInArguments);

		// Falta recorrer a la derecha y a�adir es.uniovi.reflection.progquery.cache porque vamos a visitar
		// todos los calls
		return Pair.create(new ArrayList<>(), thisArgRet.getSecond());
	}

	public List<Pair<NodeWrapper, Boolean>> scanNewClass(NodeWrapper newClass) {

		Map<Integer, List<PDGMutatedDecInfoInMethod>> varDecsInArguments = new HashMap<>();
		//We introduce always de ARG 0 never affecting the this object on the caller so empty list
		varDecsInArguments.put(0,new ArrayList<>());

		for (RelationshipWrapper argumentRel : newClass.getRelationships(Direction.OUTGOING,
				RelationTypes.NEW_CLASS_ARGUMENTS))
			// System.out.println("Argument " + (int)
			// argumentRel.getProperty("argumentIndex") + ":\n"
			// + NodeUtils.nodeToString(argumentRel.getEndNode()));
			varDecsInArguments.put((int) argumentRel.getProperty("argumentIndex"),
					scan(argumentRel.getEndNode()).getFirst());

		invocationsMayModifyVars.put(newClass, varDecsInArguments);
		return new ArrayList<Pair<NodeWrapper, Boolean>>();
	}
}

package mig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import database.relations.CGRelationTypes;
import database.relations.RelationTypes;

public class DynamicMethodCallAnalysis {

	private Map<Node, Map<String, Set<Node>>> typeToGroupedMethods = new HashMap<Node, Map<String, Set<Node>>>();
	private Set<Node> trustableInv;

	public DynamicMethodCallAnalysis(Set<Node> superCalls) {
		this.trustableInv = superCalls;
	}

	public Map<String, Set<Node>> dynamicMethodCallAnalysis(Node typeDec) {
		Map<String, Set<Node>> descendentsGroupedMethods = typeToGroupedMethods.get(typeDec);
		if (descendentsGroupedMethods != null)
			return descendentsGroupedMethods;

		descendentsGroupedMethods = new HashMap<String, Set<Node>>();
		for (Relationship subTypeRel : typeDec.getRelationships(Direction.INCOMING, RelationTypes.IS_SUBTYPE_EXTENDS,
				RelationTypes.IS_SUBTYPE_IMPLEMENTS)) {
			Node subType = subTypeRel.getStartNode();
			Map<String, Set<Node>> subtypeGroupedMethods = dynamicMethodCallAnalysis(subType);
			for (Entry<String, Set<Node>> entry : subtypeGroupedMethods.entrySet()) {
				Set<Node> groupedMethods = descendentsGroupedMethods.get(entry.getKey());
				if (groupedMethods == null)
					descendentsGroupedMethods.put(entry.getKey(), groupedMethods = new HashSet<Node>());
				groupedMethods.addAll(entry.getValue());
			}

		}
		for (Relationship declaredMethodRel : typeDec.getRelationships(RelationTypes.DECLARES_METHOD,
				Direction.OUTGOING)) {

			Node declaredMethod = declaredMethodRel.getEndNode();
			if ((boolean) declaredMethod.getProperty("isStatic"))
				continue;
			String typedName = declaredMethod.getProperty("fullyQualifiedName").toString().split(":")[1]
					.split("\\)")[0];

			Set<Node> overrideMethods = descendentsGroupedMethods.get(typedName);
			boolean isAbstract = (boolean) declaredMethod.getProperty("isAbstract");

			Iterable<Relationship> invocationRels = declaredMethod.getRelationships(CGRelationTypes.REFER_TO,
					Direction.INCOMING);
			if (overrideMethods == null)
				descendentsGroupedMethods.put(typedName, overrideMethods = new HashSet<Node>());

			else {
				boolean mayRefer = !isAbstract || overrideMethods.size() > 1;
				final int s = overrideMethods.size();
				// MAY_REFER OR REFER TO THE OV_METHOD
				overrideMethods.forEach(ovMethod -> {
					ovMethod.createRelationshipTo(declaredMethod, CGRelationTypes.OVERRIDES);
					if (!(boolean) ovMethod.getProperty("isAbstract"))
						invocationRels.forEach(r -> {
							if (!trustableInv.contains(r.getStartNode()))
								r.getStartNode().createRelationshipTo(ovMethod,
										mayRefer ? CGRelationTypes.MAY_REFER_TO : CGRelationTypes.REFER_TO);

						});
				});
				// MAY_REFER INSTEAD OF REFER FOR THE DECLARED METHOD
				if (!isAbstract)
					invocationRels.forEach(r -> {
						if (!trustableInv.contains(r.getStartNode())) {
							r.getStartNode().createRelationshipTo(declaredMethod, CGRelationTypes.MAY_REFER_TO);
							r.delete();
						}
					});

			}

			if (isAbstract)
				invocationRels.forEach(r -> r.delete());
			overrideMethods.add(declaredMethod);

		}
		typeToGroupedMethods.put(typeDec, descendentsGroupedMethods);
		return descendentsGroupedMethods;
	}

	// private boolean notIsSuperCall(Node mInv) {
	// Node methodSelect =
	// mInv.getSingleRelationship(RelationTypes.METHODINVOCATION_METHOD_SELECT,
	// Direction.OUTGOING)
	// .getEndNode();
	// if (methodSelect.hasLabel(NodeTypes.MEMBER_SELECTION)) {
	// Node memberSelect =
	// methodSelect.getSingleRelationship(RelationTypes.MEMBER_SELECT_EXPR,
	// Direction.OUTGOING)
	// .getEndNode();
	// if (memberSelect.hasLabel(NodeTypes.IDENTIFIER)
	// && memberSelect.getProperty("name").toString().contentEquals("super"))
	// return false;
	// }
	// return true;
	// }
}

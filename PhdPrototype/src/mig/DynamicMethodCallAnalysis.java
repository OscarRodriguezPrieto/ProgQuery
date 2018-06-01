package mig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import database.relations.CGRelationTypes;
import database.relations.RelationTypes;

public class DynamicMethodCallAnalysis {

	private Map<Node, Map<String, List<Node>>> typeToGroupedMethods = new HashMap<Node, Map<String, List<Node>>>();

	public Map<String, List<Node>> dynamicMethodCallAnalysis(Node typeDec) {
		Map<String, List<Node>> descendentsGroupedMethods = typeToGroupedMethods.get(typeDec);
		if (descendentsGroupedMethods != null)
			return descendentsGroupedMethods;

		descendentsGroupedMethods = new HashMap<String, List<Node>>();
		for (Relationship subTypeRel : typeDec.getRelationships(Direction.INCOMING, RelationTypes.IS_SUBTYPE_EXTENDS,
				RelationTypes.IS_SUBTYPE_IMPLEMENTS)) {
			Node subType = subTypeRel.getStartNode();
			Map<String, List<Node>> subtypeGroupedMethods = dynamicMethodCallAnalysis(subType);
			for (Entry<String, List<Node>> entry : subtypeGroupedMethods.entrySet()) {
				List<Node> groupedMethods = descendentsGroupedMethods.get(entry.getKey());
				if (groupedMethods == null)
					descendentsGroupedMethods.put(entry.getKey(), groupedMethods = new ArrayList<Node>());
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
			List<Node> overrideMethods = descendentsGroupedMethods.get(typedName);
			boolean isAbstract = (boolean) declaredMethod.getProperty("isAbstract");

			Iterable<Relationship> invocationRels = declaredMethod.getRelationships(CGRelationTypes.REFER_TO,
					Direction.INCOMING);
			if (overrideMethods == null)
				descendentsGroupedMethods.put(typedName, overrideMethods = new ArrayList<Node>());

			else {
				boolean mayRefer = !isAbstract || overrideMethods.size() > 1;

				// Metodo declarado para el que hay descendientes
				overrideMethods.forEach(ovMethod -> {
					ovMethod.createRelationshipTo(declaredMethod, CGRelationTypes.OVERRIDES);
					if (!(boolean) ovMethod.getProperty("isAbstract"))
						invocationRels.forEach(r -> r.getStartNode().createRelationshipTo(ovMethod,
								mayRefer ? CGRelationTypes.MAY_REFER_TO : CGRelationTypes.REFER_TO));
				});
				if (!isAbstract)
					invocationRels.forEach(r -> {
						r.getStartNode().createRelationshipTo(declaredMethod, CGRelationTypes.MAY_REFER_TO);
						r.delete();
					});

			}

			if (isAbstract)
				invocationRels.forEach(r -> r.delete());
			overrideMethods.add(declaredMethod);

		}
		typeToGroupedMethods.put(typeDec, descendentsGroupedMethods);

		// System.out.println("TYPE_DEC:\n" + NodeUtils.nodeToString(typeDec));
		// descendentsGroupedMethods.forEach((k, v) -> {
		// System.out.println("KEY:\t" + k + "\nVALUES:");
		// v.forEach(n -> System.out.println(NodeUtils.nodeToString(n)));
		// });
		return descendentsGroupedMethods;
	}

}

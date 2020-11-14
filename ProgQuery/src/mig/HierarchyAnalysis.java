package mig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import database.nodes.NodeUtils;
import database.querys.cypherWrapper.EdgeDirection;
import database.relations.CGRelationTypes;
import database.relations.RelationTypes;
import database.relations.TypeRelations;
import node_wrappers.NodeWrapper;
import node_wrappers.RelationshipWrapper;

public class HierarchyAnalysis {

	private Map<NodeWrapper, InfoFromSubtypes> typeToInheritedInfo = new HashMap<>();
	private Set<NodeWrapper> trustableInv;

	public HierarchyAnalysis(Set<NodeWrapper> superCalls) {
		this.trustableInv = superCalls;
	}

	private static class InfoFromSubtypes {
		Map<String, Set<NodeWrapper>> transitiveOverridersMethods = new HashMap<>();
		Map<NodeWrapper, Map<String, NodeWrapper>> subtypesToLastOverrider = new HashMap<>();

	}

	public InfoFromSubtypes dynamicMethodCallAnalysis(NodeWrapper typeDec) {
		InfoFromSubtypes inheritedInfo = typeToInheritedInfo.get(typeDec);
//		System.out.println("STARTING FOR " + typeDec.getProperty("fullyQualifiedName"));
		if (inheritedInfo != null)
			return inheritedInfo;

//		System.out.println("NOT FOUND FOR " + typeDec.getProperty("fullyQualifiedName"));
		inheritedInfo = new InfoFromSubtypes();
//		for (RelationshipWrapper subTypeRel : typeDec.getRelationships(EdgeDirection.INCOMING,
//				TypeRelations.IS_SUBTYPE_EXTENDS, TypeRelations.IS_SUBTYPE_IMPLEMENTS))
//			System.out.println("REL  " + typeDec.getProperty("fullyQualifiedName") + " WITH "
//					+ subTypeRel.getStartNode().getProperty("fullyQualifiedName"));

		for (RelationshipWrapper subTypeRel : typeDec.getRelationships(EdgeDirection.INCOMING,
				TypeRelations.IS_SUBTYPE_EXTENDS, TypeRelations.IS_SUBTYPE_IMPLEMENTS)) {
			NodeWrapper subType = subTypeRel.getStartNode();
			// System.out.println("subtype " +
			// subType.getProperty("fullyQualifiedName"));

			InfoFromSubtypes infoFromSubtypes = dynamicMethodCallAnalysis(subType);
//			System.out.println("ANALYZING " + typeDec.getProperty("fullyQualifiedName") + " WITH "
//					+ subType.getProperty("fullyQualifiedName"));

			// if
			// (typeDec.getProperty("fullyQualifiedName").equals("examples.mig.C")
			// ||
			// typeDec.getProperty("fullyQualifiedName").equals("annotations.MyAnn"))
			// {
			// System.out.println(infoFromSubtypes.subtypesToLastOverrider);
			// System.out.println(infoFromSubtypes.transitiveOverridersMethods);
			// }

			inheritedInfo.subtypesToLastOverrider.putAll(infoFromSubtypes.subtypesToLastOverrider);

			Map<String, Set<NodeWrapper>> subtypeOverriders = infoFromSubtypes.transitiveOverridersMethods;
			for (Entry<String, Set<NodeWrapper>> entry : subtypeOverriders.entrySet()) {
				Set<NodeWrapper> groupedMethods = inheritedInfo.transitiveOverridersMethods.get(entry.getKey());
				if (groupedMethods == null)
					inheritedInfo.transitiveOverridersMethods.put(entry.getKey(), groupedMethods = new HashSet<>());
				groupedMethods.addAll(entry.getValue());
			}

		}
		// AQuí tenemos el mapa subtypesToLastOverrider lleno con la info de los
		// subtypes
		Iterable<RelationshipWrapper> declaredFields = typeDec.getRelationships(EdgeDirection.OUTGOING,
				RelationTypes.DECLARES_FIELD),
				declaredMethods = typeDec.getRelationships(EdgeDirection.OUTGOING, RelationTypes.DECLARES_METHOD);
		for (Entry<NodeWrapper, Map<String, NodeWrapper>> subTypeInfo : inheritedInfo.subtypesToLastOverrider
				.entrySet()) {
//			System.out.println("FIELDS OF "+typeDec + " TO "+ subTypeInfo.getKey());
			fieldAnalysis(subTypeInfo.getKey(), declaredFields);

			for (RelationshipWrapper declaredMethodRel : declaredMethods) {

				NodeWrapper declaredMethod = declaredMethodRel.getEndNode();
				if ((boolean) declaredMethod.getProperty("isStatic"))
					continue;
				String typedName = declaredMethod.getProperty("fullyQualifiedName").toString().split(":")[1]
						.split("\\)")[0];

				NodeWrapper overriderMethodInThisSubtype = subTypeInfo.getValue().get(typedName);
				if (overriderMethodInThisSubtype == null) {
//					System.out
//							.println("NO OVERRIDER FOUND SO,  " + subTypeInfo.getKey() + " INHERITS " + declaredMethod);

					subTypeInfo.getKey().createRelationshipTo(declaredMethod, TypeRelations.INHERITS_METHOD);
//					subTypeInfo.getValue().put(typedName, declaredMethod);
				} else {
//					System.out.println(
//							"OVERRIDER FOUND FOR " + typeDec + "  WITH " + subTypeInfo.getKey() + " AND " + typedName);
//					System.out.println(overriderMethodInThisSubtype);
//					if (!subTypeInfo.getValue().get(typedName)
//							.getSingleRelationship(EdgeDirection.OUTGOING, TypeRelations.OVERRIDES).getEndNode()
//							.equals(declaredMethod))
						subTypeInfo.getValue().get(typedName).createRelationshipTo(declaredMethod,
								TypeRelations.OVERRIDES);
					inheritedInfo.subtypesToLastOverrider.get(subTypeInfo.getKey()).remove(typedName);
				}
			}
		}

		inheritedInfo.subtypesToLastOverrider.put(typeDec, new HashMap<>());
		// POR CADA DECLARACIÓN DE mËTODO SE HACE EL ANALYSIS Y LOS DE MISMA
		// CLAVE SON SOBREESCRITOS SI LOS HAY
		// Todas las claves, menos los metodos sobreescritos son heredados,pero
		// hablando de metodos solo la ultima
		// if
		// (typeDec.getProperty("fullyQualifiedName").equals("examples.mig.C")
		// ||
		// typeDec.getProperty("fullyQualifiedName").equals("annotations.MyAnn")
		// ||
		// typeDec.getProperty("fullyQualifiedName").equals("java.lang.Object")
		// ) {
		// System.out.println("INHERITED INFO BEFORE OV");
		// System.out.println("TO LAST");
		// System.out.println(inheritedInfo.subtypesToLastOverrider);
		// System.out.println("TRANSITIVE");
		// System.out.println(inheritedInfo.transitiveOverridersMethods);
		// }
		for (RelationshipWrapper declaredMethodRel : declaredMethods) {

			NodeWrapper declaredMethod = declaredMethodRel.getEndNode();
			if ((boolean) declaredMethod.getProperty("isStatic"))
				continue;

			String typedName = declaredMethod.getProperty("fullyQualifiedName").toString().split(":")[1]
					.split("\\)")[0];
			inheritedInfo.subtypesToLastOverrider.get(typeDec).put(typedName, declaredMethod);

			Set<NodeWrapper> overriderMethods = inheritedInfo.transitiveOverridersMethods.get(typedName);
			boolean isAbstract = (boolean) declaredMethod.getProperty("isAbstract");

			Iterable<RelationshipWrapper> invocationRels = declaredMethod.getRelationships(EdgeDirection.INCOMING,
					CGRelationTypes.REFERS_TO);
			if (overriderMethods == null)
				inheritedInfo.transitiveOverridersMethods.put(typedName, overriderMethods = new HashSet<>());

			else {
				boolean mayRefer = !isAbstract || overriderMethods.size() > 1;

				// MAY_REFER OR REFER TO THE OV_METHOD
				overriderMethods.forEach(ovMethod -> {
					// inheritedInfo.
					if (!(boolean) ovMethod.getProperty("isAbstract"))
						invocationRels.forEach(r -> {
							if (!trustableInv.contains(r.getStartNode()))
								r.getStartNode().createRelationshipTo(ovMethod,
										mayRefer ? CGRelationTypes.MAY_REFER_TO : CGRelationTypes.REFERS_TO);

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
			overriderMethods.add(declaredMethod);

		}

		typeToInheritedInfo.put(typeDec, inheritedInfo);
		return inheritedInfo;
	}

	private void fieldAnalysis(NodeWrapper subtype, Iterable<RelationshipWrapper> declaredFields) {
		for (RelationshipWrapper declaredFieldRel : declaredFields)
			if (!(Boolean) declaredFieldRel.getEndNode().getProperty("isStatic"))
				subtype.createRelationshipTo(declaredFieldRel.getEndNode(), TypeRelations.INHERITS_FIELD);

	}
}

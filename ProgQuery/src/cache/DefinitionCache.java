package cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;

import database.DatabaseFachade;
import database.nodes.NodeTypes;
import database.relations.CDGRelationTypes;
import database.relations.RelationTypes;
import typeInfo.TypeHierarchy;

public class DefinitionCache<TKEY> {
	private static final boolean DEBUG = false;
	public static final DefinitionCache<Symbol> CLASS_TYPE_CACHE = new DefinitionCache<Symbol>();
	public static final DefinitionCache<Symbol> METHOD_TYPE_CACHE = new DefinitionCache<Symbol>();

	private final Map<TKEY, Node> auxNodeCache = new HashMap<TKEY, Node>();
	private final Map<TKEY, Node> definitionNodeCache = new HashMap<TKEY, Node>();

	public void put(TKEY k, Node v) {
		if (DEBUG)
			System.out.println("putting " + k + " " + v);
		if (auxNodeCache.containsKey(k))
			throw new IllegalArgumentException("Key " + k + " twice ");
		if (!definitionNodeCache.containsKey(k))
			auxNodeCache.put(k, v);
	}

	public void putClassDefinition(TKEY classSymbol, Node classDec, List<Node> typeDecNodeList,
			Set<Node> typeDecsUses) {
		boolean containsKey;
		if (containsKey = auxNodeCache.containsKey(classSymbol)) {
			Node oldClassNode = auxNodeCache.get(classSymbol);
			for (Relationship r : oldClassNode.getRelationships(Direction.OUTGOING, RelationTypes.DECLARES_METHOD,
					RelationTypes.DECLARES_CONSTRUCTOR))
				r.delete();
			typeDecNodeList.remove(oldClassNode);
			oldClassNode.getRelationships(CDGRelationTypes.USES_TYPE_DEC, Direction.OUTGOING)
					.forEach(usesTypeDecRel -> typeDecsUses.add(usesTypeDecRel.getEndNode()));
		}

		putDefinition(classSymbol, classDec, containsKey);
	}

	public void putDefinition(TKEY k, Node v) {
		putDefinition(k, v, auxNodeCache.containsKey(k));
	}

	private void putDefinition(TKEY k, Node v, boolean containsKey) {
		if (DEBUG)
			System.out.println("putting def " + k + " " + v);

		if (containsKey) {
			if (DEBUG)
				System.out.println("Removing " + auxNodeCache.get(k));
			// No me deja eliminalo porque todavía tiene relaciones

			// Habria que pasar las relaciones al nuevo type
			// Igual es más eficiente iterar todas y un if??? con direction
			Node cachedNode = auxNodeCache.get(k);
			for (Relationship r : cachedNode.getRelationships(Direction.INCOMING)) {
				r.getStartNode().createRelationshipTo(v, r.getType());
				r.delete();
			}
			for (Relationship r : cachedNode.getRelationships(Direction.OUTGOING)) {
				v.createRelationshipTo(r.getEndNode(), r.getType());
				r.delete();
			}
			auxNodeCache.get(k).delete();

			auxNodeCache.remove(k);
		}

		definitionNodeCache.put(k, v);
	}

	public Node get(TKEY k) {
		return definitionNodeCache.containsKey(k) ? definitionNodeCache.get(k) : auxNodeCache.get(k);
	}

	public boolean containsKey(TKEY k) {
		return auxNodeCache.containsKey(k) || definitionNodeCache.containsKey(k);
	}

	public boolean containsDef(TKEY k) {
		return definitionNodeCache.containsKey(k);
	}

	public int totalTypesCached() {
		return auxNodeCache.size();
	}

	public int totalDefsCached() {
		return definitionNodeCache.size();
	}

	public static Node getOrCreateTypeDec(ClassSymbol classSymbol, List<Node> typeDecNodeList) {

		return getOrCreateTypeDec(classSymbol,
				classSymbol.isInterface() ? NodeTypes.INTERFACE_DECLARATION
						: classSymbol.isEnum() ? NodeTypes.ENUM_DECLARATION : NodeTypes.CLASS_DECLARATION,
				typeDecNodeList);
	}

	public static Node getOrCreateTypeDec(ClassSymbol classSymbol, NodeTypes type, List<Node> typeDecNodeList) {
		return getOrCreateNode(classSymbol, () -> DatabaseFachade.createTypeDecNode(classSymbol, type),
				typeDecNodeList);
	}

	public static Node getOrCreateNode(ClassSymbol classSymbol, Supplier<Node> supplier, List<Node> typeDecList) {

		return DefinitionCache.CLASS_TYPE_CACHE.containsKey(classSymbol)
				? DefinitionCache.CLASS_TYPE_CACHE.get(classSymbol) : createTypeDec(classSymbol, supplier, typeDecList);
	}

	public static void createTypeDecIfNecessary(ClassSymbol classSymbol, List<Node> typeDecList) {
		if (!DefinitionCache.CLASS_TYPE_CACHE.containsKey(classSymbol))
			createTypeDec(classSymbol, typeDecList);
	}

	public static Node createTypeDec(ClassSymbol classSymbol, List<Node> typeDecList) {
		return createTypeDec(classSymbol,
				() -> DatabaseFachade.createTypeDecNode(classSymbol,
						classSymbol.isInterface() ? NodeTypes.INTERFACE_DECLARATION
								: classSymbol.isEnum() ? NodeTypes.ENUM_DECLARATION : NodeTypes.CLASS_DECLARATION),
				typeDecList);
	}

	public static Node createTypeDec(ClassSymbol classSymbol, Supplier<Node> supplier, List<Node> typeDecList) {
		Node classNode = supplier.get();
		typeDecList.add(classNode);
		TypeHierarchy.addTypeHierarchy(classSymbol, classNode, typeDecList, null);
		DefinitionCache.CLASS_TYPE_CACHE.put(classSymbol, classNode);
		return classNode;
	}
}

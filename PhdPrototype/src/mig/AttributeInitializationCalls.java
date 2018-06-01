package mig;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Node;

public class AttributeInitializationCalls {

	public static AttributeInitializationCalls CURRENT_CLASS_INFO;
	private List<Node> lastConstructors = new ArrayList<Node>(), lastAttributes = new ArrayList<Node>(),
			lastStaticAttributes = new ArrayList<Node>();

	public Node lastStaticConstructor;

	public static void addConstructor(Node n) {
		CURRENT_CLASS_INFO.lastConstructors.add(n);
	}

	public static void addAttribute(Node attr) {
		CURRENT_CLASS_INFO.lastAttributes.add(attr);
	}

	public static void addStaticAttribute(Node attr) {

		CURRENT_CLASS_INFO.lastStaticAttributes.add(attr);
	}

}

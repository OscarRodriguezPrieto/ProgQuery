package es.uniovi.reflection.progquery.mig;

import java.util.ArrayList;
import java.util.List;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class AttributeInitializationCalls {

	public static AttributeInitializationCalls CURRENT_CLASS_INFO;
	private List<NodeWrapper> lastConstructors = new ArrayList<>(), lastAttributes = new ArrayList<>(),
			lastStaticAttributes = new ArrayList<>();

	public NodeWrapper lastStaticConstructor;

	public static void addConstructor(NodeWrapper n) {
		CURRENT_CLASS_INFO.lastConstructors.add(n);
	}

	public static void addAttribute(NodeWrapper attr) {
		CURRENT_CLASS_INFO.lastAttributes.add(attr);
	}

	public static void addStaticAttribute(NodeWrapper attr) {

		CURRENT_CLASS_INFO.lastStaticAttributes.add(attr);
	}

}

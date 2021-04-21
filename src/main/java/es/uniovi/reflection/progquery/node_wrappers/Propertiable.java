package es.uniovi.reflection.progquery.node_wrappers;

import java.util.Map.Entry;
import java.util.Set;

public interface Propertiable {
	default void setProperty(String name, Object value) {
		setProp(name, value);
	}

	default void setProperty(String name, String value) {
		// setProp(name, WrapperUtils.stringToNeo4jQueryString(value));
		setProp(name, value);
	}

	default void setProperties(Object[] props) {
		for (int i = 0; i < props.length; i += 2)
			setProperty(props[i].toString(), props[i + 1]);
	}

	void setProp(String name, Object value);

	boolean hasProperty(String string);

	Object getProperty(String string);

	Set<Entry<String, Object>> getAllProperties();
}

package node_wrappers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

abstract class AbstractNeo4jLazyServerDriverElement implements Propertiable {

	Map<String, Object> properties = new HashMap<String, Object>();

	public AbstractNeo4jLazyServerDriverElement(Object... props) {
		for (int i = 0; i < props.length; i = i + 2)
			properties.put(props[i].toString(), props[i + 1]);
	}

	public void setProp(String name, Object value) {
		properties.put(name, value);
	}

	public boolean hasProperty(String name) {
		return properties.get(name) != null;
	}

	public Object getProperty(String name) {

		return properties.get(name);
	}

	public Set<Entry<String, Object>> getAllProperties() {
		// TODO Auto-generated method stub
		return properties.entrySet();
	}

}

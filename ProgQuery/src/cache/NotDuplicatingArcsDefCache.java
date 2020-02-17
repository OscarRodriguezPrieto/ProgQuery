package cache;

import node_wrappers.NodeWrapper;

public class NotDuplicatingArcsDefCache<TKEY> extends DefinitionCache<TKEY> {
	@Override
	public void putDefinition(TKEY k, NodeWrapper v) {

		definitionNodeCache.put(k, v);

	}

}

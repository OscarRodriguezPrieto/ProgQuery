package utils.dataTransferClasses;

import java.util.Set;

import node_wrappers.NodeWrapper;

public interface ASTVisitorResult {
	boolean isInstance();
	Set<NodeWrapper> paramsPreviouslyModifiedForSwitch();
}

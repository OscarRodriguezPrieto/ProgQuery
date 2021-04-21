package es.uniovi.reflection.progquery.utils.dataTransferClasses;

import java.util.Set;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public interface ASTVisitorResult {
	boolean isInstance();
	Set<NodeWrapper> paramsPreviouslyModifiedForSwitch();
}

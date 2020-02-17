package utils.dataTransferClasses;

import java.util.Set;

import node_wrappers.NodeWrapper;

public class VisitorResultImpl implements ASTVisitorResult {
	private boolean isInstance;
private Set<NodeWrapper> paramsPreviouslyModifiedInCases;

	public VisitorResultImpl(boolean isInstance, Set<NodeWrapper> paramsPreviouslyModifiedInCases) {
	super();
	this.isInstance = isInstance;
	this.paramsPreviouslyModifiedInCases = paramsPreviouslyModifiedInCases;
}
	public VisitorResultImpl(boolean isInstance) {
	super();
	this.isInstance = isInstance;
}


	public VisitorResultImpl(Set<NodeWrapper> paramsPreviouslyModifiedInCases) {
		super();
		this.paramsPreviouslyModifiedInCases = paramsPreviouslyModifiedInCases;
	}
	@Override
	public boolean isInstance() {
		// TODO Auto-generated method stub
		return isInstance;
	}


	@Override
	public Set<NodeWrapper> paramsPreviouslyModifiedForSwitch() {
		return paramsPreviouslyModifiedInCases;
	}

}

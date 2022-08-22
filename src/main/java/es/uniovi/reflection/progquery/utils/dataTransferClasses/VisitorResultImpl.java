package es.uniovi.reflection.progquery.utils.dataTransferClasses;

import java.util.Set;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class VisitorResultImpl extends AbstractASTResult {
	private boolean isInstance;
private Set<NodeWrapper> paramsPreviouslyModifiedInCases;

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


	public void setInstance(boolean isInstance) {
		this.isInstance = isInstance;
	}
	@Override
	public Set<NodeWrapper> paramsPreviouslyModifiedForSwitch() {
		return paramsPreviouslyModifiedInCases;
	}

}

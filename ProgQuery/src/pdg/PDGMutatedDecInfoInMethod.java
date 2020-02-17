package pdg;

import node_wrappers.NodeWrapper;
import pdg.GetDeclarationFromExpression.IsInstance;

public class PDGMutatedDecInfoInMethod {

	public boolean isMay;
	public IsInstance isOuterMostImplicitThisOrP;
	public NodeWrapper dec;

	public PDGMutatedDecInfoInMethod(boolean isMay, IsInstance isOuterMostImplicitThisOrP,
			NodeWrapper dec) {
		super();
		this.isMay = isMay;
		this.isOuterMostImplicitThisOrP = isOuterMostImplicitThisOrP;
		this.dec = dec;
	}


}

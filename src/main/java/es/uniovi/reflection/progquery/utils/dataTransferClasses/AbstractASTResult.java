package es.uniovi.reflection.progquery.utils.dataTransferClasses;

import com.sun.tools.javac.code.Type;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

import java.util.Set;

public abstract class AbstractASTResult implements ASTVisitorResult{
    @Override
    public boolean isInstance() {
     throw new IllegalStateException(getClass()+" does not contain IS_INSTANCE info");
    }

    @Override
    public Set<NodeWrapper> paramsPreviouslyModifiedForSwitch() {
        throw new IllegalStateException(getClass()+" does not contain PARAM_MODIFIED  info");
    }

    @Override
    public NodeWrapper getNodeInfo() {
        throw new IllegalStateException(getClass()+" does not contain NODE info");
    }
}

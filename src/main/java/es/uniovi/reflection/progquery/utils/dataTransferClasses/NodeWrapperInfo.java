package es.uniovi.reflection.progquery.utils.dataTransferClasses;

import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;


public class NodeWrapperInfo extends AbstractASTResult {
    private NodeWrapper node;

    public NodeWrapperInfo(NodeWrapper node) {
        this.node = node;
    }


    @Override
    public NodeWrapper getNodeInfo() {
        return node;
    }
}

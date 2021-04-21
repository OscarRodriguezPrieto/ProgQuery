package es.uniovi.reflection.progquery.utils.dataTransferClasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.tools.javac.code.Symbol.MethodSymbol;

import es.uniovi.reflection.progquery.cache.SimpleTreeNodeCache;
import es.uniovi.reflection.progquery.database.relations.PDGRelationTypes;
import es.uniovi.reflection.progquery.node_wrappers.NodeWrapper;

public class MethodState {
	public SimpleTreeNodeCache<Tree> cfgNodeCache;
	public Map<Tree, Pair<NodeWrapper, NodeWrapper>> finallyCache;
	public Map<TryTree, List<Pair<NodeWrapper, List<MethodSymbol>>>> invocationsInStatements;

	public NodeWrapper lastMethodDecVisited = null;


	public Map<NodeWrapper, NodeWrapper> identificationForLeftAssignExprs;
//	public Map<NodeWrapper, Boolean> instanceAssignsXX;

	public PDGRelationTypes thisRelationsOnThisMethod;
	public NodeWrapper thisNode;
	public Map<NodeWrapper, PDGRelationTypes> paramsToPDGRelations;
	public Map<NodeWrapper,Set<NodeWrapper>> callsToParamsPreviouslyModified,callsToParamsMaybePreviouslyModified;
	
	
	public MethodState(NodeWrapper methodDec) {
		thisRelationsOnThisMethod = null;
		identificationForLeftAssignExprs = new HashMap<>();
		lastMethodDecVisited = methodDec;
		invocationsInStatements = new HashMap<TryTree, List<Pair<NodeWrapper, List<MethodSymbol>>>>();
		cfgNodeCache = new SimpleTreeNodeCache<Tree>();
		finallyCache = new HashMap<Tree, Pair<NodeWrapper, NodeWrapper>>();
//		instanceAssigns = new HashMap<>();  
		paramsToPDGRelations = new HashMap<>();
		callsToParamsPreviouslyModified = new HashMap<>();
		callsToParamsMaybePreviouslyModified = new HashMap<>();
		
	} 

	public void putCfgNodeInCache(Tree t, NodeWrapper n) {
		cfgNodeCache.put(t, n);
	}

	public void putFinallyInCache(Tree t, NodeWrapper finallyNodeWrapper, NodeWrapper lastStatement) {
		finallyCache.put(t, Pair.create(finallyNodeWrapper, lastStatement));
	}
}

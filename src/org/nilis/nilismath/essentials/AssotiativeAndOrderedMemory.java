package org.nilis.nilismath.essentials;

import java.util.LinkedHashMap;
import java.util.Set;


public class AssotiativeAndOrderedMemory<TData, TKey> implements Memory<TData, TKey> {
	
	protected static class GraphNode<TData> {
		protected Set<GraphNode<TData>> linkedNodes;
		protected TData value;
		public void setValue(TData value) {
			this.value = value;
		}
		public TData getValue() {
			return this.value;
		}
		public boolean setLink(GraphNode<TData> node) {
			return linkedNodes.add(node);
		}
		public boolean unsetLink(GraphNode<TData> node) {
			return linkedNodes.remove(node);
		}
		public boolean hasLinkTo(GraphNode<TData> node) {
			return linkedNodes.contains(node);
		}
	}
	
	protected static interface NodeCriteria<TData> {
		GraphNode<TData> getMatchingNode(TData data, Set<GraphNode<TData>> existingNodes);
		void setupNodeLinks(GraphNode<TData> node, Set<GraphNode<TData>> existingNodes);
	}
	
	protected Set<GraphNode<TData>> nodesSet;
	protected LinkedHashMap<TKey, GraphNode<TData>> nodesMap;
	protected NodeCriteria<TData> nodeCriteria;

	@Override
	public void remember(TKey key, TData value) {
		GraphNode<TData> node = nodeCriteria.getMatchingNode(value, nodesSet);
		nodesSet.add(node);
		nodeCriteria.setupNodeLinks(node, nodesSet);
		nodesMap.put(key, node);
	}

	@Override
	public TData get(TKey key) {
		if(nodesMap.containsKey(key)) {
			return nodesMap.get(key).getValue();
		}
		return null;
	}
}

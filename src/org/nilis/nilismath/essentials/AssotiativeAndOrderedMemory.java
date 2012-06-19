package org.nilis.nilismath.essentials;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

public class AssotiativeAndOrderedMemory<TData, TKey> implements Memory<TData, TKey> {
	
	public static class GraphNode<TData> {
		protected HashMap<GraphNode<TData>, Double> linkedNodes = new HashMap<GraphNode<TData>, Double>();
		protected TData value;
		
		public GraphNode(TData value) {
			this.value = value;
		}
		
		public void setValue(TData value) {
			this.value = value;
		}
		public TData getValue() {
			return this.value;
		}
		public void setLink(GraphNode<TData> node, double linkPower) {
			if(linkPower == 0) {
				unsetLink(node);
			} else {
				linkedNodes.put(node, Double.valueOf(linkPower));
			}
		}
		public void unsetLink(GraphNode<TData> node) {
			linkedNodes.remove(node);
		}
		public boolean hasLinkTo(GraphNode<TData> node) {
			return linkedNodes.containsKey(node);
		}
		public HashMap<GraphNode<TData>, Double> getLinkedNodes() {
			return linkedNodes;
		}
	}
	
	public static interface NodeCriteria<TData> {
		GraphNode<TData> getMatchingNode(TData data, Set<GraphNode<TData>> existingNodes);
		void setupNodeLinks(GraphNode<TData> node, Set<GraphNode<TData>> existingNodes);
	}
	
	protected Set<GraphNode<TData>> nodesSet = new HashSet<GraphNode<TData>>();
	protected LinkedHashMap<TKey, GraphNode<TData>> nodesMap = new LinkedHashMap<TKey, GraphNode<TData>>();
	protected NodeCriteria<TData> nodeCriteria;
	
	public AssotiativeAndOrderedMemory(NodeCriteria<TData> nodeCriteria) {
		this.nodeCriteria = nodeCriteria;
	}

	@Override
	public void remember(TKey key, TData value) {
		GraphNode<TData> node = nodeCriteria.getMatchingNode(value, nodesSet);
		nodesSet.add(node);
		nodeCriteria.setupNodeLinks(node, nodesSet);
		nodesMap.put(key, node);
		if(nodesMap.size() > nodesSet.size()) {
			List<TKey> keys = new LinkedList<TKey>(nodesMap.keySet());
			nodesMap.remove(keys.get(0));
		}
	}

	@Override
	public TData get(TKey key) {
		if(nodesMap.containsKey(key)) {
			return nodesMap.get(key).getValue();
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ AssotiativeAndOrderedMemory, size: "+nodesSet.size()+" nodes \n");
		int counter = 0;
		for(Entry<TKey, GraphNode<TData>> entry : nodesMap.entrySet()) {
			sb.append("   {"+counter+++" Node '"+entry.getKey()+"' with value "+entry.getValue().getValue()+", linked with nodes with values: \n");
			for(Entry<GraphNode<TData>, Double> nodeEntry : entry.getValue().getLinkedNodes().entrySet()) {
				sb.append("      [ "+nodeEntry.getKey().getValue()+", power: "+nodeEntry.getValue()+"], \n");
			}
			sb.append("   } \n");
		}
		sb.append("\n }");
		return sb.toString();
	}
}

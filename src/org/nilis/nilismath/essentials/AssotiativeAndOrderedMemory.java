package org.nilis.nilismath.essentials;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class AssotiativeAndOrderedMemory<TData extends Object> implements SelfKeyedMemory<TData> {
	
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
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object another) {
			if(another instanceof GraphNode) {
				return value.equals(((GraphNode<TData>) another).getValue());
			}
			return false;
		}
	}
	
	public static abstract class NodeCriteria<TData> {
		abstract public GraphNode<TData> getMatchingNode(TData data, Set<GraphNode<TData>> existingNodes);
		public GraphNode<TData> getMatchingExistingNode(TData data, Set<GraphNode<TData>> existingNodes) {
			return getMatchingExistingNode(data, existingNodes, 0);
		}
		abstract public GraphNode<TData> getMatchingExistingNode(TData data, Set<GraphNode<TData>> existingNodes, double minimalSimilarity);
		abstract public void setupNodeLinks(GraphNode<TData> node, Set<GraphNode<TData>> existingNodes);
	}
	
	protected Set<GraphNode<TData>> nodesSet = new HashSet<GraphNode<TData>>();
	protected LinkedHashMap<TData, GraphNode<TData>> nodesMap = new LinkedHashMap<TData, GraphNode<TData>>();
	protected NodeCriteria<TData> nodeCriteria;
	
	public AssotiativeAndOrderedMemory(NodeCriteria<TData> nodeCriteria) {
		this.nodeCriteria = nodeCriteria;
	}
	
	protected void setNodeIntoMemory(TData key, TData storedValue) {
		GraphNode<TData> node = new GraphNode<TData>(storedValue);
		nodesSet.add(node);
		nodesMap.put(key, node);
	}
	
	protected void setNodeLinks(TData nodeStoredValue, Set<TData> linkedNodesStoredValues) {
//		GraphNode<TData> node = new GraphNode<TData>(storedValue);
//		nodesSet.add(node);
//		nodesMap.put(key, node);
	}
	
	public Set<GraphNode<TData>> getNodes() {
		return nodesSet;
	}

	@Override
	public void remember(TData value) {
		GraphNode<TData> node = nodeCriteria.getMatchingNode(value, nodesSet);
		node.setValue(value);
		nodesSet.add(node);
		nodeCriteria.setupNodeLinks(node, nodesSet);
		nodesMap.put(value, node);
		if(nodesMap.size() > nodesSet.size()) {
			List<TData> keys = new LinkedList<TData>(nodesMap.keySet());
			nodesMap.remove(keys.get(0));
		}
	}

	@Override
	public TData get(TData key) {
		if(nodesMap.containsKey(key)) {
			return nodesMap.get(key).getValue();
		}
		return null;
	}
	
	public TData getAssociated(TData key) {
		return nodeCriteria.getMatchingExistingNode(key, nodesSet).getValue();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ AssotiativeAndOrderedMemory, size: "+nodesSet.size()+" nodes \n");
		int counter = 0;
		for(Entry<TData, GraphNode<TData>> entry : nodesMap.entrySet()) {
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

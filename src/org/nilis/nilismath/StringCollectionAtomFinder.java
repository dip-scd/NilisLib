package org.nilis.nilismath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory.GraphNode;


public class StringCollectionAtomFinder{
	
	protected Set<GraphNode<String>> nodes = new HashSet<GraphNode<String>>();
	protected HashMap<String, GraphNode<String>> outputNodesMapping = new HashMap<String, GraphNode<String>>();

	public void putWords(Set<String> input) {
		for(String word : input) {
			putWord(word);
		}
	}

	public void putWord(String wordInput) {
		if(wordInput == null || wordInput.length() == 0 || wordInput.equals("")) {
			return;
		}
		String word = wordInput.toLowerCase();
		GraphNode<String> wordContainingNode = null;
		if(outputNodesMapping.containsKey(word)) {
			wordContainingNode = outputNodesMapping.get(word);
		} else {
			wordContainingNode = new GraphNode<String>(word);
			nodes.add(wordContainingNode);
			outputNodesMapping.put(word, wordContainingNode);
		}
		relinkHiddenNodes(wordContainingNode);
	}
	
	protected void relinkHiddenNodes(GraphNode<String> addedNode) {
		findNodesWithValuesContainedByProvidedNode(addedNode);
		findNodesWithValuesContainingProvidedNodeValue(addedNode);
	}
	
	protected void findNodesWithValuesContainedByProvidedNode(GraphNode<String> providedNode) {
		for(GraphNode<String> node : nodes) {
			if(!providedNode.equals(node) && providedNode.getValue().contains(node.getValue())) {
				node.setLink(providedNode, 1);
			}
		}
	}
	
	protected void findNodesWithValuesContainingProvidedNodeValue(GraphNode<String> providedNode) {
		for(GraphNode<String> node : nodes) {
			if(!providedNode.equals(node) && node.getValue().contains(providedNode.getValue())) {
				providedNode.setLink(node, 1);
			}
		}
	}
	
	public Set<GraphNode<String>> getNodes() {
		return nodes;
	}
}

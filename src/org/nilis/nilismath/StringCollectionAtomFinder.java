package org.nilis.nilismath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory.GraphNode;


public class StringCollectionAtomFinder{
	
	protected Set<GraphNode<String>> nodes;
	protected HashMap<String, GraphNode<String>> outputNodesMapping;

	public void putWords(Set<String> input) {
		for(String word : input) {
			putWord(word);
		}
	}

	public void putWord(String word) {
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
	}
	
	protected Set<GraphNode<String>> findNodesWithValuesContainedByProvidedNode(GraphNode<String> providedNode) {
		Set<GraphNode<String>> ret = new HashSet<GraphNode<String>>();
		for(GraphNode<String> node : nodes) {
			if(providedNode.getValue().contains(node.getValue())) {
				ret.add(node);
			}
		}
		return ret;
	}
}

package org.nilis.nilismath;

import java.util.Set;

import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory.GraphNode;
import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory.NodeCriteria;
import org.nilis.utils.data.DataPair;


public class BasicNodeCriteria<TData> implements NodeCriteria<TData> {
	
	public static interface DataSimilarityCriteria<TData> {
		double howSimilar(TData data1, TData data2);
	}
	
	protected DataSimilarityCriteria<TData> similarityCriteria;
	protected double minimalSimilarityForUsingExistingNode = 1;
	
	public BasicNodeCriteria(DataSimilarityCriteria<TData> similarytiCriteria) {
		this(similarytiCriteria, 0.95);
	}
	
	public BasicNodeCriteria(DataSimilarityCriteria<TData> similarytiCriteria, double minimalSimilarityForUsingExistingNode) {
		this.similarityCriteria = similarytiCriteria;
		this.minimalSimilarityForUsingExistingNode = minimalSimilarityForUsingExistingNode;
	}

	@Override
	public GraphNode<TData> getMatchingNode(TData data,
			Set<GraphNode<TData>> existingNodes) {
		DataPair<GraphNode<TData>, Double> mostSimilar = findMostSimilarNode(data, existingNodes);
		if(mostSimilar != null && mostSimilar.getData().doubleValue() >= minimalSimilarityForUsingExistingNode) {
			mostSimilar.getTag().setValue(data);
			return mostSimilar.getTag();
		}
		return new GraphNode<TData>(data);
	}

	@Override
	public void setupNodeLinks(GraphNode<TData> node, Set<GraphNode<TData>> existingNodes) {
		double currentHowSimilar = 0;
		for(GraphNode<TData> checkedNode : existingNodes) {
			if(checkedNode != node) {
				currentHowSimilar = similarityCriteria.howSimilar(node.getValue(), checkedNode.getValue());
				node.setLink(checkedNode, currentHowSimilar);
				checkedNode.setLink(node, currentHowSimilar);
			}
		}
	}
	
	protected DataPair<GraphNode<TData>, Double> findMostSimilarNode(TData data, Set<GraphNode<TData>> existingNodes) {
		GraphNode<TData> mostSimilar = null;
		double maxSimilarity = -1;
		double currentSimilarity;
		for(GraphNode<TData> node : existingNodes) {
			currentSimilarity = similarityCriteria.howSimilar(data, node.getValue());
			if(currentSimilarity > maxSimilarity) {
				maxSimilarity = currentSimilarity;
				mostSimilar = node;
			}
		}
		if(mostSimilar != null) {
			return new DataPair<GraphNode<TData>, Double>(mostSimilar, Double.valueOf(maxSimilarity));
		}
		return null;
	}

}

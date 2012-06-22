package org.nilis.nilismath;

import java.util.HashSet;
import java.util.Set;

import org.nilis.nilismath.BasicNodeCriteria.DataSimilarityCriteria;


public class StringsSimilarityCriteria implements DataSimilarityCriteria<String> {
	
	private double lowestAcceptableSimilarity = 0;
	private double highestAcceptableSimilarity = 1;
	
	public StringsSimilarityCriteria(double lowestAcceptableSimilarity, double highestAcceptableSimilarity) {
		this.lowestAcceptableSimilarity = lowestAcceptableSimilarity;
		this.highestAcceptableSimilarity = highestAcceptableSimilarity;
	}
	
	public StringsSimilarityCriteria() {
		this(0.3, 0.7);
	}

	@Override
	public double howSimilar(String data1, String data2) {
		String data1Copy = data1.toLowerCase();
		String data2Copy = data2.toLowerCase();
		double ret = 0;
		double ret2 = 0;
		for(int i = 0; i < data1Copy.length(); i++) {
			char c = data1Copy.charAt(i);
			int nearestSameCharPositionInSecondString = findNearestSameCharPosition(c, i, data2Copy);
			if(nearestSameCharPositionInSecondString >= 0) {
				ret += 1 / Math.pow(2, Math.abs(nearestSameCharPositionInSecondString - i));
				ret2 += 1 / Math.pow(2, Math.abs(
						(data2Copy.length()-nearestSameCharPositionInSecondString) - 
						(data1Copy.length()-i)));
			}
		}
		ret = Math.max(ret, ret2) / ((data1Copy.length() + data2Copy.length()) / 2);
		if(ret < lowestAcceptableSimilarity) {
			ret = 0;
		}
		if(ret > highestAcceptableSimilarity) {
			ret = 1;
		}
		if(Double.valueOf(ret).isNaN()) {
			ret = 0;
		}
		return ret;
	}

	private static int findNearestSameCharPosition(char charToFind, int originalPosition, String stringToSearchIn) {
		int ret = -1;
		Set<Integer> foundPositions = new HashSet<Integer>();
		for(int i = 0; i < stringToSearchIn.length(); i++) {
			if(stringToSearchIn.charAt(i) == charToFind) {
				foundPositions.add(Integer.valueOf(i));
			}
		}
		int shortestDistance = Integer.MAX_VALUE;
		int currentDistance = -1;
		for(Integer pos : foundPositions) {
			currentDistance = Math.abs(pos.intValue() - originalPosition);
			if(currentDistance < shortestDistance) {
				shortestDistance = currentDistance;
				ret = pos.intValue();
			}
		}
		return ret;
	}
}

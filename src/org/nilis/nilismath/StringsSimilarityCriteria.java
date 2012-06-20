package org.nilis.nilismath;

import java.util.HashSet;
import java.util.Set;

import org.nilis.nilismath.BasicNodeCriteria.DataSimilarityCriteria;


public class StringsSimilarityCriteria implements DataSimilarityCriteria<String> {

	@Override
	public double howSimilar(String data1, String data2) {
		double ret = 0;
		for(int i = 0; i < data1.length(); i++) {
			char c = data1.charAt(i);
			int nearestSameCharPositionInSecondString = findNearestSameCharPosition(c, i, data2);
			if(nearestSameCharPositionInSecondString >= 0) {
				ret += 1 / Math.pow(2, Math.abs(nearestSameCharPositionInSecondString - i));
				ret += 1 / Math.pow(2, Math.abs(nearestSameCharPositionInSecondString - i));
			}
		}
		ret /= data1.length();
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

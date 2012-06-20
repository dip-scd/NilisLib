package org.nilis.nilismath;

import org.nilis.nilismath.BasicNodeCriteria.DataSimilarityCriteria;

public class NumbersSimilarityCriteria implements DataSimilarityCriteria<Double> {
	
	protected double maxNumbersDifferenceAllowingToLinkThem;
	
	public NumbersSimilarityCriteria() {
		this(1);
	}
	
	public NumbersSimilarityCriteria(double maxNumbersDifferenceAllowingToLinkThem) {
		this.maxNumbersDifferenceAllowingToLinkThem = maxNumbersDifferenceAllowingToLinkThem;
	}

	@Override
	public double howSimilar(Double data1, Double data2) {
		double diff = Math.abs(data1.doubleValue() - data2.doubleValue());
		if(diff <= maxNumbersDifferenceAllowingToLinkThem) {
			return 1 - diff/maxNumbersDifferenceAllowingToLinkThem;
		}
		return 0;
	}

}

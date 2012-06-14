package org.nilis.nilismath;

import java.util.Date;
import java.util.Random;

import org.nilis.nilismath.essentials.Mutator;


public class NumericMutator extends Mutator<Double> {
	
	private double deviation;
	private Random random;
	public NumericMutator(double deviation) {
		this.deviation = deviation;
		this.random = new Random(new Date().getTime());
	}
	
	public NumericMutator() {
		this(1);
	}

	@Override
	public Double mutate(Double input) {
		return Double.valueOf(input.doubleValue() + random.nextGaussian()*deviation);
	}
}

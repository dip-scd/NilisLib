package org.nilis.nilismath.essentials;

public class LinearMutatingNumericFunctor implements HomogenicFunctor<Double> {

	protected NumericMutator mutator;
	public LinearMutatingNumericFunctor(double deviation) {
		mutator = new NumericMutator(deviation);
	}
	
	@Override
	public Double perform(Double... input) {
		if(input.length > 0) {
			return mutator.mutate(input[0]);
		}
		return null;
	}

}

package org.nilis.nilismath.essentials;

public abstract class Mutator<TData> implements HomogenicFunctor<TData> {

	@Override
	public TData perform(TData... input) {
		if(input.length > 0) {
			return mutate(input[0]);
		}
		return null;
	}
	
	abstract public TData mutate(TData input);
}

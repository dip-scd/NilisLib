package org.nilis.nilismath;

import org.nilis.nilismath.essentials.Mutator;


public class Spawnlings {


	public static void out(Object message) {
		System.out.println(message);
	}
	
	public static void main(String[] args) {
		Mutator<Double> mutator = new NumericMutator(10);
		for(int i=0; i<40; i++) {
			out(mutator.mutate((double) 20));
		}
	}

}

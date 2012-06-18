package org.nilis.nilismath;

import org.nilis.nilismath.essentials.HomogenicFunctor;
import org.nilis.nilismath.essentials.LinearMutatingNumericFunctor;


public class Spawnlings {


	public static void out(Object message) {
		System.out.println(message);
	}
	
	public static void main(String[] args) {
		HomogenicFunctor<Double> functor = new LinearMutatingNumericFunctor(10);
		for(int i=0; i<40; i++) {
			out(functor.perform((double) 20));
		}
	}
}

package org.nilis.nilismath;

import org.nilis.nilismath.BasicNodeCriteria.DataSimilarityCriteria;
import org.nilis.nilismath.essentials.AssotiativeAndOrderedMemory;
import org.nilis.nilismath.essentials.Memory;


public class Spawnlings {


	public static void out(Object message) {
		System.out.println(message);
	}
	
	public static void main(String[] args) {
		//HomogenicFunctor<Double> functor = new LinearMutatingNumericFunctor(10);
		Memory<Double, Double> testMemory = new AssotiativeAndOrderedMemory<Double, Double>(
				new BasicNodeCriteria<Double>(
						new DataSimilarityCriteria<Double>() {

			@Override
			public double howSimilar(Double data1, Double data2) {
				double diff = Math.abs(data1.doubleValue() - data2.doubleValue());
				if(diff <= 100) {
					return 1 - diff/100.0;
				}
				return 0;
			}
		}));
		for(int i=0; i<40; i++) {
			double val = Math.random() * 500;
			testMemory.remember(val, val);
			//out(functor.perform((double) 20));
		}
		out(testMemory);
	}
}

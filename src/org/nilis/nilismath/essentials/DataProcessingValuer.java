package org.nilis.nilismath.essentials;


public interface DataProcessingValuer<TInputData, TOutputData> {
	public interface EvaluatedDataProcessor<TInputData, TOutputData> {
		void evaluate(TInputData input, TOutputData output, double mark);
	}
	
	void evaluate(TInputData input, TOutputData output, EvaluatedDataProcessor<TInputData, TOutputData> dataProcessor);
}

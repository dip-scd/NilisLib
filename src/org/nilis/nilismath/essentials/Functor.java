package org.nilis.nilismath.essentials;

public interface Functor<TInputData, TOutputData> {
	TOutputData perform(TInputData... input);
}

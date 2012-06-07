package org.nilis.utils.data;

public abstract class SyncDataConverter<TTag, TInputData, TOutputData> {
	
	public abstract TOutputData forwardConvert(TTag tag, TInputData data);
	
	@SuppressWarnings("unused")
	public TInputData backwardConvert(TTag tag, TOutputData data) {
		return null;
	}
	
	public TOutputData forwardConvert(TInputData data) {
		return forwardConvert(null, data);
	}
	
	public TInputData backwardConvert(TOutputData data) {
		return backwardConvert(null, data);
	}
}

package org.nilis.utils.data;

public interface DataConverter<TInputData, TOutputData> {
	static interface OnConvertedDataListener<TOutputData> {
		void onConvertedDataReceived(TOutputData data);

		void onDataConvertionFailed(Exception e);
	}

	void convert(TInputData value, OnConvertedDataListener<TOutputData> listener);
}
